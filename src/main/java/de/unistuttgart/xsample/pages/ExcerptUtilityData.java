/**
 * 
 */
package de.unistuttgart.xsample.pages;

import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExcerptUtilityData implements Serializable {

	private static final long serialVersionUID = -6599365533185080045L;

	/** Total number of segments available */
	private long range = 1;
	/** Upper limit of allowed segments to be published */
	private long limit = 1;
	/** Encoded used up quota */
	private String quota = "";
	
	public long getRange() { return range; }
	public void setRange(long range) { this.range = range; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
}
