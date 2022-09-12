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

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.shared.AbstractSlicePage;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class SlicePage extends AbstractSlicePage {
	
	public static final String PAGE = "slice";
	
	private static final Logger logger = Logger.getLogger(SlicePage.class.getCanonicalName());
	
	static final String NAV_MSG = "navMsgs";

	/** Callback for button to continue workflow */
	public void next() {
		// Commit current excerpt since we normal only do this on a selection change!
		commitExcerpt();
		
		for(ExcerptEntry entry : allEntries().collect(Collectors.toList())) {
			final List<XmpFragment> fragments = entry.getFragments();
			
			if(fragments==null || fragments.isEmpty()) {
				entry.clear();
				continue;
			}
			
			XmpExcerpt excerpt = findQuota(entry.getCorpusId());
			
			/* The following issue should never occur, since we do the same
			 * validation on the client side to enable/disable the button.
			 * We need this additional sanity check to defend against bugs 
			 * or tampering with the JS code on the client side!
			 */
			long usedUpSlots = XSampleUtils.combinedSize(fragments, excerpt.getFragments());
			if(usedUpSlots>entry.getLimit()) {
				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
						_long(entry.getLimit()), entry.getCorpusId(), sharedData.getServer(), sharedData.getDataverseUser()));
				Messages.addError(NAV_MSG, BundleUtil.get("slice.msg.quotaExceeded"), 
						_long(usedUpSlots), _long(entry.getLimit()));
				return;
			}
		}
		
		forward(DownloadPage.PAGE);
	}
}
