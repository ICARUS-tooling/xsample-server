/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import de.unistuttgart.xsample.util.DataBean;

/**
 * Models basic properties of encoded search results.
 * 
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("serial")
public abstract class EncodedResultData implements DataBean {

	/** Encoded search result mapped to proper segments */
	private String segments = "";
	/** Encoded search result as native (sub)segments */
	private String resultHits = "";

	public String getSegments() { return segments; }
	public void setSegments(String encodedResults) { this.segments = encodedResults; }
	
	public String getResultHits() { return resultHits; }
	public void setResultHits(String resultHits) { this.resultHits = resultHits; }
	
	public void reset() {
		segments = "";
		resultHits = "";
	}
}
