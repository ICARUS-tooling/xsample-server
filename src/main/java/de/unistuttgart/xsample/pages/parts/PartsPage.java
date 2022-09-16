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
package de.unistuttgart.xsample.pages.parts;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
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
			ui.addError(NAV_MSG, BundleUtil.get("welcome.msg.unknownPage"), excerptType);
			return;
		}
		
		forward(page);
	}
}
