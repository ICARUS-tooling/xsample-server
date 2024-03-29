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
package de.unistuttgart.xsample.dv;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.unistuttgart.xsample.pages.shared.FragmentCodec;

/**
 * @author Markus Gärtner
 *
 */
class FragmentTest {
	
	@Nested
	class Factory {

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#encode(de.unistuttgart.xsample.dv.XmpFragment)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {1, 99, 999999999999999L})
		void testEncodeSingle(long value) {
			XmpFragment f = XmpFragment.of(value);
			assertThat(FragmentCodec.encode(f)).isEqualTo(String.valueOf(value));
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#encode(de.unistuttgart.xsample.dv.XmpFragment)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"1, 2",
			"1, 99",
			"1, 999999",
			"2, 9",
			"2, 999",
			"99, 999",
		})
		void testEncodeSpan(long begin, long end) {
			XmpFragment f = XmpFragment.of(begin, end);
			assertThat(FragmentCodec.encode(f)).isEqualTo(String.format("%d-%d", _long(begin), _long(end)));
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#decode(java.lang.String)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"1, 2",
			"1, 99",
			"1, 999999",
			"2, 9",
			"2, 999",
			"99, 999",
		})
		void testDecodeSpan(long begin, long end) {
			String s = String.format("%d-%d", _long(begin), _long(end));
			XmpFragment f = FragmentCodec.decode(s);
			assertThat(f.getBeginIndex()).isEqualTo(begin);
			assertThat(f.getEndIndex()).isEqualTo(end);
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#decode(java.lang.String)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {1, 99, 999999999999999L})
		void testDecodeSingle(long value) {
			String s = String.valueOf(value);
			XmpFragment f = FragmentCodec.decode(s);
			assertThat(f.getBeginIndex()).isEqualTo(value);
			assertThat(f.getEndIndex()).isEqualTo(value);
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#encodeAll(java.util.List)}.
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#decodeAll(java.lang.String)}.
		 */
		@Test
		void testEncodeAndDecodeAll() {
			XmpFragment[] fragments = new XmpFragment[] {
					XmpFragment.of(1, 2),
					XmpFragment.of(3, 9),
					XmpFragment.of(111, 999),
					XmpFragment.of(6666, 99999)
			};
			
			String s = FragmentCodec.encodeAll(Arrays.asList(fragments));
			assertThat(s).isNotBlank();
			
			List<XmpFragment> decoded = FragmentCodec.decodeAll(s);
			assertThat(decoded).hasSize(fragments.length);
			for (int i = 0; i < fragments.length; i++) {
				assertThat(decoded.get(i).compareTo(fragments[i]))
					.as("equality mismatch at %d", _int(i))
					.isEqualTo(0);
			}
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#of(long, long)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"1, 1",
			"1, 2",
			"1, 99",
			"1, 999999999999",
			"2, 2",
			"2, 99",
			"999, 99999",
		})
		void testOfLongLong(long begin, long end) {
			XmpFragment f = XmpFragment.of(begin, end);
			assertThat(f.getBeginIndex()).isEqualTo(begin);
			assertThat(f.getEndIndex()).isEqualTo(end);
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#of(long)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"0, 1",
			"-1, 0",
			"0, -1",
			"1, 0",
			"1, -1",
			"2, 1"
		})
		void testOfLongLong_illegalArgs(long begin, long end) {
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
					() -> XmpFragment.of(begin, end));
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#of(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {1, 2, 99, 9999999999999L})
		void testOfLong(long value) {
			XmpFragment f = XmpFragment.of(value);
			assertThat(f.getBeginIndex()).isEqualTo(value);
			assertThat(f.getEndIndex()).isEqualTo(value);
		}

		/**
		 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#of(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {0, -1, Long.MIN_VALUE})
		void testOfLong_negative(long value) {
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
					() -> XmpFragment.of(value));
		}
		
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#getBeginIndex()}.
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#setBeginIndex(long)}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {1, 2, 9, 9999999999L})
	void testBeginIndex(long value) {
		XmpFragment f = new XmpFragment();
		f.setBeginIndex(value);
		assertThat(f.getBeginIndex()).isEqualTo(value);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#getEndIndex()}.
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#setEndIndex(long)}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {1, 2, 9, 9999999999L})
	void testGetEndIndex(long value) {
		XmpFragment f = new XmpFragment();
		f.setEndIndex(value);
		assertThat(f.getEndIndex()).isEqualTo(value);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#getId()}.
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#setId(java.lang.Long)}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {0, 1, 2, 9, 9999999999L})
	void testGetId(long value) {
		XmpFragment f = new XmpFragment();
		f.setId(_long(value));
		assertThat(f.getId()).isEqualTo(value);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#getExcerpt()}.
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#setExcerpt(de.unistuttgart.xsample.dv.XmpExcerpt)}.
	 */
	@Test
	void testGetExcerpt() {
		XmpFragment f = new XmpFragment();
		assertThat(f.getExcerpt()).isNull();
		XmpExcerpt e = mock(XmpExcerpt.class);
		f.setExcerpt(e);
		assertThat(f.getExcerpt()).isSameAs(e);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#stream()}.
	 */
	@ParameterizedTest
	@CsvSource({
		"1, 1",
		"1, 2",
		"1, 22",
		"2, 2",
		"22, 99",
	})
	void testStream(long begin, long end) {
		XmpFragment f = XmpFragment.of(begin, end);
		long[] elements = f.stream().toArray();
		long[] expected = LongStream.rangeClosed(begin, end).toArray();
		assertThat(elements).containsExactly(expected);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#size()}.
	 */
	@ParameterizedTest
	@CsvSource({
		"1, 1",
		"1, 2",
		"1, 22",
		"2, 2",
		"22, 99",
	})
	void testSize(long begin, long end) {
		XmpFragment f = XmpFragment.of(begin, end);
		assertThat(f.size()).isEqualTo(end-begin+1);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#compareTo(de.unistuttgart.xsample.dv.XmpFragment)}.
	 */
	@ParameterizedTest
	@CsvSource({
		"1, 1, 1, 1, 0",
		"1, 2, 2, 2, -1",
		"1, 2, 1, 3, -1",
		"1, 3, 1, 2, 1",
		//TODO
	})
	void testCompareTo(long begin1, long end1, long begin2, long end2, int expected) {
		XmpFragment f1 = XmpFragment.of(begin1, end1);
		XmpFragment f2 = XmpFragment.of(begin2, end2);
		assertThat(f1.compareTo(f2)).isEqualTo(expected);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.dv.XmpFragment#detach()}.
	 */
	@Test
	void testDetach() {
		XmpFragment f = new XmpFragment();
		f.setExcerpt(mock(XmpExcerpt.class));
		assertThat(f.getExcerpt()).isNotNull();
		f.detach();
		assertThat(f.getExcerpt()).isNull();
	}

}
