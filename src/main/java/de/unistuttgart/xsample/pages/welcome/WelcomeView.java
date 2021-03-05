/**
 * 
 */
package de.unistuttgart.xsample.pages.welcome;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.shared.ExcerptType;
import de.unistuttgart.xsample.util.Property;

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
	/** Cached properties for the manifest */
	private List<Property> manifestProperties = Collections.emptyList();
	/** Cached properties for the currently selected file */
	private List<Property> fileProperties = Collections.emptyList();

	public String getSelectedCorpus() { return selectedCorpus; }
	public void setSelectedCorpus(String selectedCorpus) { this.selectedCorpus = selectedCorpus; }

	public ExcerptType getExcerptType() { return excerptType; }
	public void setExcerptType(ExcerptType type) { this.excerptType = requireNonNull(type); }

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }

	public String getStaticExcerptLabel() { return staticExcerptLabel; }
	public void setStaticExcerptLabel(String staticExcerptLabel) { this.staticExcerptLabel = staticExcerptLabel; }
	
	public List<Property> getManifestProperties() { return manifestProperties; }
	public void setManifestProperties(List<Property> manifestProperties) { this.manifestProperties = manifestProperties; }
	
	public List<Property> getFileProperties() { return fileProperties; }
	public void setFileProperties(List<Property> fileProperties) { this.fileProperties = fileProperties; }	
}
