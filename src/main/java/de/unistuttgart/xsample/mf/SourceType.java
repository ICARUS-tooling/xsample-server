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
package de.unistuttgart.xsample.mf;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.XSampleUtils;

public enum SourceType {
	@SerializedName(XsampleManifest.NS+"pdf")
	PDF(".pdf"),
	@SerializedName(XsampleManifest.NS+"epub")
	EPUB(".epub"),
	@SerializedName(XsampleManifest.NS+"plain-text")
	TXT(".txt"),
	;

	private final String[] endings;

	private SourceType(String...endings) {
		Preconditions.checkArgument(endings.length>0, "Must have at least 1 ending registered");
		this.endings = endings;
	}
	
	public static @Nullable SourceType forFileName(String filename) {
		for(SourceType type : SourceType.values()) {
			for(String ending : type.endings) {
				if(filename.endsWith(ending)) {
					return type;
				}
			}
		}
		return null;
	}
	
	private static final Map<String, SourceType> mimeMap = new HashMap<>();
	static {
		mimeMap.put(XSampleUtils.MIME_TXT, TXT);
		mimeMap.put(XSampleUtils.MIME_EPUB, EPUB);
		mimeMap.put(XSampleUtils.MIME_PDF, PDF);
	}
	
	public static @Nullable SourceType forMimeType(String mimeType) {
		return mimeMap.get(requireNonNull(mimeType));
	}
}