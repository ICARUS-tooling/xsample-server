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