/**
 * 
 */
package de.unistuttgart.xsample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.query.EncodedResultData;
import de.unistuttgart.xsample.pages.query.QueryData;
import de.unistuttgart.xsample.pages.query.ResultData;
import de.unistuttgart.xsample.pages.query.ResultsData;
import de.unistuttgart.xsample.pages.shared.CorpusData;
import de.unistuttgart.xsample.pages.shared.EncodedCorpusData;
import de.unistuttgart.xsample.pages.shared.ExcerptEntry;
import de.unistuttgart.xsample.pages.shared.InputData;
import de.unistuttgart.xsample.pages.shared.LabelData;
import de.unistuttgart.xsample.pages.shared.PartData;
import de.unistuttgart.xsample.pages.shared.SelectionData;
import de.unistuttgart.xsample.pages.shared.SharedData;
import de.unistuttgart.xsample.pages.shared.WorkflowData;
import de.unistuttgart.xsample.pages.shared.WorkflowData.Status;
import de.unistuttgart.xsample.pages.slice.SliceData;

/**
 * @author Markus Gärtner
 *
 */
public final class Assertions {
	
	// DataBean assertions

	public static class DownloadDataAssert extends AbstractAssert<DownloadDataAssert, DownloadData> {

		public DownloadDataAssert(DownloadData actual) {
			super(actual, DownloadDataAssert.class);
		}
		
		public DownloadDataAssert includesAnnotations(boolean expected) {
			isNotNull();
			assertThat(actual.isIncludeAnnotations()).isEqualTo(expected);
			return this;
		}
		
		public DownloadDataAssert hasEntriesFor(String...corpusIds) {
			isNotNull();
			for(String corpusId : corpusIds) {
				if(actual.findEntry(corpusId) == null) {
					failWithMessage("expected entry for", corpusId);
				}
			}
			return this;
		}
		
