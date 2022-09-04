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
package de.unistuttgart.xsample.qe;

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class QueryEngine implements Serializable {

	private static final long serialVersionUID = 4603513765690341840L;

	@Inject
	LocalCache cache;
	
	@Inject
	XsampleExcerptData excerptData;
	
	@Inject
	XsampleServices services;
	
	public QueryInfo query(String manifestId, String query) {
		//TODO implement actual forwarding to ICARUS2 engine
		
		return createDummyResults(manifestId);
	}
	
	private QueryInfo createDummyResults(String manifestId) {
		final List<Result> list = new ArrayList<>();
		long totalQuerySegments = 0;
		
		for(Corpus corpus : excerptData.getManifest().getAllParts()) {
			final Result result = new Result();
			result.setCorpusId(corpus.getId());
			
			final XmpDataverse dataverse = excerptData.getServer();
			final Long fileId = corpus.getPrimaryData().getId();
			final XmpResource resource = services.findResource(dataverse, fileId);
			final XmpFileInfo fileInfo = services.findFileInfo(resource);
			
			checkState("File info not populated", fileInfo.isSet());
			
			int segments = strictToInt(fileInfo.getSegments());
			int querySegments = segments * 10;
			Random r = new Random();
			int count = Math.min(40, querySegments/2);
			
			LongSet hits = new LongOpenHashSet();
			while(hits.size()<count) {
				hits.add(r.nextInt(querySegments)+1);
			}
			
			result.setHits(hits.longStream().sorted().toArray());
			
			list.add(result);
			totalQuerySegments += querySegments;
		}
		
		return new QueryInfo(list, totalQuerySegments);
	}
	
	/** Maps 1-based hits in the annotation space into 1-based segments of the primary data */
	public List<Result> mapSegments(List<Result> results) {
		requireNonNull(results);
		checkArgument("Only supports a single result set currently", results.size()==1);
		
		final long max = excerptData.getSegments(); 
		
		List<Result> result = new ArrayList<>();
		
		for(Result original : results) {
			Result mapped = new Result();
			mapped.setCorpusId(original.getCorpusId());
			mapped.setHits(LongStream.of(original.getHits())
				.map(i -> {
					long seg = i/10;
					if(i%10!=0) {
						seg++;
					}
					return Math.min(max, seg);
				})
				.distinct()
				.toArray());
			
			result.add(mapped);
		}
		
		return result;
	}
}
