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
	private String mappedHits = "";
	/** Encoded search result as native (sub)segments */
	private String rawHits = "";

	public String getMappedHits() { return mappedHits; }
	public void setMappedHits(String encodedResults) { this.mappedHits = encodedResults; }
	
	public String getRawHits() { return rawHits; }
	public void setRawHits(String resultHits) { this.rawHits = resultHits; }
	
	public void reset() {
		mappedHits = "";
		rawHits = "";
	}
}
