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
package de.unistuttgart.xsample.pages.shared;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
	private List<ExcerptEntry> excerpt = new ArrayList<>();	
	
	/** Accumulated segment count from parts or a monolithic corpus. */
	private long segments = -1;
	
	private long staticExcerptBegin = 0;
	private long staticExcerptEnd = 10;
	private boolean staticExcerptFixed = false;
	private boolean onlySmallFiles = false;
	
	/** Switch to prevent redundant calls to verification chains */
	private boolean verified = false;
	
	private String selectedCorpus;
	
	public XmpDataverse getServer() { return server; }
	public void setServer(XmpDataverse server) { this.server = server; }
	
	public XmpDataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(XmpDataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public XsampleManifest getManifest() { return manifest; }
	public void setManifest(XsampleManifest manifest) { this.manifest = manifest; }
	
	public long getStaticExcerptBegin() { return staticExcerptBegin; }
	public void setStaticExcerptBegin(long staticExcerptBegin) { this.staticExcerptBegin = staticExcerptBegin; }
	
	public long getStaticExcerptEnd() { return staticExcerptEnd; }
	public void setStaticExcerptEnd(long staticExcerptEnd) { this.staticExcerptEnd = staticExcerptEnd; }
	
	public boolean isStaticExcerptFixed() { return staticExcerptFixed; }
	public void setStaticExcerptFixed(boolean staticExcerptFixed) { this.staticExcerptFixed = staticExcerptFixed; }
	
	public long getSegments() { return segments; }
	public void setSegments(long segments) { this.segments = segments; }
	
	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }
	
	public List<ExcerptEntry> getExcerpt() { return excerpt; }
	public void setExcerpt(List<ExcerptEntry> excerpt) { this.excerpt = requireNonNull(excerpt); }
	
	public boolean isOnlySmallFiles() { return onlySmallFiles; }
	public void setOnlySmallFiles(boolean onlySmallFiles) { this.onlySmallFiles = onlySmallFiles; }

	public String getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(String selectedCorpus) { this.selectedCorpus = selectedCorpus; }
	
	// Utility
	public Corpus getStaticExcerptCorpus() {
		checkState("No manifest available", manifest!=null);
		return Optional.of(manifest)
				.map(XsampleManifest::getStaticExcerptCorpus)
				.map(this::findCorpus)
				.orElse(manifest.getCorpora().get(0));
	}
	
	/** Find top-level corpus in manifest (if present) with given id */
	public Corpus findCorpus(String corpusId) {
		requireNonNull(corpusId);
		if(manifest!=null) {
			for(Corpus corpus : manifest.getCorpora()) {
				if(corpusId.equals(corpus.getId())) {
					return corpus;
				}
			}
		}
		return null;
	}
	/** Find top-level corpus in manifest (if present) pointing to given resource */
	public Corpus findCorpus(XmpResource resource) {
		requireNonNull(resource);
		if(manifest!=null) {
			final Long fileId = resource.getFile();
			for(Corpus corpus : manifest.getCorpora()) {
				if(fileId.equals(corpus.getPrimaryData().getId())) {
					return corpus;
				}
			}
		}
		return null;
	}
	/** Find our entry (if present) matching given id */
	public ExcerptEntry findEntry(String corpusId) {
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
		if(manifest!=null) {
			for(ManifestFile manifestFile : manifest.getManifests()) {
				if(label.equals(manifestFile.getLabel())) {
					return manifestFile;
				}
			}
		}
		return null;
	}
	
	public boolean hasCorpus(Predicate<? super Corpus> pred) {
		return manifest!=null && manifest.getCorpora().stream().anyMatch(pred);
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
