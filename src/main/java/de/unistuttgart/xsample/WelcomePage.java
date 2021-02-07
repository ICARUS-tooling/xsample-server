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
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleWorkflow.Flag;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.util.Property;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class WelcomePage {
	
	@Inject
	XsampleWorkflow workflow;
	
	@Inject
	XsampleExcerptInfo excerptInfo;
	
	public String getStatusInfo() {
		if(isShowOutline()) {
			return "";
		}
		
		return workflow.getStatus().getLabel();
	}
	
	public boolean isShowOutline() {
		return workflow.getStatus().isFlagSet(Flag.FILE_VALID);
	}
	
	public List<Property> getFileProperties() {
		if(!isShowOutline()) {
			return Collections.emptyList();
		}
		
		List<Property> properties = new ArrayList<>();
		
		InputType inputType = excerptInfo.getInputType();
		
		properties.add(new Property("type", inputType.name()));
		
		FileInfo info = excerptInfo.getFileInfo();
		
		properties.add(new Property("name", info.getTitle()));
		properties.add(new Property("content-type", info.getContentType()));
		properties.add(new Property("encoding", info.getEncoding().displayName()));
		properties.add(new Property("size", String.valueOf(info.getSize())));
		properties.add(new Property("segments", String.valueOf(info.getSegments())));
		
		return properties;
	}
	
	public void onContinue() {
		
	}
}
