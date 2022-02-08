/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package de.unistuttgart.xsample.io;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.decrypt;
import static de.unistuttgart.xsample.util.XSampleUtils.encrypt;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.ejb.Schedule;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;

import de.unistuttgart.xsample.XsampleApp;
import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.util.XSampleUtils;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Markus Gärtner
 *
 */
@Named
@SessionScoped
public class LocalCache implements Serializable {
	
	private static final long serialVersionUID = -6969800383991211733L;

	private static final Logger log = Logger.getLogger(LocalCache.class.getCanonicalName());
	
	private static final int MIN_EXPIRE_DAYS = 1;
	private static final long KEEP_ALIVE_HOURS = 2;
	private static final long STALE_FILE_SECONDS = (2 * MIN_EXPIRE_DAYS) * 24 * 60 * 60;
	
	private static final String CACHE_FOLDER = "xsample_cache";
	private static final String TMP_PREFIX = "xsample_file_";
	private static final String DATA_SUFFIX = ".tmp";
	
	@Inject
	XsampleServices services;
	
	@Inject
	XsampleApp app;
	
	private transient final Object lock = new Object();
	
	private String tempFolder;
	
	private transient final Map<String, DataverseClient> clientCache = new HashMap<>();

	@PostConstruct
	@Transactional
	private void init() {
		Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
		Path tempFolder = dir.resolve(CACHE_FOLDER);
		tempFolder = tempFolder.resolve(app.getVersion());
		try {
			Files.createDirectories(tempFolder);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to initialize cache folder", e);
		}
		
		this.tempFolder = tempFolder.toAbsolutePath().toString();
	}
	
	@PreDestroy
	private void cleanup() {
		//TODO add a GC queue and weak references to copies we give out and ensure upon cleanup that they are released?
	}
	
	private Path tempFolder() { return Paths.get(tempFolder); }
	
	private Path createTempFile() throws IOException { return Files.createTempFile(tempFolder(), TMP_PREFIX, DATA_SUFFIX); }

	private Path file(String name) {
		requireNonNull(name);
		return tempFolder().resolve(name); 
	}
	
    @Schedule(hour="0", minute="0", second="0", persistent=false)
	private void purgeExpiredCopies() {
    	synchronized (lock) {
    		final List<XmpLocalCopy> expired = services.findExpiredCopies();
    		for(XmpLocalCopy copy : expired) {
    			try {
    				Path file = file(copy.getFilename());
    				Files.deleteIfExists(file);
    			} catch (IOException e) {
    				log.log(Level.SEVERE, "Failed to purge expired file: "+copy.getFilename(), e);
				} finally {
    				services.delete(copy);
    			}
    		}
		}
	}
    
