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
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.LongStream;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.ManifestFile;
import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
import de.unistuttgart.xsample.qe.QueryEngine;
import de.unistuttgart.xsample.qe.QueryInfo;
import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class QueryPage extends XsamplePage {
	
	public static final String PAGE = "query";
	
	private static final Logger logger = Logger.getLogger(QueryPage.class.getCanonicalName());
	
	static final String NAV_MSG = "navMsgs";
	
	static final String QUERY_MSG = "queryMsgs";
	
	static final String NONE = "<none>";
	
	@Inject
	XsampleQueryData queryData;
	
	@Inject
	QueryView view;
	
	@Inject
	QueryEngine queryEngine;

	public void init() {
		initQuota(queryData);
		
		final XsampleManifest manifest = excerptData.getManifest();
		
		queryData.setSelectedManifestId(Optional.of(manifest.getManifests())
				.map(l -> l.isEmpty() ? null : l.get(0))
				.map(ManifestFile::getLabel)
				.orElse(NONE));
		
		//TODO further initialize query data
	}
	
	/** Callback for button to run ICARUS2 query */
	public void runQuery() {
		String rawQuery = view.getQuery();
		if(isNullOrEmpty(rawQuery)) {
			return;
		}
		
		final QueryInfo info = queryEngine.query(queryData.getSelectedManifestId(), rawQuery);
		final List<Result> results = info.getResults();
		
		if(results.isEmpty()) {
			Messages.addInfo(NAV_MSG, BundleUtil.get("query.msg.noHits"), rawQuery);
			queryData.setResultHits("");
			queryData.setResultSegments("");
			queryData.getMappedSegments().clear();
		} else {
			// Accumulate and encode hits
			long[] hits = results.stream()
					.map(Result::getHits)
					.flatMapToLong(LongStream::of)
					.toArray();
			queryData.setResultHits(XmpFragment.encodeAll(hits));
			
			// Perform mapping
			final List<Result> mappedSegments = queryEngine.mapSegments(results);
			assert mappedSegments.size()==results.size() : "corpus lost in mapping process";
			queryData.setMappedSegments(mappedSegments);

			// Accumulate and encode mapped segments
			long[] segments = mappedSegments.stream()
					.map(Result::getHits)
					.flatMapToLong(LongStream::of)
					.toArray();
			queryData.setResultSegments(XmpFragment.encodeAll(segments));
		}
		
		queryData.setResults(results);
		queryData.setResultRange(info.getSegments());
	}

	/** Callback for button to continue workflow */
	public void next() {
		final List<Result> results = queryData.getResults();
		if(results.isEmpty()) {
			logger.severe("Client side result check failed! <this error needs more log context!!!!>");
			Messages.addError(NAV_MSG, BundleUtil.get("query.msg.emptyResult"));
			return;
		}
		
		final List<Result> mappedSegments = queryData.getMappedSegments();
		if(results.isEmpty()) {
			logger.severe("Client side result check failed! <this error needs more log context!!!!>");
			Messages.addError(NAV_MSG, BundleUtil.get("query.msg.emptyMapping"));
			return;
		}
		
		boolean reset = false;
		
		final List<XmpFragment> slice = Arrays.asList(XmpFragment.of(
				queryData.getBegin(), queryData.getEnd()));
		
		for(Result mapped : mappedSegments) {
			final ExcerptEntry entry = excerptData.findEntry(mapped.getCorpusId());
			final List<XmpFragment> quota = entry.getQuota().getFragments();
			final List<XmpFragment> rawFragments = XSampleUtils.asFragments(mapped);
			final List<XmpFragment> fragments = XSampleUtils.intersect(rawFragments, slice);
			
			/* The following issue should never occur, since we do the same
			 * validation on the client side to enable/disable the button.
			 * We need this additional sanity check to defend against bugs 
			 * or tampering with the JS code on the client side!
			 */
			long usedUpSlots = XSampleUtils.combinedSize(fragments, quota);
			if(usedUpSlots>entry.getLimit()) {
				logger.severe(String.format("Sanity check on client side failed: quota of %d exceeded for %s at %s by %s", 
						_long(entry.getLimit()), entry.getResource(), excerptData.getServer(), excerptData.getDataverseUser()));
				Messages.addError(NAV_MSG, BundleUtil.get("query.msg.quotaExceeded"), 
						mapped.getCorpusId(), _long(usedUpSlots), _long(entry.getLimit()));
				reset = true;
				break;
			}
			
			entry.setFragments(fragments);
		}
		
		// If we failed at some point make sure all excerpts are cleared and we bail
		if(reset) {
			excerptData.getExcerpt().forEach(ExcerptEntry::clear);
			
			return;
		}
		
		// All fine, continue on to download
		forward(DownloadPage.PAGE);
	}
	
	@Override
	protected void rollBack() {
		excerptData.getExcerpt().forEach(ExcerptEntry::clear);
	}
	
	public boolean isShowQuota() {
		return excerptData.hasEntry(entry -> !entry.getQuota().isEmpty());
	}
}
