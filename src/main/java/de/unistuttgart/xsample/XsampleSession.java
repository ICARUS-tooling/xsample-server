/**
 * 
 */
package de.unistuttgart.xsample;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@SessionScoped
public class XsampleSession implements Serializable {

	private static final long serialVersionUID = 3102900115073845531L;
	
	private String localeCode = "en";

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) {
		requireNonNull(localeCode);
		this.localeCode = localeCode;
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(localeCode));
	}
}
