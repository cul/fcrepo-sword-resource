package edu.columbia.cul.sword.fileHandlers;

public class HandlerKey {
	
	private String contentType;
	private String namespaceFormat;
	
	public HandlerKey(String contentType, String namespaceFormat){
		this.contentType = contentType;
		this.namespaceFormat = namespaceFormat;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result
				+ ((namespaceFormat == null) ? 0 : namespaceFormat.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HandlerKey other = (HandlerKey) obj;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (namespaceFormat == null) {
			if (other.namespaceFormat != null)
				return false;
		} else if (!namespaceFormat.equals(other.namespaceFormat))
			return false;
		return true;
	}

} // =========================== 
