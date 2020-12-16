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

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.Resource;
import de.unistuttgart.xsample.dv.User;
import de.unistuttgart.xsample.dv.UserId;



/**
 * @author Markus Gärtner
 *
 */
@Stateless
@Named
public class XsampleServices {
	
	private static final Logger log = Logger.getLogger(XsampleServices.class.getCanonicalName());

	@PersistenceContext
	private EntityManager em;
	
	public static enum Key {
		SourceFileParam("file"),
		ApiKeyParam("key"),
		SourceDataverseParam("site"),
		ServerRoute("/xsample-server"),
		ExcerptLimit("15.0"),
		;
		
		private final String defaultValue;

		private Key(String defaultValue) {
			this.defaultValue = requireNonNull(defaultValue);
		}

		public String getLabel() { return name(); }

		public String getDefaultValue() { return defaultValue; }
	}
	
	// DB LOOKUP METHODS
	
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
			em.merge(resource);
		} else {
			resource = resources.get(0);
		}
		
		return resource;
	}
	
	public List<Resource> findAllResources() {
		return em.createNamedQuery("Resource.findAll").getResultList();
	}
	
	public Dataverse findDataverseByUrl(String url) {
		requireNonNull(url);
		
		List<Dataverse> dataverses = em.createNamedQuery("Dataverse.findByUrl")
					.setParameter("url", url)
					.getResultList();
		
		Dataverse dataverse;
		if(dataverses.isEmpty()) {
			log.finer("creating Dataverse for url: "+url);
			dataverse = new Dataverse();
			dataverse.setUrl(url);
			em.merge(dataverse);
		} else {
			dataverse = dataverses.get(0);
		}
		
		return dataverse;
	}
	
	public User findDataverseUser(Dataverse dataverse, String userId) {
		requireNonNull(dataverse);
		requireNonNull(userId);
		final String url = requireNonNull(dataverse.getUrl());
		
		List<User> users = em.createNamedQuery("User.find")
					.setParameter("url", url)
					.setParameter("id", userId)
					.getResultList();

		User user;
		if(users.isEmpty()) {
			log.finer(String.format("creating User for dataverse '%s' and id '%s'", url, userId));
			user = new User();
			user.setId(new UserId(url, userId));
			user.setDataverse(dataverse);
			em.merge(user);
		} else {
			user = users.get(0);
		}
		
		return user;
	}
	
	public List<User> findAllUsers() {
		return em.createNamedQuery("User.findAll").getResultList();
	}
	
	// SETTINGS METHODS
	
	public String getSetting(Key key) { 
		return key.getDefaultValue(); //TODO replace with actual DB query once the settings backend is implemented 
	}
	
	public int getIntSetting(Key key) { return Integer.parseInt(getSetting(key)); }
	
	public long getLongSetting(Key key) { return Long.parseLong(getSetting(key)); }
	
	public boolean getBooleanSetting(Key key) { return Boolean.parseBoolean(getSetting(key)); }
}
