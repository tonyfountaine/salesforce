package nz.co.trineo.model;

import java.util.HashMap;

public class SObject extends HashMap<String, Object> {
	private static final long serialVersionUID = 8114849341770322502L;

	@SuppressWarnings("unchecked")
	public <T> T getField(final String name) {
		return (T) get(name);
	}

	public <T> void setField(final String name, final T value) {
		put(name, value);
	}
}
