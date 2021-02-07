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
public class DownloadPage {
	
	public static final String PAGE = "download";

	public void onDownload() {
		//TODO
	}
}
