/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.util.DataBean;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Accumulated (global) info about corpus.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class CorpusData implements DataBean {
	
	private static final long serialVersionUID = 2235465150226921953L;
	
	/** Accumulated segment count from parts or a monolithic corpus. */
	private long segments = -1;	
	/** Allowed maximum total number of segments to be given out */
	private long excerptLimit = -1;
	/** Encoded global quota */
	private String quota = "";
	/** Maps corpus ids to the determined segment counts that should be used for them */
	private Object2LongMap<String> segmentsByCorpus = new Object2LongOpenHashMap<>();

	public CorpusData() {
		segmentsByCorpus.defaultReturnValue(-1);
	}
	
	public long getSegments() { return segments; }
	public void setSegments(long segments) { this.segments = segments; }
	
	public long getExcerptLimit() { return excerptLimit; } 
	public void setExcerptLimit(long excerptLimit) { this.excerptLimit = excerptLimit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
	
	public void registerSegments(String corpusId, long segments) {
		segmentsByCorpus.put(requireNonNull(corpusId), segments);
	}
	
	public long getSegments(Corpus part) {
		long segments = segmentsByCorpus.getLong(requireNonNull(part).getId());
		if(segments==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+part.getId());
		return segments;
	}
	
}
