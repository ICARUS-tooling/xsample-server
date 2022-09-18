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
package de.unistuttgart.xsample.pages.download;

import static de.unistuttgart.xsample.util.XSampleUtils._boolean;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.util.DataBean;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Contains all the download info to create excerpts.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class DownloadData implements DataBean {

	private static final long serialVersionUID = -1359149615918083628L;

	/** Current excerpt, can be from multiple files */
	private List<ExcerptEntry> entries = new ObjectArrayList<>();
	/**  Cache for improved lookup performance */
	private transient Map<String, ExcerptEntry> lookup = new Object2ObjectOpenHashMap<>();
	/** Cached accumulated size of excerpts */
	private long size = -1;
	/** Flag to indicate that annotations should be made part of the final excerpt */
	private boolean includeAnnotations = false;

	public List<ExcerptEntry> getEntries() { return entries; }
	public void setEntries(List<ExcerptEntry> excerpt) { 
		this.entries = requireNonNull(excerpt);
		lookup.clear();
		size = -1;
	}

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }

	public @Nullable ExcerptEntry findEntry(Corpus corpus) {
		return findEntry(corpus.getId());
	}

	/** Find our entry (if present) matching given corpus id */
	public @Nullable ExcerptEntry findEntry(String corpusId) {
		requireNonNull(corpusId);
		if(lookup.isEmpty() && !entries.isEmpty()) {
			entries.forEach(e -> lookup.put(e.getCorpusId(), e));
		}
		return lookup.get(corpusId);
	}

	public void addEntry(ExcerptEntry entry) { 
		if(lookup.putIfAbsent(entry.getCorpusId(), entry)!=null)
			throw new IllegalStateException("Duplicate entry ID: "+entry.getCorpusId());
		entries.add(requireNonNull(entry)); 
		size = -1;
	}
	
	public long getSize() {
		if(size==-1) {
			size = entries.isEmpty() ? 0 : entries.stream().mapToLong(ExcerptEntry::size).sum();
		}
		return size;
	}

	public void clear() {
		entries.clear();
		lookup.clear();
	}
	
	@Override
	public String toString() {
		return String.format("%s@[includeAnno=%b, entries=%s]", getClass().getSimpleName(), _boolean(includeAnnotations), entries);
	}
}
