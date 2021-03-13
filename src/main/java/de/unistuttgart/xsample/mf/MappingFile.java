/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Models a mapping file that allows translation from the designated
 * primary layer of a ICARUS2 manifest file to the basic segments
 * used for excerpt generation in a corpus. 
 */
public class MappingFile extends DataverseFile {
	
	private static final long serialVersionUID = 3254397727391866951L;

	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"mappingFile";
	
	/** Format identifier or pattern expression. */
	@Expose
	@SerializedName(XsampleManifest.NS+"format")
	private String format;
	
	/** Type indicator for the mapping. Currently we only support {@link MappingType#TABULAR}! */
	@Expose
	@SerializedName(XsampleManifest.NS+"mappingType")
	private MappingType mappingType;

	/** Offset to be applied to source indices in the mapping. */
	@Expose
	@SerializedName(XsampleManifest.NS+"sourceOffset")
	@Nullable
	private Long sourceOffset;

	/** Offset to be applied to target indices in the mapping. */
	@Expose
	@SerializedName(XsampleManifest.NS+"targetOffset")
	@Nullable
	private Long targetOffset;

	public String getFormat() { return format; }
	public MappingType getMappingType() { return mappingType; }
	public long getSourceOffset() { return sourceOffset==null ? 0L : sourceOffset.longValue(); }
	public long getTargetOffset() { return targetOffset==null ? 0L : targetOffset.longValue(); }
	
	@Override
	public void validate() {
		super.validate();
		checkState("Missing 'format' field", format!=null);
		checkState("Missing 'mappingType' field", mappingType!=null);
	}

	public static MappingFile.Builder builder() { return new Builder(); }

	public static class Builder extends AbstractBuilder<MappingFile.Builder, MappingFile> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected MappingFile makeInstance() { return new MappingFile(); }
		
		public MappingFile.Builder format(String format) {
			checkNotEmpty(format);
			checkState("Format already set", instance.format==null);
			instance.format = format;
			return this;
		}
		
		public MappingFile.Builder mappingType(MappingType mappingType) {
			requireNonNull(mappingType);
			checkState("Format already set", instance.mappingType==null);
			instance.mappingType = mappingType;
			return this;
		}
		
		public MappingFile.Builder sourceOffset(long sourceOffset) {
			checkArgument("Offset must not be negative", sourceOffset>=0);
			checkState("Source offset already set", instance.sourceOffset==null);
			instance.sourceOffset = Long.valueOf(sourceOffset);
			return this;
		}
		
		public MappingFile.Builder targetOffset(long targetOffset) {
			checkArgument("Offset must not be negative", targetOffset>=0);
			checkState("Target offset already set", instance.targetOffset==null);
			instance.targetOffset = Long.valueOf(targetOffset);
			return this;
		}
	}
}