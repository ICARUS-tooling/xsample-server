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
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.shared.CorpusData;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.pages.shared.PartData;
import de.unistuttgart.xsample.pages.shared.SelectionData;
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
	PartsData partsData;
	@Inject
	PartData partData;
	@Inject
	SelectionData selectionData;
	@Inject
	CorpusData corpusData;
	@Inject
	DownloadData downloadData;
		
	private Stream<ExcerptEntry> allEntries() {
		return sharedData.getManifest().getAllParts()
				.stream()
				.map(Corpus::getId)
				.map(downloadData::findEntry)
				.filter(Objects::nonNull);
	}
	
	private ExcerptEntry currentEntry() {
		Corpus part = selectionData.getSelectedCorpus();
		return part==null ? null : downloadData.findEntry(part);
	}
	
	private void refreshSlice(ExcerptEntry entry) {
		sliceData.reset();
		if(entry!=null) {
			List<XmpFragment> fragments = entry.getFragments();
			if(fragments!=null && fragments.size()==1) {
				XmpFragment fragment = fragments.get(0);
				sliceData.setBegin(fragment.getBeginIndex());
				sliceData.setEnd(fragment.getEndIndex());
			}
		}
		
		if(!sliceData.isValid()) {
			sliceData.setBegin(1);
			sliceData.setEnd(1);
		}
		
//		System.out.printf("refreshSlice: part=%s begin=%d end=%d%n", entry==null? "?" : entry.getCorpusId(),sliceData.getBegin(), sliceData.getEnd());
	}

	private void refreshPart(Corpus part, ExcerptEntry entry) {
		partData.reset();
		if(part!=null) {
			long segments = corpusData.getSegments(part);
			partData.setSegments(segments);
			partData.setExcerptLimit((long) Math.floor(segments * services.getDoubleSetting(Key.ExcerptLimit)));
			
			if(entry!=null) {
				XmpExcerpt quota = entry.getQuota();
				if(!quota.isEmpty()) {
					partData.setQuota(XmpFragment.encodeAll(quota.getFragments()));
				}
			}
		}
	}
	
	public void init() {
		Corpus part = selectionData.getSelectedCorpus();
		if(part==null || !partsData.containsPart(part)) {
			part = partsData.getSelectedParts().get(0);
			selectionData.setSelectedCorpus(part);
		}
		ExcerptEntry entry = downloadData.findEntry(part);

		refreshPart(part, entry);
		refreshSlice(entry);
	}
	
	/** Reset all slices and refresh shared data */
	public void reset() {
		allEntries().forEach(ExcerptEntry::clear);
		refreshSlice(currentEntry());
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
	
	public void selectionChanged(ValueChangeEvent evt) {

//		System.out.printf("selectionChanged: old=%s new=%s%n",
//				Optional.ofNullable(evt.getOldValue()).map(Corpus.class::cast).map(Corpus::getId).orElse("null"), 
//				Optional.ofNullable(evt.getNewValue()).map(Corpus.class::cast).map(Corpus::getId).orElse("null"));
		
		// If we had a selected part, commit current slice as fragments
		commitExcerpt();
		
		if(evt.getNewValue()!=null) {
			final Corpus part = (Corpus) evt.getNewValue();
			final ExcerptEntry entry = downloadData.findEntry(part);
			
			refreshPart(part, entry);
			refreshSlice(entry);
		}
		
//		System.out.printf("selectionChanged: sliceData=%s dlData=%s%n", sliceData, downloadData);
	}
	
	private void commitExcerpt() {
		final Corpus corpus = selectionData.getSelectedCorpus();
		if(corpus!=null) {
			final ExcerptEntry entry = downloadData.findEntry(corpus);
			long begin = -1, end = -1;
			if(sliceData.getBegin()>0 && sliceData.getEnd()>0) {
				begin = sliceData.getBegin();
				end = sliceData.getEnd();
				entry.setFragments(Arrays.asList(XmpFragment.of(begin, end)));
			} else {
				entry.clear();
			}
//			System.out.printf("commitExcerpt: part=%s begin=%d end=%d%n",corpus.getId(), begin, end);
		}
	}
	
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
