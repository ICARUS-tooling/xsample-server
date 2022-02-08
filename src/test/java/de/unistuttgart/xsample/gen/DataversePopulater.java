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
import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import de.unistuttgart.xsample.dv.DvResult;
import de.unistuttgart.xsample.gen.DvFileInfo.DvDataFile;
import de.unistuttgart.xsample.gen.ManifestGenerator.Entry;
import de.unistuttgart.xsample.gen.Setup.DatasetSetup;
import de.unistuttgart.xsample.gen.Setup.FileSetup;
import de.unistuttgart.xsample.gen.Setup.FolderSetup;
import de.unistuttgart.xsample.gen.Setup.GeneratorSetup;
import de.unistuttgart.xsample.mf.SourceType;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
public class DataversePopulater {

	public static void main(String[] args) throws Exception {
		DataversePopulater populater = new DataversePopulater();
		
		populater.readArgs(args);		
		populater.readSettings();
		populater.prepareClient();
		
		populater.setupAll();
	}
	

	private Setup setup;
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
		checkState("No setup file defined", settingsFile!=null);
		
		Gson gson = new Gson();
		setup = gson.fromJson(Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8), Setup.class);
	}
	
	private void prepareClient() {
		client = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(setup.server)
				.build().create(AdminClient.class);		
		key = requireNonNull(setup.key);
		auth = AdminClient.asAuth(key);		
	}
	
	private void setupAll() throws IOException {
		for(DatasetSetup dataset : setup.datasets) {
			setupDataset(dataset);
		}
	}
	
	private void setupDataset(DatasetSetup dataset) throws IOException {
		DatasetHandler handler = handler(dataset);
		
		if(dataset.clearAll) {
			clearDataset(handler);
		}
		
		for(FileSetup fileSetup : dataset.files) {
			uploadFile(fileSetup, handler);
		}
		
		for(FolderSetup folderSetup : dataset.folders) {
			uploadFolder(folderSetup, handler);
		}
		
		for(GeneratorSetup generatorSetup : dataset.generators) {
			uploadGenerated(generatorSetup, handler);
		}
		
		//TODO if needed clear out all existing but unknown files in dataset
		//TODO if needed fetch file ids from dataset here
	}
	
	private void clearDataset(DatasetHandler handler) throws IOException {
		final List<DvFileInfo> files = doCall(client.getFiles(handler.version, handler.persistentId, key));
		System.out.printf("Found %d existing files in dataset %s%n", _int(files.size()), handler.persistentId);
		
		for(DvFileInfo file : files) {
			long fileId = file.getDataFile().getId();
			System.out.println("Deleting file: "+fileId);
			handler.delete(fileId);
		}
		System.out.printf("Deleted existing files in dataset %s%n", handler.persistentId);
	}

	private void uploadFile(FileSetup fileSetup, DatasetHandler handler) throws IOException {

		final FileUploadInfo uploadInfo = new FileUploadInfo();
		uploadInfo.setDescription(fileSetup.description);
		uploadInfo.setDirectoryLabel(fileSetup.path);
		uploadInfo.setFileName(fileSetup.label);
		uploadInfo.setMimeType(fileSetup.mediaType);
		uploadInfo.setRestricted(fileSetup.restricted);
		
		final Path file = Paths.get(fileSetup.localPath);
		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			throw new NoSuchFileException(fileSetup.localPath);
		
		final MediaType contentType = MediaType.get(fileSetup.mediaType);
		final MultipartBody.Part filePart = filePart(contentType, file, fileSetup.label, fileSetup.expand);
		
		handler.add(fileSetup.label, fileSetup.id, filePart, uploadInfo);
	}
	
	private static String format(String pattern, Path file, int index) {
		return pattern.replace(FolderSetup.NAME_PATTERN, file.getFileName().toString())
				.replace(FolderSetup.INDEX_PATTERN, String.valueOf(index));
	}

	private void uploadFolder(FolderSetup folderSetup, DatasetHandler handler) throws IOException {

		final Path folder = Paths.get(folderSetup.localPath);
		if(!Files.exists(folder, LinkOption.NOFOLLOW_LINKS))
			throw new NoSuchFileException(folderSetup.localPath);
		if(!Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS))
			throw new NotDirectoryException(folderSetup.localPath);
		
		final String glob = folderSetup.include==null ? "*" : folderSetup.include;
		final PathMatcher exclude = folderSetup.exclude==null ? null :
			folder.getFileSystem().getPathMatcher(folderSetup.exclude);
		
		int index = 0;
		
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder, glob)) {
			for(Path file : stream) {
				// We only support flat fodler structures!
				if(Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
					continue;
				}
				
				// Filter unwanted files
				if(exclude!=null && exclude.matches(file)) {
					continue;
				}

				// We wanna start at 1-based indices
				index++;
				
				final String description = format(folderSetup.descriptionPattern, file, index);
				final String label = format(folderSetup.labelPattern, file, index);
				
				final FileUploadInfo uploadInfo = new FileUploadInfo();
				uploadInfo.setDescription(description);
				uploadInfo.setDirectoryLabel(folderSetup.path);
				uploadInfo.setFileName(label);
				uploadInfo.setMimeType(folderSetup.mediaType);
				uploadInfo.setRestricted(folderSetup.restricted);
				
				final MediaType contentType = MediaType.get(folderSetup.mediaType);
				final MultipartBody.Part filePart = filePart(contentType, file, label, folderSetup.expand);
				
				handler.add(label, folderSetup.id, filePart, uploadInfo);
			}
		}
	}

	private void uploadGenerated(GeneratorSetup generatorSetup, DatasetHandler handler) throws IOException {
		//TODO init generator, create manifests and then upload them 1-by-1
		//TODO remove thrown exception when implemenattion is done
		throw new IOException("not implemented!!!");
	}
	
	/** Map dataset PID to dataset handler */
	private final Map<String, DatasetHandler> handlers = new Object2ObjectOpenHashMap<>(); 
	
	private DatasetHandler handler(DatasetSetup dataset) {
		return handlers.computeIfAbsent(dataset.persistentId, key -> new DatasetHandler(dataset));
	}
	
	class DatasetHandler {
		
		final String persistentId;
		final String version;
		final DatasetSetup dataset;
		
		/** Map id to file names */
		private final Map<String, List<String>> fileLookup = new Object2ObjectOpenHashMap<>();
		/** Map file name to id (used during reload) - populated via {@code mapFile} */
		private final Map<String, String> idLookup = new Object2ObjectOpenHashMap<>();
		/** All the files present in the dataset */
		private final Map<String, DvFileInfo> files = new Object2ObjectOpenHashMap<>();
		private int modCount = -1;
		
		public static final String DUMMY_ID = "__DUMMY__";
		
		DatasetHandler(DatasetSetup dataset) {
			this.dataset = requireNonNull(dataset);
			persistentId = requireNonNull(dataset.persistentId, "No PID set for dataset");
			version = Optional.ofNullable(dataset.version).orElse(AdminClient.DRAFT);
		}

		void incrementModCount() { modCount++; }
		
		private void maybeRefreshLookup() throws IOException {
			if(modCount>0 || modCount==-1) {
				files.clear();
				fileLookup.clear();
				
				final List<DvFileInfo> currentFiles = doCall(client.getFiles(version, persistentId, key));
								
				for(DvFileInfo info : currentFiles) {
					files.put(info.getDataFile().getFilename(), info);
				}
				
				modCount = 0;
			}
		}
		
		void mapFile(String fileName, String id) {
			requireNonNull(fileName);
			requireNonNull(id);
			
			String oldId = idLookup.get(fileName);
			if(oldId!=null && !oldId.equals(id))
				throw new IllegalArgumentException("File already registered for different id: "+fileName);
			
			idLookup.put(fileName, id);
			fileLookup.computeIfAbsent(id, k -> new ArrayList<>()).add(fileName);
		}
		
		/** Gather ids of all files that are unknown to the setup declaration */
		LongList collectUnknown() throws IOException {
			maybeRefreshLookup();
			
			return files.values().stream()
					.map(DvFileInfo::getDataFile)
					.filter(data -> !idLookup.containsKey(data.getFilename()))
					.mapToLong(DvDataFile::getId)
					.collect(LongArrayList::new, LongList::add, (l1, l2) -> l1.addAll(l2));
					
		}
			
		/** 
		 * Resolves an id to the actual file id(s) in the dataverse. 
		 * @throws IOException */
		LongList resolveFileId(String id) throws IOException {
			checkNotEmpty(id);
			
			maybeRefreshLookup();
			
			return fileLookup.getOrDefault(id, Collections.emptyList())
					.stream()
					.map(files::get)
					.map(DvFileInfo::getDataFile)
					.mapToLong(DvDataFile::getId)
					.collect(LongArrayList::new, LongList::add, (l1, l2) -> l1.addAll(l2));
		}

		/** Add file, replace existing id present */				
		void add(String fileName, String id, MultipartBody.Part filePart, FileUploadInfo info) throws IOException {
			final RequestBody jsonData = RequestBody.create(JSON, 
					ByteString.encodeUtf8(gson.toJson(info)));
			
			DvFileInfo existing = files.get(fileName);
			
			if(existing!=null) {
				long fileId = existing.getDataFile().getId();
				System.out.printf("Replacing file %d in dataset %s: ", _long(fileId), info.getFileName(), persistentId);
				doGenericCall(client.replaceFile(fileId, key, jsonData, filePart));
			} else {
				System.out.printf("Adding file %s to dataset %s%n", info.getFileName(), persistentId);
				doGenericCall(client.addFile(persistentId, key, jsonData, filePart));
				incrementModCount();
			}
			
		}
		
		/** */
		void delete(long fileId) throws IOException {
			doGenericCall(client.deleteFileSWORD(fileId, auth));
		}
	}
	
	/** 
	 * Resolves an id to the actual file ids in any dataverse.
	 * @throws IOException in acase anything goes wrong during cache reloads
	 * @throws IllegalArgumentException in case the given {@code id} is unknown
	  */
	private LongList resolveFileId(String id) throws IOException {
		checkNotEmpty(id);
		
		for(DatasetHandler cache : this.handlers.values()) {
			LongList fileIds = cache.resolveFileId(id);
			if(!fileIds.isEmpty()) {
				return fileIds;
			}
		}
		
		throw new IllegalArgumentException("Unknown id: "+id);
	}
	
	private final Gson gson = new Gson();
	
	private static final MediaType JSON = MediaType.get("application/json");
	private static final MediaType JSON_LD = MediaType.get("application/ld+json");
	
	private MultipartBody.Part filePart(MediaType contentType, Path file, String label, boolean expand) throws IOException {
		final RequestBody fileContent;
		
		if(expand) {
			final String content = expand(file);
			fileContent = RequestBody.create(contentType, ByteString.encodeUtf8(content));
		} else {
			fileContent = RequestBody.create(contentType, file.toFile());
		}
		return MultipartBody.Part.createFormData("file", label, fileContent); 
	}
	
	private String expand(Path file) throws IOException {
		StringBuilder sb = new StringBuilder(500);
		try(BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			CharBuffer cb = CharBuffer.allocate(1<<14);
			StringBuilder output = new StringBuilder(20);
			StringBuilder idBuf = new StringBuilder(10);
			boolean readId = false;
			while(reader.read(cb)>0) {
				cb.flip();
				while(cb.hasRemaining()) {
					char c = cb.get();
					if(c=='$') {
						if(readId) {
							String id = idBuf.toString();
							idBuf.setLength(0);
							
							checkArgument("Empty id", !id.isEmpty());
							LongList fileIds = resolveFileId(id);
							if(fileIds.size()>1)
								throw new IllegalStateException("Too many files mapped to id: "+id);
							output.append(fileIds.getLong(0));
						}
						readId = !readId;
					} else if(readId) {
						idBuf.append(c);
					} else {
						output.append(c);
					}
					
				}
				cb.clear();
			}
		}
		
		return sb.toString();
	}
	
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
	
	static class FileInfo {
		private Path file;
		private boolean expand;
		private int index;
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
