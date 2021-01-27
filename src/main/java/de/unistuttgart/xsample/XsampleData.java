/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.Resource;
import de.unistuttgart.xsample.dv.User;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsamplePage implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	private static final Logger logger = Logger.getLogger(XsamplePage.class.getCanonicalName());

	private User user;
	private Resource resource;

    @EJB
	private XsampleServices xsampleServices;    
    
    private XsampleConfig config = new XsampleConfig();
	
	private String page = "welcome";
	
	private String localeCode = "en";
	
	private final AtomicBoolean loaded = new AtomicBoolean();

	@Transactional
	public void init() {
		//TODO remove for production use
		if(loaded.compareAndSet(false, true)) {
			Dataverse dv = xsampleServices.findDataverseByUrl("http://193.196.55.101:8080");
			dv.setMasterKey("c286eef4-cd9f-4572-8930-a5e2c06bf1a9");
		}
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceFileParam)))
			.map(Long::valueOf).ifPresent(config::setFile);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.ApiKeyParam)))
			.ifPresent(config::setKey);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceDataverseParam)))
			.ifPresent(config::setSite);
		
		config.setServerRoute(xsampleServices.getSetting(Key.ServerRoute));
	} 
	
	public void checkAndContinue() {
		//TODO check file and continue depending on setup
		
		String page = null;
		switch (config.getType()) {
		case STATIC: page = "download"; break;
		case WINDOW: page = "window"; break;
		case QUERY: page = "query"; break;

		default:
			Messages.addGlobalError("Unknown excerpt type: %s", config.getType());
			break;
		}
		
		if(page!=null) {
			setPage(page);
		}
	}

	public String getPage() { return page; }
	public void setPage(String page) { this.page = requireNonNull(page); }	
	
	public XsampleConfig getConfig() { return config; }
	public void setConfig(XsampleConfig config) { this.config = config; }

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) {
		requireNonNull(localeCode);
		this.localeCode = localeCode;
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(localeCode));
	}
	
	public User getUser() { return user; }
	
	public Resource getResource() { return resource; }
}
