/**
 * 
 */
package de.unistuttgart.xsample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class QueryPage {
	
	public static final String PAGE = "query";

	public void init() {
		
	}
}
