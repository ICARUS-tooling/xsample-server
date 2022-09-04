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
package de.unistuttgart.xsample.pages.welcome;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.shared.ExcerptType;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class WelcomeView implements Serializable {

	private static final long serialVersionUID = 8541628961742394679L;

	/** Individual (sub) corpus selected for excerpt generation */
	private String selectedCorpus;
	/** Type of excerpt generation, legal values are 'static', 'window' and 'query'. */
	private ExcerptType excerptType = ExcerptType.STATIC;
	/** Flag to indicate that annotations should be made part of the final excerpt */
	private boolean includeAnnotations = false;
	/** Label indicating the range and type of the static excerpt */
	private String staticExcerptLabel;

	public String getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(String selectedCorpus) { this.selectedCorpus = selectedCorpus; }

	public ExcerptType getExcerptType() { return excerptType; }
	public void setExcerptType(ExcerptType type) { this.excerptType = requireNonNull(type); }

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }

	public String getStaticExcerptLabel() { return staticExcerptLabel; }
	public void setStaticExcerptLabel(String staticExcerptLabel) { this.staticExcerptLabel = staticExcerptLabel; }
}
