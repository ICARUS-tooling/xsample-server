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
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.mf.Corpus;
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
	
	/** Physical info about primary source file(s) */
	private List<FileInfo> fileInfos = new ArrayList<>();	
	
	/** The root manifest for the current workflow. */
	private XsampleManifest manifest;
	/** Current excerpt, can be from multiple files */
	private List<ExcerptEntry> excerpt = new ArrayList<>();	
	
	/** Accumulated segment count from parts or a monolithic corpus. */
	private long segments = -1;
	
	private long staticExcerptBegin = 0;
	private long staticExcerptEnd = 10;
	private boolean staticExcerptFixed = false;
	private boolean hasAnnotation = false;
	
	/** Switc hto prevent redundant calls to verification chains */
	private boolean verified = false;
	
	public XmpDataverse getServer() { return server; }
	public void setServer(XmpDataverse server) { this.server = server; }
	
	public XmpDataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(XmpDataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public XsampleManifest getManifest() { return manifest; }
	public void setManifest(XsampleManifest manifest) { this.manifest = manifest; }
	
	public List<FileInfo> getFileInfos() { return fileInfos; }
	public void setFileInfos(List<FileInfo> fileInfos) { this.fileInfos = requireNonNull(fileInfos); }
	
	public long getStaticExcerptBegin() { return staticExcerptBegin; }
	public void setStaticExcerptBegin(long staticExcerptBegin) { this.staticExcerptBegin = staticExcerptBegin; }
	
	public long getStaticExcerptEnd() { return staticExcerptEnd; }
	public void setStaticExcerptEnd(long staticExcerptEnd) { this.staticExcerptEnd = staticExcerptEnd; }
	
	public boolean isStaticExcerptFixed() { return staticExcerptFixed; }
	public void setStaticExcerptFixed(boolean staticExcerptFixed) { this.staticExcerptFixed = staticExcerptFixed; }
	
	public long getSegments() { return segments; }
	public void setSegments(long segments) { this.segments = segments; }
	
	public boolean isHasAnnotation() { return hasAnnotation; }
	public void setHasAnnotation(boolean hasAnnotation) { this.hasAnnotation = hasAnnotation; }
	
	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }
	
	public List<ExcerptEntry> getExcerpt() { return excerpt; }
	public void setExcerpt(List<ExcerptEntry> excerpt) { this.excerpt = requireNonNull(excerpt); }
	
	// Utility
	
	/** Find file info with given corpus id */
	@Nullable
	public FileInfo findFileInfo(String corpusId) { 
		requireNonNull(corpusId);
		for(FileInfo fileInfo : fileInfos) {
			if(corpusId.equals(fileInfo.getCorpusId())) {
				return fileInfo;
			}
		}
		return null;
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
	/** Find corpus associated with given file info */
	public Corpus findCorpus(FileInfo fileInfo) {
		requireNonNull(fileInfo);
		return findCorpus(fileInfo.getCorpusId());
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
	/** Fetch file info matching manifest entry or first registered file info if no such label is set. */
	public FileInfo getStaticExcerptFileInfo() {
		checkState("No file infos registered", !fileInfos.isEmpty());
		return Optional.ofNullable(manifest)
				.map(XsampleManifest::getStaticExcerptFile)
				.map(this::findFileInfo)
				.orElse(fileInfos.get(0));
	}
	public boolean getIsMultiPartCorpus() { return fileInfos.size()>1; }
	public void forEachFileInfo(Consumer<FileInfo> action) { fileInfos.forEach(action); }
	
	public boolean hasFileInfo(Predicate<? super FileInfo> pred) {
		return fileInfos.stream().anyMatch(pred);
	}
	public boolean hasCorpus(Predicate<? super Corpus> pred) {
		return manifest.getCorpora().stream().anyMatch(pred);
	}
	
	public void addFileInfo(FileInfo file) { fileInfos.add(requireNonNull(file)); }
	public void addExcerptEntry(ExcerptEntry entry) { excerpt.add(requireNonNull(entry)); }
	public void resetExcerpt() { excerpt = new ArrayList<>(); }
	
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
		
		public XmpResource getResource() { return resource; }
		public void setResource(XmpResource xmpResource) { this.resource = xmpResource; }
		
		public XmpExcerpt getQuota() { return quota; }
		public void setQuota(XmpExcerpt quota) { this.quota = quota; }
		
	}
}
