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

import de.unistuttgart.xsample.util.SelfValidating;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleManifest implements Serializable, SelfValidating {
	
	private static final long serialVersionUID = 2256551725004203579L;
	
	private static final Gson gson = new GsonBuilder() 
		.excludeFieldsWithoutExposeAnnotation()
		.create();
	
	public static XsampleManifest parse(Reader reader) {
		synchronized (gson) {
			return gson.fromJson(reader, XsampleManifest.class);
		}
	}
	
	/** Namespace prefix for XSample manifest elements. */
	static final String NS = "xmp:";
	/** Type identifier (from JSON-LD). */
	static final String TYPE = "@type";
	/** Context scheme identifier (from JSON-LD). */
	private static final String CONTEXT = "@context";
	
	@Expose
	@SerializedName(TYPE)
	private final String _type = NS+"manifest";
	
	@Expose
	@SerializedName(CONTEXT)
	private final String _context = "http://www.uni-stuttgart.de/xsample/json-ld/manifest";

	@Expose
	@SerializedName(NS+"description")
	private String description;

	@Expose
	@Nullable
	@SerializedName(value = NS+"metadata", alternate = {NS+"properties"})
	private Map<String, String> metadata;
	
	@Expose
	@SerializedName(NS+"corpus")
	private Corpus corpus;
	
	@Expose
	@Nullable
	@SerializedName(NS+"staticExcerpt")
	private Span staticExcerpt;

	@Expose
	@Nullable
	@SerializedName(NS+"staticExcerptCorpus")
	private String staticExcerptCorpus;
	
	/** Optional (external) manifests sued by query engines and other utility modules. */
	@Expose
	@Nullable
	@SerializedName(XsampleManifest.NS+"manifests")
	private List<ManifestFile> manifests = new ArrayList<>();
	
	public String getDescription() { return description; }
	public Span getStaticExcerpt() { return staticExcerpt; }
	public Map<String, String> getMetadata() {
		return metadata==null ? Collections.emptyMap() : new HashMap<>(metadata);
	}
	public Corpus getCorpus() { return corpus; }
	public String getStaticExcerptCorpus() { return staticExcerptCorpus; }
	/** Optional (external) manifests used by query engines and other utility modules. */
	public List<ManifestFile> getManifests() {
		return manifests==null ? Collections.emptyList() : new ArrayList<>(manifests);
	}
	
	// Helpers	
	public boolean hasManifests() { return manifests!=null && !manifests.isEmpty(); }	
	public boolean hasMetadata() { return metadata!=null && !metadata.isEmpty(); }
	public boolean hasStaticExcerpt() { return staticExcerpt!=null; }
	
	public String toJSON() { return gson.toJson(this); }
	
	/** Returns a flat collection of corpus parts. Does not recursively collect deeply nested parts! */
	public List<Corpus> getAllParts() {
		List<Corpus> parts = corpus.getParts();
		if(parts.isEmpty()) {
			parts = Collections.singletonList(corpus);
		}
		return parts;
	}

	@Override
	public void validate() {
		checkState("Missing 'description' field", description!=null);
		SelfValidating.validateNested(corpus, "corpus");
		SelfValidating.validateOptionalNested(staticExcerpt);
		SelfValidating.validateOptionalNested(manifests);
	}
	
	public static Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<XsampleManifest> {
		
		private Builder() { /* no-op */ }

		@Override
		protected XsampleManifest makeInstance() { return new XsampleManifest(); }
		
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
		
		public Builder corpus(Corpus corpus) {
			requireNonNull(corpus);
			checkState("Corpus already added", instance.corpus==null);
			instance.corpus = corpus;
			return this;
		}
		
		public Builder manifests(List<ManifestFile> manifests) {
			requireNonNull(manifests);
			instance.manifests.clear();
			instance.manifests.addAll(manifests);
			return this;
		}
		
		public Builder manifest(ManifestFile manifest) {
			requireNonNull(manifest);
			checkState("Manifest already added", instance.manifests.add(manifest));
			return this;
		}
		
		public Builder staticExcerptCorpus(String staticExcerptCorpus) {
			requireNonNull(staticExcerptCorpus);
			checkState("Static excerpt corpus already set", instance.staticExcerptCorpus==null);
			instance.staticExcerptCorpus = staticExcerptCorpus;
			return this;
		}
	}
}
