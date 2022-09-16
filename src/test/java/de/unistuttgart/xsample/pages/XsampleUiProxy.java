/**
 * 
 */
package de.unistuttgart.xsample.pages;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleUiProxy extends XsampleUi {
	
	private final Map<Object, List<FacesMessage>> messages = new Object2ObjectOpenHashMap<>();
	
	private static final Object GLOBAL = new Object() {
		@Override
		public String toString() { return "global"; }
	};
	
	private static Object key(@Nullable String clientId) {
		return clientId==null ? GLOBAL : clientId;
	}

	@Override
	public void addMessage(@Nullable String clientId, FacesMessage message) {
		requireNonNull(message);
		messages.computeIfAbsent(key(clientId), k -> new ObjectArrayList<>()).add(message);
		
		System.out.printf("message for '%s': [%s: %s] %s %n", key(clientId), 
				message.getSeverity(), message.getSummary(), message.getDetail());
	}
	
	private final Set<String> updated = new ObjectOpenHashSet<>();
	
	@Override
	public void update(String... components) {
		Collections.addAll(updated, components);
	}
	
	// ASSERTIONS
	
	public boolean hasMessages(@Nullable String clientId) {
		return !messages.getOrDefault(key(clientId), Collections.emptyList()).isEmpty();
	}
}
