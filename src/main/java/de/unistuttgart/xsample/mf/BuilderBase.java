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
package de.unistuttgart.xsample.mf;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.NotThreadSafe;

import de.unistuttgart.xsample.util.SelfValidating;

@NotThreadSafe
abstract class BuilderBase<T extends SelfValidating> {
	protected T instance;
	
	protected BuilderBase() {
		instance = requireNonNull(makeInstance());
	}
	
	protected abstract T makeInstance();
	
	public T build() {
		if(instance==null)
			throw new IllegalStateException("Instance already obtained - can't re-use builder");
		instance.validate();
		T result = instance;
		instance = null;
		return result;
	}
}