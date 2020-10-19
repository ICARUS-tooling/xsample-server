/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

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
			
			final byte[] data = body.bytes(); //TODO for now we load the data directly into memory. for bigger corpora we'd need to buffer it on disk.
			
			final String contentType = mediaType.type()+"/"+mediaType.subtype();
			final String encoding = mediaType.charset(StandardCharsets.UTF_8).name();
			final String title = extractName(mediaType.toString());
			final int segments = 1; //TODO delegate to actual handler for the given MIME type
			
			config.setFileData(title, contentType, encoding, data, segments);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to load file content", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.error.load"));
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
