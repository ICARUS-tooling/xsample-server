/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusInfo {

	private final String title;
	private final String corpusId;
	
	public CorpusInfo(String title, String corpusId) {
		this.title = title;
		this.corpusId = corpusId;
	}
	
	public String getTitle() { return title; }
	public String getCorpusId() { return corpusId; }	
}
