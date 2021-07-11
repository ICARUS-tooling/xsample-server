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
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Models a resource inside the dataverse that can be used to create
 * excerpts from and that is (normally) protected and not publicly
 * available. 
 * 
 * @author Markus Gärtner
 *
 */
public class SourceFile extends DataverseFile {
	
	private static final long serialVersionUID = -95555390721328529L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"dataverseFile";

	/** 
	 * Provides a direct indicator for the segment count in the target file.
	 * If this value is present and smaller than the computed size of the
	 * resource, it will be used instead. 
	 */
	@Expose
	@Nullable
	@SerializedName(value = XsampleManifest.NS+"segments", alternate = {XsampleManifest.NS+"size", XsampleManifest.NS+"elements"})
	private Long segments;
	
	/** Type indicator to designate how the file is to be treated. */
	@Expose
	@SerializedName(XsampleManifest.NS+"sourceType")
	private SourceType sourceType;

	@Nullable
	public Long getSegments() { return segments; }

	public SourceType getSourceType() { return sourceType; }
	
	@Override
	public void validate() {
		super.validate();
		checkState("Missing 'source-type' field", sourceType!=null);
	}

	public static SourceFile.Builder builder() { return new Builder(); }
	
	public static class Builder extends AbstractBuilder<SourceFile.Builder, SourceFile> {
		
		private Builder() { /* no-op */ }

		@Override
		protected SourceFile makeInstance() { return new SourceFile(); }
		
		public SourceFile.Builder segments(long segments) {
			checkState("Segments already set", instance.segments==null);
			instance.segments = Long.valueOf(segments);
			return this;
		}
		
		public SourceFile.Builder sourceType(SourceType sourceType) {
			requireNonNull(sourceType);
			checkState("Source type already set", instance.sourceType==null);
			instance.sourceType = sourceType;
			return this;
		}
	}
}