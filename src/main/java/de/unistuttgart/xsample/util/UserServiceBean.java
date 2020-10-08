/**
 * 
 */
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Gärtner
 *
 */
@Stateless
@Named
public class UserServiceBean {
	
	private static final Logger log = LoggerFactory.getLogger(UserServiceBean.class);

	@PersistenceContext
	private EntityManager em;
	
	public User findByKey(String key) {
		requireNonNull(key);

		User user;
		
		List<User> users = em.createNamedQuery("User.findByKey")
					.setParameter("key", key)
					.getResultList();
		if(users.isEmpty()) {
			if(log.isDebugEnabled())
				log.debug("creating User for key: {}", key);
			user = new User();
			user.setKey(key);
			em.merge(user);
		} else {
			if(log.isDebugEnabled())
				log.debug("found User for key: {}", key);
			user = users.get(0);
		}
		
		return user;
	}
	
	public List<User> findAll() {
		return em.createNamedQuery("User.findAll").getResultList();
	}
}
