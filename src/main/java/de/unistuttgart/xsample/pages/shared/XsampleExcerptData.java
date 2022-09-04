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
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.ManifestFile;
import de.unistuttgart.xsample.mf.XsampleManifest;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Input information regarding the excerpt to be created.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleExcerptData implements Serializable {

	private static final long serialVersionUID = 142653554299182977L;
	
	public static final String ROOT = ":root";

	/** Wrapper for remote server to download data from */
	private XmpDataverse server;
	/** User to be used for tracking excerpt quota */
	private XmpDataverseUser dataverseUser;	
	
	/** The root manifest for the current workflow. */
	private XsampleManifest manifest;
	/** Current excerpt, can be from multiple files */
	private List<ExcerptEntry> excerpt = new ObjectArrayList<>();	
	
	/** Maps corpus ids to the determined segment counts that should be used for them */
	private Object2LongMap<String> segmentsByCorpus = new Object2LongOpenHashMap<>();
	
	/** Accumulated segment count from parts or a monolithic corpus. */
	private long segments = -1;
	
	/** Allowed maximum total number of segments to be given out */
	private long limit = -1;
	
	/** 0-based begin index on segments to create static excerpt */
	private long staticExcerptBegin = 0;
	/** 0-based end index on segments to create static excerpt */
	private long staticExcerptEnd = 10;
	private boolean onlySmallFiles = false;
	
	/** Switch to prevent redundant calls to verification chains */
	private boolean verified = false;
	
	// CACHES

	
	private transient Map<String, Corpus> id2Corpus = new Object2ObjectOpenHashMap<>();
	private transient Map<String, ManifestFile> label2Manifest = new Object2ObjectOpenHashMap<>();
	private transient Map<String, ManifestFile> corpusId2Manifest = new Object2ObjectOpenHashMap<>();
	private transient Long2ObjectMap<Corpus> fileId2Corpus = new Long2ObjectOpenHashMap<>();
	
	public XsampleExcerptData() {
		segmentsByCorpus.defaultReturnValue(-1);
	}
	
	private void invalidateCaches() {
		id2Corpus.clear();
		label2Manifest.clear();
		fileId2Corpus.clear();
	}
	
	private void cacheCorpus(Corpus corpus) {
		if(id2Corpus.putIfAbsent(corpus.getId(), corpus)!=null)
			throw new IllegalStateException("Duplicate corpus id: "+corpus.getId());
		
		if(corpus.getPrimaryData()!=null) {
			fileId2Corpus.put(corpus.getPrimaryData().getId().longValue(), corpus);
		}
		
		corpus.getParts().forEach(this::cacheCorpus);
	}
	
	private void cacheManifests() {
		for(ManifestFile mf : manifest.getManifests()) {
			if(label2Manifest.putIfAbsent(mf.getLabel(), mf)!=null)
				throw new IllegalStateException("Duplicate manifest label: "+mf.getLabel());
			if(corpusId2Manifest.putIfAbsent(mf.getCorpusId(), mf)!=null)
				throw new IllegalStateException("Concurrent manifests for corpus: "+mf.getCorpusId());
		}
	}

	private void validateCaches() {
		cacheCorpus(manifest.getCorpus());
		cacheManifests();
	}
	
	private void maybeValidateCaches() {
		if(manifest!=null && id2Corpus.isEmpty()) {
			validateCaches();
		}
	}
	
	public XmpDataverse getServer() { return server; }
	public void setServer(XmpDataverse server) { this.server = server; }
	
	public XmpDataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(XmpDataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public XsampleManifest getManifest() { return manifest; }
	public void setManifest(XsampleManifest manifest) { 
		this.manifest = manifest;
		invalidateCaches();
	}

	/** 0-based begin index on segments to create static excerpt */
	public long getStaticExcerptBegin() { return staticExcerptBegin; }
	public void setStaticExcerptBegin(long staticExcerptBegin) { this.staticExcerptBegin = staticExcerptBegin; }

	/** 0-based end index on segments to create static excerpt */
	public long getStaticExcerptEnd() { return staticExcerptEnd; }
	public void setStaticExcerptEnd(long staticExcerptEnd) { this.staticExcerptEnd = staticExcerptEnd; }
	
	public long getSegments() { return segments; }
	public void setSegments(long segments) { this.segments = segments; }
	
	public long getLimit() { return limit; } 
	public void setLimit(long limit) { this.limit = limit; }

	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }
	
	public List<ExcerptEntry> getExcerpt() { return excerpt; }
	public void setExcerpt(List<ExcerptEntry> excerpt) { this.excerpt = requireNonNull(excerpt); }
	
	public boolean isOnlySmallFiles() { return onlySmallFiles; }
	public void setOnlySmallFiles(boolean onlySmallFiles) { this.onlySmallFiles = onlySmallFiles; }
	
	public void registerSegments(String corpusId, long segments) {
		segmentsByCorpus.put(requireNonNull(corpusId), segments);
	}
	
	public long getSegments(Corpus part) {
		long segments = segmentsByCorpus.getLong(requireNonNull(part).getId());
		if(segments==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+part.getId());
		return segments;
	}
	
	// Utility
	
	public boolean isMultiPartCorpus() {
		return !Optional.ofNullable(manifest)
				.map(XsampleManifest::getCorpus)
				.map(Corpus::getParts)
				.orElse(Collections.emptyList())
				.isEmpty();
	}
	
	/** Find top-level corpus in manifest (if present) with given id */
	public Corpus findCorpus(String corpusId) {
		requireNonNull(corpusId);
		maybeValidateCaches();
		return id2Corpus.get(corpusId);
	}
	/** Find top-level corpus in manifest (if present) pointing to given resource */
	public Corpus findCorpus(XmpResource resource) {
		requireNonNull(resource);
		maybeValidateCaches();
		return fileId2Corpus.get(resource.getFile().longValue());
	}
	/** Find our entry (if present) matching given id */
	public ExcerptEntry findEntry(String corpusId) {
		//TODO cache?!
		requireNonNull(corpusId);
		for(ExcerptEntry entry : excerpt) {
			if(corpusId.equals(entry.getCorpusId())) {
				return entry;
			}
		}
		return null;
	}
	/** Find manifest file with given label */
	public ManifestFile findManifest(String label) {
		requireNonNull(label);
		maybeValidateCaches();
		return label2Manifest.get(label);
	}
	/** Find manifest file for given corpus */
	public ManifestFile findManifest(Corpus corpus) {
		requireNonNull(corpus);
		maybeValidateCaches();
		return corpusId2Manifest.get(corpus.getId());
	}
	
	public boolean hasEntry(Predicate<? super ExcerptEntry> pred) {
		return excerpt.stream().anyMatch(pred);
	}
	
	public void addExcerptEntry(ExcerptEntry entry) { excerpt.add(requireNonNull(entry)); }
	
	/**
	 * 
	 * @author Markus Gärtner
	 *
	 */
	public static class ExcerptEntry implements Serializable {

		private static final long serialVersionUID = 3111407155877405794L;
		
		/** Corpus or subcorpus to extract from */
		private String corpusId;
		/** Designated output */
		private List<XmpFragment> fragments;
		/** DB wrapper for the source */
		private XmpResource resource;
		/** Used up quota */
		private XmpExcerpt quota;
		/** Limit within the associated corpus */
		private long limit;
		
		public String getCorpusId() {
			return corpusId;
		}
		public void setCorpusId(String corpusId) {
			this.corpusId = corpusId;
		}
		public List<XmpFragment> getFragments() {
			return fragments;
		}
		public void setFragments(List<XmpFragment> fragments) {
			this.fragments = fragments;
		}
		
		/** Reset the fragment data on this excerpt */
		public void clear() {
			fragments = null;
		}
		
		public XmpResource getResource() { return resource; }
		public void setResource(XmpResource xmpResource) { this.resource = xmpResource; }
		
		public XmpExcerpt getQuota() { return quota; }
		public void setQuota(XmpExcerpt quota) { this.quota = quota; }
		
		public long getLimit() { return limit; }
		public void setLimit(long limit) { this.limit = limit; }
		
	}
}
