/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.slice.SliceData;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSlicePage extends XsamplePage {

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
				.map(Corpus::getId)
				.map(downloadData::findEntry)
				.filter(Objects::nonNull);
	}
	
	protected ExcerptEntry currentEntry() {
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
	
	/** Reset all slices and refresh shared data */
	public void reset() {
		allEntries().forEach(ExcerptEntry::clear);
		refreshSlice(currentEntry());
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
	
	protected void commitExcerpt() {
		final Corpus corpus = selectionData.getSelectedCorpus();
		if(corpus!=null) {
			final ExcerptEntry entry = downloadData.findEntry(corpus);
			long begin = -1, end = -1;
			if(sliceData.getBegin()>0 && sliceData.getEnd()>0) {
				begin = sliceData.getBegin();
				end = sliceData.getEnd();
				entry.setFragments(Arrays.asList(XmpFragment.of(begin, end)));
				partData.setExcerpt(FragmentCodec.encodeAll(entry.getFragments()));
			} else {
				entry.clear();
			}
			refreshGlobalExcerpt();
//			System.out.printf("commitExcerpt: part=%s begin=%d end=%d%n",corpus.getId(), begin, end);
		}
	}
	
	public boolean isAllowEmptySlice() {
		return false;
	}
}
