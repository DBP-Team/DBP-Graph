package org.dfpl.lecture.blueprints.persistent;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.util.HashMap;
import java.util.Set;

public class PersistentEdge implements Edge {
    // CREATE OR REPLACE TABLE edge (id varchar(50), outV varchar(50), inV varchar(50), label varchar(50), properties json);
    // "INSERT INTO edge ('" + inV.getId() + "|" + label + "|" + outV.getId() + "', '" + outV.getId() + "', '" + inV.getId() + "', '" + label + "', null);";
    private String id;
    private Vertex outV;
    private Vertex inV;
    private String label;
    private HashMap<String, Object> properties;

    public PersistentEdge(String id, Vertex outV, Vertex inV, String label) {
        this.id = id;
        this.outV = outV;
        this.inV = inV;
        this.label = label;
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.OUT)) // bug check
            return outV;
        return inV;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void remove() {

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
        return properties.remove(key);
    }
}
