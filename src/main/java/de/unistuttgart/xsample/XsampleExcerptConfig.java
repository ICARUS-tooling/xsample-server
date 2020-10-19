/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;

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
	
	private int start = 1, end = 15;
	private int segments = 200;
	
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
	
	public int getPercent() {
		return (int) Math.ceil((end-start+1.0) / segments * 100.0);
	}
	
	public int getRange() {
		return end-start+1;
	}
}
