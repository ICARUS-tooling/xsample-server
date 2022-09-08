/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class PartData extends EncodedCorpusData {

	private static final long serialVersionUID = -4223925752620450354L;
	
	private long offset = 0;

	public long getOffset() { return offset; }
	public void setOffset(long offset) { this.offset = offset; }
	
	@Override
	public void reset() {
		super.reset();
		
		offset = 0;
	}
}
