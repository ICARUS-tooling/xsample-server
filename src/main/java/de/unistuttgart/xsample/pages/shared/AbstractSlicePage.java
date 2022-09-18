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
package de.unistuttgart.xsample.pages.shared;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.slice.SliceData;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSlicePage extends XsamplePage {
	
	private static final Logger logger = Logger.getLogger(AbstractSlicePage.class.getCanonicalName());

	@Inject
	protected SliceData sliceData;
	@Inject
	protected PartsData partsData;
	@Inject
	protected PartData partData;
	@Inject
	protected SelectionData selectionData;
	@Inject
	protected CorpusData corpusData;
	@Inject
	protected DownloadData downloadData;
		
	protected Stream<ExcerptEntry> allEntries() {
		return sharedData.getManifest().getAllParts()
				.stream()
				.map(downloadData::findEntry)
				.filter(Objects::nonNull);
	}
	
	protected @Nullable ExcerptEntry currentEntry() {
		Corpus part = selectionData.getSelectedCorpus();
		return part==null ? null : downloadData.findEntry(part);
	}
	
	protected void refreshSlice(ExcerptEntry entry) {
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
			assignDefaultSlice();
		}
		
	//	System.out.printf("refreshSlice: part=%s begin=%d end=%d%n", entry==null? "?" : entry.getCorpusId(),sliceData.getBegin(), sliceData.getEnd());
	}
	
	/** Set the field son {@link #sliceData} to some sensible defaults. */
	protected void assignDefaultSlice() {
		sliceData.setBegin(1);
		sliceData.setEnd(1);
	}
	
	protected void refreshPart(Corpus part, ExcerptEntry entry) {
		partData.reset();
		if(part!=null) {
			long segments = corpusData.getSegments(part);
			partData.setSegments(segments);
			partData.setLimit(corpusData.getLimit(part));
			partData.setOffset(corpusData.getOffset(part));
	
			XmpExcerpt quota = findQuota(part);
			if(!quota.isEmpty()) {
				partData.setQuota(FragmentCodec.encodeAll(quota.getFragments()));
			}
		}
		
		refreshSlice(entry);
	}
	
	protected Corpus resetSelection() {
		Corpus part = partsData.getSelectedParts().get(0);
		selectionData.setSelectedCorpus(part);
		return part;
	}
	
	/**
	 * Updates {@link SelectionData}, {@link PartData}, {@link CorpusData}.
	 * <p>
	 * Reads {@link SelectionData}, {@link DownloadData}, {@link PartsData}, {@link CorpusData}.
	 */
	public void init() {
		Corpus part = selectionData.getSelectedCorpus();
		if(part==null || !partsData.containsPart(part)) {
			part = resetSelection();
		}
		ExcerptEntry entry = downloadData.findEntry(part);
	
		refreshPart(part, entry);
	
		refreshGlobalExcerpt();
		refreshGlobalQuota();
	}
	
	public long getSegments() {
		return partsData.getSelectedParts().stream()
				.mapToLong(corpusData::getSegments)
				.sum();
	}
	
	public long getLimit() {
		return partsData.getSelectedParts().stream()
				.mapToLong(corpusData::getLimit)
				.sum();
	}
	
	/**
	 * Updates {@link DownloadData}, {@link PartData}, {@link CorpusData}.
	 * <p>
	 * Reads {@link SliceData}, {@link DownloadData}, {@link SelectionData}, {@link PartsData}.
	 * @param evt
	 */
	public void selectionChanged(ValueChangeEvent evt) {

//		System.out.printf("selectionChanged: old=%s new=%s%n",
//				Optional.ofNullable(evt.getOldValue()).map(Corpus.class::cast).map(Corpus::getId).orElse("null"), 
//				Optional.ofNullable(evt.getNewValue()).map(Corpus.class::cast).map(Corpus::getId).orElse("null"));
		
		// If we had a selected part, commit current slice as fragments
//		commitExcerpt();
		
		if(evt.getNewValue()!=null) {
			final Corpus part = (Corpus) evt.getNewValue();
			final ExcerptEntry entry = downloadData.findEntry(part);
			
			refreshPart(part, entry);
		}
		
//		System.out.printf("selectionChanged: sliceData=%s dlData=%s%n", sliceData, downloadData);
	}
	
	protected void refreshGlobalQuota() {
		System.out.println("refreshGlobalQuota: "+partsData.getSelectedParts());
		final FragmentCodec fc = new FragmentCodec();
		for(Corpus part : partsData.getSelectedParts()) {
			final XmpExcerpt quota = findQuota(part);
			final long offset = corpusData.getOffset(part);
			fc.append(quota.getFragments(), offset);
		}
		corpusData.setQuota(fc.toString());
		System.out.println("refreshGlobalQuota: "+corpusData.getQuota());
	}
	
	protected void refreshGlobalExcerpt() {
		System.out.println("refreshGlobalExcerpt: "+partsData.getSelectedParts());
		final FragmentCodec fc = new FragmentCodec();
		for(Corpus part : partsData.getSelectedParts()) {
			final ExcerptEntry entry = downloadData.findEntry(part);
			if(entry!=null) {
				final long offset = corpusData.getOffset(part);
				fc.append(entry.getFragments(), offset);
			}
		}
		corpusData.setExcerpt(fc.toString());
		System.out.println("refreshGlobalExcerpt: "+corpusData.getExcerpt());
	}
	
	protected void ensureExcerptAdded() {
		if(!sharedData.isMultiPartCorpus()) {
			addExcerpt();
		}
	}
	
	public boolean isCanReset() {
		return downloadData.getSize() > 0;
	}
	
	/** Reset all slices and refresh shared data */
	public void reset() {
		downloadData.clear();
		refreshSlice(currentEntry());
		refreshGlobalExcerpt();
	}
	
	protected List<XmpFragment> asFragments(long begin, long end) {
		return Arrays.asList(XmpFragment.of(begin, end));
	}
	
	protected boolean canApplySlice() {
		return true;
	}
	
	public boolean isCanAddExcerpt() {
		return sliceData.isValid() && canApplySlice();
	}

	public void addExcerpt() {
		final Corpus corpus = selectionData.getSelectedCorpus();
		if(corpus!=null) {
			ExcerptEntry entry = downloadData.findEntry(corpus);
			long begin = -1, end = -1;
			if(canApplySlice() && sliceData.getBegin()>0 && sliceData.getEnd()>0) {
				if(entry==null) {
					entry = downloadData.createEntry(corpus);
				}
				begin = sliceData.getBegin();
				end = sliceData.getEnd();
				List<XmpFragment> fragments = asFragments(begin, end);
				entry.setFragments(fragments);
				partData.setExcerpt(FragmentCodec.encodeAll(entry.getFragments()));
			} else if(entry!=null) {
				entry.clear();
			}
			refreshSlice(entry);
			refreshGlobalExcerpt();
//			System.out.printf("addExcerpt: part=%s begin=%d end=%d%n",corpus.getId(), begin, end);
		}
	}
	
	public boolean isCanRemoveExcerpt() {
		ExcerptEntry entry = currentEntry();
		return entry!=null && entry.size() > 0;
	}
	
	public void removeExcerpt() {
		final Corpus corpus = selectionData.getSelectedCorpus();
		if(corpus!=null) {
			final ExcerptEntry entry = downloadData.findEntry(corpus);
			if(entry!=null) {
				entry.clear();
				downloadData.removeEntry(entry);
			}
			refreshSlice(null);
			refreshGlobalExcerpt();
//			System.out.printf("removeExcerpt: part=%s%n",corpus.getId());
		}
	}
	
	public boolean isAllowEmptySlice() {
		return false;
	}

	/** 
	 * Callback for button to continue workflow.
	 * <p>
	 * Updates {@link WorkflowData}, {@link DownloadData}. 
	 */
	protected void next(String clientId, String page) {
		ensureExcerptAdded();

		long totalExcerptSize = 0;
		
		for(ExcerptEntry entry : allEntries().collect(Collectors.toList())) {
			final List<XmpFragment> fragments = entry.getFragments();
			
			if(fragments==null || fragments.isEmpty()) {
				entry.clear();
				continue;
			}

			totalExcerptSize += fragments.stream().mapToLong(XmpFragment::size).sum();
			
			XmpExcerpt excerpt = findQuota(entry.getCorpusId());
			
			/* The following issue should never occur, since we do the same
			 * validation on the client side to enable/disable the button.
			 * We need this additional sanity check to defend against bugs 
			 * or tampering with the JS code on the client side!
			 */
			long usedUpSlots = XSampleUtils.combinedSize(fragments, excerpt.getFragments());
			long limit = corpusData.getLimit(entry.getCorpusId());
			if(usedUpSlots>limit) {
				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
						_long(limit), entry.getCorpusId(), sharedData.getServer(), sharedData.getDataverseUser()));
				ui.addError(clientId, BundleUtil.get("slice.msg.quotaExceeded"), 
						_long(usedUpSlots), _long(limit));
				return;
			}
		}
		
		if(totalExcerptSize>0) {
			forward(page);
		}
	}
}
