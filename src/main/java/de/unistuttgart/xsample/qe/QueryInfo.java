/**
 * 
 */
package de.unistuttgart.xsample.qe;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public class QueryInfo {

	private final List<Result> results;
	private final long segments;
	
	public QueryInfo(List<Result> results, long segments) {
		this.results = requireNonNull(results);
		this.segments = segments;
	}
	
	public List<Result> getResults() {
		return results;
	}
	public long getSegments() {
		return segments;
	}
}
