/**
 * 
 */
package de.unistuttgart.xsample.pages.parts;

import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.util.DataBean;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class PartsData implements DataBean {
	
	private static final long serialVersionUID = 3691376427655900948L;
	
	private List<Corpus> selectedParts;
	
	private transient final Set<String> ids = new ObjectOpenHashSet<>();
	
	public List<Corpus> getSelectedParts() { return selectedParts; }
	public void setSelectedParts(List<Corpus> selectedParts) { 
		this.selectedParts = selectedParts;
		ids.clear();
	}

	public boolean isEmpty() { return selectedParts==null || selectedParts.isEmpty(); }
	
	private void ensureLookup() {
		if(ids.isEmpty() && !isEmpty()) {
			selectedParts.forEach(c -> ids.add(c.getId()));
		}
	}
	
	public boolean containsPart(Corpus part) { 
		ensureLookup();
		return ids.contains(part.getId());
	}
	
	public boolean containsPart(String partId) { 
		ensureLookup();
		return ids.contains(partId);
	}

	@Override
	public String toString() {
		return String.format("%s@[selectedParts=%s]", getClass().getSimpleName(), selectedParts);
	}
}
