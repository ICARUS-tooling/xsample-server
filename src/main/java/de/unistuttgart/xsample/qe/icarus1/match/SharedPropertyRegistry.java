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
package de.unistuttgart.xsample.qe.icarus1.match;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SharedPropertyRegistry {

	public static final Object SYLLABLE_LEVEL = "syllable"; //$NON-NLS-1$
	public static final Object WORD_LEVEL = "word"; //$NON-NLS-1$
	public static final Object SPAN_LEVEL = "span"; //$NON-NLS-1$
	public static final Object SENTENCE_LEVEL = "sentence"; //$NON-NLS-1$
	public static final Object DOCUMENT_LEVEL = "document"; //$NON-NLS-1$
	public static final Object ENVIRONMENT_LEVEL = "environment"; //$NON-NLS-1$

	private static final Map<String, ValueHandler> handlers = new HashMap<>();
	private static final Map<Class<?>, Set<String>> typedSpecifiers = new HashMap<>();
	private static final Map<Object, Set<String>> levelSpecificSpecifiers = new HashMap<>();

	private static final Set<String> generalSpecifiers = new HashSet<>();

	private static <E extends Object> Set<String> set(Map<E, Set<String>> map, E key) {
		Set<String> result = map.get(key);
		if(result==null) {
			result = new HashSet<>();
			map.put(key, result);
		}
		return result;
	}

	public static boolean registerHandler(String specifier, ValueHandler handler, Class<?> contentType, Object level) {
		if(contentType!=null) {
			set(typedSpecifiers, contentType).add(specifier);
		}
		if(level!=null) {
			set(levelSpecificSpecifiers, level).add(specifier);
		} else {
			generalSpecifiers.add(specifier);
		}

		return handlers.put(specifier, handler)==null;
	}

	public static ValueHandler getHandler(Object specifier) {
		if(specifier==null) {
			return ValueHandler.stringHandler;
		}

		ValueHandler handler = handlers.get(specifier);
		return handler==null ? ValueHandler.stringHandler : handler;
	}

	private static final String[] EMPTY_SPECIFIERS = {};

	public static final int INCLUDE_GENERAL_LEVEL = (1<<0);
	public static final int INCLUDE_COMPATIBLE_TYPES = (1<<1);

	public static String[] getSpecifiers(Object level, Class<?>...types) {
		return getSpecifiers(level, 0, types);
	}

	public static String[] getSpecifiers(Object level, int flags, Class<?>...types) {

		Set<String> buffer = new HashSet<>();

		if((flags & INCLUDE_COMPATIBLE_TYPES)==INCLUDE_COMPATIBLE_TYPES) {
			for(Entry<Class<?>, Set<String>> entry : typedSpecifiers.entrySet()) {
				for(Class<?> type : types) {
					if(entry.getKey().isAssignableFrom(type)) {
						buffer.addAll(entry.getValue());
					}
				}
			}
		} else {
			for(Class<?> type : types) {
				Set<String> specifiers = typedSpecifiers.get(type);
				if(specifiers!=null) {
					buffer.addAll(specifiers);
				}
			}
		}

		if(level!=null) {
			Set<String> filter = levelSpecificSpecifiers.get(level);
			if(filter!=null) {
				buffer.retainAll(filter);
			}
		}

		if((flags & INCLUDE_GENERAL_LEVEL)==INCLUDE_GENERAL_LEVEL) {
			buffer.addAll(generalSpecifiers);
		}

		String[] result = EMPTY_SPECIFIERS;
		if(!buffer.isEmpty()) {
			result = new String[buffer.size()];
			buffer.toArray(result);
			Arrays.sort(result);
		}
		return result;
	}

	static {

		// General level
		SharedPropertyRegistry.registerHandler(LanguageConstants.NUMBER_KEY, ValueHandler.stringHandler, null, null);
		SharedPropertyRegistry.registerHandler(LanguageConstants.GENDER_KEY, ValueHandler.stringHandler, null, null);
		SharedPropertyRegistry.registerHandler(LanguageConstants.SIZE_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(LanguageConstants.LENGTH_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(LanguageConstants.INDEX_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(LanguageConstants.ID_KEY, ValueHandler.stringHandler, null, null);

		// Word level
		Class<?> contentType = SentenceData.class;
		SharedPropertyRegistry.registerHandler(LanguageConstants.FORM_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.TAG_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.PARSE_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.LEMMA_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.SENSE_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.ENTITY_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.FRAMESET_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LanguageConstants.SPEAKER_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
	}
}