		public DownloadDataAssert hasNoEntries() {
			isNotNull();
			List<ExcerptEntry> entries = actual.getEntries();
			if(!entries.isEmpty()) {
				failWithMessage("unexpected entries: %s", entries.stream()
						.map(ExcerptEntry::getCorpusId)
						.collect(Collectors.toList()));
			}
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	public abstract static class EncodedCorpusDataAssert<A extends EncodedCorpusDataAssert<A,T>, T extends EncodedCorpusData>
			extends AbstractAssert<A, T> {

		protected EncodedCorpusDataAssert(T actual, Class<?> selfType) {
			super(actual, selfType);
		}
		
		public A hasSegments(long expected) {
			isNotNull();
			assertThat(actual.getSegments()).as("segments").isEqualTo(expected);
			return (A) this;
		}
		
		public A hasLimit(long expected) {
			isNotNull();
			assertThat(actual.getLimit()).as("limit").isEqualTo(expected);
			return (A) this;
		}
		
		public A hasQuota(String expected) {
			isNotNull();
			assertThat(actual.getQuota()).as("quota").isEqualTo(expected);
			return (A) this;
		}
		
		public A hasNoQuota() {
			isNotNull();
			assertThat(actual.getQuota()).as("quota").isEmpty();
			return (A) this;
		}
		
		public A hasExcerpt(String expected) {
			isNotNull();
			assertThat(actual.getExcerpt()).as("excerpt").isEqualTo(expected);
			return (A) this;
		}
		
		public A hasNoExcerpt() {
			isNotNull();
			assertThat(actual.getExcerpt()).as("excerpt").isEmpty();
			return (A) this;
		}
		
		public A isEmpty() {
			isNotNull();
			hasSegments(-1);
			hasLimit(-1);
			hasNoQuota();
			hasNoExcerpt();
			return (A) this;
		}
	}

	public static class CorpusDataAssert extends EncodedCorpusDataAssert<CorpusDataAssert, CorpusData> {

		public CorpusDataAssert(CorpusData actual) {
			super(actual, CorpusDataAssert.class);
		}
		
		//TODO
	}

	public static class PartDataAssert extends EncodedCorpusDataAssert<PartDataAssert, PartData> {

		public PartDataAssert(PartData actual) {
			super(actual, PartDataAssert.class);
		}
		
		public PartDataAssert hasOffset(long expected) {
			isNotNull();
			assertThat(actual.getOffset()).as("offset").isEqualTo(expected);
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	public abstract static class EncodedResultDataAssert<A extends EncodedResultDataAssert<A,T>, T extends EncodedResultData>
			extends AbstractAssert<A, T> {
	
		protected EncodedResultDataAssert(T actual, Class<?> selfType) {
			super(actual, selfType);
		}
		
		public A hasRawHits(String expected) {
			isNotNull();
			assertThat(actual.getRawHits()).as("raw hits").isEqualTo(expected);
			return (A) this;
		}
		
		public A hasMappedHits(String expected) {
			isNotNull();
			assertThat(actual.getMappedHits()).as("mapped hits").isEqualTo(expected);
			return (A) this;
		}
		
		public A isEmpty() {
			isNotNull();
			hasRawHits("");
			hasMappedHits("");
			return (A) this;
		}
	}

	public static class ResultDataAssert extends EncodedResultDataAssert<ResultDataAssert, ResultData> {

		public ResultDataAssert(ResultData actual) {
			super(actual, ResultDataAssert.class);
		}
		
		//TODO
	}
	
	public static class ResultsDataAssert extends EncodedResultDataAssert<ResultsDataAssert, ResultsData> {
		
		public ResultsDataAssert(ResultsData actual) {
			super(actual, ResultsDataAssert.class);
		}
		
		//TODO
	}

	public static class InputDataAssert extends AbstractAssert<InputDataAssert, InputData> {

		public InputDataAssert(InputData actual) {
			super(actual, InputDataAssert.class);
		}
		
		//TODO
	}
	
	public static class LabelDataAssert extends AbstractAssert<LabelDataAssert, LabelData> {
		
		public LabelDataAssert(LabelData actual) {
			super(actual, LabelDataAssert.class);
		}
		
		//TODO
	}
	
	public static class PartsDataAssert extends AbstractAssert<PartsDataAssert, PartsData> {
		
		public PartsDataAssert(PartsData actual) {
			super(actual, PartsDataAssert.class);
		}
		
		//TODO
	}
	
	public static class QueryDataAssert extends AbstractAssert<QueryDataAssert, QueryData> {
		
		public QueryDataAssert(QueryData actual) {
			super(actual, QueryDataAssert.class);
		}
		
		public QueryDataAssert hasQuery(String expected) {
			isNotNull();
			assertThat(actual.getQuery()).as("query").isEqualTo(expected);
			return this;
		}
	}
	
	public static class SelectionDataAssert extends AbstractAssert<SelectionDataAssert, SelectionData> {
		
		public SelectionDataAssert(SelectionData actual) {
			super(actual, SelectionDataAssert.class);
		}
		
		public SelectionDataAssert isEmpty() {
			isNotNull();
			assertThat(actual.getSelectedCorpus()).isNull();
			return this;
		}
		
		public SelectionDataAssert hasSelection(Corpus expected) {
			isNotNull();
			assertThat(actual.getSelectedCorpus()).isSameAs(expected);
			return this;
		}
	}
	
	public static class SharedDataAssert extends AbstractAssert<SharedDataAssert, SharedData> {
		
		public SharedDataAssert(SharedData actual) {
			super(actual, SharedDataAssert.class);
		}
		
		//TODO
	}
	
	public static class SliceDataAssert extends AbstractAssert<SliceDataAssert, SliceData> {
		
		public SliceDataAssert(SliceData actual) {
			super(actual, SliceDataAssert.class);
		}
		
		//TODO
	}
	
	public static class WorkflowDataAssert extends AbstractAssert<WorkflowDataAssert, WorkflowData> {
		
		public WorkflowDataAssert(WorkflowData actual) {
			super(actual, WorkflowDataAssert.class);
		}
		
		public WorkflowDataAssert hasPage(String expected) {
			isNotNull();
			assertThat(actual.getPage()).as("page").isEqualTo(expected);
			return this;
		}
		
		public WorkflowDataAssert hasStatus(Status expected) {
			isNotNull();
			assertThat(actual.getStatus()).as("status").isEqualTo(expected);
			return this;
		}
	}
	
	// Other assertions

	public static class ExcerptEntryAssert extends AbstractAssert<ExcerptEntryAssert, ExcerptEntry> {
		
		public ExcerptEntryAssert(ExcerptEntry actual) {
			super(actual, ExcerptEntryAssert.class);
		}
		
		public ExcerptEntryAssert hasCorpusId(String expected) {
			isNotNull();
			assertThat(actual.getCorpusId()).as("corpusId").isEqualTo(expected);
			return this;
		}
		
		public ListAssert<XmpFragment> fragments() {
			isNotNull();
			return assertThat(actual.getFragments());
		}

		public ExcerptEntryAssert hasExactlyFragment(long value) {
			return hasExactlyFragment(value, value);
		}

		public ExcerptEntryAssert hasExactlyFragment(long begin, long end) {
			isNotNull();
			assertThat(actual.getFragments())
				.isNotNull()
				.hasSize(1);
			XmpFragment fragment = actual.getFragments().get(0);
			assertThat(fragment.getBeginIndex()).as("end").isEqualTo(begin);
			assertThat(fragment.getEndIndex()).as("begin").isEqualTo(end);
			return this;
		}
	}
}
