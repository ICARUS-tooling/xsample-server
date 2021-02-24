/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.omnifaces.cdi.Eager;

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
	
	@Inject
	XsampleServices services;

	@PostConstruct
	@Transactional
	private void init() {
		
		if(DebugUtils.isActive()) {
			
			logger.info("Performing debug initialization with default dataverse and resource");

			DebugUtils.makeDataverse(services);
			
			DebugUtils.makeQuota(services);
		}
	}
}
