/**
 * 
 */
package de.unistuttgart.xsample;

import java.util.stream.Stream;

/**
 * @author Markus Gärtner
 *
 */
public class XSampleTestUtils {

	public static long[] asIndices(Fragment[] fragments) {
		return Stream.of(fragments)
				.flatMapToLong(Fragment::stream)
				.toArray();
	}
}
