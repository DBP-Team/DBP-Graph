package org.dfpl.lecture.blueprints.persistent.team7;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PersistentGraph implements Graph {
    static String id;
    static String pw;
    static String dbName;
    public static Connection connection;
    public static Statement stmt;

    public PersistentGraph(String id, String pw, String dbName) {
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306", id, pw);
            stmt = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // String id, String pw, String dbName
        try {
            stmt.executeUpdate("CREATE OR REPLACE DATABASE " + dbName);
            stmt.executeUpdate("USE " + dbName);
            stmt.executeUpdate("CREATE OR REPLACE TABLE verticies (vertex_id int PRIMARY KEY, properties json)");
            stmt.executeUpdate("CREATE OR REPLACE TABLE edges (edge_id varchar(50) PRIMARY KEY, outV int, inV int, label varchar(50), properties json);");
            stmt.executeUpdate("CREATE OR REPLACE TABLE vertex_properties (key_ varchar(50), value_ varchar(50), vertex_id int, value_type varchar(10), " +
                    "FOREIGN KEY (vertex_id) REFERENCES verticies (vertex_id) ON DELETE CASCADE, PRIMARY KEY (vertex_id, key_));");
            stmt.executeUpdate("CREATE OR REPLACE TABLE edge_properties (key_ varchar(50), value_ varchar(50), edge_id varchar(50), value_type varchar(10), " +
                    "FOREIGN KEY (edge_id) REFERENCES edges (edge_id) ON DELETE CASCADE, PRIMARY KEY (edge_id, key_));");
            stmt.executeUpdate("CREATE INDEX edges_index1 ON edges (inV)");
            stmt.executeUpdate("CREATE INDEX edges_index2 ON edges (outV)");
            stmt.executeUpdate("CREATE INDEX edge_index ON edge_properties (key_, value_)");
            stmt.executeUpdate("CREATE INDEX vertex_index ON vertex_properties (key_, value_)");
            // CREATE OR REPLACE TABLE vertex_properties (key_ varchar(50), value_ varchar(50), vertex_id varchar(50)), FOREIGN KEY vertex_id REFERENCES verticies vertex_id

            /*
            index를 중지시키곳 싶을 경우
            stmt.executeUpdate("ALTER TABLE edge_properties DISABLE edge_index");
            stmt.executeUpdate("ALTER TABLE vertex_properties DISABLE vertex_index");

            현재 index 를 확인하고 싶을 경우
            show index from edge_properties;
            show index from vertex_properties;
             */
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // vertex_id | properties(KEY VALUE)

    static boolean isNumeric(String str){
        return str != null && str.matches("[0-9]+");
    }

    static String makeIntId(String id){
        int idNum = 0;
        for(int i=0;i<id.length();i++){
            idNum += id.charAt(i)*(i+1);
        }
        return Integer.toString(idNum);
    }

    @Override
    public Vertex addVertex(String id) throws IllegalArgumentException {
        Vertex v = null;
        if (id.contains("|"))
            throw new IllegalArgumentException("id cannot contain '|'");
        try {
            if(!isNumeric(id))
                id = makeIntId(id);
            String query = "INSERT IGNORE INTO verticies values(" + id + ", '{}');";
            stmt.executeQuery(query); // id duplication check ?
            return new PersistentVertex(id);
        } catch (SQLException e) {
            v = this.getVertex(id);
        }
        return v;
    }

    @Override
    public Vertex getVertex(String id) {
        ResultSet rs = null;
        try {
            if(!isNumeric(id))
                id = makeIntId(id);
            rs = stmt.executeQuery("SELECT vertex_id FROM verticies WHERE vertex_id=" + id + ";");
            if (rs.next())
                return (new PersistentVertex(id));
            else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeVertex(Vertex vertex) {
        try {
            stmt.executeQuery("DELETE FROM verticies WHERE vertex_id=" + vertex.getId() + ";");
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
    }

    @Override
    public Collection<Vertex> getVertices() {
//      Collection 설명
//      https://gangnam-americano.tistory.com/41

        Collection<Vertex> arrayList = new ArrayList<Vertex>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT vertex_id FROM verticies;");
            while (rs.next()) {
                String id = rs.getString(1);
                arrayList.add(new PersistentVertex(id));
            }
        }catch (SQLException e){
            System.out.println("Exception Occur: " + e);
        }

        return arrayList;
    }

    @Override
    public Collection<Vertex> getVertices(String key, Object value) { // 10_28/4:22/test 하지 못했음
        String query = "SELECT distinct vertex_id FROM vertex_properties WHERE key_=\"" + key + "\" and " + "value_=\"" + value + "\"";
        Collection<Vertex> vertexCollection = new ArrayList<Vertex>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String edgeID = rs.getString(1);
                Vertex v = getVertex(edgeID);
                vertexCollection.add(v);
            }
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
        return vertexCollection;
    }

    public void validEdgeArgumentCheck(Vertex outVertex, Vertex inVertex, String label) {
        if (label.contains("|"))
            throw new IllegalArgumentException("label cannot contain '|'");
        if (outVertex == null)
            throw new NullPointerException("outVertex cannot be null");
        if (inVertex == null)
            throw new NullPointerException("inVertex cannot be null");
    }

    public String makeID(String outVertexId, String inVertexId, String label)
            throws NullPointerException, IllegalArgumentException {
        return outVertexId + "|" + label + "|" + inVertexId;
    }

    @Override
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label)
            throws IllegalArgumentException, NullPointerException {
        validEdgeArgumentCheck(outVertex, inVertex, label);
        String id = makeID(outVertex.getId(), inVertex.getId(), label);
        Edge edge = getEdge(id);
        if (edge != null)
            return edge;
        try {
            edge = new PersistentEdge(id, outVertex, inVertex, label);
            String query = "INSERT IGNORE INTO edges VALUES('" + id + "', " + outVertex.getId() + ", " + inVertex.getId() + ", '" + label + "', '{}');";
            stmt.executeUpdate(query);

        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
        return edge;
    }

    @Override
    public Edge getEdge(Vertex outVertex, Vertex inVertex, String label) {
        String edgeID = makeID(outVertex.getId(), inVertex.getId(), label);
        String query = "SELECT * FROM edges WHERE edge_id=\'" + edgeID + "\'";
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next())
                return (new PersistentEdge(edgeID, outVertex, inVertex, label));
            else
                return null;
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
        return null;
    }

    @Override
    public Edge getEdge(String id) {
        String query = "SELECT * FROM edges WHERE edge_id=\'" + id + "\'";
        HashMap propertiesMap = null;
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                String[] arr = id.split("\\|");
                String outVertexString = rs.getString(2);
                String inVertexString = rs.getString(3);
                String label = rs.getString(4);

                Vertex outVertex = getVertex(outVertexString);
                Vertex inVertex = getVertex(inVertexString);

                return (new PersistentEdge(id, outVertex, inVertex, label));
            } else
                return null;
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
        return null;
    }

    @Override
    public void removeEdge(Edge edge) {
        try {
            stmt.executeQuery("DELETE FROM edges WHERE edge_id=\'" + edge.getId() + "\';");
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
    }

    @Override
    public Collection<Edge> getEdges() {
        String query = "SELECT * FROM edges";
        Collection<Edge> edgeCollection = new ArrayList<Edge>();

        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String edgeID = rs.getString(1);
                String outVertexId = rs.getString(2);
                String inVertexId = rs.getString(3);
                String label = rs.getString(4);

                Vertex outVertex = getVertex(outVertexId);
                Vertex inVertex = getVertex(inVertexId);

                edgeCollection.add(new PersistentEdge(edgeID, outVertex, inVertex, label));
            }
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }

        return edgeCollection;
    }

    @Override
    public Collection<Edge> getEdges(String key, Object value) { // 10_28/4:22/test 하지 못했음
        String query = "SELECT distinct edge_id FROM edge_properties WHERE key_=\"" + key + "\" and value_=\"" + value + "\"";
        Collection<Edge> edgeCollection = new ArrayList<Edge>();

        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String edgeID = rs.getString(1);
                Edge e = getEdge(edgeID);
                edgeCollection.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }

        return edgeCollection;
    }

    @Override
    public void shutdown() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
    }
}
