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

import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.mf.XsampleManifest.SourceFile;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestGenerator {
	
	public static Builder builder() { return new Builder(); } 
	
	private long fileId = -1;
	private long unsupportedfileId = -1;
	private long size = -1;
	private XsampleManifest.SourceType sourceType = null;
	private String baseName = null;
	
	private ManifestGenerator() { /* no-op */ }
	
	private String name(String suffix) {
		return baseName+suffix+".json";
	}
	
	public List<Entry> generate() {
		return Arrays.asList(
				// Correct manifests
				new Entry(name("_default"), "", plain()),
				new Entry(name("_shifted_static"), "", shiftedStatic()),
				new Entry(name("_open_static_begin"), "", openStaticBegin()),
				new Entry(name("_open_static_end"), "", openStaticEnd()),
				//TODO manifests with hierarchical parts
				
				// Expected fails
				new Entry(name("_exceeds_quota"), "fail", exceedsQuota()),
				new Entry(name("_unsupported"), "fail", unsupportedTarget()),
				new Entry(name("_invalid_target"), "fail", invalidTarget()),
				new Entry(name("_invalid_open_static_begin"), "fail", invalidOpenStaticBegin()),
				new Entry(name("_invalid_open_static_end"), "fail", invalidOpenStaticEnd())
		);
	}
	
	// CORRECT FILES
	
	/** Plain manifest that should work */
	private XsampleManifest plain() {
		return base()
				.description("Plain manifest with no customization")
				.staticExcerptBegin(0)
				.staticExcerptEnd(10)
				.build();
	}
	
	/** Plain manifest with a shifted window for static excerpt */
	private XsampleManifest shiftedStatic() {
		return base()
				.description("Plain manifest with shifted static excerpt window")
				.staticExcerptBegin(20)
				.staticExcerptEnd(30)
				.build();
	}
	
	/** Plain manifest with a open begin for static excerpt */
	private XsampleManifest openStaticBegin() {
		return base()
				.description("Plain manifest with open begin of static excerpt interval")
				.staticExcerptEnd(10)
				.build();
	}
	
	/** Plain manifest with a open end for static excerpt */
	private XsampleManifest openStaticEnd() {
		return base()
				.description("Plain manifest with open end of static excerpt interval")
				.staticExcerptBegin(90)
				.build();
	}
	
	// EXPECTED FAILS
	
	/** Plain manifest with static excerpt declaration that exceeds quota */
	private XsampleManifest exceedsQuota() {
		return base()
				.description("Manifest that exceeds quota limit with static excerpt")
				.staticExcerptBegin(0)
				.staticExcerptEnd(50)
				.build();
	}
	
	/** Manifest that references an unsupported file and declares given source type */
	private XsampleManifest unsupportedTarget() {
		return XsampleManifest.builder()
				.target(SourceFile.builder()
						.id(unsupportedfileId)
						.segments(10)
						.sourceType(sourceType)
						.build())
				.description("Manifest that lists an unsupported target for "+sourceType)
				.staticExcerptBegin(0)
				.staticExcerptEnd(10)
				.build();
	}
	
	/** Manifest that references a non-existing file */
	private XsampleManifest invalidTarget() {
		return XsampleManifest.builder()
				.target(SourceFile.builder()
						.id(Integer.MAX_VALUE)
						.segments(size)
						.sourceType(sourceType)
						.build())
				.description("Manifest that lists an invalid target")
				.staticExcerptBegin(0)
				.staticExcerptEnd(10)
				.build();
	}
	
	/** Generate basic manfiest with only target file set */
	private XsampleManifest.Builder base() {
		return XsampleManifest.builder()
				.target(SourceFile.builder()
						.id(fileId)
						.segments(size)
						.sourceType(sourceType)
						.build())
				;
	}
	
	/** Plain manifest with a open begin for static excerpt */
	private XsampleManifest invalidOpenStaticBegin() {
		return base()
				.description("Invalid open begin of static excerpt interval")
				.staticExcerptEnd(50)
				.build();
	}
	
	/** Plain manifest with a open end for static excerpt */
	private XsampleManifest invalidOpenStaticEnd() {
		return base()
				.description("Invalid open end of static excerpt interval")
				.staticExcerptBegin(50)
				.build();
	}

	public static class Entry {
		public final XsampleManifest manifest;
		public final String name;
		public final String path;
		
		public Entry(String name, String path, XsampleManifest manifest) {
			this.name = checkNotEmpty(name);
			this.path = path;
			this.manifest = requireNonNull(manifest);
		}
	}
	
	public static class Builder {
		private final ManifestGenerator instance = new ManifestGenerator();
		
		public Builder fileId(long fileId) {
			instance.fileId = fileId;
			return this;
		}
		
		public Builder unsupportedfileId(long unsupportedfileId) {
			instance.unsupportedfileId = unsupportedfileId;
			return this;
		}
		
		public Builder size(long size) {
			instance.size = size;
			return this;
		}

		public Builder sourceType(XsampleManifest.SourceType sourceType) {
			instance.sourceType = requireNonNull(sourceType);
			return this;
		}

		public Builder baseName(String baseName) {
			instance.baseName = requireNonNull(baseName);
			return this;
		}
		
		public ManifestGenerator create() {
			checkState("File ID not set", instance.fileId!=-1);
			checkState("Unsupported file ID not set", instance.unsupportedfileId!=-1);
			checkState("Size not set", instance.size!=-1);
			checkState("Source type not set", instance.sourceType!=null);
			checkState("Base name not set", instance.baseName!=null);
			
			return instance;
		}
	}
}