    private static final DirectoryStream.Filter<Path> STALE_FILES = file -> {
    	// Ignore all links and folders
    	if(Files.isSymbolicLink(file) || Files.isDirectory(file)) {
    		return false;
    	}
    	FileTime lastModDate = Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);
    	long diff = System.currentTimeMillis()-lastModDate.to(TimeUnit.SECONDS);
    	return diff >= STALE_FILE_SECONDS;
    };
	
    @Schedule(hour="1", minute="0", second="0", persistent=false)
	private void purgeStaleFiles() {
    	synchronized (lock) {
    		try(DirectoryStream<Path> stream = Files.newDirectoryStream(tempFolder(), STALE_FILES)) {
    			for(Iterator<Path> it = stream.iterator(); it.hasNext();) {
    				final Path file = it.next();
    				final boolean isDataFile = file.endsWith(DATA_SUFFIX);
    				Optional<XmpLocalCopy> copy = services.findCopy(file.toString());
    				if(copy.isPresent()) {
    					services.delete(copy);
    				} else if(isDataFile) {
    					log.severe("Cache inconsistency: missing copy entry for cache file "+file);
    				}
    				
    				Files.delete(file);
    			}
    		} catch (IOException e) {
    			log.log(Level.SEVERE, "Failed to purge stale cache files", e);
			}
    	}
    }
    
    private XmpLocalCopy keepAlive(XmpLocalCopy copy) {
    	requireNonNull(copy);
    	LocalDateTime min = LocalDateTime.now().plusHours(KEEP_ALIVE_HOURS);
    	if(copy.getExpiresAt().compareTo(min)<0) {
    		copy.setExpiresAt(min);
    	}
    	return copy;
    }
    
    private Path relativize(Path file) {
    	if(!file.startsWith(tempFolder))
    		throw new IllegalArgumentException("Corrupted or foreign cache file: "+file);
    	return tempFolder().relativize(file);
    }
	
    @Nullable
	public XmpLocalCopy getCopy(XmpResource resource) {
		synchronized (lock) {
			Optional<XmpLocalCopy> existing = services.findCopy(resource);
			if(existing.isPresent()) {
				return keepAlive(existing.get());
			}
			
			SecretKey key;
			try {
				key = XSampleUtils.makeKey();
			} catch (NoSuchAlgorithmException e) {
				log.log(Level.SEVERE, "Failed to create key for temp file", e);
				return null;
			}
			
			Path dataFile;
			try {
				dataFile = createTempFile();
				touch(dataFile);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Failed to create/touch temporary file", e);
				return null;
			}
			
			XmpLocalCopy copy = new XmpLocalCopy();
			copy.setExpiresAt(LocalDateTime.now().plusDays(MIN_EXPIRE_DAYS));
			copy.setResource(resource);
			copy.setFilename(relativize(dataFile).toString());
			copy.setKey(XSampleUtils.serializeKey(key));
			services.store(copy);
			
			log.info(String.format("Local copy created: filename=%s key=%s resource=%s expiresAt=%s", 
					copy.getFilename(), copy.getKey(), copy.getResource(), copy.getExpiresAt()));
			
			return copy;
		}
	}
	
