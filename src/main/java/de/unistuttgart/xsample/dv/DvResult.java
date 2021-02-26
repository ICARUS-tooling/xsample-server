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
