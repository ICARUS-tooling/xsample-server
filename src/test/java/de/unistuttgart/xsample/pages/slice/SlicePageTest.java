/**
 * 
 */
package de.unistuttgart.xsample.pages.slice;

import static de.unistuttgart.xsample.XSampleTestUtils.assertThat;
import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.unistuttgart.xsample.XSampleTestUtils;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.LegalNote;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.parts.PartsPage;
import de.unistuttgart.xsample.pages.shared.AbstractSlicePageTest;
import de.unistuttgart.xsample.pages.shared.SharedData;
import de.unistuttgart.xsample.pages.welcome.WelcomePage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SlicePageTest extends AbstractSlicePageTest<SlicePage> {

	@Override
	protected SlicePage createPage() { return new SlicePage(); }
	
	/** Prepares data beans to represent a state after successful  forwarding 
	 * from {@link WelcomePage} and/or {@link PartsPage}. */
	private void prepareEnvironment(Corpus corpus) {
		SharedData sharedData = getSharedData();
		sharedData.setServer(XSampleTestUtils.DATAVERSE);
		sharedData.setDataverseUser(XSampleTestUtils.USER);
		sharedData.setVerified(true);
	}
		
	@Nested
	class ForSingleton {
		
		Corpus corpus;
		long limit, segments;
		
		@BeforeEach
		void setUp() {
			segments = 20;
			limit = 3;
			corpus = XSampleTestUtils.createCorpus("corpus01", 0, segments);
			
			getSharedData().setManifest(XSampleTestUtils.createManifest(corpus));
			
			getPartsData().setSelectedParts(Arrays.asList(corpus));
			
			getCorpusData().setSegments(segments);
			getCorpusData().setLimit(limit);
			getCorpusData().registerSegments(corpus.getId(), segments);
			getCorpusData().registerLimit(corpus.getId(), limit);
			getCorpusData().registerOffset(corpus.getId(), 0);
			
			getDownloadData().addEntry(XSampleTestUtils.createEntry(corpus, limit));
		}

		@Nested
		class ForInit {

			@Test
			void testWihtoutPreselection() {				
				page.init();
				
				assertThat(getSelectionData()).hasSelection(corpus);
				assertThat(getPartData()).hasSegments(segments)
					.hasLimit(limit)
					.hasOffset(0)
					.hasNoQuota()
					.hasNoExcerpt();
			}

			@Test
			void testWihtPreselection() {
				getSelectionData().setSelectedCorpus(corpus);
				
				page.init();

				assertThat(getSelectionData()).hasSelection(corpus);
				assertThat(getPartData()).hasSegments(segments)
					.hasLimit(limit)
					.hasOffset(0)
					.hasNoQuota()
					.hasNoExcerpt();
			}
			
		}
		
		@Nested
		class ForSliceAssignment {
			
			@BeforeEach
			void setUp() {
				page.init();
			}

			@Test
			void testSingleChange() {
				// Prepare slice data
				long begin = 2;
				long end = 3;
				getSliceData().setBegin(begin);
				getSliceData().setEnd(end);
				
				// Let page try to advance
				page.next();
				
				// Assert state of download data
				assertThat(uiProxy.hasMessages(SlicePage.NAV_MSG)).isFalse();
				assertThat(getWorkflow()).hasPage(DownloadPage.PAGE);
				
				assertThat(getDownloadData().findEntry(corpus))
					.hasExactlyFragment(begin, end);
			}

			@Test
			void testRepeatedChange() {
				// Prepare slice data
				long begin1 = 2;
				long end1 = 3;
				getSliceData().setBegin(begin1);
				getSliceData().setEnd(end1);
				// Perform a second slice change
				long begin2 = 11;
				long end2 = 13;
				getSliceData().setBegin(begin2);
				getSliceData().setEnd(end2);
				
				// Let page try to advance
				page.next();
				
				// Assert state of download data
				assertThat(uiProxy.hasMessages(SlicePage.NAV_MSG)).isFalse();
				assertThat(getWorkflow()).hasPage(DownloadPage.PAGE);
				
				assertThat(getDownloadData().findEntry(corpus))
					.hasExactlyFragment(begin2, end2);
			}
			
		}
	}
		
	//TODO test for more corpus constellations

	@Nested
	class ForMultiPart {
		List<Corpus> corpora;
		long[] a_seg, a_limit, a_offset;
		
		@BeforeEach
		void setUp() {
			a_seg = new long[]{20, 40, 20, 80, 200};
			a_limit = new long[]{03, 06, 03, 12, 030};
			a_offset = new long[a_seg.length];
			corpora = new ObjectArrayList<>(a_seg.length);
			
			for (int i = 0; i < a_seg.length; i++) {
				long offset = i==0 ? 0 : a_offset[i-1] + a_seg[i-1];
				long segments = a_seg[i];
				long limit = a_limit[i];
				a_offset[i] = offset;
				Corpus corpus = XSampleTestUtils.createCorpus(String.format("corpus_%2d", _int(i)), i, segments);
				corpora.add(corpus);
				getCorpusData().registerSegments(corpus.getId(), segments);
				getCorpusData().registerLimit(corpus.getId(), limit);
				getCorpusData().registerOffset(corpus.getId(), offset);
				
				getDownloadData().addEntry(XSampleTestUtils.createEntry(corpus, limit));
			}
			
			getSharedData().setManifest(XSampleTestUtils.createManifest(Corpus.builder()
					.id("root")
					.description("root corpus")
					.parts(corpora)
					.legalNote(LegalNote.builder()
							.author("root author")
							.publisher("root publisher")
							.year(2022)
							.title("root corpus")
							.source("root source")
							.build())
					.build()));
			
			getPartsData().setSelectedParts(corpora);

			getCorpusData().setLimit(LongStream.of(a_limit).sum());
			getCorpusData().setSegments(LongStream.of(a_seg).sum());
		}
		
		@Nested
		class ForInit {

			@Test
			void testWihtoutPreselection() {				
				page.init();

				assertThat(getSelectionData()).hasSelection(corpora.get(0));
				assertThat(getPartData()).hasSegments(a_seg[0])
					.hasLimit(a_limit[0])
					.hasOffset(a_offset[0])
					.hasNoQuota()
					.hasNoExcerpt();
			}
			
			@Test
			void testWihtPreselection() {	
				int selected = 2;
				getSelectionData().setSelectedCorpus(corpora.get(selected));
				
				page.init();

				assertThat(getSelectionData()).hasSelection(corpora.get(selected));
				assertThat(getPartData()).hasSegments(a_seg[selected])
					.hasLimit(a_limit[selected])
					.hasOffset(a_offset[selected])
					.hasNoQuota()
					.hasNoExcerpt();
				assertThat(getCorpusData()).hasExcerpt("");
			}
		}
		
		@Nested
		class ForSelectionChange {
			
			@Test
			void testNullToNull() {
				int selected = 1;
				getSelectionData().setSelectedCorpus(corpora.get(selected));
				
				page.selectionChanged(new ValueChangeEvent(mock(UIComponent.class), null, corpora.get(selected)));
				
				
			}
		}
		
		@Nested
		class ForSliceAssignment {
			
		}
	}
}
