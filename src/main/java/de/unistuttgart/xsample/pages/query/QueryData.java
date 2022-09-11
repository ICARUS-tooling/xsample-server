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
package de.unistuttgart.xsample.pages.query;

import static de.unistuttgart.xsample.util.XSampleUtils._boolean;
import static de.unistuttgart.xsample.util.XSampleUtils._int;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.util.DataBean;

/**
 * Input data for the query process. That is, query payload and configuration data.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class QueryData implements DataBean {

	private static final long serialVersionUID = -3741300128814073907L;
	
	/** The raw query as defined by the user */
	private String query = "[form=Der]";
	
	private boolean caseSensitive = true;
	private int limit = 0;
	
	public String getQuery() { return query; }
	public void setQuery(String selectedCorpus) { this.query = selectedCorpus; }
	
	@Override
	public String toString() {
		return String.format("%s@[query=%s, caseSensitive=%b, limit=%d]", getClass().getSimpleName(),
				query, _boolean(caseSensitive), _int(limit));
	}
}
