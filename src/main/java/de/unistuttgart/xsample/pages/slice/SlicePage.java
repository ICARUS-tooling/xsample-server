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
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
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
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
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
	SliceData sliceData;
	
	@Inject
	SliceView view;
		
	private Stream<ExcerptEntry> allEntries() {
		return sharedData.getManifest().getAllParts()
				.stream()
				.map(Corpus::getId)
				.map(sharedData::findEntry)
				.filter(Objects::nonNull);
	}
	
	private ExcerptEntry currentEntry() {
		String corpusId = view.getSelectedCorpus();
		return corpusId==null ? null : sharedData.findEntry(corpusId);
	}
	
	public void init() {
		initGlobalQuota(sliceData);
		sliceData.setBegin(1);
		sliceData.setEnd(1);
		
//		view.setSelectedParts(excerptData.getManifest().getAllParts());
//		view.setSelectedCorpus(view.getSelectedParts().get(0).getId());
		
		Corpus part = sharedData.getManifest().getAllParts().get(0);
		
		view.setSelectedCorpus(part.getId());
		
		sliceData.setGlobalLimit(corpusData.getSegments());
		sliceData.setGlobalLimit(getQuotaSize());
		
		refreshLocalQuota(part);
	}

	public void refreshLocalQuota(Corpus corpus) {
		final double limit = services.getDoubleSetting(Key.ExcerptLimit);
		final long segments = sharedData.getSegments(corpus);
		sliceData.setSegments(segments);
		sliceData.setLimit((long) Math.floor(segments * limit));
		
		final List<XmpFragment> quota = new ArrayList<>();
		ExcerptEntry entry = sharedData.findEntry(corpus.getId());
		if(!entry.getQuota().isEmpty()) {
			quota.addAll(entry.getQuota().getFragments());
		}
		
		if(!quota.isEmpty()) {
			sliceData.setQuota(XmpFragment.encodeAll(quota));
		}
	}
	
	@Override
	protected void rollBack() {
		allEntries().forEach(ExcerptEntry::clear);
	}

	/** Callback for button to continue workflow */
	public void next() {
		//TODO commit currently selected part as well
		
		for(ExcerptEntry entry : allEntries().collect(Collectors.toList())) {
			final List<XmpFragment> fragments = entry.getFragments();
			final List<XmpFragment> quota = entry.getQuota().getFragments();
			
			if(fragments==null || fragments.isEmpty()) {
				entry.clear();
				continue;
			}
			
			/* The following issue should never occur, since we do the same
			 * validation on the client side to enable/disable the button.
			 * We need this additional sanity check to defend against bugs 
			 * or tampering with the JS code on the client side!
			 */
			long usedUpSlots = XSampleUtils.combinedSize(fragments, quota);
			if(usedUpSlots>entry.getLimit()) {
				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
						_long(entry.getLimit()), entry.getResource(), sharedData.getServer(), sharedData.getDataverseUser()));
				Messages.addError(NAV_MSG, BundleUtil.get("slice.msg.quotaExceeded"), 
						_long(usedUpSlots), _long(entry.getLimit()));
				return;
			}
			
			// Everything's fine, continue the workflow
			entry.setFragments(fragments);
		}
		
		forward(DownloadPage.PAGE);
	}
	
//	public void corpusCompositionChanged(ValueChangeEvent vce) {
//		System.out.println(vce);
//	}
	
//	public void corpusCompositionChanged(AjaxBehaviorEvent vce) {
//		System.out.println(vce);
//	}
	
//	public void corpusCompositionChanged() {
//		System.out.println("generic composition change");
//	}
	
	public void corpusInclusionChanged() {
		System.out.println("Corpus included: "+view.isIncludeCorpus());
	}
	
	//FIXME the entire "include corpus" mechanic needs a rework
	
	private void commitExcerpt() {
		final String corpusId = view.getSelectedCorpus();
		if(corpusId!=null) {
			final ExcerptEntry entry = sharedData.findEntry(corpusId);
			if(view.isIncludeCorpus()) {
				view.getSelectedParts().add(corpusId);
			} else {
				view.getSelectedParts().remove(corpusId);
			}
			if(sliceData.getBegin()>0 && sliceData.getEnd()>0) {
				entry.setFragments(Arrays.asList(XmpFragment.of(sliceData.getBegin(), sliceData.getEnd())));
			} else {
				entry.clear();
			}
		}
	}
	
	public void corpusSelectionChanged(ValueChangeEvent vce) {
		// If we had a selected part, commit current slice as fragments
		commitExcerpt();
		
		if(vce.getNewValue()!=null) {
			final String corpusId = (String)vce.getNewValue();
			final ExcerptEntry entry = sharedData.findEntry(corpusId);
			final List<XmpFragment> fragments = entry.getFragments();
			final XmpFragment slice = fragments==null || fragments.isEmpty() ? null : fragments.get(0);
			sliceData.setBegin(slice==null ? 0 : slice.getBeginIndex());
			sliceData.setEnd(slice==null ? 0 : slice.getEndIndex());
			view.setIncludeCorpus(view.getSelectedParts().contains(corpusId));
			refreshLocalQuota(sharedData.findCorpus(corpusId));

			System.out.printf("old=%s new=%s included_old=%b included_new=%b%n",vce.getOldValue(), vce.getNewValue(), 
					view.isIncludeCorpus(),
					view.getSelectedParts().contains(corpusId));
		}
	}
	
//	public void corpusSelectionChanged(AjaxBehaviorEvent vce) {
//		System.out.println(vce);
//	}
	
	public boolean isShowQuota() {
		ExcerptEntry entry = currentEntry();
		return entry!=null && entry.getQuota()!=null && !entry.getQuota().isEmpty();
	}
	
	public boolean isShowGlobalQuota() {
		return !allEntries()
				.map(ExcerptEntry::getQuota)
				.filter(Objects::nonNull)
				.allMatch(XmpExcerpt::isEmpty);
	}
	
	public long getQuotaSize() {
		return allEntries()
				.map(ExcerptEntry::getQuota)
				.mapToLong(XmpExcerpt::size)
				.sum();
	}
}
