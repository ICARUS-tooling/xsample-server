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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
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
		
	private Stream<ExcerptEntry> currentEntries() {
		return view.getSelectedParts()
				.stream()
				.map(Corpus::getId)
				.map(excerptData::findEntry);
	}
	
	public void init() {
		initGlobalQuota(sliceData);
		sliceData.setBegin(1);
		sliceData.setEnd(1);
		
		sliceData.setGlobalLimit(excerptData.getSegments());
		sliceData.setGlobalLimit(getQuotaSize());
		
		view.setSelectedParts(excerptData.getManifest().getAllParts());
		view.setSelectedCorpus(view.getSelectedParts().get(0).getId());
		
		refreshLocalQuota();
	}

	public void refreshLocalQuota() {
		final String corpusId = view.getSelectedCorpus();
		final Corpus corpus = excerptData.findCorpus(corpusId);
		final double limit = services.getDoubleSetting(Key.ExcerptLimit);
		final long segments = excerptData.getSegments(corpus);
		sliceData.setSegments(segments);
		sliceData.setLimit((long) Math.floor(segments * limit));
		
		final List<XmpFragment> quota = new ArrayList<>();
		ExcerptEntry entry = excerptData.findEntry(corpusId);
		if(!entry.getQuota().isEmpty()) {
			quota.addAll(entry.getQuota().getFragments());
		}
		
		if(!quota.isEmpty()) {
			sliceData.setQuota(XmpFragment.encodeAll(quota));
		}
	}
	
	@Override
	protected void rollBack() {
		currentEntries().forEach(ExcerptEntry::clear);
	}

	/** Callback for button to continue workflow */
	public void next() {
		for(ExcerptEntry entry : currentEntries().collect(Collectors.toList())) {
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
		}
		
		forward(DownloadPage.PAGE);
	}
	
	public void corpusCompositionChanged(ValueChangeEvent vce) {
		System.out.println(vce);
	}
	
	public void corpusCompositionChanged(AjaxBehaviorEvent vce) {
		System.out.println(vce);
	}
	
	public void corpusSelectionChanged(ValueChangeEvent vce) {
		System.out.println(vce);
	}
	
	public void corpusSelectionChanged(AjaxBehaviorEvent vce) {
		System.out.println(vce);
	}
	
	public boolean isShowQuota() {
		return !currentEntries()
				.map(ExcerptEntry::getQuota)
				.allMatch(XmpExcerpt::isEmpty);
	}
	
	public long getQuotaSize() {
		return currentEntries()
				.map(ExcerptEntry::getQuota)
				.mapToLong(XmpExcerpt::size)
				.sum();
	}
	
	public double getQuotaPercent() {
		return (double)getQuotaSize() / sliceData.getSize() * 100.0;
	}
}
