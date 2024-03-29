package org.dfpl.lecture.blueprints.persistent.team7;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PersistentVertex implements Vertex {

    private String id;

    @Override
    public String toString() {

        return "id: " + id + "keySet" + getPropertyKeys();
    }

    public PersistentVertex(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getProperty(String key) {
        String query = "SELECT value_, value_type FROM vertex_properties WHERE key_ = '" + key + "';";
        Object value = null;
        try{
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            while(rs.next()) {
                if (rs.getString(2).equals("Boolean"))
                    value = rs.getBoolean(1);
                else if (rs.getString(2).equals("Integer"))
                    value = rs.getInt(1);
                else if (rs.getString(2).equals("Double"))
                    value = rs.getDouble(1);
                else // == "String"
                    value = rs.getString(1);
            }
        }catch (SQLException e){
            System.out.println(e);
        }
        return value;
    }

    @Override
    public Set<String> getPropertyKeys() {

        Set<String> keySet = new HashSet<>();

        String query = "SELECT key_ FROM vertex_properties;";
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            while(rs.next()){
                String key = rs.getString(1);
                keySet.add(key);
            }
        }catch(SQLException e){
            System.out.println(e);
        }

        return keySet;
    }

    @Override
    public void setProperty(String key, Object value) {
        String verticiesUpdateQuery = "UPDATE verticies SET properties=JSON_SET(properties," +
                " \'$." + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\'";
        String propertiesInsertQuery = "INSERT IGNORE INTO vertex_properties VALUES('" + key + "', '" + value + "', '" + this.id + "', '" + value.getClass().getSimpleName() + "');";

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
    }

    @Override
    public Object removeProperty(String key) {

        HashMap<String, Object> map = null;
        Object returnObj = null;
        String query = "SELECT properties FROM verticies WHERE vertex_id = '" + this.id + "';";

        String updateVerticiesQuery = "UPDATE verticies SET properties=" +
                "JSON_REMOVE(properties, \'$." + key + "\') WHERE vertex_id=\'" + this.id + "\';";
        String deletePropertiesQuery = "DELETE FROM vertex_properties WHERE vertex_id = '"
                + this.id + "' AND key_ = '" + key + "'";
        try{
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            PersistentGraph.stmt.executeUpdate(updateVerticiesQuery);
            PersistentGraph.stmt.executeUpdate(deletePropertiesQuery);

            if (rs.next()) {
                String prop = rs.getString(1);
                if (prop != null)
                    map = new ObjectMapper().readValue(prop, HashMap.class);
            }
            else
                return null; // 사실 도달 할 일 없음
            returnObj = map.remove(key);
        }catch (SQLException e){
            System.out.println(e);
        } catch (Exception e1){
            System.out.println(e1);
        }

        return returnObj;
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

        query = "SELECT edge_id, inV, outV, label FROM edges WHERE " + condition + "\'" + this.id + "\'";

        if (labels.length > 0) {
            query += " AND (";
            for (int i = 0; i < labels.length; i++) {
                query += "label = '" + labels[i] + "'";

                if (i != labels.length - 1) {
                    query += " OR ";
                }
            }
            query += ")";
        }
        query += ";";

        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);

            while (rs.next()) {
                String edge_id = rs.getString(1);
                String inVertexName = rs.getString(2);
                String outVertexName = rs.getString(3);
                String label = rs.getString(4);

                Vertex outV;
                Vertex inV;
                if (direction == Direction.OUT) {
                    outV = this;
                    inV = (new PersistentVertex(inVertexName));
                }
                else{ // IN
                    outV = (new PersistentVertex(outVertexName));
                    inV = this;
                }

                Edge e = new PersistentEdge(edge_id, outV, inV, label);

                edgeCollection.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }

        return edgeCollection;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException {
        String selectQuery = "";
        if (direction == Direction.OUT) {
            selectQuery = "SELECT vertex_id, properties FROM verticies NATURAL JOIN (SELECT inV AS vertex_id FROM edges WHERE outV = '" + id + "'";
        } else { //direction == Direction.IN
            selectQuery = "SELECT vertex_id, properties FROM verticies NATURAL JOIN (SELECT outV AS vertex_id FROM edges WHERE inV = '" + id + "'";
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
        selectQuery += ") AS a";
        Collection<Vertex> verticies = new ArrayList<Vertex>();
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(selectQuery);
            HashMap<String, Object> map = null;
            while (rs.next()) {
                String vertexId = rs.getString(1);
                verticies.add(new PersistentVertex(vertexId));
            }
        } catch (SQLException exception) {
            System.out.println(exception);
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
        Collection<Vertex> vCol1 = new ArrayList<Vertex>();
        // example
        // SELECT edges.inV AS result
        // FROM (SELECT edges.inV
        //       FROM (SELECT DISTINCT verticies.vertex_id, verticies.properties
        //             FROM verticies JOIN edges
        //             WHERE edges.outV = '9' AND edges.inV = verticies.vertex_id) AS r1
        //       JOIN edges WHERE r1.vertex_id = edges.outV) AS r2
        // JOIN edges WHERE r2.inV = edges.outV;
        if(direction == Direction.OUT) {
            String query = "SELECT edges.inV AS result FROM" +
                    " (SELECT edges.inV FROM (SELECT DISTINCT verticies.vertex_id, verticies.properties FROM verticies JOIN edges WHERE edges.outV = '" + this.id + "' AND edges.inV = verticies.vertex_id) AS r1 JOIN edges" +
                    " WHERE r1.vertex_id = edges.outV) AS r2 JOIN edges WHERE r2.inV = edges.outV";
            if (labels.length > 0) {
                query += " AND (";
                for (int i = 0; i < labels.length; i++) {
                    query += "label = '" + labels[i] + "'";
                    if (i != labels.length - 1) {
                        query += " OR ";
                    }
                }
                query += ")";
            }
            query += ";";

            try {
                ResultSet rs = PersistentGraph.stmt.executeQuery(query);
                while (rs.next()) {
                    String vId = rs.getString(1);
                    vCol1.add((new PersistentVertex(vId)));
                }
            }catch (SQLException e){
                System.out.println("Exception Occur: " + e);
            }
        }

        else{// IN
            String query = "SELECT edges.outV AS result FROM" +
                    " (SELECT edges.outV FROM (SELECT verticies.vertex_id, verticies.properties FROM verticies JOIN edges WHERE edges.inV = '"+ this.id+"' AND edges.outV = verticies.vertex_id) AS r1 JOIN edges" +
                    " WHERE r1.vertex_id = edges.inV) as r2 JOIN edges WHERE r2.outV = edges.inV";
            if (labels.length > 0) {
                query += " AND (";
                for (int i = 0; i < labels.length; i++) {
                    query += "label = '" + labels[i] + "'";

                    if (i != labels.length - 1) {
                        query += " OR ";
                    }
                }
                query += ")";
            }
            query += ";";

            try {
                ResultSet rs = PersistentGraph.stmt.executeQuery(query);

                while (rs.next()) {
                    String vId = rs.getString(1);
                    vCol1.add((new PersistentVertex(vId)));
                }
            }catch (SQLException e){
                System.out.println("Exception Occur: " + e);
            }
        }

        return vCol1;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException {
        String selectQuery = "";
        if (labels.length != 0) {
            if(direction == Direction.OUT)
                selectQuery = "SELECT vertex_id, properties FROM verticies AS a NATURAL JOIN (SELECT SUBSTRING_INDEX(edge_id, '|', -1) AS vertex_id FROM edge_properties WHERE key_ = '" + key + "' AND value_ = '" + value + "' INTERSECT SELECT inV FROM edges AS id WHERE (outV = '" + id + "') AND (";
           else // Direction.IN
                selectQuery = "SELECT vertex_id, properties FROM verticies AS a NATURAL JOIN (SELECT SUBSTRING_INDEX(a.edge_id, '|', 1) AS vertex_id FROM edge_properties WHERE key_ = '" + key + "' AND value_ = '" + value + "' INTERSECT SELECT inV FROM edges AS id WHERE (inV = '" + id + "') AND (";
            for (int i = 0; i < labels.length; i++) {
                selectQuery += " label = '" + labels[i] + "'";
                if (i < labels.length - 1)
                    selectQuery += " OR";
            }
            selectQuery += " )) AS b ON a.vertex_id = b.id";
        } else {
            if (direction == Direction.OUT) {
                selectQuery = "SELECT vertex_id, properties FROM verticies AS a NATURAL JOIN (SELECT SUBSTRING_INDEX(edge_id, '|', -1) AS vertex_id FROM edge_properties WHERE edge_id LIKE '" + id + "|%' AND (key_ = '" + key + "' AND value_ = '" + value.toString() + "')) AS b";
            } else { // Direction.IN
                selectQuery = "SELECT vertex_id, properties FROM verticies AS a NATURAL JOIN (SELECT SUBSTRING_INDEX(edge_id, '|', 1) AS vertex_id FROM edge_properties WHERE edge_id LIKE '%|" + id + "' AND (key_ = '" + key + "' AND value_ = '" + value.toString() + "')) AS b";

            }
        }
        //System.out.println(selectQuery);
        Collection<Vertex> verticies = new ArrayList<Vertex>();
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(selectQuery);
            HashMap<String, Object> map = null;
            while (rs.next()) {
                String vertexId = rs.getString(1);
                verticies.add(new PersistentVertex(vertexId));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }catch (Exception e) {
            System.out.println(e);
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
