/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.unistuttgart.xsample.util.Payload;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

/**
 * @author Markus G�rtner
 *
 */
interface ExcerptHandlerTest<H extends ExcerptHandler> {
	
	static final String INVALID_CONTENT_TYPE = "unknown/type+nonsense";
	
	H create();
	
	String[] supportedContentTypes();
	
	Payload input(int size, String contentType, Charset encoding) throws IOException;
	
	default Payload input(int size) throws IOException {
		return input(size, supportedContentTypes()[0], defaultEncoding());
	}
	
	default Charset defaultEncoding() { return StandardCharsets.UTF_8; }
	
	void assertExcerpt(Payload excerpt, long[] fragments) throws IOException;
	

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
							final int size = params[0];
							final int from = params[1];
							final int to = params[2];
							final Charset encoding = defaultEncoding();
							
							try(H handler = create()) {
								Payload source = input(size, contentType, encoding);
								handler.init(source);
								
								Fragment[] fragments = new Fragment[]{Fragment.of(from, to)};
								FastByteArrayOutputStream out = new FastByteArrayOutputStream();
								Payload output = Payload.forOutput(encoding, contentType, out);
								handler.excerpt(fragments, output);
								
								FastByteArrayInputStream in = new FastByteArrayInputStream(out.array, 0, out.length);
								Payload excerpt = Payload.forInput(encoding, contentType, in);
								assertExcerpt(excerpt, XSampleTestUtils.asIndices(fragments));
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
