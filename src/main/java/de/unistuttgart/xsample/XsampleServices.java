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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unistuttgart.xsample.dv.UserId;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;



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
		/** URL param key used for the file */
		SourceFileParam,
		/** URL param key used for the API token */
		ApiKeyParam,
		/** URL param key used for the source dataverse */
		SourceDataverseParam,
		/** Fully qualified domain name of this XSample server instance */
		ServerName,
		/** Limit in factor percent of the portion a user is allowed to receive per resource. */
		ExcerptLimit,
		/** Percent value of a resource that will be returned as the default slice.  */
		DefaultStaticExcerpt,
		/** Lower boundary in segments for a resource to count as "small".  */
		SmallFileLimit
		;
		
		public String getLabel() { return name(); }
	}
	
	// DB LOOKUP METHODS
	
	public <T> T save(T obj) {
		T result = em.merge(obj);
		
		em.flush();
		
		return result;
	}
	
	public <T> void delete(T obj) { em.remove(obj); }
	
	public void sync() { em.flush(); }
	
	public XmpResource findResource(XmpDataverse dataverse, Long file) {
		requireNonNull(file);
		
		List<XmpResource> resources = em.createNamedQuery("Resource.find")
					.setParameter("dataverse", dataverse)
					.setParameter("file", file)
					.getResultList();
		
		XmpResource resource;
		if(resources.isEmpty()) {
			log.finer("creating Resource for file: "+file);
			resource = new XmpResource();
			resource.setFile(file);
			resource.setDataverse(dataverse);
			resource = em.merge(resource);
		} else {
			resource = resources.get(0);
		}
		
		return resource;
	}
	
	public Optional<XmpDataverse> findDataverseByUrl(String url) {
		requireNonNull(url);
		
		List<XmpDataverse> dataverses = em.createNamedQuery("Dataverse.findByUrl")
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
	
	public XmpDataverseUser findDataverseUser(XmpDataverse dataverse, String userId) {
		requireNonNull(dataverse);
		requireNonNull(userId);
		final String url = requireNonNull(dataverse.getUrl());
		
		List<XmpDataverseUser> dataverseUsers = em.createNamedQuery("DataverseUser.find")
					.setParameter("url", url)
					.setParameter("id", userId)
					.getResultList();

		XmpDataverseUser dataverseUser;
		if(dataverseUsers.isEmpty()) {
			log.finer(String.format("creating DataverseUser for dataverse '%s' and id '%s'", url, userId));
			dataverseUser = new XmpDataverseUser();
			dataverseUser.setId(new UserId(url, userId));
			dataverseUser.setDataverse(dataverse);
			dataverseUser = em.merge(dataverseUser);
		} else {
			dataverseUser = dataverseUsers.get(0);
		}
		
		return dataverseUser;
	}
	
	public List<XmpDataverseUser> findAllUsers() {
		return em.createNamedQuery("DataverseUser.findAll").getResultList();
	}
	
	public XmpExcerpt findQuota(XmpDataverseUser user, XmpResource resource) {
		requireNonNull(user);
		requireNonNull(resource);
		
		List<XmpExcerpt> excerpts = em.createNamedQuery("Excerpt.find")
					.setParameter("user", user)
					.setParameter("resource", resource)
					.getResultList();
		
		XmpExcerpt excerpt;
		if(excerpts.isEmpty()) {
			excerpt = new XmpExcerpt();
			excerpt.setResource(resource);
			excerpt.setDataverseUser(user);
			excerpt = em.merge(excerpt);
		} else {
			excerpt = excerpts.get(0);
		}
		
		return excerpt;
	}
	
	public List<XmpLocalCopy> findExpiredCopies() {
		return em.createNamedQuery("LocalCopy.findExpired")
				.setParameter("timestamp", LocalDateTime.now())
				.getResultList();
	}
	
	public Optional<XmpLocalCopy> findCopy(XmpResource resource) {
		requireNonNull(resource);

		List<XmpLocalCopy> copies = em.createNamedQuery("LocalCopy.findByResource")
					.setParameter("resource", resource)
					.getResultList();
		
		return copies.isEmpty() ? Optional.empty() : Optional.of(copies.get(0));
	}
	
	public Optional<XmpLocalCopy> findCopy(String filename) {
		requireNonNull(filename);

		List<XmpLocalCopy> copies = em.createNamedQuery("LocalCopy.findByTempFile")
					.setParameter("filename", filename)
					.getResultList();
		
		return copies.isEmpty() ? Optional.empty() : Optional.of(copies.get(0));
	}
	
	// SETTINGS METHODS

	public String getSetting(Key key) {
		//TODO replace with actual DB query once the settings backend is implemented
		String value = defaultSettings.getProperty(key.getLabel());
		return value;  
	}
	
	public int getIntSetting(Key key) { return Integer.parseInt(getSetting(key)); }
	
	public long getLongSetting(Key key) { return Long.parseLong(getSetting(key)); }
	
	public double getDoubleSetting(Key key) { return Double.parseDouble(getSetting(key)); }
	
	public boolean getBooleanSetting(Key key) { return Boolean.parseBoolean(getSetting(key)); }
	
}
