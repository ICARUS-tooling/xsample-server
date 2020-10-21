/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import com.google.common.io.CountingInputStream;

import de.unistuttgart.xsample.ct.EmptyResourceException;
import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.FileInfo;
import de.unistuttgart.xsample.util.Payload;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

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
		
		final XsampleExcerptConfig config = xsamplePage.getConfig();
		
		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(config.getSite())
				.build();
		
		final DataverseClient client = retrofit.create(DataverseClient.class);
		
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
			
			final String contentType = mediaType.type()+"/"+mediaType.subtype();
			final Charset encoding = mediaType.charset(StandardCharsets.UTF_8);
			final String title = extractName(mediaType.toString());
			final ExcerptHandler handler = ExcerptHandlers.forContentType(contentType);
			
			final CountingInputStream countingStream = new CountingInputStream(body.byteStream());
			final Payload input = Payload.forInput(encoding, contentType, countingStream);			
			handler.init(input);
			final long size = countingStream.getCount();
			
			final FileInfo fileInfo = new FileInfo(title, contentType, encoding, size);
			
			config.setFileData(fileInfo, handler);
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
		}
		
		Messages.addGlobalInfo(BundleUtil.get("homepage.tabs.data.error.done"));
		
		System.out.println("fetched");
	}
	
	public interface DataverseClient {
		
		@Streaming
		@GET("api/access/datafile/{id}")
		Call<ResponseBody> downloadFile(@Path("id") long file, @Header("X-Dataverse-key") String key);
	}
}
