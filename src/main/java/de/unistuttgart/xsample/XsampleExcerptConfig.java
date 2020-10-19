/**
 * 
 */
package de.unistuttgart.xsample;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.unistuttgart.xsample.util.Property;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleExcerptConfig implements Serializable {

	private static final long serialVersionUID = -3897121271961387495L;
	
	/** The file id as supplied by the external-tools URL */
	private Long file;	
	/** The user's API key as supplied by the external-tools URL */
	private String key;
	/** The source URL of the dataverse the request originated from */
	private String site;
	
	/** Display name of the source file */
	private String title;
	/** MIME type of the source file */
	private String contentType;
	/** Character encoding used for source file */
	private String encoding;
	/** Binary form of the source file */
	private byte[] data;
	/** Total number of segments available in source file */
	private int segments;
	
	/** Begin of user defined excerpt */
	private int start;
	/** End of user defined excerpt */
	private int end;
	
	
	public Long getFile() { return file; }
	public void setFile(Long file) { this.file = file; }
	
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	public String getSite() { return site; }
	public void setSite(String site) { this.site = site; }
	
	public int getStart() { return start; }
	public void setStart(int excerptStart) { this.start = excerptStart; }

	public int getEnd() { return end; }
	public void setEnd(int excerptEnd) { this.end = excerptEnd; }
	
	public int getSegments() { return segments; }
	public byte[] getData() { return data; }
	public String getContentType() { return contentType; }
	public String getEncoding() { return encoding; }
	
	// Bulk modifications
	
	public void setFileData(String title, String contentType, String encoding, byte[] data, int segments) {
		this.title = requireNonNull(title, "missing title");
		this.contentType = requireNonNull(contentType, "missing content type");
		this.encoding = requireNonNull(encoding, "missing encoding");
		this.data = requireNonNull(data, "missing data");
		this.segments = segments;
	}
	
	public void resetFileData() {
		title = null;
		contentType = null;
		encoding = null;
		data = null;
		segments = 0;
	}
	
	// Utility methods
	
	public int getPercent() {
		return (int) Math.ceil((end-start+1.0) / segments * 100.0);
	}
	
	public int getRange() { return end-start+1; }
	
	public boolean isHasFile() { return data!=null; }


	private DecimalFormat decimalFormat = new DecimalFormat("#,###");

	private String formatDecimal(long value) {
		return decimalFormat.format(value);
	}
	
	public List<Property> getProperties() {
		if(!isHasFile()) {
			return Collections.emptyList();
		}
		
		List<Property> props = new ArrayList<>();
		props.add(new Property("Title", title));
		props.add(new Property("Content Type", contentType));
		props.add(new Property("Character Encoding", encoding));
		props.add(new Property("Segments", formatDecimal(segments)));
		props.add(new Property("Size", formatDecimal(data.length/1024)+"KB"));
		return props;
	}
}
