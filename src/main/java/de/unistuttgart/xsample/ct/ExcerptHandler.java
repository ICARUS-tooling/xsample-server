/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package de.unistuttgart.xsample.ct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.FileInfo;
 
/**
 * @author Markus Gärtner
 *
 */
public interface ExcerptHandler {

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
	void analyze(FileInfo file, InputStream in) throws IOException, UnsupportedContentTypeException, EmptyResourceException;
	
	/**
	 * Splits the underlying resource based on the specified fragments and stores
	 * the result in the given {@link DataOutput output}.
	 * 
	 * @param fragments specification of the actual excerpt to generate
	 * @param output the destination for the excerpt generation
	 *  yet or failed in some way.
	 * @throws IOException
	 */
	void excerpt(FileInfo file, InputStream in, Fragment[] fragments, OutputStream out) throws IOException;
	
	//TODO add methods for fetching localized strings related to segment names etc
	
	String getSegmentLabel();
}
