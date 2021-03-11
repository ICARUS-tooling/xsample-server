/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class QueryView implements Serializable {

	private static final long serialVersionUID = 1561623153508718900L;
	
	/** The raw query as defined by the user */
	private String query;
	
	public String getQuery() { return query; }
	public void setQuery(String selectedCorpus) { this.query = selectedCorpus; }

}
