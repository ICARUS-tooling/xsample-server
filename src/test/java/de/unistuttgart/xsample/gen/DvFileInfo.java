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
package de.unistuttgart.xsample.gen;

import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public class DvFileInfo implements Serializable {

	private static final long serialVersionUID = 1961412499856342344L;

	/**
	 * <pre>
	 * {
      "label": "100p_custom_slice.json",
      "restricted": false,
      "version": 1,
      "datasetVersionId": 7,
      "dataFile": {
        "id": 35,
        "persistentId": "",
        "pidURL": "",
        "filename": "100p_custom_slice.json",
        "contentType": "application/json",
        "filesize": 485,
        "storageIdentifier": "file://177d82c1527-a8adb2c87f22",
        "rootDataFileId": -1,
        "md5": "c40a16b0d50a9c0c9409d60a7279a267",
        "checksum": {
          "type": "MD5",
          "value": "c40a16b0d50a9c0c9409d60a7279a267"
        },
        "creationDate": "2021-02-25"
      }
    }
	 * </pre>
	 */
	

    private String label;
    private boolean restricted;
    private int version;
    private int datasetVersionId;
    private DvDataFile dataFile;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getDatasetVersionId() {
		return datasetVersionId;
	}

	public void setDatasetVersionId(int datasetVersionId) {
		this.datasetVersionId = datasetVersionId;
	}

	public DvDataFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(DvDataFile dataFile) {
		this.dataFile = dataFile;
	}

	public static class DvDataFile implements Serializable {

		private static final long serialVersionUID = 556166659379792376L;
		
		private long id;
        private String persistentId;
        private String pidURL;
        private String filename;
        private String contentType;
        private long filesize;
        private String storageIdentifier;
        
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getPersistentId() {
			return persistentId;
		}
		public void setPersistentId(String persistentId) {
			this.persistentId = persistentId;
		}
		public String getPidURL() {
			return pidURL;
		}
		public void setPidURL(String pidURL) {
			this.pidURL = pidURL;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public String getContentType() {
			return contentType;
		}
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}
		public long getFilesize() {
			return filesize;
		}
		public void setFilesize(long filesize) {
			this.filesize = filesize;
		}
		public String getStorageIdentifier() {
			return storageIdentifier;
		}
		public void setStorageIdentifier(String storageIdentifier) {
			this.storageIdentifier = storageIdentifier;
		}
	}
}
