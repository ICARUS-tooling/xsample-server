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

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.SelfValidating;

public class LegalNote implements Serializable, SelfValidating {

	private static final long serialVersionUID = 3921667001929679618L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"legalNote";

	@Expose
	@SerializedName(XsampleManifest.NS+"author")
	private String author;

	@Expose
	@SerializedName(XsampleManifest.NS+"title")
	private String title;

	@Expose
	@SerializedName(XsampleManifest.NS+"publisher")
	private String publisher;
	
	@Expose
	@SerializedName(XsampleManifest.NS+"year")
	private int year = Integer.MIN_VALUE;

	public String getAuthor() { return author; }
	public String getTitle() { return title; }
	public String getPublisher() { return publisher; }
	public int getYear() { return year; }
	
	@Override
	public void validate() {
		checkState("Missing 'author' field", author!=null);
		checkState("Missing 'title' field", title!=null);
		checkState("Missing 'publisher' field", publisher!=null);
		checkState("Missing 'year' field", year!=Integer.MIN_VALUE);
	}

	public static LegalNote.Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<LegalNote> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected LegalNote makeInstance() { return new LegalNote(); }
		
		public LegalNote.Builder author(String author) {
			requireNonNull(author);
			checkState("Author already set", instance.author==null);
			instance.author = author;
			return this;
		}
		
		public LegalNote.Builder title(String title) {
			requireNonNull(title);
			checkState("Title already set", instance.title==null);
			instance.title = title;
			return this;
		}
		
		public LegalNote.Builder publisher(String publisher) {
			requireNonNull(publisher);
			checkState("Publisher already set", instance.publisher==null);
			instance.publisher = publisher;
			return this;
		}
		
		public LegalNote.Builder year(int year) {
			checkState("Year already set", instance.year==Integer.MIN_VALUE);
			instance.year = year;
			return this;
		}
	}
}