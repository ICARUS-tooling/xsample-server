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

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
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
	
	static final String NAV_MSG = "navMsgs";

	@Inject
	XsampleSliceData sliceData;
	
	@Inject
	SliceView view;
	
	private ExcerptEntry currentEntry() {
		String corpusId = view.getSelectedCorpus();
		return corpusId==null ? null : excerptData.findEntry(corpusId);
	}
	
	public void init() {
		initQuota(sliceData);
		sliceData.setBegin(1);
		sliceData.setEnd(1);
		assert excerptData.getSelectedCorpus()!=null : "no corpus selected";
		view.setSelectedCorpus(excerptData.getSelectedCorpus());
	}
	
	@Override
	protected void rollBack() {
		currentEntry().clear();
	}

	/** Callback for button to continue workflow */
	public void next() {
		final ExcerptEntry entry = currentEntry();
		final List<XmpFragment> fragments = Arrays.asList(XmpFragment.of(
				sliceData.getBegin(), sliceData.getEnd()));
		final List<XmpFragment> quota = entry.getQuota().getFragments();
		
		/* The following issue should never occur, since we do the same
		 * validation on the client side to enable/disable the button.
		 * We need this additional sanity check to defend against bugs 
		 * or tampering with the JS code on the client side!
		 */
		long usedUpSlots = XSampleUtils.combinedSize(fragments, quota);
		if(usedUpSlots>entry.getLimit()) {
			logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
					_long(entry.getLimit()), entry.getResource(), excerptData.getServer(), excerptData.getDataverseUser()));
			Messages.addError(NAV_MSG, BundleUtil.get("slice.msg.quotaExceeded"), 
					_long(usedUpSlots), _long(entry.getLimit()));
			return;
		}
		
		// Everything's fine, continue the workflow
		entry.setFragments(fragments);
		
		forward(DownloadPage.PAGE);
	}
	
	public boolean isShowQuota() {
		final ExcerptEntry entry = currentEntry();
		return entry!=null && !entry.getQuota().isEmpty();
	}
	
	public long getQuotaSize() {
		final ExcerptEntry entry = currentEntry();
		return entry==null ? 0L : entry.getQuota().size();
	}
	
	public double getQuotaPercent() {
		return (double)getQuotaSize() / sliceData.getRange() * 100.0;
	}
}
