/**
 * 
 */
package de.unistuttgart.xsample.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.omnifaces.cdi.Eager;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@Eager
@Singleton
public class LocalCache {
	
	private static final Logger log = Logger.getLogger(LocalCache.class.getCanonicalName());
	
	private static final int MIN_EXPIRE_DAYS = 1;
	private static final long KEEP_ALIVE_HOURS = 2;
	private static final long STALE_FILE_SECONDS = (2 * MIN_EXPIRE_DAYS) * 24 * 60 * 60;
	
	private static final String CACHE_FOLDER = "xsample_cache";
	private static final String TMP_PREFIX = "xsample_file_";
	private static final String TMP_SUFFIX = ".tmp";
	
	@Inject
	XsampleServices services;
	
	private final Object lock = new Object();
	
	private Path tempFolder;

	@PostConstruct
	@Transactional
	private void init() {
		Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
		tempFolder = dir.resolve(CACHE_FOLDER);
		if(!Files.exists(tempFolder, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createDirectory(tempFolder);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to initialize cache folder", e);
			}
		}
	}
	
	private Path createTempFile() throws IOException { return Files.createTempFile(tempFolder, TMP_PREFIX, TMP_SUFFIX); }

	private Path file(String name) {
		requireNonNull(name);
		return tempFolder.resolve(name); 
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
    		try(DirectoryStream<Path> stream = Files.newDirectoryStream(tempFolder, STALE_FILES)) {
    			for(Iterator<Path> it = stream.iterator(); it.hasNext();) {
    				Path file = it.next();
    				Optional<XmpLocalCopy> copy = services.findCopy(file.toString());
    				if(copy.isPresent()) {
    					services.delete(copy);
    				} else {
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
	
    @Nullable
	public XmpLocalCopy ensureCopy(XmpResource resource) {
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
			
			Path file;
			try {
				file = createTempFile();
				touch(file);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Failed to create/touch temporary file", e);
				return null;
			}
			
			XmpLocalCopy copy = new XmpLocalCopy();
			copy.setExpiresAt(LocalDateTime.now().plusDays(MIN_EXPIRE_DAYS));
			copy.setResource(resource);
			copy.setFilename(file.toString());
			copy.setKey(key);
			services.save(copy);
			
			return copy;
		}
	}
	
    @Nullable
	public XmpLocalCopy getCopy(XmpResource resource) {
		synchronized (lock) {
			Optional<XmpLocalCopy> existing = services.findCopy(resource);
			if(existing.isPresent()) {
				return keepAlive(existing.get());
			}
		}
		return null;
    }
	
	private static void touch(Path file) throws IOException {
		if(Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
		}
	}
	
	public Path getFile(XmpLocalCopy copy) {
		return tempFolder.resolve(copy.getFilename());
	}
	
	public boolean isPopulated(XmpLocalCopy copy) {
		Path file = getFile(copy);
		try {
			return Files.size(file)>0;
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to check size of file: "+file, e);
			return false;
		}
	}
}
