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
package de.unistuttgart.xsample.pages.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;

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
	
	private boolean caseSensitive = true;
	private int limit = 0;
	
	private List<Corpus> selectedParts = Collections.emptyList();

	public List<Corpus> getSelectedParts() { return selectedParts; }
	public void setSelectedParts(List<Corpus> selectedParts) { this.selectedParts = selectedParts; }
	
	public String getQuery() { return query; }
	public void setQuery(String selectedCorpus) { this.query = selectedCorpus; }
}
