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
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.welcome.WelcomePage;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.DataBean;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class WorkflowData implements DataBean {

	private static final long serialVersionUID = -2848543491521080449L;
	
	private static final Logger logger = Logger.getLogger(WorkflowData.class.getCanonicalName());

	/** Status of the analysis process */
	private Status status = Status.LOADING;
	
	/** Content page to load. Initially set to {@link WelcomePage#PAGE}. */
	private String page = WelcomePage.PAGE;
	
	private final Stack<String> history = new ObjectArrayList<>(); 

	public String getPage() { return page; }
	public boolean forward(String page) { 
		requireNonNull(page);
		
		logger.log(Level.FINE, String.format("Navigating from '%s' to '%s'", this.page, page));
		
		if(this.page.equals(page)) {
			return false;
		}
		
		history.push(this.page);
		this.page = page;
		
		return true;
	}	
	
	public boolean back() {
		if(history.isEmpty()) {
			return false;
		}
		
		String currentPage = page;
		page = history.pop();
		logger.log(Level.FINE, String.format("Navigating back from '%s' to '%s'", currentPage, page));
		return true;
	}

	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = requireNonNull(status); }
	
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
}
