/**
 * 
 */
package de.unistuttgart.xsample.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.unistuttgart.xsample.dv.Fragment;

/**
 * @author Markus Gärtner
 *
 */
class XSampleUtilsTest {

	/**
	 * Test method for {@link de.unistuttgart.xsample.util.XSampleUtils#combinedSize(java.util.List, java.util.List)}.
	 */
	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
		"1;2;2",
		"1-2;2-3;3",
		"1-2;1-2;2",
		"1-2,3-6,9;2,3-5,8;8",
		"1-4;2-6;6",
		"1-4,8-9;2-6;8",
	})
	void testCombinedSize(String s1, String s2, long size) {
		List<Fragment> f1 = Fragment.decodeAll(s1);
		List<Fragment> f2 = Fragment.decodeAll(s2);
		assertThat(XSampleUtils.combinedSize(f1, f2)).isEqualTo(size);
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.util.XSampleUtils#merge(java.util.List, java.util.List, java.util.function.Consumer, java.util.function.BiConsumer)}.
	 */
	@Test
	@Disabled("for now we rely on the test for 'combinedSize'")
	void testMerge() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.util.XSampleUtils#encrypt(javax.crypto.SecretKey)}.
	 */
	@Test
	void testEncrypt() throws Exception {
		assertThat(XSampleUtils.encrypt(XSampleUtils.makeKey())).isNotNull();
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.util.XSampleUtils#decrypt(javax.crypto.SecretKey)}.
	 */
	@Test
	void testDecrypt() throws Exception {
		assertThat(XSampleUtils.decrypt(XSampleUtils.makeKey())).isNotNull();
	}

	/**
	 * Test method for {@link de.unistuttgart.xsample.util.XSampleUtils#makeKey()}.
	 */
	@Test
	void testMakeKey() throws Exception {
		assertThat(XSampleUtils.makeKey()).isNotNull();
	}

}
