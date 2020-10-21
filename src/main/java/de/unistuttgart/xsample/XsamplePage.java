/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FlowEvent;

import de.unistuttgart.xsample.XsampleServices.Key;

/**
 * @author Markus G�rtner
 *
 */
@Named
@ViewScoped
public class XsamplePage implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	private static final Logger logger = Logger.getLogger(XsamplePage.class.getCanonicalName());

	private XsampleExcerptConfig config = new XsampleExcerptConfig();

	private User user;
	private Resource resource;
	
	private String serverRoute;

    @EJB
	private XsampleServices xsampleServices;
	
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceFileParam)))
			.map(Long::valueOf).ifPresent(config::setFile);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.ApiKeyParam)))
			.ifPresent(config::setKey);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceDataverseParam)))
			.ifPresent(config::setSite);
		
		serverRoute = xsampleServices.getSetting(Key.ServerRoute);
		
//		if(config.site==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noSite.summary"), 
//					BundleUtil.get("homepage.noSite.detail", "site")));
//		}
//		
//		if(file==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noUser.summary"), 
//					BundleUtil.get("homepage.noUser.detail", "file")));
//		}
//		
//		if(key==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noFile.summary"), 
//					BundleUtil.get("homepage.noFile.detail", "key")));
//		}
	} 
	
	public XsampleExcerptConfig getConfig() { return config; }
	public void setConfig(XsampleExcerptConfig config) { this.config = config; }
	
	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}

	public User getUser() { return user; }
	
	public Resource getResource() { return resource; }
	
	public String getServerRoute() { return serverRoute; }
}
