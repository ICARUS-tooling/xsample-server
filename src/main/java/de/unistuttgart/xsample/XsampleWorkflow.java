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
package de.unistuttgart.xsample;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ejb.Asynchronous;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.transaction.Transactional;

import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleWorkflow implements Serializable {

	private static final long serialVersionUID = -2848543491521080449L;
	
	private static final Logger logger = Logger.getLogger(XsampleWorkflow.class.getCanonicalName());

	/** Status of the analysis process */
	private Status status = Status.LOADING;
	
	private String page = "welcome";

	public String getPage() { return page; }
	public void setPage(String page) { this.page = requireNonNull(page); }	

	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = requireNonNull(status); }
	
	private Task task;
	
	@Asynchronous
	@Transactional
	public void execute(Task task) {
		if(hasTask())
			throw new UnsupportedOperationException("Already got a taks pending");
		
		this.task = requireNonNull(task);
		
		task.execute();
	}
	
	public <T> void cleanupTask(Class<T> type, @Nullable Consumer<? super T> action) {
		if(!hasTask())
			throw new UnsupportedOperationException("No pending task to clean up");
		
		Task task = this.task;
		
		if(!type.isInstance(task))
			throw new IllegalStateException(String.format(
					"Type mismatch for current task: expected '%s' - but got '%s'", 
					type.getName(), task.getClass().getName()));
		
		this.task = DUMMY_TASK;
		
		if(action!=null) {
			action.accept(type.cast(task));
		}
	}
	
	public void syncTask() {
		if(hasTask()) {
			FacesContext fc = FacesContext.getCurrentInstance();
			task.consumeMessages(m -> fc.addMessage("msg", m));
		}
	}
	
	public boolean hasTask() { return task!=DUMMY_TASK; }
	public boolean isTaskActive() { return task.isActive(); }
	
	public enum Flag {
		FILE_VALID,
		LOADING,
		ERROR,
		;
	}
	
	public enum Status {
		LOADING("workflow.status.loadTarget", Flag.LOADING),
		INTERNAL_ERROR("workflow.status.internalError", Flag.ERROR),
		FILE_VALID("workflow.status.success", Flag.FILE_VALID),
		FILE_INVALID("workflow.status.failed", Flag.ERROR),
		
		;
		
		private Status(String key, Flag...flags) { 
			this.key = requireNonNull(key);
			this.flags = EnumSet.noneOf(Flag.class);
			Collections.addAll(this.flags, flags);
		}
		
		private final String key;
		private final EnumSet<Flag> flags;
		
		public String getLabel() { return BundleUtil.get(key); }
		
		public boolean isFlagSet(Flag flag) { return flags.contains(flag); }
	}
	
	private static final Task DUMMY_TASK = new Task() {

		@Override
		protected boolean doBackground() { throw new UnsupportedOperationException("DUMMY TASK"); }
	};
}
