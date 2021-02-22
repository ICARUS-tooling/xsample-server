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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleManifest implements Serializable {
	
	private static final long serialVersionUID = 2256551725004203579L;

	@Expose
	@SerializedName("target")
	private SourceFile target;

	@Expose
	@SerializedName("description")
	private String description;

	@Expose
	@Nullable
	@SerializedName(value = "metadata", alternate = {"properties"})
	private Map<String, String> metadata;
	
	@Expose
	@Nullable
	@SerializedName("manifests")
	private List<ManifestFile> manifests;
	
	public SourceFile getTarget() { return target; }

	public String getDescription() { return description; }

	public Map<String, String> getMetadata() {
		return metadata==null ? Collections.emptyMap() : new HashMap<>(metadata);
	}

	public List<ManifestFile> getManifests() {
		return manifests==null ? Collections.emptyList() : new ArrayList<>(manifests);
	}
	
	// Helpers
	
	public boolean hasManifests() { return manifests!=null && !manifests.isEmpty(); }
	
	public boolean hasMetadata() { return metadata!=null && !metadata.isEmpty(); }
	
	
	public static Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<XsampleManifest> {
		
		private Builder() { /* no-op */ }

		@Override
		protected XsampleManifest makeInstance() { return new XsampleManifest(); }

		/**
		 * @see de.unistuttgart.xsample.mf.XsampleManifest.BuilderBase#validate()
		 */
		@Override
		protected void validate() {
			checkState("Missing 'target' field", instance.target!=null);
			checkState("Missing 'description' field", instance.description!=null);
		}
		
		public Builder target(SourceFile target) {
			requireNonNull(target);
			checkState("Target already set", instance.target==null);
			instance.target = target;
			return this;
		}
		
		public Builder description(String description) {
			checkNotEmpty(description);
			checkState("Description already set", instance.description==null);
			instance.description = description;
			return this;
		}
		
		private Map<String, String> ensureMetadata() {
			if(instance.metadata==null) {
				instance.metadata = new HashMap<>();
			}
			return instance.metadata;
		}
		
		public Builder metadata(Map<String, String> metadata) {
			requireNonNull(metadata);
			checkArgument("Metadata map is empty", !metadata.isEmpty());
			ensureMetadata().putAll(metadata);
			return this;
		}
		
		public Builder metadata(String key, String value) {
			requireNonNull(key);
			requireNonNull(value);
			ensureMetadata().put(key, value);
			return this;
		}
		
		private List<ManifestFile> ensureManifests() {
			if(instance.manifests==null) {
				instance.manifests = new ArrayList<>();
			}
			return instance.manifests;
		}
		
		public Builder manifests(List<ManifestFile> manifests) {
			requireNonNull(manifests);
			checkArgument("Manifest list is empty", !manifests.isEmpty());
			ensureManifests().addAll(manifests);
			return this;
		}
		
		public Builder manifest(ManifestFile manifest) {
			requireNonNull(manifest);
			ensureManifests().add(manifest);
			return this;
		}
	}

	/**
	 * Models a resource in the dataverse context that can be identified either
	 * via (internal) id or a persistent URI string.
	 * 
	 * @author Markus Gärtner
	 *
	 */
	public static abstract class DataverseFile implements Serializable {

		private static final long serialVersionUID = -5725458746293245542L;

		/** Internal numerical ID used by dataverse */
		@Expose
		@Nullable
		@SerializedName("id")
		private Long id;

		/** Internal persistent identifier used by dataverse */
		@Expose
		@Nullable
		@SerializedName("persistent-id")
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

		protected static abstract class AbstractBuilder<B extends AbstractBuilder<B, F>, F extends DataverseFile> 
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
	
	/**
	 * Models a resource inside the dataverse that can be used to create
	 * excerpts from and that is (normally) protected and not publicly
	 * available. 
	 * 
	 * @author Markus Gärtner
	 *
	 */
	public static class SourceFile extends DataverseFile {
		
		private static final long serialVersionUID = -95555390721328529L;

		/** 
		 * Provides a direct indicator for the segment count in the target file.
		 * If this value is present and smaller than the computed size of the
		 * resource, it will be used instead. 
		 */
		@Expose
		@Nullable
		@SerializedName(value = "segments", alternate = {"size", "elements"})
		private Long segments;
		
		/** Type indicator to designate how the file is to be treated. */
		@Expose
		@SerializedName("source-type")
		private SourceType sourceType;

		@Nullable
		public Long getSegments() { return segments; }

		public SourceType getSourceType() { return sourceType; }

		public static Builder builder() { return new Builder(); }
		
		public static class Builder extends AbstractBuilder<Builder, SourceFile> {
			
			private Builder() { /* no-op */ }

			@Override
			protected SourceFile makeInstance() { return new SourceFile(); }
			
			@Override
			protected void validate() {
				super.validate();
				checkState("Missing 'source-type' field", instance.sourceType!=null);
			}
			
			public Builder segments(long segments) {
				checkState("Segments already set", instance.segments==null);
				instance.segments = Long.valueOf(segments);
				return this;
			}
			
			public Builder sourceType(SourceType sourceType) {
				requireNonNull(sourceType);
				checkState("Source type already set", instance.sourceType==null);
				instance.sourceType = sourceType;
				return this;
			}
		}
	}
	
	public enum SourceType {
		@SerializedName("pdf")
		PDF(".pdf"),
		@SerializedName("epub")
		EPUB(".epub"),
		@SerializedName("plain-text")
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
	
	/**
	 * Models a third-party manifest file associated with a particular
	 * {@link SourceFile} and that is used to guide/enable the excerpt
	 * creation process based on the content of that source file or
	 * external annotations. 
	 */
	public static class ManifestFile extends DataverseFile {
		
		private static final long serialVersionUID = -4467949132014774288L;
		
		/** Type indicator for the manifest. Currently we only support {@link ManifestType#ICARUS2}! */
		@Expose
		@SerializedName("manifest-type")
		private ManifestType manifestType = ManifestType.ICARUS2;

		public ManifestType getManifestType() { return manifestType; }

		public static Builder builder() { return new Builder(); }

		public static class Builder extends AbstractBuilder<Builder, ManifestFile> {
			
			private Builder() { /* no-op */ }
		
			@Override
			protected ManifestFile makeInstance() { return new ManifestFile(); }
			
			@Override
			protected void validate() {
				super.validate();
				checkState("Missing 'manifest-type' field", instance.manifestType!=null);
			}
			
			public Builder manifestType(ManifestType manifestType) {
				requireNonNull(manifestType);
				checkState("Manifest type already set", instance.manifestType==null);
				instance.manifestType = manifestType;
				return this;
			}
		}
	}
	
	public enum ManifestType {
		@SerializedName("icarus-2")
		ICARUS2,
		;
	}
	
	@NotThreadSafe
	public static abstract class BuilderBase<T> {
		protected T instance;
		
		protected BuilderBase() {
			instance = requireNonNull(makeInstance());
		}
		
		protected abstract T makeInstance();
		
		protected abstract void validate();
		
		public T build() {
			if(instance==null)
				throw new IllegalStateException("Instance already obtained - can't re-use builder");
			validate();
			T result = instance;
			instance = null;
			return result;
		}
	}
}
