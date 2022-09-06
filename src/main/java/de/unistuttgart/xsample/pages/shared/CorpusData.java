/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
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
public class CorpusData extends ExcerptUtilityData {
	
	private static final long serialVersionUID = 2235465150226921953L;
	
	/** Maps corpus ids to the determined segment counts that should be used for them */
	private Object2LongMap<String> segmentsByCorpus = new Object2LongOpenHashMap<>();

	public CorpusData() {
		segmentsByCorpus.defaultReturnValue(-1);
	}
	
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
