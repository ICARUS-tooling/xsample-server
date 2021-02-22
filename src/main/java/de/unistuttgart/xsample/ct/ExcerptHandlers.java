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
package de.unistuttgart.xsample.ct;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.ServiceLoader;

import de.unistuttgart.xsample.ct.spi.DefaultExcerptHandlerFactory;
import de.unistuttgart.xsample.ct.spi.ExcerptHandlerFactory;
import de.unistuttgart.xsample.mf.XsampleManifest.SourceType;

/**
 * @author Markus Gärtner
 *
 */
public class ExcerptHandlers {

	private static final ServiceLoader<ExcerptHandlerFactory> loader = 
			ServiceLoader.load(ExcerptHandlerFactory.class);
	
	private static final ExcerptHandlerFactory defaultFactory = new DefaultExcerptHandlerFactory();
	
	public static ExcerptHandler forSourceType(SourceType type) throws UnsupportedContentTypeException {
		requireNonNull(type);
		for(ExcerptHandlerFactory factory : loader) {
			Optional<ExcerptHandler> handler = factory.create(type);
			if(handler.isPresent()) {
				return handler.get();
			}
		}
		//TODO currently we use the default factory as fallback since the SPI approach doesn't work
		return defaultFactory.create(type).orElseThrow(
				() -> new UnsupportedContentTypeException("Unsupported content type: "+type));
	}
}
