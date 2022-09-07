/**
 * 
 */
package de.unistuttgart.xsample.pages.parts;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.util.DataBean;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class PartsData implements DataBean {
	
	private static final long serialVersionUID = 3691376427655900948L;
	
	private List<Corpus> selectedParts;
	
	public List<Corpus> getSelectedParts() { return selectedParts; }
	public void setSelectedParts(List<Corpus> selectedParts) { this.selectedParts = selectedParts; }

	public boolean isEmpty() { return selectedParts==null || selectedParts.isEmpty(); }
	
	public boolean containsPart(Corpus part) { return selectedParts!=null && selectedParts.contains(part); }

	@Override
	public String toString() {
		return String.format("%s@[selectedParts=%s]", getClass().getSimpleName(), selectedParts);
	}
}
