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

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.unistuttgart.xsample.mf.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationHandlers {

	private static final Map<ManifestType, Supplier<AnnotationHandler>> handlers = new EnumMap<>(ManifestType.class);
	static {
		handlers.put(ManifestType.ICARUS_LEGACY, CoNLL09Handler::new);
	}

	public static AnnotationHandler forManifestType(ManifestType type) throws UnsupportedManifestTypeException {
		requireNonNull(type);
		Supplier<AnnotationHandler> sup = handlers.get(type);
		if(sup==null)
			throw new UnsupportedManifestTypeException("Unsupported manifest typet: "+type);
		return sup.get();
	}
}
