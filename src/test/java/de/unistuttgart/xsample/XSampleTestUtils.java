/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.unistuttgart.xsample.dv.XmpFragment;

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
}
