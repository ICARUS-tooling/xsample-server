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
package de.unistuttgart.xsample.qe.icarus1.match;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProxyRootMatcher extends Matcher {

	public ProxyRootMatcher() {
		super(new SearchNode(), null);
	}

	@Override
	public boolean matches() {
		if(!matchesExclusions()) {
			if(next!=null) {
				return next.matches();
			} else {
				// In case all the root matchers are negated
//				commit();
				return true;
			}
		} else if(alternate!=null) {
			return alternate.matches();
		} else {
			return false;
		}
	}

	@Override
	public int getAllocation() {
		return -1;
	}

	@Override
	protected boolean matchesNext() {
		return false;
	}

	@Override
	protected boolean matchesConstraints() {
		return false;
	}

	/*@Override
	protected void commit() {
		// no-op
	}*/

	@Override
	public void deallocate() {
		// no-op
	}

	@Override
	protected void allocate() {
		// no-op
	}

}
