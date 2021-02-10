/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleSliceData implements Serializable {

	private static final long serialVersionUID = -5558438814838979800L;

	/** Begin of slice. 1-based. */
	private long begin = 1;
	/** End of slice. 1-based. */
	private long end = 1;
	/** Total number of segments available */
	private long range = 1;
	/** Upper limit of allowed segments to be published */
	private long limit = 1;
	/** Encoded used up quota */
	private String quota = "";
	
	public long getBegin() { return begin; }
	public void setBegin(long begin) { this.begin = begin; }
	
	public long getEnd() { return end; }
	public void setEnd(long end) { this.end = end; }
	
	public long getSize() { return end-begin+1; }
	
	public long getRange() { return range; }
	public void setRange(long range) { this.range = range; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
	
	/** Size of current slice in percent. Including quota */
	public double getPercent() {
		return (double)getSize() / getRange() * 100.0;
	}
}
