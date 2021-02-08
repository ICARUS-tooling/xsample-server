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
package de.unistuttgart.xsample.dep;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.context.FacesContext;

import de.unistuttgart.xsample.dv.DataverseUser;
import de.unistuttgart.xsample.dv.Resource;

/**
 * @author Markus Gärtner
 *
 */
//@Named
//@ViewScoped
@Deprecated
public class XsampleData implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;

	private DataverseUser dataverseUser;
	private Resource resource;
    
    private XsampleConfig config = new XsampleConfig();
    
    private ExcerptConfig excerpt = new ExcerptConfig();
	
	private String page = "welcome";
	
	private String localeCode = "en";

	public String getPage() { return page; }
	public void setPage(String page) { this.page = requireNonNull(page); }	
	
	public XsampleConfig getConfig() { return config; }
	public void setConfig(XsampleConfig config) { this.config = config; }
	
	public ExcerptConfig getExcerpt() { return excerpt; }
	public void setExcerpt(ExcerptConfig excerpt) { this.excerpt = excerpt; }
	
	public String getLocaleCode() { return localeCode; }
	public void setLocaleCode(String localeCode) {
		requireNonNull(localeCode);
		this.localeCode = localeCode;
		//TODO kinda bad, we shouldn't be doing any external modifications here
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(localeCode));
	}
	
	public DataverseUser getUser() { return dataverseUser; }
	public void setUser(DataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public Resource getResource() { return resource; }
	public void setResource(Resource resource) { this.resource = resource; }
}
