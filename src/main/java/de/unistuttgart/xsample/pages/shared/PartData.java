/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class PartData extends ExcerptUtilityData {

	private static final long serialVersionUID = -4223925752620450354L;

	public void reset() {
		setExcerptLimit(0);
		setSegments(0);
		setQuota("");
	}
}
