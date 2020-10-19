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



/**
 * @author Markus G�rtner
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
		ServerRoute("/xsample-server")
		;
		
		private final String defaultValue;

		private Key(String defaultValue) {
			this.defaultValue = requireNonNull(defaultValue);
		}

		public String getLabel() { return name(); }

		public String getDefaultValue() { return defaultValue; }
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
			em.merge(resource);
		} else {
			resource = resources.get(0);
		}
		
		return resource;
	}
	
	public List<Resource> findAllResources() {
		return em.createNamedQuery("Resource.findAll").getResultList();
	}
	
	public User findUserByKey(String key) {
		requireNonNull(key);
		
		List<User> users = em.createNamedQuery("User.findByKey")
					.setParameter("key", key)
					.getResultList();

		User user;
		if(users.isEmpty()) {
			log.finer("creating User for key: "+key);
			user = new User();
			user.setKey(key);
			em.merge(user);
		} else {
			user = users.get(0);
		}
		
		return user;
	}
	
	public List<User> findAllUsers() {
		return em.createNamedQuery("User.findAll").getResultList();
	}
	
	public String getSetting(Key key) { return key.getDefaultValue(); }
	
	public int getIntSetting(Key key) { return Integer.parseInt(getSetting(key)); }
	
	public long getLongSetting(Key key) { return Long.parseLong(getSetting(key)); }
	
	public boolean getBooleanSetting(Key key) { return Boolean.parseBoolean(getSetting(key)); }
}
