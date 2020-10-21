/**
 * 
 */
package de.unistuttgart.xsample.ct;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.XSampleTestUtils;
import de.unistuttgart.xsample.util.DataInput;
import de.unistuttgart.xsample.util.DataOutput;

/**
 * @author Markus Gärtner
 *
 */
interface ExcerptHandlerTest<H extends ExcerptHandler> {
	
	static final String INVALID_CONTENT_TYPE = "unknown/type+nonsense";
	
	H create();
	
	String[] supportedContentTypes();
	
	DataInput input(int size, String contentType, Charset encoding) throws IOException;
	
	default DataInput input(int size) throws IOException {
		return input(size, supportedContentTypes()[0], defaultEncoding());
	}
	
	default Charset defaultEncoding() { return StandardCharsets.UTF_8; }
	
	void assertExcerpt(DataInput excerpt, long[] fragments) throws IOException;
	

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#init(de.unistuttgart.xsample.util.DataInput)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testInit() {
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicTest(contentType, () -> {
					try(H handler = create()) {
						handler.init(input(10, contentType, defaultEncoding()));
					}
				}));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#init(de.unistuttgart.xsample.util.DataInput)}.
	 */
	@Test
	default void testInitNull() throws Exception {
		try(H handler = create()) {
			assertThatNullPointerException().isThrownBy(() -> handler.init(null));
		}
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#init(de.unistuttgart.xsample.util.DataInput)}.
	 */
	@Test
	default void testInitInvalidContentType() throws Exception {
		try(H handler = create()) {
			assertThatExceptionOfType(UnsupportedContentTypeException.class)
				.isThrownBy(() -> handler.init(input(10, INVALID_CONTENT_TYPE, defaultEncoding())));
		}
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#init(de.unistuttgart.xsample.util.DataInput)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testInitEmpty() {
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicTest(contentType, () -> {
					try(H handler = create()) {
						assertThatExceptionOfType(EmptyResourceException.class)
						.isThrownBy(() -> handler.init(input(0, contentType, defaultEncoding())));
					}
				}));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#segments()}.
	 * @throws IOException 
	 * @throws EmptyResourceException 
	 * @throws UnsupportedContentTypeException 
	 */
	@ValueSource(ints = {1, 2, 3, 10, 100, 101})
	@ParameterizedTest
	default void testSegments(int size) throws Exception {
		try(H handler = create()) {
			handler.init(input(size));
			assertThat(handler.segments()).isEqualTo(size);
		}
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.Fragment[], de.unistuttgart.xsample.util.DataOutput)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExcerpt() {
		int[][] config = {
			new int[] {1, 0, 0},
			new int[] {2, 0, 0},
			new int[] {2, 1, 1}
		};
		
		
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicContainer(contentType, 
						Stream.of(config).map(params -> dynamicTest(String.format(
								"(%d-%d) out of %d pages", _int(params[1]),
								_int(params[2]), _int(params[0])), () -> {
							int size = params[0];
							int from = params[1];
							int to = params[2];
							
							try(H handler = create()) {
								DataInput source = input(size, contentType, defaultEncoding());
								handler.init(source);
								Fragment[] fragments = new Fragment[]{Fragment.of(from, to)};
								DataOutput excerpt = handler.excerpt(fragments);
								
								assertExcerpt(excerpt.bridge(), XSampleTestUtils.asIndices(fragments));
							}
						}))));
	}

	/**
	 * Test method for {@link java.io.Closeable#close()}.
	 */
	@Test
	default void testClose() throws Exception {
		try(H handler = create()) {
			handler.init(input(10));
			handler.close();
		}
	}

	/**
	 * Test method for {@link java.io.Closeable#close()}.
	 */
	@Test
	default void testCloseUninitialized() throws Exception {
		try(H handler = create()) {
			handler.close();
		}
	}

}
