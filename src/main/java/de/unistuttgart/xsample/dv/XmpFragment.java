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
package de.unistuttgart.xsample.dv;

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;

import java.util.stream.LongStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
 
/**
 * Models a span of 1-based indices.
 * 
 * @author Markus Gärtner
 *
 */
@Entity
@Table(name = "Fragment")
public class XmpFragment implements Comparable<XmpFragment> {
	
	public static XmpFragment of(long from, long to) {
		checkArgument("'from' must be greater than 0", from>0);
		checkArgument("'tro' must be greater than 0", to>0);
		checkArgument("'to' must be greater or equal to 'from'", to>=from);
		XmpFragment f = new XmpFragment();
		f.setBeginIndex(from);
		f.setEndIndex(to);
		return f;
	}
	
	public static XmpFragment of(long value) {
		checkArgument(value>0);
		return of(value, value);
	}

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private XmpExcerpt excerpt;

	@Column
	private long beginIndex;
	@Column
	private long endIndex;
	
	/** Return 1-based begin index */
	public long getBeginIndex() { return beginIndex; }
	public void setBeginIndex(long from) { this.beginIndex = from; }

	/** Return 1-based end index */
	public long getEndIndex() { return endIndex; }
	public void setEndIndex(long to) { this.endIndex = to; }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public XmpExcerpt getExcerpt() { return excerpt; }

	public void setExcerpt(XmpExcerpt xmpExcerpt) { this.excerpt = xmpExcerpt; }

	public LongStream stream() { return LongStream.rangeClosed(beginIndex, endIndex); }
	
	public long size() {
		assert beginIndex>=0 : "invalid beginIndex";
		assert endIndex>=beginIndex : "invalid endIndex"; 
		return endIndex-beginIndex+1; 
	}
	
	@Override
	public int compareTo(XmpFragment other) {
		long res = beginIndex-other.beginIndex;
		if(res==0L) {
			res = endIndex-other.endIndex;
		}
		return (int)res;
	}
	
	@Override
	public String toString() { return "["+beginIndex+","+endIndex+"]"; }

	@Override
	public int hashCode() {
		return XmpFragment.class.hashCode();
	}

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XmpFragment )) return false;
        return id != null && id.equals(((XmpFragment) o).getId());
	}
	
	public void detach() { setExcerpt(null); }
	
	/** Check if this fragment completely covers the other one */
	public boolean contains(XmpFragment other) {
		return beginIndex<=other.beginIndex && endIndex>=other.endIndex;
	}
	
	/** Check if this fragment contains the given value */
	public boolean contains(long value) {
		return beginIndex<=value && endIndex>=value;
	}
	
	/** 
	 * Tries to append the given value if it is adjacent to current 
	 * end index and returns true if so. 
	 */
	public boolean append(long value) {
		if(value==endIndex+1) {
			endIndex = value;
			return true;
		}
		
		return false;
	}
}
