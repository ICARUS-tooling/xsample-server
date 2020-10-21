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
package de.unistuttgart.xsample.ct.spi;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.PdfHandler;
import de.unistuttgart.xsample.ct.PlaintextHandler;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus G�rtner
 *
 */
public class DefaultExcerptHandlerFactory implements ExcerptHandlerFactory {
	
	private static final Map<String, Supplier<ExcerptHandler>> handlers = new HashMap<>();
	static {
		handlers.put(XSampleUtils.MIME_PDF, PdfHandler::new);
		handlers.put(XSampleUtils.MIME_TXT, PlaintextHandler::new);
	}

	@Override
	public Optional<ExcerptHandler> create(String contentType) {
		requireNonNull(contentType);
		return Optional.ofNullable(handlers.get(contentType)).map(Supplier::get);
	}

}