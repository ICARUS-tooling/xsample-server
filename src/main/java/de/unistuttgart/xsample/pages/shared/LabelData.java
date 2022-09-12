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

import de.unistuttgart.xsample.util.DataBean;

/**
 * Stores labels for UI components that are used frequently
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class LabelData implements DataBean {

	private static final long serialVersionUID = 186905978679723821L;
	
	private String singularSegmentLabel;
	private String pluralSegmentLabel;
	/** Label indicating the range and type of the static excerpt */
	private String staticExcerptLabel;
	
	public String getSingularSegmentLabel() { return singularSegmentLabel; }
	public void setSingularSegmentLabel(String singularSegmentLabel) { this.singularSegmentLabel = singularSegmentLabel; }
	
	public String getPluralSegmentLabel() { return pluralSegmentLabel; }
	public void setPluralSegmentLabel(String pluralSegmentLabel) { this.pluralSegmentLabel = pluralSegmentLabel; }
	
	public String getSegmentLabel(boolean plural) {
		return plural ? pluralSegmentLabel : singularSegmentLabel;
	}

	public String getStaticExcerptLabel() { return staticExcerptLabel; }
	public void setStaticExcerptLabel(String staticExcerptLabel) { this.staticExcerptLabel = staticExcerptLabel; }
}
