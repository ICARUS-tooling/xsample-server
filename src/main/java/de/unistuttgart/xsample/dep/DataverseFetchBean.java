/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.dep;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.encrypt;
import static de.unistuttgart.xsample.util.XSampleUtils.makeKey;

import java.io.IOException;
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

import de.unistuttgart.xsample.XsampleInputData;
import de.unistuttgart.xsample.ct.EmptyResourceException;
import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.CountingSplitStream;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Deprecated
@Named
@RequestScoped
public class DataverseFetchBean {

	private static final Logger logger = Logger.getLogger(DataverseFetchBean.class.getCanonicalName());
	
	@Inject
	XsampleData xsampleData;
	
	@Inject
	XsampleInputData inputData;

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
		
		final ExcerptConfig excerpt = xsampleData.getExcerpt();
		final DataverseClient client = DataverseClient.forServer(null); //FIXME fetch correct url
		
		final Call<ResponseBody> request = client.downloadFile(
				inputData.getFile().longValue(), inputData.getKey());
	
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

			final ExcerptHandler handler = ExcerptHandlers.forInputType(null) /*ExcerptHandlers.forContentType(fileInfo.getContentType())*/;
			
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
			excerpt.setFileInfo(fileInfo);
			excerpt.setHandler(handler);
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
}
