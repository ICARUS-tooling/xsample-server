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

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;

import de.unistuttgart.xsample.dv.Dataverse;

/**
 * @author Markus Gärtner
 *
 */
@Named
@SessionScoped
public class XsampleSession implements Serializable {

	private static final long serialVersionUID = 3102900115073845531L;
	
	private String localeCode = "en";

    @EJB
	private EntityManager em;

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) {
		requireNonNull(localeCode);
		this.localeCode = localeCode;
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(localeCode));
	}
	
	@PostConstruct
	private void prepareDummyData() {
		Dataverse dv = new Dataverse();
		dv.setUrl("http://193.196.55.101:8080");
		dv.setMasterKey("c286eef4-cd9f-4572-8930-a5e2c06bf1a9");
		em.persist(dv);
	}
}
