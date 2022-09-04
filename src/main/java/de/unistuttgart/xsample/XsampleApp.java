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
package de.unistuttgart.xsample;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.omnifaces.cdi.Eager;

import de.unistuttgart.xsample.util.DebugUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@Eager
@ApplicationScoped
public class XsampleApp implements Serializable {

	private static final long serialVersionUID = 5658490939190694626L;
	
	private static final Logger logger = Logger.getLogger(XsampleApp.class.getCanonicalName());
	
	//TODO inject version identifier from gradle build file here and display it in UI
	
	@Inject
	XsampleServices services;
	
	private Properties properties = new Properties();

	@PostConstruct
	@Transactional
	private void init() {
		
		try(InputStream in = XsampleApp.class.getResourceAsStream("/META-INF/app.properties")) {
			properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read application properties file", e);
		}
		
		if(DebugUtils.isActive()) {
			
			logger.info("Performing debug initialization with default dataverse");

			DebugUtils.makeDataverse(services);
			
//			DebugUtils.makeQuota(services);
		}
		
		logger.info("XSample server initialized");
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key, "<unset_"+key+">");
	}
	
	public String getVersion() { return getProperty("version"); }
}
