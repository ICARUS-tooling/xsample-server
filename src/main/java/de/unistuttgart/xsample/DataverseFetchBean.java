/**
 * 
 */
package de.unistuttgart.xsample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Markus G�rtner
 *
 */
@Named
@RequestScoped
public class DataverseFetchBean {
	
	@Inject
	XsamplePage xsamplePage;

	public void fetchResource() {
		System.out.println("fetching");
	}
}
