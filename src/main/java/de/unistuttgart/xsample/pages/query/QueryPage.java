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

import static de.unistuttgart.xsample.util.XSampleUtils.isNullOrEmpty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.commons.text.StringEscapeUtils;

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

	private void queryMessage(Severity severity, String key, Object...args) {
		ui.addMessage(QUERY_MSG, severity, BundleUtil.get(key), args);
	}
	
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
			case IO_ERROR: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.loadingError", resourceId); break;
			case INTERNAL_ERROR: queryMessage(FacesMessage.SEVERITY_FATAL, "query.msg.internalError", resourceId); break;
			case SYNTAX_ERROR: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.invalidQuerySyntax"); break;
			case UNSUPPORTED_FORMAT: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.unsupportedFormat", resourceId); break;
			case SECURITY_ERROR: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.decryptionFailed", resourceId); break;
			case RESOURCE_LOCKED: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.cacheBusy", resourceId); break;
			default:
				break;
			}
			return;
		}
		
		// Doubles as offset for the new result
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
			Map<String, Result> mappedResults;
			try {
				mappedResults = queryEngine.mapSegments(results);
			} catch (MappingException e) {
				logger.log(Level.SEVERE, "Mapping query results failed: "+e.getMessage(), e);
				String resourceId = e.getResourceId().orElse(sharedData.getManifest().getCorpus().getId());
				switch (e.getCode()) {
				case IO_ERROR: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.loadingError", resourceId); break;
				case INTERNAL_ERROR: queryMessage(FacesMessage.SEVERITY_FATAL, "query.msg.internalError", resourceId); break;
				case UNSUPPORTED_FORMAT: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.unsupportedFormat", resourceId); break;
				case SECURITY_ERROR: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.decryptionFailed", resourceId); break;
				case RESOURCE_LOCKED: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.cacheBusy", resourceId); break;
				case MISSING_MANIFEST: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.missingManifest", resourceId); break;
				case MISSING_MAPPING: queryMessage(FacesMessage.SEVERITY_ERROR, "query.msg.missingMapping", resourceId); break;
				default:
					break;
				}
				return;
			}
			assert mappedResults.size()==results.size() : "corpus lost in mapping process";
			
			FragmentCodec mappedHits = new FragmentCodec();
			
			for (int i = 0; i < results.size(); i++) {
				final QueryResult result = results.get(i);
				final Result raw = result.getResult();
				final Result mapped = mappedResults.get(raw.getCorpusId());
				if(mapped!=null) {
					final long offset = corpusData.getOffset(raw.getCorpusId());
					
	//				System.out.printf("part=%s, offset=%d, raw=%s, mapped=%s%n", raw.getCorpusId(), _long(offset), raw, mapped);
					
					mappedHits.append(mapped.getHits(), offset);
					
					// Update individual parts data
					resultsData.registerMappedResult(mapped);
				}
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
		if(part!=null) {
			selectionData.setSelectedCorpus(part);
			return part;
		}
		return super.resetSelection();
	}
	
	@Override
	protected void refreshPart(Corpus part, ExcerptEntry entry) {
		resultData.reset();
		
		super.refreshPart(part, entry);
		
		refreshResult(part);
	}
	
	protected void refreshResult(Corpus part) {
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
	
	/**
	 * @see de.unistuttgart.xsample.pages.shared.AbstractSlicePage#asFragments(long, long)
	 */
	@Override
	protected List<XmpFragment> asFragments(long begin, long end) {
		if(resultData.isEmpty()) {
			return Collections.emptyList();
		}
		final long[] values = filter(resultData.getMappedResult().getHits(), begin, end);
		return XmpFragment.from(values);
	}
	
	protected boolean canApplySLice() {
		return !resultData.isEmpty();
	}

	/** Callback for button to continue workflow */
	public void next() {
		next(NAV_MSG, DownloadPage.PAGE);
	}

	@Override
	protected void rollBack() {
		resultData.reset();
		resultsData.reset();
	}
}
