/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.XsamplePage;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class QueryPage extends XsamplePage{
	
	public static final String PAGE = "query";
	
	@Inject
	XsampleQueryData queryData;

	public void init() {
		initQuota(queryData);
		//TODO further initialize query data
	}
	
	/** Callback for button to run ICARUS2 query */
	public void runQuery() {
		
	}

	/** Callback for button to continue workflow */
	public void continueWorkflow() {
		
	}
}
