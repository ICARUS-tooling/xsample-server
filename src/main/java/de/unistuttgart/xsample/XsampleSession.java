/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@SessionScoped
public class XsampleSession implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	
	
	private String localeCode = "en";

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) { 	this.localeCode = localeCode; }
}
