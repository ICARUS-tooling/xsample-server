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
