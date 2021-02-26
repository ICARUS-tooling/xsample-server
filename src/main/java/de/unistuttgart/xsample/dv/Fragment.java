/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Models a span of 1-based indices.
 * 
 * @author Markus Gärtner
 *
 */
@Entity
public class Fragment implements Comparable<Fragment> {
	
	public static String encode(Fragment f) {
		if(f.size()==1) {
			return String.valueOf(f.getBeginIndex());
		} 
		
		return String.valueOf(f.getBeginIndex())+"-"+String.valueOf(f.getEndIndex());
	}
	
	public static String encodeAll(List<Fragment> fragments) {
		StringBuilder sb = new StringBuilder(fragments.size()*4);
		for (int i = 0; i < fragments.size(); i++) {
			if(i>0) {
				sb.append(',');
			}
			sb.append(encode(fragments.get(i)));
		}
		return sb.toString();
	}
	
	public static Fragment decode(String s) {
		requireNonNull(s);
		Fragment f;
		int sep = s.indexOf('-');
		if(sep!=-1) {
			f = of(Long.parseUnsignedLong(s.substring(0, sep)), 
					Long.parseUnsignedLong(s.substring(sep+1)));
		} else {
			f = of(Long.parseUnsignedLong(s));
		}
		return f;
	}
	
	public static List<Fragment> decodeAll(String s) {
		return Stream.of(s.split(","))
				.map(Fragment::decode)
				.collect(Collectors.toList());
	}
	
	public static Fragment of(long from, long to) {
		checkArgument(from>0);
		checkArgument(to>0);
		checkArgument(to>=from);
		Fragment f = new Fragment();
		f.setBeginIndex(from);
		f.setEndIndex(to);
		return f;
	}
	
	public static Fragment of(long value) {
		checkArgument(value>0);
		return of(value, value);
	}

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Excerpt excerpt;

	@Column
	private long beginIndex;
	@Column
	private long endIndex;
	
	public long getBeginIndex() { return beginIndex; }
	public void setBeginIndex(long from) { this.beginIndex = from; }
	
	public long getEndIndex() { return endIndex; }
	public void setEndIndex(long to) { this.endIndex = to; }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Excerpt getExcerpt() { return excerpt; }

	public void setExcerpt(Excerpt excerpt) { this.excerpt = excerpt; }

	public LongStream stream() { return LongStream.rangeClosed(beginIndex, endIndex); }
	
	public long size() {
		assert beginIndex>=0 : "invalid beginIndex";
		assert endIndex>=beginIndex : "invalid endIndex"; 
		return endIndex-beginIndex+1; 
	}
	
	@Override
	public int compareTo(Fragment other) {
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
		return Fragment.class.hashCode();
	}

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fragment )) return false;
        return id != null && id.equals(((Fragment) o).getId());
	}
	
	public void detach() { setExcerpt(null); }
}
