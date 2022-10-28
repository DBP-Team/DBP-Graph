package org.dfpl.lecture.blueprints.persistent;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

public class PersistentEdge implements Edge {
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
        this.properties = new HashMap<>();
    }

    @Override
    public String toString() {
        return id + "/" + outV.getId() + "/" + inV.getId() + "/" + label;
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
    public void setProperty(String key, Object value) throws SQLException {
        String updateQuery = "UPDATE edges SET properties=JSON_SET(properties," +
                " \'$." + key + "\', \'" + value + "\') WHERE edge_id=\'" + this.id + "\';";
        String insertQuery = "INSERT INTO edge_properties VALUES('" + key + "', '" + value + "', '" + this.id + "')";

        PersistentGraph.stmt.executeUpdate(updateQuery);
        System.out.println(insertQuery);
        PersistentGraph.stmt.executeUpdate(insertQuery);


        properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return properties.remove(key);
    }
}
