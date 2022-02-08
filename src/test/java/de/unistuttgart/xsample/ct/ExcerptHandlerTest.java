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
package de.unistuttgart.xsample.ct;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.unistuttgart.xsample.XSampleTestUtils;
import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.SourceType;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

/**
 * @author Markus Gärtner
 *
 */
interface ExcerptHandlerTest<H extends ExcerptHandler> {
	
	static final String INVALID_CONTENT_TYPE = "unknown/type+nonsense";
	
	H create();
	
	String[] supportedContentTypes();
	
	byte[] input(int size, String contentType, Charset encoding) throws IOException;
	
	default byte[] input(int size) throws IOException {
		return input(size, supportedContentTypes()[0], defaultEncoding());
	}
	
	default Charset defaultEncoding() { return StandardCharsets.UTF_8; }
	
	void assertExcerpt(byte[] original, InputStream in, long[] fragments) throws IOException;
	

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAnalyze() {
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicTest(contentType, () -> {
					H handler = create();
					InputStream in = new FastByteArrayInputStream(input(10));
					handler.analyze(new XmpFileInfo(), defaultEncoding(), in);
				}));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
	 */
	@Test
	default void testAnalyzeNullFileInfo() throws Exception {
		H handler = create();
		assertThatNullPointerException().isThrownBy(() -> handler.analyze(
				null, defaultEncoding(), new FastByteArrayInputStream(new byte[1])));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
	 */
	@Test
	default void testAnalyzeNullEncoding() throws Exception {
		H handler = create();
		assertThatNullPointerException().isThrownBy(() -> handler.analyze(
				new XmpFileInfo(), null, new FastByteArrayInputStream(new byte[1])));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
	 */
	@Test
	default void testAnalyzeNullStream() throws Exception {
		H handler = create();
		assertThatNullPointerException().isThrownBy(() -> handler.analyze(
				new XmpFileInfo(), defaultEncoding(), null));
	}

//	/**
//	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
//	 */
//	@Test
//	default void testAnalyzeInvalidContentType() throws Exception {
//		H handler = create();
//		assertThatExceptionOfType(UnsupportedContentTypeException.class)
//			.isThrownBy(() -> {
//				InputStream in = new FastByteArrayInputStream(input(1));
//				handler.analyze(new XmpFileInfo(), defaultEncoding(), in);
//			});
//	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#analyze(XmpFileInfo, InputStream)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testAnalyzeEmpty() {
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicTest(contentType, () -> {
					H handler = create();
					InputStream in = new FastByteArrayInputStream(input(0));
					assertThatExceptionOfType(EmptyResourceException.class)
						.isThrownBy(() -> handler.analyze(new XmpFileInfo(), defaultEncoding(), in));
				}));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#segments()}.
	 * @throws IOException 
	 * @throws EmptyResourceException 
	 * @throws UnsupportedContentTypeException 
	 */
	@TestFactory
	default Stream<DynamicNode> testSegments() throws Exception {
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicContainer(contentType, 
						IntStream.of(1, 2, 3, 10, 100, 101).mapToObj(
								size -> dynamicTest(String.valueOf(size), () -> {
					H handler = create();
					XmpFileInfo fileInfo = new XmpFileInfo();
					byte[] data = input(size, contentType, defaultEncoding());
					InputStream in = new FastByteArrayInputStream(data);
					handler.analyze(fileInfo, defaultEncoding(), in);
					assertThat(fileInfo.getSegments()).isEqualTo(size);
				}))));
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(XmpFileInfo, InputStream, List, java.io.OutputStream)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testExcerpt() {
		int[][] config = {
			new int[] {1, 1, 1},
			new int[] {2, 1, 1},
			new int[] {2, 2, 2},
			new int[] {2, 1, 2},
			new int[] {4, 2, 3},
		};
		
		
		return Stream.of(supportedContentTypes())
				.map(contentType -> dynamicContainer(contentType, 
						Stream.of(config).map(params -> dynamicTest(String.format(
								"(%d-%d) out of %d pages", _int(params[1]),
								_int(params[2]), _int(params[0])), () -> {
							final int size = params[0];
							final int from = params[1];
							final int to = params[2];
							final Charset encoding = defaultEncoding();
							
							XmpFileInfo fileInfo = new XmpFileInfo();
							fileInfo.setSegments(size);
							fileInfo.setSourceType(SourceType.forMimeType(contentType));
							
							H handler = create();
							byte[] data = input(size, contentType, encoding);
							InputStream in = new FastByteArrayInputStream(data);
							
							List<XmpFragment> fragments = Arrays.asList(XmpFragment.of(from, to));
							FastByteArrayOutputStream out = new FastByteArrayOutputStream();
							handler.excerpt(fileInfo, encoding, in, fragments, out);
							
							in = new FastByteArrayInputStream(out.array, 0, out.length);
							assertExcerpt(data, in, XSampleTestUtils.asIndices(fragments));
						}))));
	}
}
