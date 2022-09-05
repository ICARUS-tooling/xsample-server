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
package de.unistuttgart.xsample.pages.slice;

import java.io.Serializable;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SliceView implements Serializable {
	
	private static final long serialVersionUID = 5111464214456376898L;
	
	private Set<String> selectedParts = new ObjectOpenHashSet<>();
	private String selectedCorpus;
	private boolean includeCorpus = false;

	public Set<String> getSelectedParts() { return selectedParts; }
	public void setSelectedParts(Set<String> selectedParts) { this.selectedParts = selectedParts;  }
	
	public String getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(String selectedCorpus) { this.selectedCorpus = selectedCorpus; }
	
	public boolean isIncludeCorpus() { return includeCorpus; }
	public void setIncludeCorpus(boolean includeCorpus) { this.includeCorpus = includeCorpus; }
}
