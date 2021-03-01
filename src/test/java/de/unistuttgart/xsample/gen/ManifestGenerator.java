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
import de.unistuttgart.xsample.mf.XsampleManifest.SourceType;
import de.unistuttgart.xsample.mf.XsampleManifest.Span;
import de.unistuttgart.xsample.mf.XsampleManifest.SpanType;

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
	private String basePath = null;
	
	private ManifestGenerator() { /* no-op */ }
	
	private String name(String suffix) {
		return baseName+suffix+".json";
	}
	
	private String path(String path) {
		if("".equals(path)) {
			return basePath;
		}
		String s = basePath;
		if(s==null) {
			s = "";
		} else if(!s.endsWith("/")) {
			s += '/';
		}
		s += path;
		return s;
	}
	
	public List<Entry> generate() {
		final String validPath = path("");
		final String failPath = path("fail");
		return Arrays.asList(
				// Correct manifests
				new Entry(name("_c001_default"), validPath, plain()),
				new Entry(name("_c002_fixed_excerpt"), validPath, fixedExcerpt()),
				new Entry(name("_c003_shifted_static"), validPath, shiftedStatic()),
				new Entry(name("_c004_open_static_begin"), validPath, openStaticBegin()),
				new Entry(name("_c005_open_static_end"), validPath, openStaticEnd()),
				//TODO manifests with hierarchical parts
				
				// Expected fails
				new Entry(name("_f001_exceeds_quota"), failPath, exceedsQuota()),
				new Entry(name("_f002_exceeds_fixed_quota"), failPath, exceedsFixedQuota()),
				new Entry(name("_f003_unsupported"), failPath, unsupportedTarget()),
				new Entry(name("_f004_invalid_target"), failPath, invalidTarget()),
				new Entry(name("_f005_invalid_open_static_begin"), failPath, invalidOpenStaticBegin()),
				new Entry(name("_f006_invalid_open_static_end"), failPath, invalidOpenStaticEnd())
		);
	}
	
	// CORRECT FILES
	
	/** Plain manifest that should work */
	private XsampleManifest plain() {
		return base()
				.description("Plain manifest with no customization (first 10%)")
				.staticExcerpt(span(SpanType.RELATIVE, 0, 10))
				.build();
	}
	
	/** Fixed pages */
	private XsampleManifest fixedExcerpt() {
		return base()
				.description("Plain manifest with fixed excerpt pages [5-14]")
				.staticExcerpt(span(SpanType.FIXED, 5, 14))
				.build();
	}
	
	/** Plain manifest with a shifted window for static excerpt */
	private XsampleManifest shiftedStatic() {
		return base()
				.description("Plain manifest with shifted static excerpt window [20%-30%)")
				.staticExcerpt(span(SpanType.RELATIVE, 20, 30))
				.build();
	}
	
	/** Plain manifest with a open begin for static excerpt */
	private XsampleManifest openStaticBegin() {
		return base()
				.description("Plain manifest with open begin of static excerpt interval")
				.staticExcerpt(span(SpanType.RELATIVE, -1, 10))
				.build();
	}
	
	/** Plain manifest with a open end for static excerpt */
	private XsampleManifest openStaticEnd() {
		return base()
				.description("Plain manifest with open end of static excerpt interval")
				.staticExcerpt(span(SpanType.RELATIVE, 90, -1))
				.build();
	}
	
	// EXPECTED FAILS
	
	/** Plain manifest with static excerpt declaration that exceeds quota */
	private XsampleManifest exceedsQuota() {
		return base()
				.description("Manifest that exceeds quota limit with static excerpt")
				.staticExcerpt(span(SpanType.RELATIVE, 0, 50))
				.build();
	}
	
	/** Plain manifest with fixed excerpt declaration that exceeds quota */
	private XsampleManifest exceedsFixedQuota() {
		return base()
				.description("Manifest that exceeds quota limit with fixed excerpt")
				.staticExcerpt(span(SpanType.FIXED, 0, 50))
				.build();
	}
	
	/** Manifest that references an unsupported file and declares given source type */
	private XsampleManifest unsupportedTarget() {
		return base(unsupportedfileId, 10, sourceType)
				.description("Manifest that lists an unsupported target for "+sourceType)
				.staticExcerpt(span(SpanType.RELATIVE, 0, 10))
				.build();
	}
	
	/** Manifest that references a non-existing file */
	private XsampleManifest invalidTarget() {
		return base(Integer.MAX_VALUE, size, sourceType)
				.description("Manifest that lists an invalid target")
				.staticExcerpt(span(SpanType.RELATIVE, 0, 10))
				.build();
	}
	
	/** Plain manifest with a open begin for static excerpt */
	private XsampleManifest invalidOpenStaticBegin() {
		return base()
				.description("Invalid open begin of static excerpt interval")
				.staticExcerpt(span(SpanType.RELATIVE, -1, 50))
				.build();
	}

	/** Plain manifest with a open end for static excerpt */
	private XsampleManifest invalidOpenStaticEnd() {
		return base()
				.description("Invalid open end of static excerpt interval")
				.staticExcerpt(span(SpanType.RELATIVE, 50, -1))
				.build();
	}

	/** Generate basic manfiest with only target file set */
	private XsampleManifest.Builder base(long fileId, long size, SourceType sourceType) {
		return XsampleManifest.builder()
				.target(SourceFile.builder()
						.id(fileId)
						.segments(size)
						.sourceType(sourceType)
						.build())
				;
	}
	
	private XsampleManifest.Builder base() { return base(fileId, size, sourceType); }
	
	private Span span(SpanType spanType,  long begin, long end) {
		Span.Builder builder = Span.builder().spanType(spanType);
		if(begin!=-1) builder.begin(begin);
		if(end!=-1) builder.end(end);
		return builder.build();
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

		public Builder basePath(String basePath) {
			instance.basePath = requireNonNull(basePath);
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
