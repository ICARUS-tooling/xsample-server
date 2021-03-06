/**
 * 
 */
package de.unistuttgart.xsample.pages.slice;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SliceView implements Serializable {
	
	private static final long serialVersionUID = 5111464214456376898L;
	
	private String selectedCorpus;

	public String getSelectedCorpus() {
		return selectedCorpus;
	}

	public void setSelectedCorpus(String selectedCorpus) {
		this.selectedCorpus = selectedCorpus;
	}

	
}
