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

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.util.ExcerptUtilityData;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleQueryData extends ExcerptUtilityData {

	private static final long serialVersionUID = -3741300128814073907L;
	
	/** Raw result data */
	private final List<Result> results = new ArrayList<>();
	
	/** Result data mapped into native segments of the primary data */
	private final List<Result> mappedSegments = new ArrayList<>();
	
	/** Encoded search result mapped to proper segments */
	private String resultSegments = "";
	/** Encoded search result as native (sub)segments */
	private String resultHits = "";
	
	/** Id of the currently selected manifest that defines what kind of search we support */
	private String selectedManifestId;
	
	/** Number of possible result segments */
	private long resultRange = -1;

	/** Begin of slice. 1-based. */
	private long begin = 1;
	/** End of slice. 1-based. */
	private long end = 1;
	
	public List<Result> getResults() { return Collections.unmodifiableList(results); }
	
	public void setResults(List<Result> results) {
		requireNonNull(results);
		this.results.clear();
		results.forEach(this::addResult);
	}
	public void addResult(Result result) { 
		requireNonNull(result);
		checkArgument("Result is empty", !result.isEmpty());
		results.add(result); 
	}
	
	public List<Result> getMappedSegments() { return Collections.unmodifiableList(mappedSegments); }
	
	public void setMappedSegments(List<Result> mappedSegments) {
		requireNonNull(mappedSegments);
		this.mappedSegments.clear();
		mappedSegments.forEach(this.mappedSegments::add);
	}
	
	public long getBegin() { return begin; }
	public void setBegin(long begin) { this.begin = begin; }
	
	public long getEnd() { return end; }
	public void setEnd(long end) { this.end = end; }

	public String getSelectedManifestId() { return selectedManifestId; }
	public void setSelectedManifestId(String selectedManifestId) { this.selectedManifestId = requireNonNull(selectedManifestId); }

	public String getResultSegments() { return resultSegments; }
	public void setResultSegments(String encodedResults) { this.resultSegments = encodedResults; }
	
	public long getResultRange() { return resultRange; }
	public void setResultRange(long resultRange) { this.resultRange = resultRange; }
	
	public String getResultHits() { return resultHits; }
	public void setResultHits(String resultHits) { this.resultHits = resultHits; }
	
	
	public boolean isHasResults() { return results!=null && !results.isEmpty(); }
}
