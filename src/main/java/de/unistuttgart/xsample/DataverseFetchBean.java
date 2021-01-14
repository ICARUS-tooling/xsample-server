/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.encrypt;
import static de.unistuttgart.xsample.util.XSampleUtils.makeKey;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.EmptyResourceException;
import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.FileInfo;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Named
@RequestScoped
public class DataverseFetchBean {

	private static final Logger logger = Logger.getLogger(DataverseFetchBean.class.getCanonicalName());
	
	@Inject
	XsamplePage xsamplePage;

	private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
	private static final String QUOTED = "\"([^\"]*)\"";
	private static final Pattern PARAMETER = Pattern
			.compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");
	
	private static String extractName(String mediaType) {
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

	public void fetchResource() {
		System.out.println("fetching");
		
		final ExcerptConfig config = new ExcerptConfig() /*xsamplePage.getConfig()*/;		
		final DataverseClient client = DataverseClient.forConfig(config);
		
		final Call<ResponseBody> request = client.downloadFile(config.getFile().longValue(), config.getKey());
	
		Response<ResponseBody> response;		
		try {
			response = request.execute();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch response", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.fetch"));
			return;
		}
		
		if(!response.isSuccessful()) {
			//TODO do a switch on http code and provide meaningful messages
			Messages.addGlobalError("Fetching failed ... [[TODO]]");
			return;
		}
		
		// Successfully opened and accessed data, now load content
		try(ResponseBody body = response.body()) {
			final MediaType mediaType = body.contentType();
			if(mediaType==null) {
				logger.log(Level.SEVERE, "Failed to obtain medaia type");
				Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.type"));
				return;
			}
			
			final FileInfo fileInfo = new FileInfo();
			fileInfo.setContentType(mediaType.type()+"/"+mediaType.subtype());
			fileInfo.setEncoding(mediaType.charset(StandardCharsets.UTF_8));
			fileInfo.setTitle(extractName(mediaType.toString()));

			final ExcerptHandler handler = ExcerptHandlers.forContentType(fileInfo.getContentType());
			
			// Ensure an encrypted copy of the resource
			final Path tempFile = Files.createTempFile("xsample_", ".tmp");
			final SecretKey key = makeKey();
			long size = 0;
			try(OutputStream out = new CipherOutputStream(buffer(Files.newOutputStream(tempFile)), encrypt(key));
					CountingSplitStream in = new CountingSplitStream(body.byteStream(), out)) {
				// Let handler to the actual work. Any acquired information is stored in fileInfo
				handler.analyze(fileInfo, in);
				size = in.getCount();
			}
			
			// If everything went well, finally complete
			fileInfo.setSize(size);
			fileInfo.setTempFile(tempFile);
			fileInfo.setKey(key);
			
			// Update info in current config
			config.setFileInfo(fileInfo);
			config.setHandler(handler);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to load file content", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.load"));
			return;
		} catch (UnsupportedContentTypeException e) {
			logger.log(Level.SEVERE, "Content type of file not supported", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.mime"));
			return;
		} catch (EmptyResourceException e) {
			logger.log(Level.SEVERE, "Source file empty", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.empty"));
			return;
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
			Messages.addGlobalError(BundleUtil.get("homepage.error.cipher"));
			return;
		}
		
		Messages.addGlobalInfo(BundleUtil.get("homepage.tabs.data.done"));
		
		System.out.println("fetched");
	}
	
	static class CountingSplitStream extends InputStream {

		/** Total bytes read  */
		private long count = 0;
  
		/** Original source */
		private final InputStream in;
		/** Destinatio nfor cloned input data */
		private final OutputStream out;
		
		public CountingSplitStream(InputStream in, OutputStream out) {
			this.in = requireNonNull(in);
			this.out = requireNonNull(out);
		}

		public long getCount() { return count; }

		@Override
		public int read() throws IOException {
			int b = in.read();
			if(b!=-1) {
				count++;
				out.write(b);
			}
			return b;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
//			System.out.printf("read b=%d off=%d len=%d%n",b.length,off, len);
			int read = in.read(b, off, len);
			if(read>0) {
				count += read;
				out.write(b, off, read);
			}
			return read;
		}

		@Override
		public long skip(long n) throws IOException {
//			System.out.printf("skip n=%d%n",n);
	        long remaining = n;
	        int nr;

	        if (n <= 0) {
	            return 0;
	        }

	        int size = strictToInt(Math.min(2048, remaining));
	        byte[] skipBuffer = new byte[size];
	        while (remaining > 0) {
	            nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
	            if (nr < 0) {
	                break;
	            }
	            remaining -= nr;
	            out.write(skipBuffer, 0, nr);
	        }

	        return n - remaining;
		}

		@Override
		public int available() throws IOException {
			return in.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
	        /* no-op */
		}

		@Override
		public synchronized void reset() throws IOException {
	        throw new IOException("mark/reset not supported");
		}

		@Override
		public boolean markSupported() { 
			return false;
		}
	}
}
