/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.util;

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
