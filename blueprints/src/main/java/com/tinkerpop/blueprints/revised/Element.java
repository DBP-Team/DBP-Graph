package com.tinkerpop.blueprints.revised;

import java.util.Set;

public interface Element {

	/**
	 * An identifier that is unique to its inheriting class. All vertices of a graph
	 * must have unique identifiers. All edges of a graph must have unique
	 * identifiers.
	 *
	 * @return the identifier of the element
	 */
	public String getId();

	/**
	 * Return the object value associated with the provided string key. If no value
	 * exists for that key, return null.
	 *
	 * @param key the key of the key/value property
	 * @return the object value related to the string key
	 */
	public Object getProperty(String key);

	/**
	 * Return all the keys associated with the element.
	 *
	 * @return the set of all string keys associated with the element
	 */
	public Set<String> getPropertyKeys();

	/**
	 * Assign a key/value property to the element. If a value already exists for
	 * this key, then the previous key/value is overwritten.
	 *
	 * @param key   the string key of the property
	 * @param value the object value o the property
	 */
	public void setProperty(String key, Object value);

	/**
	 * Un-assigns a key/value property from the element. The object value of the
	 * removed property is returned.
	 *
	 * @param key the key of the property to remove from the element
	 * @return the object value associated with that key prior to removal
	 */
	public Object removeProperty(String key);
}