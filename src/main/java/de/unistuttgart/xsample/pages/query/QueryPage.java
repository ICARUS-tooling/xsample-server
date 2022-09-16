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
package de.unistuttgart.xsample.pages.query;

import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static de.unistuttgart.xsample.util.XSampleUtils.isNullOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.commons.text.StringEscapeUtils;

import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.shared.AbstractSlicePage;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.pages.shared.FragmentCodec;
import de.unistuttgart.xsample.qe.MappingException;
import de.unistuttgart.xsample.qe.QueryEngine;
import de.unistuttgart.xsample.qe.QueryException;
import de.unistuttgart.xsample.qe.QueryResult;
import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class QueryPage extends AbstractSlicePage {
	
	public static final String PAGE = "query";
	
	private static final Logger logger = Logger.getLogger(QueryPage.class.getCanonicalName());
	
	static final String NAV_MSG = "navMsgs";
	
	static final String QUERY_MSG = "queryMsgs";
	
	static final String NONE = "<none>";
	
	@Inject
	QueryData queryData;
	@Inject
	QueryEngine queryEngine;
	@Inject
	ResultData resultData;
	@Inject
	ResultsData resultsData;
	
	@Override
	public boolean isAllowEmptySlice() { return true; }

	private String cleanQuery(String query) {
		return StringEscapeUtils.unescapeHtml4(query);
	}
	
	/** Callback for button to run ICARUS query */
	@Transactional
	public void runQuery() {
		String rawQuery = queryData.getQuery();
		if(isNullOrEmpty(rawQuery)) {
			return;
		}
		
		String query = cleanQuery(rawQuery);
		
		resultData.reset();
		resultsData.reset();
		
		// Execute actual search
		List<QueryResult> results;
		try {
			results = queryEngine.query(query);
		} catch(QueryException e) {
			logger.log(Level.SEVERE, "Query evaluation failed: "+e.getMessage(), e);
			String resourceId = e.getResourceId().orElse(sharedData.getManifest().getCorpus().getId());
			switch (e.getCode()) {
			case IO_ERROR: ui.addError(QUERY_MSG, "query.msg.loadingError", resourceId); break;
			case INTERNAL_ERROR: ui.addFatal(QUERY_MSG, "query.msg.internalError", resourceId); break;
			case SYNTAX_ERROR: ui.addError(QUERY_MSG, "query.msg.invalidQuerySyntax"); break;
			case UNSUPPORTED_FORMAT: ui.addError(QUERY_MSG, "query.msg.unsupportedFormat", resourceId); break;
			case SECURITY_ERROR: ui.addError(QUERY_MSG, "query.msg.decryptionFailed", resourceId); break;
			case RESOURCE_LOCKED: ui.addError(QUERY_MSG, "query.msg.cacheBusy", resourceId); break;
			default:
				break;
			}
			return;
		}
		
		// DOubles as offset for the new result
		long rawSegments = 0;
		FragmentCodec rawHits = new FragmentCodec();
		boolean hasHits = false;

		for (int i = 0; i < results.size(); i++) {
			QueryResult qr = results.get(i);
			resultsData.registerRawResult(qr.getResult());
			resultsData.registerRawSegments(qr.getCorpusId(), qr.getSegments());
			
			if(!qr.isEmpty()) {
				rawHits.append(qr.getResult().getHits(), rawSegments);
				hasHits = true;
			}
			rawSegments += qr.getSegments();
		}
		
		resultsData.setRawSegments(rawSegments);
		resultsData.setRawHits(rawHits.toString());
		
		if(!hasHits) {
			ui.addInfo(NAV_MSG, BundleUtil.get("query.msg.noHits"), rawQuery);
		} else {			
			// Perform mapping
			List<Result> mappedSegments;
			try {
				mappedSegments = queryEngine.mapSegments(results);
			} catch (MappingException e) {
				logger.log(Level.SEVERE, "Mapping query results failed: "+e.getMessage(), e);
				String resourceId = e.getResourceId().orElse(sharedData.getManifest().getCorpus().getId());
				switch (e.getCode()) {
				case IO_ERROR: ui.addError(QUERY_MSG, "query.msg.loadingError", resourceId); break;
				case INTERNAL_ERROR: ui.addFatal(QUERY_MSG, "query.msg.internalError", resourceId); break;
				case UNSUPPORTED_FORMAT: ui.addError(QUERY_MSG, "query.msg.unsupportedFormat", resourceId); break;
				case SECURITY_ERROR: ui.addError(QUERY_MSG, "query.msg.decryptionFailed", resourceId); break;
				case RESOURCE_LOCKED: ui.addError(QUERY_MSG, "query.msg.cacheBusy", resourceId); break;
				case MISSING_MANIFEST: ui.addError(QUERY_MSG, "query.msg.missingManifest", resourceId); break;
				case MISSING_MAPPING: ui.addError(QUERY_MSG, "query.msg.missingMapping", resourceId); break;
				default:
					break;
				}
				return;
			}
			assert mappedSegments.size()==results.size() : "corpus lost in mapping process";
			
			FragmentCodec mappedHits = new FragmentCodec();
			
			for (int i = 0; i < results.size() && i < mappedSegments.size(); i++) {
				final QueryResult result = results.get(i);
				final Result raw = result.getResult();
				final Result mapped = mappedSegments.get(i);
				final long offset = corpusData.getOffset(raw.getCorpusId());
				
//				System.out.printf("part=%s, offset=%d, raw=%s, mapped=%s%n", raw.getCorpusId(), _long(offset), raw, mapped);
				
				mappedHits.append(mapped.getHits(), offset);
				
				// Update individual parts data
				resultsData.registerMappedResult(mapped);
			}
			
			resultsData.setMappedHits(mappedHits.toString());
			
			Corpus part = resetSelection();
			ExcerptEntry entry = downloadData.findEntry(part);
			refreshPart(part, entry);
			
//			System.out.printf("runQuery: query=%s results=%s result=%s%n", queryData, resultsData, resultData);
		}		
	}
	
	@Override
	protected Corpus resetSelection() {
		Corpus part = null;
		if(!resultsData.isEmpty()) {
			part = partsData.getSelectedParts().stream()
					.filter(resultsData::hasResults)
					.findFirst()
					.orElse(null);
		}
		if(part==null) {
			return super.resetSelection();
		}
		return part;
	}
	
	@Override
	protected void refreshPart(Corpus part, ExcerptEntry entry) {
		resultData.reset();
		
		super.refreshPart(part, entry);
		
		if(part!=null && resultsData.hasResults(part)) {
			resultData.setRawSegments(resultsData.getRawSegments(part));
			Result raw = resultsData.getRawResult(part);
			resultData.setRawResult(raw);
			resultData.setRawHits(raw==null ? "" : FragmentCodec.encodeAll(raw.getHits()));
			Result mapped = resultsData.getMappedResult(part);
			resultData.setMappedResult(mapped);
			resultData.setMappedHits(mapped==null ? "" : FragmentCodec.encodeAll(mapped.getHits()));
		}
	}
	
	@Override
	protected void assignDefaultSlice() {
		if(!resultData.isEmpty()) {
			Result candidates = resultData.getMappedResult();
			if(!candidates.isEmpty()) {
				long firstUsableSlot = candidates.getHits()[0];
				sliceData.setBegin(firstUsableSlot);
				sliceData.setEnd(firstUsableSlot);
			}
		}
		
		if(!sliceData.isValid()) {
			super.assignDefaultSlice();
		}
	}
	
	private static final long[] EMPTY = {};
	
	private static long[] filter(long[] hits, long min, long max) {
		int first = Arrays.binarySearch(hits, min);
		if(first < 0) first = -first - 1;
		
		if(first==hits.length) {
			return EMPTY;
		}
		
		int last = Arrays.binarySearch(hits, first, hits.length, max);
		if(last < 0) {
			last = -last - 1;
		} else {
			// If we actually found the value, we need to ensure it is contained in the slice
			last = Math.min(last+1, hits.length);
		}
		
		if(first==last) {
			return EMPTY;
		}
		
		return Arrays.copyOfRange(hits, first, last);
	}
	
	@Override
	public void addExcerpt() {
		final Corpus corpus = selectionData.getSelectedCorpus();
		if(corpus!=null) {
			final ExcerptEntry entry = downloadData.findEntry(corpus);
			long begin = -1, end = -1;
			if(!resultData.isEmpty() && sliceData.getBegin()>0 && sliceData.getEnd()>0) {
				begin = sliceData.getBegin();
				end = sliceData.getEnd();
				
				final long[] values = filter(resultData.getMappedResult().getHits(), begin, end);
				final List<XmpFragment> fragments = XmpFragment.from(values);
				entry.setFragments(fragments);
				partData.setExcerpt(FragmentCodec.encodeAll(fragments));
			} else {
				entry.clear();
			}
			refreshGlobalExcerpt();
//			System.out.printf("commitExcerpt: part=%s begin=%d end=%d%n",corpus.getId(), begin, end);
		}
	}

	/** Callback for button to continue workflow */
	public void next() {
		// Commit current excerpt since we normal only do this on a selection change!
//		commitExcerpt();
		
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
			if(usedUpSlots>entry.getLimit()) {
				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
						_long(entry.getLimit()), entry.getCorpusId(), sharedData.getServer(), sharedData.getDataverseUser()));
				ui.addError(NAV_MSG, BundleUtil.get("slice.msg.quotaExceeded"), 
						_long(usedUpSlots), _long(entry.getLimit()));
				return;
			}
		}
		
		if(totalExcerptSize>0) {
			forward(DownloadPage.PAGE);
		}
	}

