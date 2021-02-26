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
package de.unistuttgart.xsample.gen;

import java.net.URL;
import java.util.Base64;
import java.util.List;

import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.dv.DvResult;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Extended interface to the Dataverse API with admin-level commands that 
 * should not be in the normal client.
 * Used solely to prepare a test dataverse and populate it with test data.
 * 
 * @author Markus Gärtner
 *
 * @see <a href="https://guides.dataverse.org/en/latest/api/index.html">API Guide<a/>
 */
public interface AdminClient extends DataverseClient {
	
	public static AdminClient forServer(URL url) {		
		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(url)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		
		return retrofit.create(AdminClient.class);
	}
	
	@DELETE("api/datasets/:persistentId/destroy")
	Call<DvResult.Generic> deleteDataset(@Query("persistentId") String persistentId, @Header("X-Dataverse-key") String key);
	
	/**
	 * https://guides.dataverse.org/en/latest/api/sword.html#delete-a-file-by-database-idtomee.apache.org/jpa-concepts.html
	 * 
	 * Needs base64 encoded token for the auth part!
	 * 
	 * @see #asAuth(String)
	 */
	@DELETE("dvn/api/data-deposit/v1.1/swordv2/edit-media/file/{fileId}")
	Call<ResponseBody> deleteFileSWORD(@Path("fileId") long fileId, @Header("Authorization") String auth);
	
	@GET("api/datasets/:persistentId/versions/{version}/files")
	Call<DvResult<List<DvFileInfo>>> getFiles(@Path("version") String version, 
			@Query("persistentId") String persistentId, 
			@Header("X-Dataverse-key") String key);
	
	@Multipart
	@POST("api/datasets/:persistentId/add")
	Call<ResponseBody> addFile(@Query("persistentId") String persistentId,
			@Header("X-Dataverse-key") String key, 
			@Part("jsonData") RequestBody jsonData, @Part MultipartBody.Part file);
	
	public static final String DRAFT = ":draft";
	
	public static String asAuth(String key) {
		String credentials = key+":";
		byte[] data = credentials.getBytes();
		String encoded = Base64.getEncoder().encodeToString(data);
		return "Basic "+encoded;
	}
}