//    @Nullable
//	public XmpLocalCopy getCopy(XmpResource resource) {
//		synchronized (lock) {
//			Optional<XmpLocalCopy> existing = services.findCopy(resource);
//			if(existing.isPresent()) {
//				return keepAlive(existing.get());
//			}
//		}
//		return null;
//    }
    
    private DataverseClient getClient(XmpResource resource) {
    	requireNonNull(resource);
    	final String url = resource.getDataverse().getUsableUrl();
    	return clientCache.computeIfAbsent(url, DataverseClient::forServer);
    }
    
    /**
     * Opens the local copy of the specified resource,
     * fetching it from the remote source if requried.
     * This method must be called under {@link XmpLocalCopy#getLock() lock} 
     * of the {@link XmpLocalCopy copy}!!
     * @throws AccessException indicating a 4xx HTTP error
     * @throws InternalServerException indicating a 5xx HTTP error
     * @throws TransmissionException indicating a general I/O error during web transfer
     * @throws GeneralSecurityException should never happen - this signals a corrupt secret key situatin
     * @throws LocalIOException general I/O error on the client side while accessing the local copy
     */
    public InputStream open(XmpLocalCopy copy) throws TransmissionException, 
    		InternalServerException, AccessException, GeneralSecurityException, LocalIOException {
    	requireNonNull(copy);
    	ensureLocal(copy);
    	
    	return openLocal(copy);
    }
    
    public void ensureLocal(XmpLocalCopy copy) throws TransmissionException, InternalServerException, AccessException, GeneralSecurityException {
    	requireNonNull(copy);
    	if(!isPopulated(copy)) {
    		loadRemote(copy);
    	}
    }
    
    public InputStream openLocal(XmpLocalCopy copy) throws GeneralSecurityException, LocalIOException {    	
    	requireNonNull(copy);
    	try {
			return accessLocal(copy);
		} catch (IOException e) {
			throw new LocalIOException("Failed to access local copy file", e);
		}
    }
    
    private void loadRemote(XmpLocalCopy copy) throws TransmissionException, 
    		InternalServerException, AccessException, GeneralSecurityException {
    	final XmpResource resource = copy.getResource();
    	final long fileId = resource.getFile().longValue();
    	final DataverseClient client = getClient(resource);
    	final String key = resource.getDataverse().getMasterKey();
		final Cipher cipher = encrypt(XSampleUtils.deserializeKey(copy.getKey()));
    	final Call<ResponseBody> request;
		request = client.downloadFile(fileId, key);
	
		Response<ResponseBody> response;		
		try {
			response = request.execute();
		} catch (IOException e) {
			throw new TransmissionException("Failed to fetch resource", e);
		}
		
		if(!response.isSuccessful()) {			
			// Fetch complete error message from remote
			String msg;
			try(ResponseBody body = response.errorBody()) {
				msg = body.string();
			} catch (IOException e) {
				throw new TransmissionException("Failed to fetch error body", e);
			}
			
			if(response.code()>=500) {
				throw new InternalServerException(msg, response.code());
			} else if(response.code()>=400) {
				throw new AccessException(msg, response.code());
			} else {
				throw new TransmissionException(msg);
			}
		}
		
		// Successfully opened and accessed data, now load content
		try(ResponseBody body = response.body()) {
			final MediaType mediaType = body.contentType();
			if(mediaType==null)
				throw new TransmissionException("Missing media type");
			
			copy.setContentType(mediaType.type()+"/"+mediaType.subtype());
			copy.setEncoding(mediaType.charset(StandardCharsets.UTF_8).name());
			copy.setTitle(extractName(mediaType.toString()));
			
			final Path file = getDataFile(copy);
			try(InputStream rawIn = body.byteStream();
					OutputStream out = buffer(Files.newOutputStream(file));
					OutputStream cout = new CipherOutputStream(out, cipher)) {
				final long size = IOUtils.copyLarge(rawIn, cout);
				copy.setSize(size);
			} catch (IOException e) {
				throw new TransmissionException("Failed to load remote resource", e);
			}
		}
    }

	private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
	private static final String QUOTED = "\"([^\"]*)\"";
	private static final Pattern PARAMETER = Pattern
			.compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");
	
	private String extractName(String mediaType) {
		final Matcher m = PARAMETER.matcher(mediaType);
		while(m.find()) {
			String name = m.group(1);
			if(name==null) {
				continue;
			}
			String value = m.group(2);
			if(value==null) {
				value = m.group(3);
			}
			if(value!=null && "name".equals(name)) {
				return value;
			}
		}
		return null;
	}
	
	private InputStream accessLocal(XmpLocalCopy copy) throws GeneralSecurityException, IOException {
		final Path file = getDataFile(copy);
		final Cipher cipher = decrypt(XSampleUtils.deserializeKey(copy.getKey()));
		touch(file);
		return new CipherInputStream(Files.newInputStream(file, StandardOpenOption.READ), cipher);
	}
    
	
	private static void touch(Path file) throws IOException {
		if(Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
		}
	}
	
	public Path getDataFile(XmpLocalCopy copy) {
		return tempFolder().resolve(copy.getFilename());
	}
	
	public boolean isPopulated(XmpLocalCopy copy) {
		Path file = getDataFile(copy);
		try {
			return Files.exists(file, LinkOption.NOFOLLOW_LINKS) && Files.size(file)>0;
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to check size of file: "+file, e);
			return false;
		}
	}
	
	/**
	 * Encodes a serializable object into a base64 string.
	 */
	public static <T extends Serializable> String encode(T data) {
		requireNonNull(data);
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream objOut = new ObjectOutputStream(out)) {
			objOut.writeObject(data);
			byte[] bytes = out.toByteArray();
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to serialize data: "+data, e);
			throw new IllegalArgumentException("Object not properly serializable", e);
		}
	}
	
	/**
	 * Deserialize a base64 string into a specific type.
	 */
	public static <T extends Serializable> T decode(String s, Class<T> type) {
		checkNotEmpty(s);
		requireNonNull(type);
		byte[] data = Base64.getDecoder().decode(s);
		try(ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream objIn = new ObjectInputStream(in)) {
			Object obj = objIn.readObject();
			return type.cast(obj);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to deserialize data of type "+type, e);
			throw new IllegalArgumentException("Object not properly deserializable", e);
		} catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "Corrupted data - unable to deserialue as type "+type, e);
			throw new IllegalArgumentException("Corrutped or outdated serialized data", e);
		}
	}
}
