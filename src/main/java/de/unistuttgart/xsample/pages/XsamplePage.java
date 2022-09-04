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
package de.unistuttgart.xsample.pages;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.primefaces.PrimeFaces;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow;
import de.unistuttgart.xsample.util.ExcerptUtilityData;

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
	
	protected void initGlobalQuota(ExcerptUtilityData data) {
		final double limit = services.getDoubleSetting(Key.ExcerptLimit);
		final long segments = excerptData.getSegments();
		data.setGlobalSegments(segments);
		data.setGlobalLimit((long) Math.floor(segments * limit));
		
		final List<XmpFragment> totalQuota = new ArrayList<>();
		for(ExcerptEntry entry : excerptData.getExcerpt()) {
			XmpExcerpt quota = entry.getQuota();
			if(!quota.isEmpty()) {
				totalQuota.addAll(quota.getFragments());
			}
		}
		
		if(!totalQuota.isEmpty()) {
			data.setGlobalQuota(XmpFragment.encodeAll(totalQuota));
		}
	}
	
	protected boolean isSmallFile(long size) {
		return size<=services.getLongSetting(Key.SmallFileLimit);
	}
	
	public final void back() {
		rollBack();
		if(workflow.back()) {
			updatePage();
		}
	}
	
	/** Callback to be invoked before {@link #back()} is fully executed. */
	protected void rollBack() { /* no-op */ }
	
	protected final void updatePage() {
		PrimeFaces.current().ajax().update(":content");
	}
	
	protected final void forward(String page) {
		// Only cause a "page change" if page actually changed
		if(workflow.forward(page)) {
			updatePage();
		}
	}
}
