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
package de.unistuttgart.xsample.pages.slice;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class SlicePage extends XsamplePage {
	
	public static final String PAGE = "slice";
	
	private static final Logger logger = Logger.getLogger(SlicePage.class.getCanonicalName());

	@Inject
	XsampleSliceData sliceData;
	
	public void init() {
		initQuota(sliceData);
		sliceData.setBegin(1);
		sliceData.setEnd(1);
	}

	/** Callback for button to continue workflow */
	public void next() {
		List<Fragment> excerpt = Arrays.asList(Fragment.of(
				sliceData.getBegin(), sliceData.getEnd()));
		List<Fragment> quota = excerptData.getQuota().getFragments();
		
		/* The following issue should never occur, since we do the same
		 * validation on the client side to enable/disable the button.
		 * We need this additional sanity check to defend against bugs 
		 * or tampering with the JS code on the client side!
		 */
		long usedUpSlots = XSampleUtils.combinedSize(excerpt, quota);
		if(usedUpSlots>sliceData.getLimit()) {
			logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
					_long(sliceData.getLimit()), excerptData.getResource(), excerptData.getServer(), excerptData.getDataverseUser()));
			Messages.addError("navMsg", BundleUtil.get("slice.msg.quotaExceeded"), 
					_long(usedUpSlots), _long(sliceData.getLimit()));
			return;
		}
		
		// Everything's fine, continue the workflow
		excerptData.setExcerpt(excerpt);
		
		forward(DownloadPage.PAGE);
	}
	
	public boolean isShowQuota() {
		return !excerptData.getQuota().isEmpty();
	}
	
	public long getQuotaSize() {
		return excerptData.getQuota().size();
	}
	
	public double getQuotaPercent() {
		return (double)getQuotaSize() / sliceData.getRange() * 100.0;
	}
}
