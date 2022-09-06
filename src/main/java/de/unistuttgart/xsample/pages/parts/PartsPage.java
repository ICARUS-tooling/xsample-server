/**
 * 
 */
package de.unistuttgart.xsample.pages.parts;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.query.QueryPage;
import de.unistuttgart.xsample.pages.shared.ExcerptType;
import de.unistuttgart.xsample.pages.slice.SlicePage;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class PartsPage extends XsamplePage {
	
	public static final String PAGE = "parts";
	
	private static final Logger logger = Logger.getLogger(PartsPage.class.getCanonicalName());

	static final String NAV_MSG = "navMsgs";
	
	@Inject
	PartsData partsData;
	
	/** Callback for button to continue workflow */
	public void next() {
		if(partsData.isEmpty()) {
			return;
		}
		
		String page = null;

		ExcerptType excerptType = sharedData.getExcerptType();
		switch (excerptType) {
		case SLICE: page = SlicePage.PAGE; break;
		case QUERY: page = QueryPage.PAGE; break;
		default:
			break;
		}
	
		if(page==null) {
			logger.severe("Unknown page result from routing in parts page for type: "+excerptType);
			String text = BundleUtil.format("welcome.msg.unknownPage", excerptType);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
			FacesContext.getCurrentInstance().addMessage(NAV_MSG, msg);
			return;
		}
		
		forward(page);
	}
}
