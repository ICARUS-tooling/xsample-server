/**
 * 
 */
package de.unistuttgart.xsample.pages.download;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

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
	
	/** Flag to indicate that annotations should be made part of the final excerpt */
	private boolean includeAnnotations = false;

	public List<ExcerptEntry> getEntries() { return entries; }
	public void setEntries(List<ExcerptEntry> excerpt) { this.entries = requireNonNull(excerpt); }

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }

	public ExcerptEntry findEntry(Corpus corpus) {
		return findEntry(corpus.getId());
	}

	/** Find our entry (if present) matching given corpus id */
	public ExcerptEntry findEntry(String corpusId) {
		requireNonNull(corpusId);
		if(lookup.isEmpty() && !entries.isEmpty()) {
			entries.forEach(e -> lookup.put(e.getCorpusId(), e));
		}
		return lookup.get(corpusId);
	}

	public void addEntry(ExcerptEntry entry) { 
		entries.add(requireNonNull(entry)); 
		if(lookup.putIfAbsent(entry.getCorpusId(), entry)!=null)
			throw new IllegalStateException("Duplicate entry ID: "+entry.getCorpusId());
	}

	public void clear() {
		entries.clear();
		lookup.clear();
	}
}
