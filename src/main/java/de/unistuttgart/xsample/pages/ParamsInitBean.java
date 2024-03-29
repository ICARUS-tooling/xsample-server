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
package de.unistuttgart.xsample.pages;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.XsampleSession;
import de.unistuttgart.xsample.pages.shared.InputData;

/**
 * Helper bean to store relevant GET parameters in our {@link InputData} bean.
 * We use this instead of the JSF {@code f:viewParam} tags since the actual parameter
 * names are customizable for the XSample instance. 
 * 
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class ParamsInitBean {

	@Inject
	InputData inputData;
	
	@Inject
	XsampleSession session;
	
	@Inject
	XsampleServices xsampleServices;
	
	/**
	 * Fetch original URL parameters and store in our data bean.
	 */
	public void storeParams() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		
		Optional.of(xsampleServices.getSetting(Key.SourceDataverseParam))
			.map(params::get)
			.ifPresent(inputData::setSite);
		Optional.of(xsampleServices.getSetting(Key.SourceFileParam))
			.map(params::get)
			.map(Long::valueOf)
			.ifPresent(inputData::setFile);
		Optional.of(xsampleServices.getSetting(Key.ApiKeyParam))
			.map(params::get)
			.ifPresent(inputData::setKey);
		
		// Toggle or set debug status
		Optional.ofNullable(params.get("debug"))
			.map(Boolean::valueOf)
			.ifPresent(session::setDebug);
	}
}
