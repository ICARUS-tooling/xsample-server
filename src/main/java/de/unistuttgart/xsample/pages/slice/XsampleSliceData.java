/**
 * 
 */
package de.unistuttgart.xsample.pages.slice;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.ExcerptUtilityData;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleSliceData extends ExcerptUtilityData {

	private static final long serialVersionUID = -5558438814838979800L;

	/** Begin of slice. 1-based. */
	private long begin = 1;
	/** End of slice. 1-based. */
	private long end = 1;
	
	public long getBegin() { return begin; }
	public void setBegin(long begin) { this.begin = begin; }
	
	public long getEnd() { return end; }
	public void setEnd(long end) { this.end = end; }
	
	public long getSize() { return end-begin+1; }
	
	/** Size of current slice in percent. Including quota */
	public double getPercent() {
		return (double)getSize() / getRange() * 100.0;
	}
}
