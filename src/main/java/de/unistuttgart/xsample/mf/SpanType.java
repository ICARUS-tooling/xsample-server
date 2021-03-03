/**
 * 
 */
package de.unistuttgart.xsample.mf;

import com.google.gson.annotations.SerializedName;

public enum SpanType {
	/** Direct addressing of segments. */
	@SerializedName(XsampleManifest.NS+"fixed")
	FIXED,
	/** Relative span definition via percentage [0..100]. */
	@SerializedName(XsampleManifest.NS+"relative")
	RELATIVE,
	;
}