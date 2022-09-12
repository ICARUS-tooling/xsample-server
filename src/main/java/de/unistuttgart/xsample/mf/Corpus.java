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

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.SelfValidating;

public class Corpus implements Serializable, SelfValidating {
	
	private static final long serialVersionUID = -9047502342614191852L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"corpus";

	/** Link to optional primary file. Note that either the root corpus must define this or EVERY sub corpus must define a separate one */
	@Expose
	@SerializedName(XsampleManifest.NS+"primaryData")
	private SourceFile primaryData;

	/** Legal information for corpus or subcorpus */
	@Expose
	@SerializedName(XsampleManifest.NS+"legalNote")
	private LegalNote legalNote;

	/** Human readable label for the corpus. Only mandatory on the top level corpus object! */
	@Expose
	@SerializedName(XsampleManifest.NS+"title")
	private String title;

	/** General information for corpus or subcorpus */
	@Expose
	@SerializedName(XsampleManifest.NS+"description")
	@Nullable
	private String description;

	/** Unique identifier of this corpus within a surrounding manifest */
	@Expose
	@SerializedName(XsampleManifest.NS+"id")
	private String id;

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

	/** Link to optional primary file. Note that either the root corpus must define this or EVERY sub corpus must define a separate one */
	public SourceFile getPrimaryData() { return primaryData; }
	/** Legal information for corpus or subcorpus */
	public LegalNote getLegalNote() { return legalNote; }
	/** General information for corpus or subcorpus */
	public String getDescription() { return description; }
	/** Span covered by corpus or subcorpus, can be omitted for main corpus without sub-parts */
	public Span getSpan() { return span; }
	/** Separate parts of the corpus with individual legal notes */
	public List<Corpus> getParts() { return parts==null ? Collections.emptyList() : new ArrayList<>(parts); }
	/** Unique identifier of this corpus within a surrounding manifest */
	public String getId() { return id; }
	public String getTitle() { return title; }
	
	// Helpers
	public boolean hasParts() { return parts!=null && !parts.isEmpty(); }	
	public boolean hasData() { return primaryData!=null; }	
	
	/** Returns {@code true} iff this corpus has subcorpora AND an assigned primary data file */
	public boolean hasComplexHierarchy() {
		return hasParts() && primaryData!=null;
	}
	
	/** Returns {@code true} iff this corpus only contains subcorpora and no own primary data file */
	public boolean isProxy() {
		return hasParts() && primaryData==null;
	}
	
	public void forEachPart(Consumer<? super Corpus> action) {
		if(parts!=null) parts.forEach(action);
	}

	@Override
	public void validate() {
		checkState("Missing 'description' field", description!=null);
		checkState("Missing 'id' field", id!=null);
		if(primaryData==null) {
			checkState("Missing 'title' field", id!=null);
		}
		SelfValidating.validateNested(legalNote, "legalNote");
		SelfValidating.validateNested(primaryData, "primaryData", parts!=null && !parts.isEmpty());
		SelfValidating.validateOptionalNested(span);
		SelfValidating.validateOptionalNested(parts);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+(id==null ? "no-id" : id);
	}
	
	public static Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<Corpus> {
		
		private Builder() { /* no-op */ }

		@Override
		protected Corpus makeInstance() { return new Corpus(); }
		
		public Builder primaryData(SourceFile primaryData) {
			requireNonNull(primaryData);
			checkState("Primary data already set", instance.primaryData==null);
			instance.primaryData = primaryData;
			return this;
		}
		
		public Builder description(String description) {
			checkNotEmpty(description);
			checkState("Description already set", instance.description==null);
			instance.description = description;
			return this;
		}
		
		public Builder id(String id) {
			checkNotEmpty(id);
			checkState("Id already set", instance.id==null);
			instance.id = id;
			return this;
		}
		
		public Builder title(String title) {
			checkNotEmpty(title);
			checkState("Title already set", instance.title==null);
			instance.title = title;
			return this;
		}

		public Builder legalNote(LegalNote legalNote) {
			requireNonNull(legalNote);
			checkState("Legal note already set", instance.legalNote==null);
			instance.legalNote = legalNote;
			return this;
		}
		
		public Builder span(Span span) {
			requireNonNull(span);
			checkState("Span already set", instance.span==null);
			instance.span = span;
			return this;
		}
		
		private List<Corpus> ensureParts() {
			if(instance.parts==null) {
				instance.parts = new ArrayList<>();
			}
			return instance.parts;
		}
		
		public Builder parts(List<Corpus> parts) {
			requireNonNull(parts);
			checkArgument("Parts list is empty", !parts.isEmpty());
			ensureParts().addAll(parts);
			return this;
		}
		
		public Builder part(Corpus part) {
			requireNonNull(part);
			ensureParts().add(part);
			return this;
		}
	}
}