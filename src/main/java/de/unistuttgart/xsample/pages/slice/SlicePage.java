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
package de.unistuttgart.xsample.pages.slice;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.shared.AbstractSlicePage;
import de.unistuttgart.xsample.pages.shared.WorkflowData;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class SlicePage extends AbstractSlicePage {
	
	public static final String PAGE = "slice";
	
	static final String NAV_MSG = "navMsgs";

	/** 
	 * Callback for button to continue workflow.
	 * <p>
	 * Updates {@link WorkflowData}, {@link DownloadData}. 
	 */
	public void next() {
		next(NAV_MSG, DownloadPage.PAGE);
	}
}
