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

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleManifest implements Serializable {
	
	private static final long serialVersionUID = 2256551725004203579L;
	
	private static final Gson gson = new GsonBuilder() 
		.excludeFieldsWithoutExposeAnnotation()
		.create();
	
	public static XsampleManifest parse(Reader reader) {
		synchronized (gson) {
			return gson.fromJson(reader, XsampleManifest.class);
		}
	}
	
	static final String NS = "xmp:";
	static final String TYPE = "@type";
	private static final String CONTEXT = "@context";
	
	@Expose
	@SerializedName(TYPE)
	private final String _type = NS+"manifest";
	
	@Expose
	@SerializedName(CONTEXT)
	private final String _context = "http://www.uni-stuttgart.de/xsample/json-ld/manifest";

	@Expose
	@SerializedName(NS+"target")
	private SourceFile target;

	@Expose
	@SerializedName(NS+"description")
	private String description;

	@Expose
	@Nullable
	@SerializedName(value = NS+"metadata", alternate = {NS+"properties"})
	private Map<String, String> metadata;
	
	@Expose
	@Nullable
	@SerializedName(NS+"manifests")
	private List<ManifestFile> manifests;
	
	@Expose
	@Nullable
	@SerializedName(NS+"staticExcerpt")
	private Span staticExcerpt;
	
	public SourceFile getTarget() { return target; }
	public String getDescription() { return description; }
	public Span getStaticExcerpt() { return staticExcerpt; }

	public Map<String, String> getMetadata() {
		return metadata==null ? Collections.emptyMap() : new HashMap<>(metadata);
	}

	public List<ManifestFile> getManifests() {
		return manifests==null ? Collections.emptyList() : new ArrayList<>(manifests);
	}
	
	// Helpers
	
	public boolean hasManifests() { return manifests!=null && !manifests.isEmpty(); }	
	public boolean hasMetadata() { return metadata!=null && !metadata.isEmpty(); }
	public boolean hasStaticExcerpt() { return staticExcerpt!=null; }
	
	public String toJSON() { return gson.toJson(this); }
	
	public static Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<XsampleManifest> {
		
		private Builder() { /* no-op */ }

		@Override
		protected XsampleManifest makeInstance() { return new XsampleManifest(); }

		/**
		 * @see de.unistuttgart.xsample.mf.BuilderBase#validate()
		 */
		@Override
		protected void validate() {
			checkState("Missing 'target' field", instance.target!=null);
			checkState("Missing 'description' field", instance.description!=null);
		}
		
		public Builder target(SourceFile target) {
			requireNonNull(target);
			checkState("Target already set", instance.target==null);
			instance.target = target;
			return this;
		}
		
		public Builder description(String description) {
			checkNotEmpty(description);
			checkState("Description already set", instance.description==null);
			instance.description = description;
			return this;
		}
		
		public Builder staticExcerpt(Span staticExcerpt) {
			requireNonNull(staticExcerpt);
			checkState("Static excerpt already set", instance.staticExcerpt==null);
			instance.staticExcerpt = staticExcerpt;
			return this;
		}
		
		private Map<String, String> ensureMetadata() {
			if(instance.metadata==null) {
				instance.metadata = new HashMap<>();
			}
			return instance.metadata;
		}
		
		public Builder metadata(Map<String, String> metadata) {
			requireNonNull(metadata);
			checkArgument("Metadata map is empty", !metadata.isEmpty());
			ensureMetadata().putAll(metadata);
			return this;
		}
		
		public Builder metadata(String key, String value) {
			requireNonNull(key);
			requireNonNull(value);
			ensureMetadata().put(key, value);
			return this;
		}
		
		private List<ManifestFile> ensureManifests() {
			if(instance.manifests==null) {
				instance.manifests = new ArrayList<>();
			}
			return instance.manifests;
		}
		
		public Builder manifests(List<ManifestFile> manifests) {
			requireNonNull(manifests);
			checkArgument("Manifest list is empty", !manifests.isEmpty());
			ensureManifests().addAll(manifests);
			return this;
		}
		
		public Builder manifest(ManifestFile manifest) {
			requireNonNull(manifest);
			ensureManifests().add(manifest);
			return this;
		}
	}
}
