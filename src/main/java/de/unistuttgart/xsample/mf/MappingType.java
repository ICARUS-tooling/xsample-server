/**
 * 
 */
package de.unistuttgart.xsample.mf;

import com.google.gson.annotations.SerializedName;

/**
 * @author Markus Gärtner
 *
 */
public enum MappingType {

	/** Tab-separated tabular format with usually 3 columns */
	@SerializedName(XsampleManifest.NS+"tabular")
	TABULAR,
	;
}
