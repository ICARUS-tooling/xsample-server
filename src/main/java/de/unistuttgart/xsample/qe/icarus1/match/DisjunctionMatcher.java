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
package de.unistuttgart.xsample.qe.icarus1.match;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DisjunctionMatcher extends Matcher {

	public DisjunctionMatcher(SearchNode node, SearchEdge edge) {
		super(node, edge);
	}

	/**
	 * Returns {@code true} if at least on of the
	 * {@code Matcher} instances registered as exclusions
	 * does {@code not} return a successful match or if there
	 * are no matchers registered as exclusion.
	 */
	@Override
	protected boolean matchesExclusions() {
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				if(!matcher.matches()) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean matchesType() {
		return true;
	}

	@Override
	protected boolean matchesConstraints() {
		return true;
	}

	@Override
	public void deallocate() {
		// no-op
	}

	@Override
	protected void allocate() {
		// no-op
	}

	@Override
	public int getAllocation() {
		return parent==null ? -1 : parent.getAllocation();
	}

	@Override
	public boolean matches() {
		
		boolean matched = false;
		
		// Check exclusions
		matched = matchesExclusions();
		
		if(matched) {
			matched = matchesNext();
		}
		
		if(options!=null && (!matched || exhaustive)) {
			for(Matcher option : options) {
				matched |= option.matches();
				
				if(matched && !exhaustive) {
					break;
				}
			}
		}
			
		return matched;
	}
}
