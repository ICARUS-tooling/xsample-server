/**
 * 
 */
package de.unistuttgart.xsample;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;



/**
 * @author Markus Gärtner
 *
 */
public class BundleUtil {
	
	private static final Logger logger = Logger.getLogger(BundleUtil.class.getCanonicalName());

    private static final String baseName = "propertyFiles/Bundle";
	private static volatile ResourceBundle bundle;
	
	private static ResourceBundle bundle() {
		if(bundle==null) {
			synchronized (BundleUtil.class) {
				if(bundle==null) {
					Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
					bundle = ResourceBundle.getBundle(baseName, locale);
				}
			}
		}
		return bundle;
	}
	
	public static String get(String key, Object...params) {
		String text;
		
		try {
			text = bundle().getString(key);
		} catch(MissingResourceException e) {
			logger.warning("Missing localization entry for key: "+key);
			return key;
		}
		if(params.length>0) {
			text = MessageFormat.format(text, params);
		}
		return text;
	}
}
