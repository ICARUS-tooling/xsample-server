/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Models a resource in the dataverse context that can be identified either
 * via (internal) id or a persistent URI string.
 * 
 * @author Markus Gärtner
 *
 */
public abstract class DataverseFile implements Serializable { 

	private static final long serialVersionUID = -5725458746293245542L;

	/** Internal numerical ID used by dataverse */
	@Expose
	@Nullable
	@SerializedName(XsampleManifest.NS+"id")
	private Long id;

	/** Internal persistent identifier used by dataverse */
	@Expose
	@Nullable
	@SerializedName(XsampleManifest.NS+"persistentId")
	private String persistentId;

	/** 
	 * Optional label for identification within the XSample manifest. 
	 * Required if the resource is referenced from another section inthe
	 * manifest!
	 */
	@Expose
	@Nullable
	private String label;

	@Nullable
	public Long getId() { return id; }

	@Nullable
	public String getPersistentId() { return persistentId; }

	@Nullable
	public String getLabel() { return label; }

	protected static abstract class AbstractBuilder<B extends DataverseFile.AbstractBuilder<B, F>, F extends DataverseFile> 
		extends BuilderBase<F> {
		
		@Override
		protected void validate() {
			DataverseFile file = instance;
			checkState("Must define either id or persietnt-id", file.id!=null || file.persistentId!=null);
		}
		
		@SuppressWarnings("unchecked")
		protected B thisAsCast() { return (B) this; }
		
		public B id(long id) {
			DataverseFile file = instance;
			checkState("ID already set", file.id==null);
			file.id = Long.valueOf(id);
			return thisAsCast();
		}
		
		public B persistentId(String persistentId) {
			requireNonNull(persistentId);
			DataverseFile file = instance;
			checkState("Persistent ID already set", file.persistentId==null);
			file.persistentId = persistentId;
			return thisAsCast();
		}
		
		public B label(String label) {
			requireNonNull(label);
			DataverseFile file = instance;
			checkState("Label already set", file.label==null);
			file.label = label;
			return thisAsCast();
		}
	}
}