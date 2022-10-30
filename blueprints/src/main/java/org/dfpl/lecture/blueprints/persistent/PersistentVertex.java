package org.dfpl.lecture.blueprints.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;
import org.dfpl.lecture.blueprints.assignment.UnitTestCustom;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PersistentVertex implements Vertex {

    private String id;
    private HashMap<String, Object> properties;

    public PersistentVertex(String id) {
        this.id = id;
        this.properties = new HashMap<>();
    }

    public PersistentVertex(String id, HashMap<String, Object> properties) {
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
        String updateQuery = "UPDATE verticies SET properties=JSON_SET(properties," +
                " \'$." + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\';";
        String insertQuery = "INSERT INTO vertex_properties VALUES('" + key + "', '" + value + "', " + this.id + ")";

        PersistentGraph.stmt.executeUpdate(updateQuery);
        PersistentGraph.stmt.executeUpdate(insertQuery);
        properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
//        UPDATE verticies SET properties=JSON_REMOVE(properties, '$.k3') WHERE vertex_id='v3';
        String query = "UPDATE verticies SET properties=JSON_REMOVE(properties, \'$." + key + "\') WHERE vertex_id=\'" + this.id + "\';";
        try {
            PersistentGraph.stmt.executeUpdate(query);
        } catch (Exception e) {
            System.out.println("Exception Occur: " + e);
        }
        return properties.remove(key);
    }

    @Override
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException, SQLException, IOException {

        String condition;
        String query = null;
        Collection<Edge> edgeCollection = new ArrayList<Edge>();

        if (direction == Direction.BOTH)
            throw new IllegalArgumentException("Direction.BOTH is not allowed");

        if (direction == Direction.OUT)
            condition = "outV = ";
        else // Direction.IN
            condition = "inV = ";

        query = "SELECT * FROM edges WHERE " + condition + "\'" + this.id + "\'";

        if (labels.length > 0) {
            query += " AND (";
            for (int i = 0; i < labels.length; i++) {
                query += "label = '" + labels[i] + "'";

                if (i != labels.length - 1) {
                    query += " OR ";
                }
            }
            query += ");";
        }

//        System.out.println(query);hello
        ResultSet rs = PersistentGraph.stmt.executeQuery(query);

        while(rs.next()){
            String edge_id = rs.getString(1);
            String outVertexName = rs.getString(2);
            String inVertexName = rs.getString(3);
            String label = rs.getString(4);

            Vertex outV = new PersistentVertex(outVertexName);
            Vertex inV = new PersistentVertex(inVertexName);

            Edge e = new PersistentEdge(edge_id, outV, inV, label);
            edgeCollection.add(e);

        }

        return edgeCollection;

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
