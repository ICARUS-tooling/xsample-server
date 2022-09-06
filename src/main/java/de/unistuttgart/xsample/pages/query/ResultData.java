/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.util.DataBean;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Data encapsulating a search result and the encoded hits as string.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class ResultData implements DataBean {
	
	private static final long serialVersionUID = -2444737450466782057L;

	/** Raw result data */
	private List<Result> results = new ObjectArrayList<>();
	
	/** Result data mapped into native segments of the primary data */
	private List<Result> mappedSegments = new ObjectArrayList<>();
	
	/** Encoded search result mapped to proper segments */
	private String segments = "";
	/** Encoded search result as native (sub)segments */
	private String resultHits = "";
	
	/** Number of possible result segments */
	private long limit = -1;
	
	public List<Result> getResults() { return results; }	
	public void setResults(List<Result> results) { this.results = requireNonNull(results); }
	
	public List<Result> getMappedSegments() { return mappedSegments; }	
	public void setMappedSegments(List<Result> mappedSegments) { this.mappedSegments = requireNonNull(mappedSegments); }

	public String getSegments() { return segments; }
	public void setSegments(String encodedResults) { this.segments = encodedResults; }
	
	public long getLimit() { return limit; }
	public void setLimit(long resultRange) { this.limit = resultRange; }
	
	public String getResultHits() { return resultHits; }
	public void setResultHits(String resultHits) { this.resultHits = resultHits; }
	
	public boolean isHasResults() { return results!=null && !results.isEmpty(); }
}
