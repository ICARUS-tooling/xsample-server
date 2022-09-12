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
package de.unistuttgart.xsample.gen;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import de.unistuttgart.xsample.dv.DvResult;
import de.unistuttgart.xsample.gen.DvFileInfo.DvDataFile;
import de.unistuttgart.xsample.gen.ManifestGenerator.Entry;
import de.unistuttgart.xsample.mf.SourceType;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Markus Gärtner
 *
 */
public class DataverseSetup {

	public static void main(String[] args) throws Exception {
		DataverseSetup setup = new DataverseSetup();
		
		setup.readArgs(args);		
		setup.readSettings();
		setup.prepareClient();
		
		setup.setupDataset();
	}
	

	private final Properties settings = new Properties();
	private Path settingsFile = null;
	
	private AdminClient client;
	private String key, auth;
	
	private void readArgs(String[] args) {
		if(args.length==0) {
			return;
		}
		
		for (int i = 0; i < args.length; i++) {
			if("-f".equals(args[i])) {
				settingsFile = Paths.get(args[++i]);
			}
		}
	}
	
	private void readSettings() throws IOException {
		if(settingsFile==null) {
			settingsFile = Paths.get("dataverse_setup.ini");
		}
		
		settings.load(Files.newBufferedReader(settingsFile));
	}
	
	private void prepareClient() {
		client = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(settings.getProperty("server"))
				.build().create(AdminClient.class);		
		key = requireNonNull(settings.getProperty("key"));
		auth = AdminClient.asAuth(key);		
	}
	
	private void setupDataset() throws IOException {
		final String datasetPID = requireNonNull(settings.getProperty("dataset.persistentId"));
		final String datasetVersion = settings.getProperty("dataset.version", AdminClient.DRAFT);
		
		populateDataset(datasetPID, datasetVersion);
	}
	
	private static final MediaType JSON = MediaType.get("application/json");
	private static final MediaType JSON_LD = MediaType.get("application/ld+json");
	
	private void populateDataset(String persistentId, String version) throws IOException {
		System.out.println("Populating dataset: "+persistentId);
		//TODO currently we only do this for a single pdf file, need to expand it to other types when ready!!

		final List<DvFileInfo> files = doCall(client.getFiles(version, persistentId, key));
		System.out.printf("Found %d existing files in dataset%n", _int(files.size()));
		
		final Map<String, DvDataFile> existing = files.stream()
				.map(DvFileInfo::getDataFile)
				.collect(Collectors.toMap(DvDataFile::getFilename, d -> d));
		
		ManifestGenerator generator = ManifestGenerator.builder()
				.baseName("100p_pdf")
				.basePath("pdf")
				.size(100)
				.sourceType(SourceType.PDF)
				.unsupportedfileId(Integer.MAX_VALUE)
				.fileId(26)
				.create();
		
		final List<Entry> entries = generator.generate();
		
		System.out.printf("Total of %d entries to populate%n", _int(entries.size()));
		
		final Gson gson = new Gson();
		
		final boolean isDraft = AdminClient.DRAFT.equals(version);
		
		for(Entry entry : entries) {
			
			RequestBody fileContent = RequestBody.create(JSON, 
					ByteString.encodeUtf8(gson.toJson(entry.manifest)));
			MultipartBody.Part file = MultipartBody.Part.createFormData("file", entry.name, fileContent); 
			
			FileUploadInfo uploadInfo = new FileUploadInfo();
			uploadInfo.setDescription(entry.manifest.getDescription());
			uploadInfo.setDirectoryLabel(entry.path);
			uploadInfo.setFileName(entry.name);
			uploadInfo.setMimeType("application/json");
			uploadInfo.setRestricted(true);
			RequestBody jsonData = RequestBody.create(JSON, 
					ByteString.encodeUtf8(gson.toJson(uploadInfo)));
			
			// File existed, just replace it
			DvDataFile existingFile;
			if(!isDraft && (existingFile = existing.remove(entry.name))!=null) {
				System.out.println("Replacing manifest: "+entry.name);
				long fileId = existingFile.getId();
				doGenericCall(client.replaceFile(fileId, key, jsonData, file));
			} else {
				System.out.println("Adding manifest: "+entry.name);
				// New file, do full add				
				doGenericCall(client.addFile(persistentId, key, jsonData, file));
			}
		}
		
		if(!existing.isEmpty()) {
			System.out.printf("Still %d leftover files to delete", _int(existing.size()));
			for(DvDataFile file : existing.values()) {
				long fileId = file.getId();
				System.out.println("Deleting file: "+fileId);
				doGenericCall(client.deleteFileSWORD(fileId, auth));
			}
		}
		
		System.out.println("  -- done --  ");
	}
	
	static class FileUploadInfo {
		/**
		 * <pre>
		 * {
			  "description": "My description.",
			  "directoryLabel": "data/subdir1",
			  "categories": [
			    "Data"
			  ],
			  "restrict": "false"
			}
		 * </pre>
		 */
		private String description;
		private String directoryLabel;
		private String mimeType;
		private String fileName;
		private boolean restricted;
		private String[] categories;
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public String getDirectoryLabel() { return directoryLabel; }
		public void setDirectoryLabel(String directoryLabel) { this.directoryLabel = directoryLabel; }
		public String getMimeType() { return mimeType; }
		public void setMimeType(String mimeType) { this.mimeType = mimeType; }
		public String getFileName() { return fileName; }
		public void setFileName(String fileName) { this.fileName = fileName; }
		public boolean isRestricted() { return restricted; }
		public void setRestricted(boolean restricted) { this.restricted = restricted; }
		public String[] getCategories() { return categories; }
		public void setCategories(String[] categories) { this.categories = categories; }
	}
	
	private <T> T doCall(Call<DvResult<T>> call) throws IOException {
		final HttpUrl url = call.request().url();
		Response<DvResult<T>> response = call.execute();
		if(!response.isSuccessful())
			throw new IllegalStateException(String.format("Failed call[%s]: %s", 
					url, response.errorBody().string()));
		
		DvResult<T> result = response.body();
		if(!result.isOk())
			throw new IllegalStateException(String.format("Invalid result[%s]: %s",
					url, result.getStatus()));
		System.out.printf("%s -> %s%n", url, result.getStatus());
		return result.getData();
	}
	
	private void doGenericCall(Call<ResponseBody> call) throws IOException {
		final HttpUrl url = call.request().url();
		Response<ResponseBody> response = call.execute();
		if(!response.isSuccessful())
			throw new IllegalStateException(String.format("Failed call[%s]: %s", 
					url, response.errorBody().string()));
		
		ResponseBody body = response.body();
		System.out.printf("%s -> %s%n", url, body==null ? "<no body>" : body.string());
	}
}
