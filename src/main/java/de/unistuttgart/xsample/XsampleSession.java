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

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices.Key;

/**
 * @author Markus Gärtner
 *
 */
@Named
@SessionScoped
public class XsampleSession implements Serializable {
	
	private static final long serialVersionUID = 7429002283550094738L;

	private static final Logger logger = Logger.getLogger(XsampleSession.class.getCanonicalName());
    
    private String localeCode;
    
    private Boolean debug;
    
    private String serverName;
    
    @Inject
    XsampleServices services;
    
    @PostConstruct
    private void init() {
    	serverName = services.getSetting(Key.ServerName);
    }
    
	public String getServerName() { return serverName; }
    
    public String getLocaleCode() {
        if (localeCode == null) {
            initLocale();
        }
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }
    
    private void initLocale() {
        
        if(FacesContext.getCurrentInstance() == null) {
            localeCode = "en";
        }
        else if (FacesContext.getCurrentInstance().getViewRoot() == null ) {
            localeCode = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage();
        }
        else if (FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage().equals("en_US")) {
            localeCode = "en";
        }
        else {
            localeCode = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        }
        
        logger.fine("init: locale set to "+localeCode);
    }
	
    public boolean isDebug() { return debug!=null && debug.booleanValue(); }
    public void setDebug(Boolean debug) { this.debug = debug; }
}
