/**
 * 
 */
package de.unistuttgart.xsample.ct;

import java.io.IOException;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.DataInput;
import de.unistuttgart.xsample.util.DataOutput;

/**
 * @author Markus Gärtner
 *
 */
public class PlaintextHandler implements ExcerptHandler {
	
	private String

	/**
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#init(de.unistuttgart.xsample.util.DataInput)
	 */
	@Override
	public void init(DataInput input) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#segments()
	 */
	@Override
	public long segments() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.Fragment[])
	 */
	@Override
	public DataOutput excerpt(Fragment[] fragments) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
