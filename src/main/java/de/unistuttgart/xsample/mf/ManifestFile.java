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
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Models a third-party manifest file associated with a particular
 * {@link SourceFile} and that is used to guide/enable the excerpt
 * creation process based on the content of that source file or
 * external annotations. 
 */
public class ManifestFile extends DataverseFile {
	
	private static final long serialVersionUID = -4467949132014774288L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"manifestFile";
	
	/** Type indicator for the manifest. Currently we only support {@link ManifestType#IMF}! */
	@Expose
	@SerializedName(XsampleManifest.NS+"manifestType")
	private ManifestType manifestType;
	
	/** File containing mapping information for the primary layer of this manifest. */
	@Expose
	@SerializedName(XsampleManifest.NS+"mappingFile")
	private MappingFile mappingFile;

	/** Id of the corpus file this manifest refers to. This allows a shallow list of
	 * manifests to be properly linked to a complex hierarchy of corpus parts. */
	@Expose
	@SerializedName(XsampleManifest.NS+"corpusId")
	private String corpusId;

	public ManifestType getManifestType() { return manifestType; }
	public MappingFile getMappingFile() { return mappingFile; }
	public String getCorpusId() { return corpusId; }
	
	@Override
	public void validate() {
		super.validate();
		checkState("Missing 'label' field", getLabel()!=null);
		checkState("Missing 'corpusId' field", corpusId!=null);
		checkState("Missing 'manifestType' field", manifestType!=null);
		checkState("Missing 'mappingFile' field", mappingFile!=null);
	}

	public static ManifestFile.Builder builder() { return new Builder(); }

	public static class Builder extends AbstractBuilder<ManifestFile.Builder, ManifestFile> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected ManifestFile makeInstance() { return new ManifestFile(); }
		
		public ManifestFile.Builder manifestType(ManifestType manifestType) {
			requireNonNull(manifestType);
			checkState("Manifest type already set", instance.manifestType==null);
			instance.manifestType = manifestType;
			return this;
		}
		
		public ManifestFile.Builder mappingFile(MappingFile mappingFile) {
			requireNonNull(mappingFile);
			checkState("Mapping file already set", instance.mappingFile==null);
			instance.mappingFile = mappingFile;
			return this;
		}
		
		public ManifestFile.Builder corpusId(String corpusId) {
			requireNonNull(corpusId);
			checkState("Corpus ID already set", instance.corpusId==null);
			instance.corpusId = corpusId;
			return this;
		}
	}
}