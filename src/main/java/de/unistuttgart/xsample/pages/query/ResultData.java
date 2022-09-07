/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.qe.Result;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class ResultData extends SearchUtilityData {

	private static final long serialVersionUID = 5842207950786664322L;

	/** Result segments as returned by the engine */
	private Result rawResult;
	/** Result mapped to primary segments for excerpt generation */
	private Result mappedResult;
	/** Number of possible result segments */
	private long limit = -1;
	
	public Result getRawResult() { return rawResult; }
	public void setRawResult(Result rawResult) { this.rawResult = rawResult; }
	
	public Result getMappedResult() { return mappedResult; }
	public void setMappedResult(Result mappedResult) { this.mappedResult = mappedResult; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	@Override
	public void reset() {
		super.reset();
		
		rawResult = null;
		mappedResult = null;
		limit = -1;
	}
	
}
