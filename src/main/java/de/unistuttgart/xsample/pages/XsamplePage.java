/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.pages;

import javax.inject.Inject;

import org.primefaces.PrimeFaces;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow;

/**
 * @author Markus Gärtner
 *
 */
public class XsamplePage {
	
	@Inject
	protected XsampleWorkflow workflow;
	
	@Inject
	protected XsampleServices services;
	
	@Inject
	protected XsampleExcerptData excerptData;
	
	protected void initQuota(ExcerptUtilityData data) {
		final long range = excerptData.getFileInfo().getSegments();
		data.setRange(range);
		data.setLimit((long) (range * services.getDoubleSetting(Key.ExcerptLimit)));
		
		Excerpt quota = excerptData.getQuota();
		if(!quota.isEmpty()) {
			data.setQuota(Fragment.encodeAll(quota.getFragments()));
		}
	}
	
	public void back() {
		if(workflow.back()) {
			updatePage();
		}
	}
	
	protected void updatePage() {
		PrimeFaces.current().ajax().update(":content");
	}
	
	protected void forward(String page) {
		// Only cause a "page change" if page actually changed
		if(workflow.forward(page)) {
			updatePage();
		}
	}
}
