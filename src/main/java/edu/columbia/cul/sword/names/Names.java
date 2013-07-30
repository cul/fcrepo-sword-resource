package edu.columbia.cul.sword.names;


public enum Names {
	
	id("id", Namespaces.ATOM),
	entry("entry", Namespaces.ATOM),
	title("title", Namespaces.ATOM),
	published("published", Namespaces.ATOM),
	updated("updated", Namespaces.ATOM),
	userAgent("userAgent", Namespaces.SWORD),
	summary("summary", Namespaces.SWORD),
	contributor("contributor", Namespaces.ATOM),
	generator("generator", Namespaces.ATOM);

	String entityName;
	String namespace;
	
	private Names(String entityName, String namespace) {
		this.entityName = entityName;
		this.namespace = namespace;
	}

} // ====================================================== //