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
package de.unistuttgart.xsample.pages.shared;

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
public class SelectionData implements DataBean {

	private static final long serialVersionUID = -25756361192547866L;
	
	/** Individual (sub) corpus selected for excerpt generation */
	private Corpus selectedCorpus;

	public Corpus getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(Corpus selectedCorpus) { this.selectedCorpus = selectedCorpus; }
}
