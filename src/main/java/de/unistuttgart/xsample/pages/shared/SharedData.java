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

import static de.unistuttgart.xsample.util.XSampleUtils.decrypt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.ManifestFile;
import de.unistuttgart.xsample.mf.MappingFile;
import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.mp.Mapping;
import de.unistuttgart.xsample.mp.Mappings;
import de.unistuttgart.xsample.util.DataBean;
import de.unistuttgart.xsample.util.XSampleUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Input information regarding the excerpt to be created.
 * Contains raw input data and some universally shared global info.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SharedData implements DataBean {

	private static final long serialVersionUID = 142653554299182977L;
	
	public static final String ROOT = ":root";

	/** Wrapper for remote server to download data from */
	private XmpDataverse server;
	/** User to be used for tracking excerpt quota */
	private XmpDataverseUser dataverseUser;	
	
	/** The root manifest for the current workflow. */
	private XsampleManifest manifest;
	
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
	private transient Map<String, Mapping> mappings = new Object2ObjectOpenHashMap<>();

	/** Type of excerpt generation, legal values are 'static', 'window' and 'query'. */
	private ExcerptType excerptType = ExcerptType.STATIC;
	
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

	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }
	
	public boolean isOnlySmallFiles() { return onlySmallFiles; }
	public void setOnlySmallFiles(boolean onlySmallFiles) { this.onlySmallFiles = onlySmallFiles; }
		
	// Utility
	
	public boolean isMultiPartCorpus() {
		return !Optional.ofNullable(manifest)
				.map(XsampleManifest::getCorpus)
				.map(Corpus::getParts)
				.orElse(Collections.emptyList())
				.isEmpty();
	}
	
	public boolean isHasAnnotations() {
		return manifest!=null && manifest.hasManifests();
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
	
	public Mapping getMapping(MappingFile mappingFile, XsampleServices services, LocalCache cache) 
			throws GeneralSecurityException, IOException, UnsupportedContentTypeException, InterruptedException {

		Mapping mapping = mappings.get(mappingFile.getLabel());
		
		if(mapping==null) {
			final XmpResource resource = services.findResource(getServer(), mappingFile.getId());
			final XmpLocalCopy copy = cache.getCopy(resource);

			copy.getLock().tryLock(50, TimeUnit.MILLISECONDS);
			try {
				final Path file = cache.getDataFile(copy);
				final Cipher cipher = decrypt(XSampleUtils.deserializeKey(copy.getKey()));
				final Charset encoding = Charset.forName(copy.getEncoding());
				
				mapping = Mappings.forMappingType(mappingFile.getMappingType());
		
				try(InputStream raw = Files.newInputStream(file, StandardOpenOption.READ);
						InputStream in = new CipherInputStream(raw, cipher);
						Reader reader = new InputStreamReader(in, encoding);) {
					mapping.load(reader);
				}
				mappings.put(mappingFile.getLabel(), mapping);
			} finally {
				copy.getLock().unlock();
			}
		}
		
		return mapping;
	}

	public ExcerptType getExcerptType() { return excerptType; }

	public void setExcerptType(ExcerptType type) { this.excerptType = requireNonNull(type); }
}
