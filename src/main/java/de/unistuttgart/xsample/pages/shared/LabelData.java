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
