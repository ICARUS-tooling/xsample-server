/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
abstract class BuilderBase<T> {
	protected T instance;
	
	protected BuilderBase() {
		instance = requireNonNull(makeInstance());
	}
	
	protected abstract T makeInstance();
	
	protected abstract void validate();
	
	public T build() {
		if(instance==null)
			throw new IllegalStateException("Instance already obtained - can't re-use builder");
		validate();
		T result = instance;
		instance = null;
		return result;
	}
}