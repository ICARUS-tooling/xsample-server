/**
 * 
 */
package de.unistuttgart.xsample;

import java.util.ResourceBundle;

import javax.ejb.Stateless;
import javax.faces.annotation.ManagedProperty;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@Stateless
public class BundleService {

	@ManagedProperty("#{bundle}")
	private ResourceBundle bundle;
	
	public String get(String key, Object...params) {
		String text = bundle.getString(key);
		if(params.length>0) {
			text = format(text, params);
		}
		return text;
	}

	private static String format(String text, Object...params) {
		if(text.indexOf('{')==-1) {
			return text;
		}

		StringBuilder result = new StringBuilder();
		String index = null;

		int paramsIndex = 0;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '{') {
				index = "";
			} else if (index != null && c == '}') {

				int tmp = paramsIndex;

				if(!index.isEmpty()) {
					tmp = Integer.parseInt(index) - 1;
				} else {
					paramsIndex++;
				}

				if (tmp >= 0 && params!=null && tmp < params.length) {
					result.append(params[tmp]);
				}

				index = null;
			} else if (index != null) {
				index += c;
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}
}
