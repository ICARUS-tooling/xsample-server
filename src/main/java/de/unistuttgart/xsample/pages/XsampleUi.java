/**
 * 
 */
package de.unistuttgart.xsample.pages;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.omnifaces.util.Messages;
import org.primefaces.PrimeFaces;

/**
 * @author Markus Gärtner
 *
 */
@Stateless
public class XsampleUi {

	public void update(String...components) {
		PrimeFaces.current().ajax().update(Arrays.asList(components));		
	}
	
	public void addMessage(@Nullable String clientId, FacesMessage message) {
		Messages.add(clientId, message);
	}
	
	public void addMessage(@Nullable String clientId, Severity severity, String message, Object... params) {
		addMessage(clientId, Messages.create(severity, message, params));
	}
	
	public void addError(@Nullable String clientId, String message, Object... params) {
		addMessage(clientId, Messages.create(FacesMessage.SEVERITY_ERROR, message, params));
	}
	
	public void addInfo(@Nullable String clientId, String message, Object... params) {
		addMessage(clientId, Messages.create(FacesMessage.SEVERITY_INFO, message, params));
	}
	
	public void addWarning(@Nullable String clientId, String message, Object... params) {
		addMessage(clientId, Messages.create(FacesMessage.SEVERITY_WARN, message, params));
	}
	
	public void addFatal(@Nullable String clientId, String message, Object... params) {
		addMessage(clientId, Messages.create(FacesMessage.SEVERITY_FATAL, message, params));
	}
	
	public void addGlobalMessage(FacesMessage message) {
		Messages.add(null, message);
	}
	
	public void addGlobalMessage(Severity severity, String message, Object... params) {
		addGlobalMessage(Messages.create(severity, message, params));
	}
	
	public void addGlobalError(String message, Object... params) {
		addGlobalMessage(Messages.create(FacesMessage.SEVERITY_ERROR, message, params));
	}
	
	public void addGlobalInfo(String message, Object... params) {
		addGlobalMessage(Messages.create(FacesMessage.SEVERITY_INFO, message, params));
	}
	
	public void addGlobalWarning(String message, Object... params) {
		addGlobalMessage(Messages.create(FacesMessage.SEVERITY_WARN, message, params));
	}
	
	public void addGlobalFatal(String message, Object... params) {
		addGlobalMessage(Messages.create(FacesMessage.SEVERITY_FATAL, message, params));
	}
}
