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
package de.unistuttgart.xsample.mp;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.mf.MappingType;

/**
 * @author Markus Gärtner
 *
 */
public class Mappings {
	
	private static final Map<MappingType, Supplier<Mapping>> mappings = new EnumMap<>(MappingType.class);
	static {
		mappings.put(MappingType.TABULAR, TabularMapping::new);
	}

	public static Mapping forMappingType(MappingType type) throws UnsupportedContentTypeException {
		requireNonNull(type);
		Supplier<Mapping> sup = mappings.get(type);
		if(sup==null)
			throw new UnsupportedContentTypeException("Unsupported mapping format: "+type);
		return sup.get();
	}
}
