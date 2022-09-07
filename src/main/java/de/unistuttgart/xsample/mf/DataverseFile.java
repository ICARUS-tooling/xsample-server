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

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.SelfValidating;

/**
 * Models a resource in the dataverse context that can be identified either
 * via (internal) id or a persistent URI string.
 * 
 * @author Markus Gärtner
 *
 */
public abstract class DataverseFile implements Serializable, SelfValidating { 

	private static final long serialVersionUID = -5725458746293245542L;

	/** Internal numerical ID used by dataverse */
	@Expose
	@Nullable
	@SerializedName(XsampleManifest.NS+"id")
	private Long id;
	
	//TODO cleanup dead code once the spec is stable enough

//	/** Internal persistent identifier used by dataverse */
//	@Expose
//	@Nullable
//	@SerializedName(XsampleManifest.NS+"persistentId")
//	private String persistentId;

	/** 
	 * Optional label for identification within the XSample manifest. 
	 * Required if the resource is referenced from another section in the
	 * manifest!
	 */
	@Expose
	@Nullable
	@SerializedName(XsampleManifest.NS+"label")
	private String label;

//	@Nullable
	public Long getId() { return id; }

//	@Nullable
//	public String getPersistentId() { return persistentId; }

	@Nullable
	public String getLabel() { return label; }
	
	@Override
	public void validate() {
//		checkState("Missing 'label' field", label!=null);
		checkState("Missing 'id' field", id!=null);
//		checkState("Must define either id or persistent-id", id!=null || persistentId!=null);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+(label==null ? "no-label" : label);
	}

	protected static abstract class AbstractBuilder<B extends DataverseFile.AbstractBuilder<B, F>, F extends DataverseFile> 
		extends BuilderBase<F> {
		
		@SuppressWarnings("unchecked")
		protected B thisAsCast() { return (B) this; }
		
		public B id(long id) {
			DataverseFile file = instance;
			checkState("ID already set", file.id==null);
			file.id = Long.valueOf(id);
			return thisAsCast();
		}
		
//		public B persistentId(String persistentId) {
//			requireNonNull(persistentId);
//			DataverseFile file = instance;
//			checkState("Persistent ID already set", file.persistentId==null);
//			file.persistentId = persistentId;
//			return thisAsCast();
//		}
		
		public B label(String label) {
			requireNonNull(label);
			DataverseFile file = instance;
			checkState("Label already set", file.label==null);
			file.label = label;
			return thisAsCast();
		}
	}
}