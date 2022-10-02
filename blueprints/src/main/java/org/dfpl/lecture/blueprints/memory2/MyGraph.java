package org.dfpl.lecture.blueprints.memory2;

import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.util.Collection;
import java.util.HashMap;

public class MyGraph implements Graph {

    private HashMap<String, Vertex> vertices;
    private HashMap<String, Edge> edges;

    public MyGraph() {
        vertices = new HashMap<>();
        edges = new HashMap<>();
    }

    @Override
    public Vertex addVertex(String id) throws IllegalArgumentException {
        /**
         * duplicate check : if duplicated id -> return Vertex with id as key
         */
        Vertex v = vertices.get(id);
        if (v == null) {
            v = new MyVertex(id);
            vertices.put(id, v);
        }
        return v;
    }

    @Override
    public Vertex getVertex(String id) {
        // If a vertex with id as a key is not in the vertices, null is returned.
        return vertices.get(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {

    }

    @Override
    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    @Override
    public Collection<Vertex> getVertices(String key, Object value) {
        return vertices.values().parallelStream().filter(v -> {
            Object propValue = v.getProperty(key);
            if (propValue == value)
                return true;
            return false;
        }).toList();
    }

    private static String getEdgeID(Vertex outVertex, Vertex inVertex, String label) {
         return outVertex.getId() + "|" + label + '|' + inVertex.getId();
    }

    @Override
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws IllegalArgumentException, NullPointerException {
        String edgeID = getEdgeID(outVertex, inVertex, label);
        Edge e = edges.get(edgeID);
        if (e == null) {
            e = new MyEdge(edgeID, outVertex, inVertex, label);
            edges.put(edgeID, e);
        }
        return e;
    }

    @Override
    public Edge getEdge(Vertex outVertex, Vertex inVertex, String label) {
        String edgeID = getEdgeID(outVertex, inVertex, label);
        return edges.get(edgeID);
    }

    @Override
    public Edge getEdge(String id) {
        return edges.get(id);
    }

    @Override
    public void removeEdge(Edge edge) {

    }

    @Override
    public Collection<Edge> getEdges() {
        return edges.values();
    }

    @Override
    public Collection<Edge> getEdges(String key, Object value) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
