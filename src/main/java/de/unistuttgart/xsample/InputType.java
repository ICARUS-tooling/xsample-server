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

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
public enum InputType {

	PDF(true, ".pdf"),
	TXT(true, ".txt"),
	EPUB(true, ".epub"),
	MANIFEST(false, ".json", ".json-ld"),
	;
	
	private final boolean raw;
	private final String[] endings;

	private InputType(boolean raw, String...endings) {
		Preconditions.checkArgument(endings.length>0, "Must have at least 1 ending registered");
		this.endings = endings;
		this.raw = raw;
	}
	
	public boolean isRaw() { return raw; }
	
	public static @Nullable InputType forFileName(String filename) {
		for(InputType type : InputType.values()) {
			for(String ending : type.endings) {
				if(filename.endsWith(ending)) {
					return type;
				}
			}
		}
		return null;
	}
	
	private static final Map<String, InputType> mimeMap = new HashMap<>();
	static {
		mimeMap.put(XSampleUtils.MIME_TXT, TXT);
		mimeMap.put(XSampleUtils.MIME_EPUB, EPUB);
		mimeMap.put(XSampleUtils.MIME_PDF, PDF);
	}
	
	public static @Nullable InputType forMimeType(String mimeType) {
		return mimeMap.get(requireNonNull(mimeType));
	}
}
