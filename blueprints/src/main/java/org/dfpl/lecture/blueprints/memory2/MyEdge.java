package org.dfpl.lecture.blueprints.memory2;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.util.Set;

public class MyEdge implements Edge {

    private String edgeID;
    private Vertex outVertex;
    private Vertex inVertex;
    private String label;


    public MyEdge(String edgeID, Vertex outVertex, Vertex inVertex, String label) {
        this.edgeID = edgeID;
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.label = label;
    }

    @Override
    public String toString() {
        return outVertex.getId() + "|" + label + "|" + inVertex.getId();
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Object getProperty(String key) {
        return null;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return null;
    }

    @Override
    public void setProperty(String key, Object value) {

    }

    @Override
    public Object removeProperty(String key) {
        return null;
    }
}
