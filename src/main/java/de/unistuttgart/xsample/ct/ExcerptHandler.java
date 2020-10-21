/**
 * 
 */
package de.unistuttgart.xsample.ct;

import java.io.Closeable;
import java.io.IOException;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.DataInput;
import de.unistuttgart.xsample.util.DataOutput;

/**
 * @author Markus Gärtner
 *
 */
public interface ExcerptHandler extends Closeable {

	/**
	 * Initialize the handler with the given {@link DataInput input}.
	 * 
	 * @param source
	 * @return
	 * @throws IOException if an underlying I/O issue prevented the initialization
	 * @throws UnsupportedContentTypeException if the data source's {@link DataInput#contentType()}
	 * is not supported by this handler
	 * @throws EmptyResourceException if the data source represents an empty file
	 */
	void init(DataInput input) throws IOException, UnsupportedContentTypeException, EmptyResourceException;
	
	/** 
	 * Returns the number of segments available in the underlying resource.
	 * 
	 * @throws IllegalStateException if {@link #init(DataInput)} hasn't been called
	 *  yet or failed in some way.
	 */
	long segments();
	
	/**
	 * Splits the underlying resource based on the specified fragments and stores
	 * the result in the given {@link DataOutput output}.
	 * 
	 * @param fragments specification of the actual excerpt to generate
	 * @param output the destination for the excerpt generation
	 * @throws IllegalStateException if {@link #init(DataInput)} hasn't been called
	 *  yet or failed in some way.
	 * @throws IOException
	 */
	DataOutput excerpt(Fragment[] fragments) throws IOException;
	
	
	//TODO add methods for fetching localized strings related to segment names etc
}
