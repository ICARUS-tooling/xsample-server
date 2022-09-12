/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.mp;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public interface Mapping extends Serializable {
	
	public void load(Reader reader) throws IOException;

//	/** Map a source index to 1 or more target indices. The return value is the number of target
//	 * indices stored in the {@code buffer} argument. If the return value is {@code -1} the buffer
//	 * did not hold enough space to store all the indices.  */
//	public int map(long sourceIndex, long[] buffer);
	
	public long getTargetBegin(long sourceIndex);
	public long getTargetEnd(long sourceIndex);
}
