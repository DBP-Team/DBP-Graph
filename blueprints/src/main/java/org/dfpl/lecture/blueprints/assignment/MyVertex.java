package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.SQLException;
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

    public MyVertex(String id, HashMap<String, Object> properties) {
        this.id = id;
        this.properties = properties;
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
    public void setProperty(String key, Object value) throws SQLException {
        // id 같은 친구 .. 데려다가 수정..
        // insert into json_test values (2 , json_object('k1' , 'test1_value')) ;

        String query = "UPDATE verticies SET properties=json_object(\'" + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\';";
        MyGraph.stmt.executeUpdate(query);
        properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return properties.remove(key);
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
