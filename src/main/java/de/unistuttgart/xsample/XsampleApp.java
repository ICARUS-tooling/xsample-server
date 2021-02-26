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
			
			logger.info("Performing debug initialization with default dataverse and resource");

			DebugUtils.makeDataverse(services);
			
			DebugUtils.makeQuota(services);
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key, "<unset_"+key+">");
	}
	
	public String getVersion() { return getProperty("version"); }
}
