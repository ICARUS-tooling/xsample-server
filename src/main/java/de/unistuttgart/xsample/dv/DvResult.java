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
package de.unistuttgart.xsample.dv;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Markus Gärtner
 *
 */
public class DvResult<T> implements Serializable {

	private static final long serialVersionUID = 9091787065928521813L;

	private String status;
	
	private T data;
	
	public boolean isOk() { return "OK".equals(getStatus()); }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public static class Generic extends DvResult<Map<String, String>> {

		private static final long serialVersionUID = -6953403755055349059L;
		
		// no-op
	}
}
