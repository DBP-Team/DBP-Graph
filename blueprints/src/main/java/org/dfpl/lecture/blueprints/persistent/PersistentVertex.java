package org.dfpl.lecture.blueprints.persistent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

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
        String insertQuery = "INSERT INTO vertex_properties VALUES('" + key + "', '" + value + "', '" + this.id + "')";

        //System.out.println(insertQuery);

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
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException {
        String selectQuery = "SELECT verticies.vertex_id, verticies.properties FROM verticies JOIN edges WHERE ";
        if (direction == Direction.OUT) {
            selectQuery += "edges.outV = '" + id + "' AND edges.inV = verticies.vertex_id";
        } else if (direction == Direction.IN) {
            selectQuery += "edges.inV = '" + id + "' AND edges.outV = verticies.vertex_id";
        } else { // dierection == Direction.BOTH
            selectQuery += "(edges.outV = '" + id + "' AND edges.inV = verticies.vertex_id) OR (edges.inV = '" + id + "' AND edges.outV = verticies.vertex_id)";
        }
        if (labels.length != 0) {
            selectQuery += " AND (";
            for (int i = 0; i < labels.length; i++) {
                selectQuery += " label = '" + labels[i] + "'";
                if (i < labels.length - 1)
                    selectQuery += " OR";
            }
            selectQuery += " )";
        }
        //System.out.println(selectQuery);
        Collection<Vertex> verticies = new ArrayList<Vertex>();
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(selectQuery);
            while (rs.next()) {
                String vertexId = rs.getString(1);
                HashMap<String, Object> prop = new ObjectMapper().readValue(rs.getString(2), HashMap.class);
                verticies.add(new PersistentVertex(vertexId, prop));
            }
        } catch (SQLException exception) {
            System.out.println(exception);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return verticies;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vertex) {
            Vertex vObj = (PersistentVertex) obj;
            if (this.id.equals(vObj.getId())) // proeprties의 비교는 보류
                return true;
        }
        return false;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException {
        String selectQuery = "SELECT a.vertex_id, a.properties FROM (SELECT verticies.vertex_id, verticies.properties FROM verticies JOIN edges WHERE ";
        if (direction == Direction.OUT) {
            selectQuery += "edges.outV = '" + id + "' AND edges.inV = verticies.vertex_id";
        } else if (direction == Direction.IN) {
            selectQuery += "edges.inV = '" + id + "' AND edges.outV = verticies.vertex_id";
        } else { // dierection == Direction.BOTH
            selectQuery += "((edges.outV = '" + id + "' AND edges.inV = verticies.vertex_id) OR (edges.inV = '" + id + "' AND edges.outV = verticies.vertex_id))";
        }
        if (labels.length != 0) {
            selectQuery += " AND (";
            for (int i = 0; i < labels.length; i++) {
                selectQuery += " label = '" + labels[i] + "'";
                if (i < labels.length - 1)
                    selectQuery += " OR";
            }
            selectQuery += " )";
        }
        selectQuery += ") AS a JOIN (SELECT vertex_id FROM vertex_properties WHERE key_ = '" + key + "' AND value_ = '" + value.toString() + "') AS b ON a.vertex_id = b.vertex_id";
        //System.out.println(selectQuery);
        Collection<Vertex> verticies = new ArrayList<Vertex>();
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(selectQuery);
            while (rs.next()) {
                String vertexId = rs.getString(1);
                HashMap<String, Object> prop = new ObjectMapper().readValue(rs.getString(2), HashMap.class);
                verticies.add(new PersistentVertex(vertexId, prop));
            }
        } catch (SQLException exception) {
            System.out.println(exception);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return verticies;
    }

    @Override
    public void remove() {

    }
}
