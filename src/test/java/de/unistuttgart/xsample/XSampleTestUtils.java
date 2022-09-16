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
package de.unistuttgart.xsample;

import java.util.List;
import java.util.stream.Stream;

import de.unistuttgart.xsample.Assertions.CorpusDataAssert;
import de.unistuttgart.xsample.Assertions.DownloadDataAssert;
import de.unistuttgart.xsample.Assertions.ExcerptEntryAssert;
import de.unistuttgart.xsample.Assertions.InputDataAssert;
import de.unistuttgart.xsample.Assertions.LabelDataAssert;
import de.unistuttgart.xsample.Assertions.PartDataAssert;
import de.unistuttgart.xsample.Assertions.PartsDataAssert;
import de.unistuttgart.xsample.Assertions.QueryDataAssert;
import de.unistuttgart.xsample.Assertions.ResultDataAssert;
import de.unistuttgart.xsample.Assertions.ResultsDataAssert;
import de.unistuttgart.xsample.Assertions.SelectionDataAssert;
import de.unistuttgart.xsample.Assertions.SharedDataAssert;
import de.unistuttgart.xsample.Assertions.SliceDataAssert;
import de.unistuttgart.xsample.Assertions.WorkflowDataAssert;
import de.unistuttgart.xsample.dv.UserId;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.LegalNote;
import de.unistuttgart.xsample.mf.SourceFile;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.mf.Span;
import de.unistuttgart.xsample.mf.SpanType;
import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.query.QueryData;
import de.unistuttgart.xsample.pages.query.ResultData;
import de.unistuttgart.xsample.pages.query.ResultsData;
import de.unistuttgart.xsample.pages.shared.CorpusData;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.pages.shared.InputData;
import de.unistuttgart.xsample.pages.shared.LabelData;
import de.unistuttgart.xsample.pages.shared.PartData;
import de.unistuttgart.xsample.pages.shared.SelectionData;
import de.unistuttgart.xsample.pages.shared.SharedData;
import de.unistuttgart.xsample.pages.shared.WorkflowData;
import de.unistuttgart.xsample.pages.slice.SliceData;

/**
 * @author Markus Gärtner
 *
 */
public class XSampleTestUtils {

	public static long[] asIndices(XmpFragment[] fragments) {
		return Stream.of(fragments)
				.flatMapToLong(XmpFragment::stream)
				.toArray();
	}

	public static long[] asIndices(List<XmpFragment> xmpFragments) {
		return xmpFragments.stream()
				.flatMapToLong(XmpFragment::stream)
				.toArray();
	}
	
	public static Corpus createCorpus(String id, long fileId, long segments) {
		return Corpus.builder()
				.id(id)
				.title("title_"+id)
				.description("desc_"+id)
				.legalNote(LegalNote.builder()
						.author("author_"+id)
						.publisher("publisher_"+id)
						.year(2022)
						.title("title_"+id)
						.source("source_"+id)
						.build())
				.primaryData(SourceFile.builder()
						.label(id)
						.segments(segments)
						.id(fileId)
						.sourceType(SourceType.PDF)
						.build())
				.build();
	}
	
	public static XsampleManifest createManifest(Corpus corpus) {
		return XsampleManifest.builder()
				.corpus(corpus)
				.description("manifest_desc")
				.staticExcerptCorpus(corpus.getId())
				.staticExcerpt(Span.builder()
						.begin(1)
						.end(1)
						.spanType(SpanType.FIXED)
						.build())
				.build();
	}
	
	public static ExcerptEntry createEntry(Corpus corpus, long limit) {
		ExcerptEntry entry = new ExcerptEntry();
		entry.setCorpusId(corpus.getId());
		entry.setLimit(limit);
		return entry;
	}
	
	public static final XmpDataverse DATAVERSE = new XmpDataverse();
	static {
		DATAVERSE.setUrl("url");
		DATAVERSE.setMasterKey("master-key");
		DATAVERSE.setOverrideUrl("override-url");
	}
	
	public static final XmpDataverseUser USER = new XmpDataverseUser();
	static {
		USER.setDataverse(DATAVERSE);
		USER.setId(new UserId("url", "user-id"));
	}
	
	// DataBean assertions
	
	public static DownloadDataAssert assertThat(DownloadData data) {
		return new DownloadDataAssert(data);
	}
	
	public static CorpusDataAssert assertThat(CorpusData data) {
		return new CorpusDataAssert(data);
	}
	
	public static PartDataAssert assertThat(PartData data) {
		return new PartDataAssert(data);
	}
	
	public static ResultDataAssert assertThat(ResultData data) {
		return new ResultDataAssert(data);
	}
	
	public static ResultsDataAssert assertThat(ResultsData data) {
		return new ResultsDataAssert(data);
	}
	
	public static InputDataAssert assertThat(InputData data) {
		return new InputDataAssert(data);
	}
	
	public static LabelDataAssert assertThat(LabelData data) {
		return new LabelDataAssert(data);
	}
	
	public static PartsDataAssert assertThat(PartsData data) {
		return new PartsDataAssert(data);
	}
	
	public static QueryDataAssert assertThat(QueryData data) {
		return new QueryDataAssert(data);
	}
	
	public static SelectionDataAssert assertThat(SelectionData data) {
		return new SelectionDataAssert(data);
	}
	
	public static SharedDataAssert assertThat(SharedData data) {
		return new SharedDataAssert(data);
	}
	
	public static SliceDataAssert assertThat(SliceData data) {
		return new SliceDataAssert(data);
	}
	
	public static WorkflowDataAssert assertThat(WorkflowData data) {
		return new WorkflowDataAssert(data);
	}
	
	// Other assertions

	public static ExcerptEntryAssert assertThat(ExcerptEntry entry) {
		return new ExcerptEntryAssert(entry);
	}
}
