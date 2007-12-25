package org.mindhaus.bdb.browser.ui.model;

public class ObjectNode {

	private Object value;
	private String name;
	private Class clazz;

	public ObjectNode(String name, Class clazz, Object value) {
		this.name = name;
		this.clazz = clazz;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public Class getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return name + " <" + clazz.getSimpleName() + "> = " + value;
	}

}
