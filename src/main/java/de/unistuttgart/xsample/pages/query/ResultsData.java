/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.qe.Result;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Search results and utility data for the entire corpus.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class ResultsData extends EncodedResultData {
	
	private static final long serialVersionUID = -2444737450466782057L;

	/** Raw result data for each part */
	private final  Map<String, Result> rawResults = new Object2ObjectOpenHashMap<>();
	/** Result data for each part mapped into native segments of the primary data */
	private final Map<String, Result> mappedResultsByCorpus = new Object2ObjectOpenHashMap<>();
	/** Number of possible result segments for all parts */
	private final Object2LongMap<String> rawSegmentsByCorpus = new Object2LongOpenHashMap<>();
	
	/** Accumulated raw segments */
	private long rawSegments = 0;
	
	public ResultsData() {
		rawSegmentsByCorpus.defaultReturnValue(0);
	}
	
	public long getRawSegments() { return rawSegments; }
	public void setRawSegments(long rawSegments) { this.rawSegments = rawSegments; }
	
	// RAW RESULTS

	public Result getRawResult(Corpus corpus) {
		return getRawResult(corpus.getId());
	}
	public Result getRawResult(String corpusId) {
		Result result = rawResults.get(requireNonNull(corpusId));
		if(result==null)
			throw new IllegalArgumentException("Unknown corpus id: "+corpusId);
		return result;
	}
	public void registerRawResult(Result result) {
		rawResults.put(result.getCorpusId(), result);
	}
	
	// MAPPED RESULTS
	
	public Result getMappedResult(Corpus corpus) {
		return getMappedResult(corpus.getId());
	}
	public Result getMappedResult(String corpusId) {
		Result result = mappedResultsByCorpus.get(requireNonNull(corpusId));
		if(result==null)
			throw new IllegalArgumentException("Unknown corpus id: "+corpusId);
		return result;
	}
	public void registerMappedResult(Result result) {
		mappedResultsByCorpus.put(result.getCorpusId(), result);
	}
	
	// RAW SEGMENTS
	
	public long getRawSegments(Corpus corpus) {
		return getRawSegments(corpus.getId());
	}
	public long getRawSegments(String corpusId) {
		long segments = rawSegmentsByCorpus.getLong(requireNonNull(corpusId));
		if(segments==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+corpusId);
		return segments;
	}
	public void registerRawSegments(String corpusId, long segments) {
		rawSegmentsByCorpus.put(corpusId, segments);
	}
	
	@Override
	public void reset() {
		super.reset();
		
		rawResults.clear();
		mappedResultsByCorpus.clear();
		rawSegmentsByCorpus.clear();
		
		rawSegments = 0;
	}
	
	public boolean isEmpty() { return rawResults==null || rawResults.isEmpty(); }
	
	public boolean hasResults(Corpus part) {
		return hasResults(part.getId());
	}
	public boolean hasResults(String corpusId) {
		// We use the mapped data here, since results can be pruned by not being in a mapped section
		return mappedResultsByCorpus.containsKey(corpusId);
	}
}
