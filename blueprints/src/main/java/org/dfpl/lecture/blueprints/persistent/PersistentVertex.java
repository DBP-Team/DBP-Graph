package org.dfpl.lecture.blueprints.persistent;

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

    @Override
    public String toString() {
        return "id: " + id + "keySet" + properties.keySet();
    }

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
    public void setProperty(String key, Object value) {
        String verticiesUpdateQuery = "UPDATE verticies SET properties=JSON_SET(properties," +
                " \'$." + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\'";
        String propertiesInsertQuery = "INSERT IGNORE INTO vertex_properties VALUES('" + key + "', '" + value + "', '" + this.id + "')";

        try {
            PersistentGraph.stmt.executeUpdate(verticiesUpdateQuery);
            PersistentGraph.stmt.executeUpdate(propertiesInsertQuery);
            // 1. insert 에서 exception 발생 시 update
            // 2. select 로 이미 있는지 알아낸 다음 update
        } catch (SQLException e) {
            String propertiesUpdateQuery = "UPDATE vertex_properties SET value_='" + value + "' WHERE vertex_id = '"
                    + this.id + "' AND key_ ='" + key + "'";
            try {
                PersistentGraph.stmt.executeUpdate(propertiesUpdateQuery);
            } catch (SQLException e2) {
                System.out.println(e2);
            }
        }
        properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        String query = "UPDATE verticies SET properties=JSON_REMOVE(properties, \'$." + key + "\') WHERE vertex_id=\'" + this.id + "\';";
        try {
            PersistentGraph.stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
        return properties.remove(key);
    }

    @Override
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException {

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

        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            Edge e = null;

            while(rs.next()){
                String edge_id = rs.getString(1);
                //String outVertexName = rs.getString(2);
                String inVertexName = rs.getString(3);
                String label = rs.getString(4);
                String prop = rs.getString(5);

                Vertex outV = this;
                Vertex inV = getVertex(inVertexName);

                if(prop != null){
                    HashMap<String, Object> map = new ObjectMapper().readValue(rs.getString(5), HashMap.class);
                    e = new PersistentEdge(edge_id, outV, inV, label, map);
                }
                else
                    e = new PersistentEdge(edge_id, outV, inV, label);

                edgeCollection.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        } catch (IOException e) {
            System.out.println("Exception Occur: " + e);
        }


        return edgeCollection;
    }

    private Vertex getVertex(String VertexName) throws SQLException, IOException {
        HashMap<String, Object> prop = null;
        ResultSet rs = PersistentGraph.stmt.executeQuery("SELECT properties FROM verticies WHERE vertex_id =\'" + VertexName + "\';");

        try{
            if(rs.next())
                prop = new ObjectMapper().readValue(rs.getString(1), HashMap.class);
            else  // 행 자체가 없을 때 ..?
                return null;
        } catch (NullPointerException e){  // properties만 없을 때
            Vertex v = new PersistentVertex(VertexName);
            return v;
        }

        Vertex v = new PersistentVertex(VertexName, prop);

        return v;
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
    public Collection<Vertex> getTwoHopVertices(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
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
        String query = "DELETE FROM verticies WHERE vertex_id='" + this.id + "'";
        try {
            PersistentGraph.stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
    }
}
