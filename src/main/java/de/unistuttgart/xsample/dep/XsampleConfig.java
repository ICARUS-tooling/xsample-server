/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.dep;

import java.io.Serializable;

import de.unistuttgart.xsample.ExcerptType;

/**
 * @author Markus Gärtner
 *
 */
@Deprecated
public class XsampleConfig implements Serializable {
	
	private static final long serialVersionUID = -7477220400939108522L;
	
	/** Type of excerpt generation, legal values are 'static', 'window' and 'query'. */
	private ExcerptType type = ExcerptType.STATIC; 
	
	private boolean includeAnnotations = true;

	private String serverRoute;
	
	public String getServerRoute() { return serverRoute; }
	public void setServerRoute(String serverRoute) { this.serverRoute = serverRoute; }
	
	public ExcerptType getType() { return type; }
	public void setType(ExcerptType type) { this.type = type; }
	
	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }
}
