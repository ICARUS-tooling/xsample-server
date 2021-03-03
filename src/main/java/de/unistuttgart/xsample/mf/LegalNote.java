/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LegalNote implements Serializable {

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

	public String getAuthor() { return author; }

	public String getTitle() { return title; }

	public String getPublisher() { return publisher; }

	public static LegalNote.Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<LegalNote> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected LegalNote makeInstance() { return new LegalNote(); }
		
		@Override
		protected void validate() {
			checkState("Missing 'author' field", instance.author!=null);
			checkState("Missing 'title' field", instance.title!=null);
			checkState("Missing 'publisher' field", instance.publisher!=null);
		}
		
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
	}
}