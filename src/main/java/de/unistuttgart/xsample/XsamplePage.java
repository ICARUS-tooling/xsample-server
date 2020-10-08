/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.omnifaces.cdi.Param;

import de.unistuttgart.xsample.util.Resource;
import de.unistuttgart.xsample.util.ResourceServiceBean;
import de.unistuttgart.xsample.util.User;
import de.unistuttgart.xsample.util.UserServiceBean;

/**
 * @author Markus Gärtner
 *
 */
@Named("XSamplePage")
@SessionScoped
public class XsamplePage implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	/** The file id as supplied by the external-tools URL */
	@Param private Long file;	
	/** The user's API key as supplied by the external-tools URL */
	@Param private String key;

	private User user;
	private Resource resource;

    @EJB
	private UserServiceBean userService;
    @EJB
    private ResourceServiceBean resourceService;
	
	@PostConstruct
	public void init() {
		//TODO handle missing params
		System.out.printf("params: file=%d key=%s %n", file, key);
		
		user = userService.findByKey(key);
		resource = resourceService.findByFile(file);
	}
	
	public Long getFile() { return file; }
	
	public String getKey() { return key; }
	
	public User getUser() { return user; }
	
	public Resource getResource() { return resource; }
	
	private String localeCode = "en";

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) { 	this.localeCode = localeCode; }
}
