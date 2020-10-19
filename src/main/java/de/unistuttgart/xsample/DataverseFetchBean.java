/**
 * 
 */
package de.unistuttgart.xsample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class DataverseFetchBean {
	
	@Inject
	XsamplePage xsamplePage;

	public void fetchResource() {
		System.out.println("fetching");
		
		String server = "http://193.196.54.150:8080";
		String file = "29";
		String key = "255534f5-d040-4d62-bfae-7b1cc9776a16";
		
		// /api/access/datafile/$id
		String endpoint = "/api/access/datafile/";
		
		//TODO
	}
}
