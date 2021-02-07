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

import java.util.stream.LongStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Markus Gärtner
 *
 */
@Entity
public class Fragment {
	
	public static Fragment of(long from, long to) {
		Fragment f = new Fragment();
		f.setFrom(from);
		f.setTo(to);
		return f;
	}
	
	public static Fragment of(long value) {
		return of(value, value);
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private long from;
	@Column
	private long to;
	
	public long getFrom() { return from; }
	public void setFrom(long from) { this.from = from; }
	
	public long getTo() { return to; }
	public void setTo(long to) { this.to = to; }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public LongStream stream() { return LongStream.rangeClosed(from, to); }
	
	public long size() {
		assert from>=0 : "invalid begin";
		assert to>=from : "invalid end"; 
		return to-from+1; 
	}
	
	@Override
	public String toString() { return "["+from+","+to+"]"; }
	
}
