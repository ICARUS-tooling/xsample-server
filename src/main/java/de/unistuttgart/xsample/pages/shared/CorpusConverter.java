/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.pages.shared;

import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.XsampleManifest;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
@FacesConverter("corpusConverter")
public class CorpusConverter implements Converter<Corpus>, Serializable {

	private static final long serialVersionUID = -8257660999622066122L;

	@Inject
	SharedData sharedData;
	
	private transient Map<String, Corpus> lookup = new Object2ObjectOpenHashMap<>();

	@Override
	public Corpus getAsObject(FacesContext context, UIComponent component, String value) {
		if(value==null || value.isEmpty()) {
			return null;
		}

		if(lookup.isEmpty()) {
			XsampleManifest manifest = sharedData.getManifest();
			if(manifest.getCorpus()!=null) {
				populateLookup(sharedData.getManifest().getCorpus());
			}
		}
		
		Corpus result = lookup.get(value);
//		if(result==null) {
//			System.out.println("Unknown corpus id: "+value);
//		}
		return result;
	}
	
	private void populateLookup(Corpus corpus) {
		lookup.put(corpus.getId(), corpus);
		corpus.forEachPart(this::populateLookup);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Corpus value) {
		return value==null ? null : value.getId();
	}
}
