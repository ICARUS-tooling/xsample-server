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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.DataverseUser;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Resource;
import de.unistuttgart.xsample.dv.UserId;



/**
 * @author Markus Gärtner
 *
 */
@Stateless
public class XsampleServices {
	
	private static final Logger log = Logger.getLogger(XsampleServices.class.getCanonicalName());

	@PersistenceContext
	private EntityManager em;
	
	static final Properties defaultSettings;
	static {
		URL config = XsampleServices.class.getResource("/config/config.ini");
		Properties settings = new Properties();
		if(config!=null) {
			try {
				settings.load(config.openStream());
			} catch (IOException e) {
				log.log(Level.SEVERE, "Failed to load default settings", e);
			}
		}
		defaultSettings = settings;
	}
	
	public static enum Key {
		SourceFileParam,
		ApiKeyParam,
		SourceDataverseParam,
		ServerName,
		ExcerptLimit,
		;
		
		public String getLabel() { return name(); }
	}
	
	// DB LOOKUP METHODS
	
	public <T> T add(T obj) {
		return em.merge(obj);
	}
	
	public Resource findResourceByFile(Long file) {
		requireNonNull(file);
		
		List<Resource> resources = em.createNamedQuery("Resource.findByFile")
					.setParameter("file", file)
					.getResultList();
		
		Resource resource;
		if(resources.isEmpty()) {
			log.finer("creating Resource for file: "+file);
			resource = new Resource();
			resource.setFile(file);
			resource = em.merge(resource);
		} else {
			resource = resources.get(0);
		}
		
		return resource;
	}
	
	public List<Resource> findAllResources() {
		return em.createNamedQuery("Resource.findAll").getResultList();
	}
	
	public Optional<Dataverse> findDataverseByUrl(String url) {
		requireNonNull(url);
		
		List<Dataverse> dataverses = em.createNamedQuery("Dataverse.findByUrl")
					.setParameter("url", url)
					.getResultList();
		
//		Dataverse dataverse = null;
//		if(dataverses.isEmpty() && createIfMissing) {
//			if(createIfMissing) {
//				log.finer("creating Dataverse for url: "+url);
//				dataverse = new Dataverse();
//				dataverse.setUrl(url);
//				dataverse = em.merge(dataverse);
//			}
//		} else {
//			dataverse = dataverses.get(0);
//		}
		
		return Optional.ofNullable(dataverses.isEmpty() ? null : dataverses.get(0));
	}
	
	public DataverseUser findDataverseUser(Dataverse dataverse, String userId) {
		requireNonNull(dataverse);
		requireNonNull(userId);
		final String url = requireNonNull(dataverse.getUrl());
		
		List<DataverseUser> dataverseUsers = em.createNamedQuery("DataverseUser.find")
					.setParameter("url", url)
					.setParameter("id", userId)
					.getResultList();

		DataverseUser dataverseUser;
		if(dataverseUsers.isEmpty()) {
			log.finer(String.format("creating DataverseUser for dataverse '%s' and id '%s'", url, userId));
			dataverseUser = new DataverseUser();
			dataverseUser.setId(new UserId(url, userId));
			dataverseUser.setDataverse(dataverse);
			dataverseUser = em.merge(dataverseUser);
		} else {
			dataverseUser = dataverseUsers.get(0);
		}
		
		return dataverseUser;
	}
	
	public List<DataverseUser> findAllUsers() {
		return em.createNamedQuery("DataverseUser.findAll").getResultList();
	}
	
	// SETTINGS METHODS
	
	public List<Excerpt> findExcerpts(DataverseUser user, Resource resource) {
		requireNonNull(user);
		requireNonNull(resource);
		
		return em.createNamedQuery("Excerpt.find")
					.setParameter("user", user)
					.setParameter("resource", resource)
					.getResultList();
	}

	public String getSetting(Key key) {
		//TODO replace with actual DB query once the settings backend is implemented
		String value = defaultSettings.getProperty(key.getLabel());
		return value;  
	}
	
	public int getIntSetting(Key key) { return Integer.parseInt(getSetting(key)); }
	
	public long getLongSetting(Key key) { return Long.parseLong(getSetting(key)); }
	
	public boolean getBooleanSetting(Key key) { return Boolean.parseBoolean(getSetting(key)); }
}