//	/** Callback for button to continue workflow */
//	public void next() {
//		
//		final List<Result> results = queryData.getResult();
//		if(results.isEmpty()) {
//			logger.severe("Client side result check failed! <this error needs more log context!!!!>");
//			Messages.addError(NAV_MSG, BundleUtil.get("query.msg.emptyResult"));
//			return;
//		}
//		
//		final List<Result> mappedSegments = queryData.getMappedSegments();
//		if(results.isEmpty()) {
//			logger.severe("Client side result check failed! <this error needs more log context!!!!>");
//			Messages.addError(NAV_MSG, BundleUtil.get("query.msg.emptyMapping"));
//			return;
//		}
//		
//		boolean reset = false;
//		
//		final List<XmpFragment> slice = Arrays.asList(XmpFragment.of(
//				queryData.getBegin(), queryData.getEnd()));
//		
//		for(Result mapped : mappedSegments) {
//			final ExcerptEntry entry = sharedData.findEntry(mapped.getCorpusId());
//			final List<XmpFragment> quota = entry.getQuota().getFragments();
//			final List<XmpFragment> rawFragments = XSampleUtils.asFragments(mapped);
//			final List<XmpFragment> fragments = XSampleUtils.intersect(rawFragments, slice);
//			
//			/* The following issue should never occur, since we do the same
//			 * validation on the client side to enable/disable the button.
//			 * We need this additional sanity check to defend against bugs 
//			 * or tampering with the JS code on the client side!
//			 */
//			long usedUpSlots = XSampleUtils.combinedSize(fragments, quota);
//			if(usedUpSlots>entry.getLimit()) {
//				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
//						_long(entry.getLimit()), entry.getResource(), sharedData.getServer(), sharedData.getDataverseUser()));
//				Messages.addError(NAV_MSG, BundleUtil.get("query.msg.quotaExceeded"), 
//						mapped.getCorpusId(), _long(usedUpSlots), _long(entry.getLimit()));
//				reset = true;
//				break;
//			}
//			
//			entry.setFragments(fragments);
//		}
//		
//		// If we failed at some point make sure all excerpts are cleared and we bail
//		if(reset) {
//			sharedData.getEntries().forEach(ExcerptEntry::clear);
//			
//			return;
//		}
//		
//		// All fine, continue on to download
//		forward(DownloadPage.PAGE);
//	}
	
	@Override
	protected void rollBack() {
		resultData.reset();
		resultsData.reset();
	}
}
