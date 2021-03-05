/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Models a third-party manifest file associated with a particular
 * {@link SourceFile} and that is used to guide/enable the excerpt
 * creation process based on the content of that source file or
 * external annotations. 
 */
public class ManifestFile extends DataverseFile {
	
	private static final long serialVersionUID = -4467949132014774288L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"manifestFile";
	
	/** Type indicator for the manifest. Currently we only support {@link ManifestType#IMF}! */
	@Expose
	@SerializedName(XsampleManifest.NS+"manifestType")
	private ManifestType manifestType = ManifestType.IMF;

	public ManifestType getManifestType() { return manifestType; }
	
	@Override
	public void validate() {
		super.validate();
		checkState("Missing 'manifest-type' field", manifestType!=null);
	}

	public static ManifestFile.Builder builder() { return new Builder(); }

	public static class Builder extends AbstractBuilder<ManifestFile.Builder, ManifestFile> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected ManifestFile makeInstance() { return new ManifestFile(); }
		
		public ManifestFile.Builder manifestType(ManifestType manifestType) {
			requireNonNull(manifestType);
			checkState("Manifest type already set", instance.manifestType==null);
			instance.manifestType = manifestType;
			return this;
		}
	}
}