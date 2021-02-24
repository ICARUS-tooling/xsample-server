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
import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.DataverseUser;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.dv.Resource;

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
		Optional<Dataverse> current = services.findDataverseByUrl(url);
		if(!current.isPresent()) {
			Dataverse dv = new Dataverse(url, token);
			services.save(dv);
		} else {
			current.get().setMasterKey(token);
		}
	}
	
	public static void makeQuota(XsampleServices services) {
		if(!isActive()) {
			return;
		}

		String url = getProperty("dataverse.url");
		Long file = Long.valueOf(getProperty("dataverse.file"));
		Dataverse dataverse = services.findDataverseByUrl(url).get();
		Resource resource = services.findResource(dataverse, file);
		DataverseUser user = services.findDataverseUser(dataverse, getProperty("user.name"));
		
		Excerpt quota = services.findQuota(user, resource);
		quota.clear();
		
		String data = getProperty("user.quota");
		Fragment.decodeAll(data).forEach(quota::addFragment);
	}
}
