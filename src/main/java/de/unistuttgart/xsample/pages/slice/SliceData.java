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
package de.unistuttgart.xsample.pages.slice;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.util.DataBean;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SliceData implements DataBean {

	private static final long serialVersionUID = -5558438814838979800L;

	/** Begin of slice. 1-based. */
	private long begin = -1;
	/** End of slice. 1-based. */
	private long end = -1;
	
	public long getBegin() { return begin; }
	public void setBegin(long begin) { this.begin = begin; }
	
	public long getEnd() { return end; }
	public void setEnd(long end) { this.end = end; }
	
	public long getLength() { return end-begin+1; }
	
	public void reset() {
		begin = end = -1;
	}
	
	public boolean isValid() {
		return begin>=1 && end>=1;
	}
	
	@Override
	public String toString() {
		return String.format("%s@[begin=%d, end=%d]", getClass().getSimpleName(), _long(begin), _long(end));
	}
}
