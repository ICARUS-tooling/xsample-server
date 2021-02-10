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
package de.unistuttgart.xsample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleWorkflow.Flag;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.Property;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class WelcomePage {
	
	public static final String PAGE = "welcome";
	
	private static final Logger logger = Logger.getLogger(WelcomePage.class.getCanonicalName());
	
	@Inject
	XsampleWorkflow workflow;
	
	@Inject
	XsampleExcerptData excerptData;
	
	/** Returns text for current status or empty string */
	public String getStatusInfo() {
		if(isShowOutline()) {
			return "";
		}
		
		return workflow.getStatus().getLabel();
	}
	
	/** Indicate that the outline for valid files should be shown */
	public boolean isShowOutline() {
		return workflow.getStatus().isFlagSet(Flag.FILE_VALID);
	}
	
	public boolean isHasAnnotations() {
		return isShowOutline() && !excerptData.getInputType().isRaw();
	}
	
	/** Produce table data for current main file */
	public List<Property> getFileProperties() {
		if(!isShowOutline()) {
			return Collections.emptyList();
		}
		
		List<Property> properties = new ArrayList<>();
		
		InputType inputType = excerptData.getInputType();
		
		properties.add(new Property("type", inputType.name()));
		
		FileInfo info = excerptData.getFileInfo();
		
		properties.add(new Property("name", info.getTitle()));
		properties.add(new Property("content-type", info.getContentType()));
		properties.add(new Property("encoding", info.getEncoding().displayName()));
		properties.add(new Property("size", String.valueOf(info.getSize())));
		properties.add(new Property("segments", String.valueOf(info.getSegments())));
		
		return properties;
	}
	
	/** Callback for button to continue workflow */
	public void onContinue() {

		String page = null;
		ExcerptType excerptType = excerptData.getExcerptType();
		switch (excerptType) {
		case STATIC: {
			page = DownloadPage.PAGE;
			excerptData.setExcerpt(Arrays.asList(Fragment.of(0, 14)));
		} break;
		case SLICE: page = SlicePage.PAGE; break;
		case QUERY: page = QueryPage.PAGE; break;
		default:
			break;
		}
		
		if(page==null) {
			String text = BundleUtil.format("welcome.msg.unknownPage", excerptType);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
			FacesContext.getCurrentInstance().addMessage("navMsg", msg);
			return;
		}

		//TODO if source is too small, delegate to full download
		
		logger.fine("Navigating to subpage "+page);
		
		workflow.setPage(page);
	}
}
