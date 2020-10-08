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
public class ResourceServiceBean {
	
	private static final Logger log = LoggerFactory.getLogger(ResourceServiceBean.class);
	
	@PersistenceContext
	private EntityManager em;
	
	public Resource findByFile(Long file) {
		requireNonNull(file);
		
		Resource resource;
		
		List<Resource> resources = em.createNamedQuery("Resource.findByFile")
					.setParameter("file", file)
					.getResultList();
		if(resources.isEmpty()) {
			if(log.isDebugEnabled())
				log.debug("creating Resource for file: {}", file);
			resource = new Resource();
			resource.setFile(file);
			em.merge(resource);
		} else {
			if(log.isDebugEnabled())
				log.debug("found Resource for file: {}", file);
			resource = resources.get(0);
		}
		
		return resource;
	}
	
	public List<Resource> findAll() {
		return em.createNamedQuery("Resource.findAll").getResultList();
	}
}
