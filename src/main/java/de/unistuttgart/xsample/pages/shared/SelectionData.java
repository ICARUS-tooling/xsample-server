/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.util.DataBean;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SelectionData implements DataBean {

	private static final long serialVersionUID = -25756361192547866L;
	
	/** Individual (sub) corpus selected for excerpt generation */
	private String selectedCorpus;

	public String getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(String selectedCorpus) { this.selectedCorpus = selectedCorpus; }
}
