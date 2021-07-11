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
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpResource;

/**
 * @author Markus Gärtner
 *
 */
public class DebugUtils {
	
	private static final Logger log = Logger.getLogger(DebugUtils.class.getCanonicalName());

	public static Properties settings;
	static {
		URL config = XsampleServices.class.getResource("/config/debug.ini");
		if(config!=null) {
			Properties settings = new Properties();
			try {
				settings.load(config.openStream());
			} catch (IOException e) {
				log.log(Level.SEVERE, "Failed to load debug settings", e);
			}
			DebugUtils.settings = settings;
		}
	}
	
	public static boolean isActive() { return settings!=null; }
	
	public static String getProperty(String key) {
		return requireNonNull(settings.getProperty(key));
	}
	
	public static void makeDataverse(XsampleServices services) {
		if(!isActive()) {
			return;
		}
		
		String url = getProperty("dataverse.url");
		String token = getProperty("dataverse.masterKey");
		String overrideUrl = getProperty("dataverse.overrideUrl");
		Optional<XmpDataverse> current = services.findDataverseByUrl(url);
		if(!current.isPresent()) {
			XmpDataverse dv = new XmpDataverse(url, token);
			dv.setOverrideUrl(overrideUrl);
			services.store(dv);
		} else {
			current.get().setMasterKey(token);
			current.get().setOverrideUrl(overrideUrl);
		}
	}
	
	public static void makeQuota(XsampleServices services) {
		if(!isActive()) {
			return;
		}

		String url = getProperty("dataverse.url");
		Long file = Long.valueOf(getProperty("dataverse.file"));
		XmpDataverse dataverse = services.findDataverseByUrl(url).get();
		XmpResource xmpResource = services.findResource(dataverse, file);
		XmpDataverseUser user = services.findDataverseUser(dataverse, getProperty("user.name"));
		
		XmpExcerpt quota = services.findQuota(user, xmpResource);
		quota.clear();
		
		String data = getProperty("user.quota");
		XmpFragment.decodeAll(data).forEach(quota::addFragment);
	}
}
