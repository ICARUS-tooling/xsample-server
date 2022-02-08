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
package de.unistuttgart.xsample;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
@Deprecated
public abstract class Task {

	private final List<FacesMessage> messages = new ArrayList<>();
	private AtomicInteger progress = new AtomicInteger();
	private String label = "...";
	private Throwable error;
	
	private final Object lock = new Object();
	
	private volatile AtomicReference<Status> status = new AtomicReference<Task.Status>(Status.BLANK);
	private volatile boolean cancelRequested, done;
	private volatile Thread thread;
	
	protected void addMessage(FacesMessage message) {
		synchronized (lock) {
			messages.add(requireNonNull(message));
		} 
	}
	
	protected void addMessage(Severity severity, String key, Object...args) {
		String text = BundleUtil.format(key, args);
		FacesMessage message = new FacesMessage(severity, text, null);
		addMessage(message);
	}
	
	public boolean hasMessages() {  
		synchronized (lock) {
			return !messages.isEmpty();
		} 
	}
	
	public void consumeMessages(Consumer<? super FacesMessage> action) {
		synchronized (lock) {
			messages.forEach(action);
			messages.clear();
		}
	}
	
	protected abstract boolean doBackground();
	
	public final void execute() {
		if(!status.compareAndSet(Status.BLANK, Status.RUNNING))
			throw new UnsupportedOperationException("Cannot execute the same task twice!");
		
		boolean result = false;
		try {
			thread = Thread.currentThread();
			
			result = doBackground();
			
			done = true;
		} catch(Throwable t) {
			error = t;
		} finally {
			thread = null;
			Status endStatus = result ? Status.SUCCESS : Status.FAILED;
			status.compareAndSet(Status.RUNNING, endStatus);
		}
	}
	
	public final void cancel(boolean mayInterruptIfRunning) {
		if(status.compareAndSet(Status.RUNNING, Status.CANCELED)) {
			cancelRequested = true;
			
			if(mayInterruptIfRunning) {
				thread.interrupt();
			}
		}
	}
	
	public boolean isCanceled() { return status.get()==Status.CANCELED; }
	public boolean isDone() { return done; }
	public boolean isFailed() { return status.get()==Status.FAILED; }
	public boolean isActive() { return status.get()==Status.RUNNING; }
	
	enum Status {
		BLANK,
		RUNNING,
		FAILED,
		CANCELED,
		SUCCESS,
		;
	}
}
