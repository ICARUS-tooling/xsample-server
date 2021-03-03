/**
 * 
 */
package de.unistuttgart.xsample.mf;

import com.google.gson.annotations.SerializedName;

public enum ManifestType {
	/** ICARUS2 Manifest Format */
	@SerializedName(XsampleManifest.NS+"IMF")
	IMF,
	;
}