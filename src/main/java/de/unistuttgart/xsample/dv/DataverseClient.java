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
package de.unistuttgart.xsample.dv;

import java.net.URL;

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
	
	public static DataverseClient forServer(URL url) {		
		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(url)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		
		return retrofit.create(DataverseClient.class);
	}
	
	@Streaming
	@GET("api/access/datafile/{id}")
	Call<ResponseBody> downloadFile(@Path("id") long file, @Header("X-Dataverse-key") String key);
	
	@Streaming
	@GET("api/files/{id}/metadata")
	Call<FileMetadata> getFileMetadata(@Path("id") long file, @Header("X-Dataverse-key") String key);
	
	@Streaming
	@GET("api/files/{id}/metadata/draft")
	Call<FileMetadata> getDraftFileMetadata(@Path("id") long file, @Header("X-Dataverse-key") String key);
	
	@GET("api/users/:me")
	Call<UserInfo> getUserInfo(@Header("X-Dataverse-key") String key);
}