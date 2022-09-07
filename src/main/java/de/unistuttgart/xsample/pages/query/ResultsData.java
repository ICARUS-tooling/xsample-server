/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.qe.Result;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Data encapsulating a search result and the encoded hits as string.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class ResultsData extends SearchUtilityData {
	
	private static final long serialVersionUID = -2444737450466782057L;

	/** Raw result data for each part */
	private final  Map<String, Result> rawResults = new Object2ObjectOpenHashMap<>();
	/** Result data for each part mapped into native segments of the primary data */
	private final Map<String, Result> mappedResults = new Object2ObjectOpenHashMap<>();
	/** Number of possible result segments for all parts */
	private final Object2LongMap<String> limits = new Object2LongOpenHashMap<>();
	
	public List<Result> getResults() { return results; }	
	public void setResults(List<Result> results) { this.results = requireNonNull(results); }
	
	public List<Result> getMappedSegments() { return mappedSegments; }	
	public void setMappedSegments(List<Result> mappedSegments) { this.mappedSegments = requireNonNull(mappedSegments); }
	
	public long getLimit() { return limit; }
	public void setLimit(long resultRange) { this.limit = resultRange; }
	
	@Override
	public void reset() {
		super.reset();
		
		rawResults.clear();
		mappedResults.clear();
		limits.clear();
	}
	
	public boolean isEmpty() { return rawResults==null || rawResults.isEmpty(); }
}
