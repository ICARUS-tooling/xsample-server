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