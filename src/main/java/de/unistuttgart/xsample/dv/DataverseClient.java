/**
 * 
 */
package de.unistuttgart.xsample.dv;

import de.unistuttgart.xsample.XsampleExcerptConfig;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Interface to the Dataverse API
 * 
 * @author Markus Gärtner
 *
 * @see <a href="https://guides.dataverse.org/en/latest/api/index.html">API Guide<a/>
 */
public interface DataverseClient {
	
	public static DataverseClient forConfig(XsampleExcerptConfig config) {		
		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(config.getSite())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		
		return retrofit.create(DataverseClient.class);
	}
	
	@Streaming
	@GET("api/access/datafile/{id}")
	Call<ResponseBody> downloadFile(@Path("id") long file, @Header("X-Dataverse-key") String key);
	
	@GET("api/users/:me")
	Call<UserInfo> getUserInfo(@Header("X-Dataverse-key") String key);
}