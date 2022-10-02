package org.dfpl.lecture.blueprints.memory2;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class MyVertex implements Vertex {

    private String id;
    private HashMap<String, Object> properties;
    public MyVertex(String id) {
        this.id = id;
        this.properties = new HashMap<>();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
       return properties.keySet();
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return null;
    }

    @Override
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void remove() {

    }
}
