/**
 * 
 */
package de.unistuttgart.xsample.pages.download;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class DownloadView implements Serializable {

	private static final long serialVersionUID = 5360870757466660896L;
	
	/** Flag to indicate that annotations should be made part of the final excerpt */
	private boolean includeAnnotations = false;

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }
	
}
