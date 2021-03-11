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
package de.unistuttgart.xsample.pages.query;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.XsamplePage;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class QueryPage extends XsamplePage {
	
	public static final String PAGE = "query";
	
	@Inject
	XsampleQueryData queryData;
	
	@Inject
	QueryView view;
	
	@Inject
	QueryEngine queryEngine;

	public void init() {
		initQuota(queryData);
		//TODO further initialize query data
	}
	
	/** Callback for button to run ICARUS2 query */
	public void runQuery() {
		String rawQuery = view.getQuery();
	}

	/** Callback for button to continue workflow */
	public void next() {
		
	}
}
