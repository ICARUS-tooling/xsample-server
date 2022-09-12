/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
