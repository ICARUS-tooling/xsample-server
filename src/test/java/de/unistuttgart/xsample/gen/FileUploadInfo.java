/**
 * 
 */
package de.unistuttgart.xsample.gen;

class FileUploadInfo {
	/**
	 * <pre>
	 * {
		  "description": "My description.",
		  "directoryLabel": "data/subdir1",
		  "categories": [
		    "Data"
		  ],
		  "restrict": "false"
		}
	 * </pre>
	 */
	private String description;
	private String directoryLabel;
	private String mimeType;
	private String fileName;
	private boolean restricted;
	private String[] categories;
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getDirectoryLabel() { return directoryLabel; }
	public void setDirectoryLabel(String directoryLabel) { this.directoryLabel = directoryLabel; }
	public String getMimeType() { return mimeType; }
	public void setMimeType(String mimeType) { this.mimeType = mimeType; }
	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }
	public boolean isRestricted() { return restricted; }
	public void setRestricted(boolean restricted) { this.restricted = restricted; }
	public String[] getCategories() { return categories; }
	public void setCategories(String[] categories) { this.categories = categories; }
}