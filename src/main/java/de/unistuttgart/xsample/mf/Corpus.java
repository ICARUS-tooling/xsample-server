/**
 * 
 */
package de.unistuttgart.xsample.mf;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Corpus implements Serializable {
	
	private static final long serialVersionUID = -9047502342614191852L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"corpus";

	/** Legal information for corpus or subcorpus */
	@Expose
	@SerializedName(XsampleManifest.NS+"legalNote")
	private LegalNote legalNote;

	/** General information for corpus or subcorpus */
	@Expose
	@SerializedName(XsampleManifest.NS+"note")
	@Nullable
	private String note;

	/** Span covered by corpus or subcorpus, can be omitted for main corpus without sub-parts */
	@Expose
	@SerializedName(XsampleManifest.NS+"span")
	@Nullable
	private Span span;

	/** Separate parts of the corpus with individual legal notes */
	@Expose
	@SerializedName(XsampleManifest.NS+"parts")
	@Nullable
	private List<Corpus> parts;
}