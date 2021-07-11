/**
 * 
 */
package de.unistuttgart.xsample.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.unistuttgart.xsample.mf.ManifestFile;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.mf.XsampleManifest;

/**
 * Encapsulates all the information to populate a dataverse with data and maniests 
 * for XSample. 
 * <p>
 * Note that the setup is performed in a single path effectively top-to-bottom 
 * following the physical representation of the setup data. This means that
 * resolution of ids to actual files in the datasets will only ever cover the
 * files that have been uplaoded already at the point an id is to be resolved!
 * So try to structure your setup in a way that all the non-referencing data
 * is defined first with subsequent blocks of data that refernces only ids
 * that have been fully populated already. Note talso that you can include 
 * multiple {@link DatasetSetup dataset} sections for the same dataset to account
 * for complex referencing situations (but you potentially have to provide 
 * metadata for that dataset in a redundant form, depending on the files contained).
 * 
 * @author Markus Gärtner
 *
 */
public class Setup {
	
	public String server;
	public String key;
	
	public List<DatasetSetup> datasets = new ArrayList<>();

	public static class DatasetSetup {
		public String persistentId;
		public String version;
		public boolean clearAll = false;
		public boolean clearUnknown = true;
		public List<FolderSetup> folders = new ArrayList<>();
		public List<FileSetup> files = new ArrayList<>();
		public List<GeneratorSetup> generators = new ArrayList<>();
		public List<XsampleManifestSetup> manifests = new ArrayList<>();
	}
	
	public abstract static class EntrySetup {
		/** Path inside dataset */
		public @Nullable String path;
		/** Path in local file system */
		public String localPath;
		/** MIME type of the resource */
		public String mediaType; 
		/** Flag to signal access restriction */
		public boolean restricted;
		/** Flag to indicate that keywords should be expanded in the underlying (text) file */
		public boolean expand;
	}
	
	public abstract static class RefSetup extends EntrySetup {
		/** Identifier to be used for references in manifest declarations. */
		public @Nullable String id;
	}

	/** Proxy for local files inside a folder {@link Path} to be uploaded */
	public static class FolderSetup extends RefSetup {
		/** File glob to match file names to be incldued */
		public @Nullable String include;
		/** FIle glob to match file names to be excluded. This is applied after the include glob. */
		public @Nullable String exclude;
		/** Maximum number of files to include (0 if no limit) */
		public int limit = 0;
		/** 
		 * Pattern to generate the final labels for files in the folder.
		 * Legal substitution sequences are {@code ${INDEX}} and {@code ${NAME}}
		 * to be replaced by the 0-based index of the file or its actual name, 
		 * respectively.
		 */
		public String labelPattern;
		/** @see #labelPattern */
		public String descriptionPattern;
		
		public static final String NAME_PATTERN = "${NAME}";
		public static final String INDEX_PATTERN = "${INDEX}";
		public static final String DEFAULT_LABEL_PATTERN = NAME_PATTERN;
	}
	
	/** Proxy for local file {@link Path} to be uploaded */
	public static class FileSetup extends RefSetup {
		public String description;
		public String label;
	}
	
	/** Proxy for {@link XsampleManifest} */
	public static class XsampleManifestSetup extends FileSetup {
		//TODO
	}
	
	/** Proxy for {@link ManifestFile} */
	public static class ManifestSetup extends FileSetup {
		//TODO
	}
	
	/** Proxy for {@link ManifestGenerator} */
	public static class GeneratorSetup {
		public String fileId;
		public Long unsupportedfileId;
		public Long size;
		public SourceType sourceType;
		public String baseNam;
		public String basePath;
	}
}
