package org.dfpl.lecture.blueprints.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.*;
<<<<<<< HEAD
import java.util.*;
=======
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
>>>>>>> 58b2513b603a077a4b295ba69c8875ebc2b6a97d

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
            stmt.executeUpdate("CREATE OR REPLACE TABLE verticies (vertex_id varchar(50) PRIMARY KEY, properties json)");
            stmt.executeUpdate("CREATE OR REPLACE TABLE edges (edge_id varchar(50) PRIMARY KEY, outV varchar(50), inV varchar(50), label varchar(50), properties json);");
            stmt.executeUpdate("CREATE OR REPLACE TABLE vertex_properties (key_ varchar(50), value_ varchar(50), vertex_id varchar(50))");
            stmt.executeUpdate("CREATE OR REPLACE TABLE edge_properties (key_ varchar(50), value_ varchar(50), edge_id varchar(50))");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // vertex_id | properties(KEY VALUE)

    @Override
    public Vertex addVertex(String id) throws IllegalArgumentException, SQLException {
        Vertex v = null;
        if (id.contains("|"))
            throw new IllegalArgumentException("id cannot contain '|'");
        try {
            String query = "INSERT IGNORE INTO verticies values('" + id + "', '{}');";
            stmt.executeQuery(query); // id duplication check ?
            return new PersistentVertex(id);
        } catch (SQLException e) {
            v = this.getVertex(id);
        }
        return v;
    }

    @Override
    public Vertex getVertex(String id) throws SQLException {
        /**
         * mariaDB에서 vertices 값들을 불러와서
         * 그 중 properties를 바로 HashMap으로 변환시켜주고
         * MyVertex 생성자에 같이 넣어줍니다.
         */
        HashMap<String, Object> map = null;
        ResultSet rs = stmt.executeQuery("SELECT * FROM verticies WHERE vertex_id=\'" + id + "\';");
        try {
            if (rs.next())
                map = new ObjectMapper().readValue(rs.getString("properties"), HashMap.class);
            else
                return null;
        } catch (NullPointerException e) {
            /**
             * new ObjectMapper().readValue(null, HashMap.class); 시 NullPointerException
             */
            Vertex v = new PersistentVertex(id);
            return v;
        } catch (Exception e) {
            System.out.println("Exception Occur: " + e);
            /**
             * JsonMappingException
             * JsonParseException
             * IOException
             */
            System.out.println("occur Exception: " + e);
        }
        Vertex v = new PersistentVertex(id, map);
        return v;
    }

    @Override
    public void removeVertex(Vertex vertex) throws SQLException {
        stmt.executeQuery("DELETE FROM verticies WHERE vertex_id=\'" + vertex.getId() + "\';");
    }

    @Override
    public Collection<Vertex> getVertices() throws SQLException {
//      Collection 설명
//      https://gangnam-americano.tistory.com/41

        Collection<Vertex> arrayList = new ArrayList<Vertex>();
        ResultSet rs = stmt.executeQuery("SELECT vertex_id FROM verticies;");
        while (rs.next()) {
            arrayList.add(this.getVertex(rs.getString(1)));
        }
        return arrayList;
    }

    @Override
    public Collection<Vertex> getVertices(String key, Object value) throws SQLException { // 10_28/4:22/test 하지 못했음
        String query = "SELECT * FROM vertex_properties WHERE key_=\"" + key + "\" and " + "value_=\"" + value + "\"";
        ResultSet rs = stmt.executeQuery(query);
        Collection<Vertex> vertexCollection = new ArrayList<Vertex>();
        while (rs.next()) {
            String edgeID = rs.getString(3);
            Vertex v = getVertex(edgeID);
            vertexCollection.add(v);
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
            throws IllegalArgumentException, NullPointerException, SQLException {
        validEdgeArgumentCheck(outVertex, inVertex, label);
        String id = makeID(outVertex.getId(), inVertex.getId(), label);
        Edge edge = getEdge(id);
        if (edge != null)
            return edge;
        try {
            edge = new PersistentEdge(id, outVertex, inVertex, label);
            String query = "INSERT IGNORE INTO edges VALUES('" + id + "', '" + outVertex.getId() + "', '" + inVertex.getId() + "', '" + label + "', null);";
            stmt.executeUpdate(query);

        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
        return edge;
    }

    @Override
    public Edge getEdge(Vertex outVertex, Vertex inVertex, String label) throws SQLException {
        String edgeID = makeID(outVertex.getId(), inVertex.getId(), label);
        String query = "SELECT * FROM edges WHERE edge_id=\'" + edgeID + "\'";
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next())
            return (new PersistentEdge(edgeID, outVertex, inVertex, label));
        else
            return null;

    }

    @Override
    public Edge getEdge(String id) throws SQLException {
        String query = "SELECT * FROM edges WHERE edge_id=\'" + id + "\'";
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
            String[] arr = id.split("\\|");
            String outVertexString = arr[0];
            String inVertexString = arr[2];
            String label = arr[1];

            Vertex outVertex = getVertex(outVertexString);
            Vertex inVertex = getVertex(inVertexString);

            return (new PersistentEdge(id, outVertex, inVertex, label));
        } else
            return null;

    }

    @Override
    public void removeEdge(Edge edge) {

    }

    @Override
    public Collection<Edge> getEdges() throws SQLException {
        String query = "SELECT * FROM edges";
        ResultSet rs = stmt.executeQuery(query);
        Collection<Edge> edgeCollection = new ArrayList<Edge>();
        while (rs.next()) {
            String edgeID = rs.getString(1);
            String outVertexId = rs.getString(2);
            String inVertexId = rs.getString(3);
            String label = rs.getString(4);

            Vertex outVertex = getVertex(outVertexId);
            Vertex inVertex = getVertex(inVertexId);

            edgeCollection.add(new PersistentEdge(edgeID, outVertex, inVertex, label));
        }
        return edgeCollection;
    }

    @Override
    public Collection<Edge> getEdges(String key, Object value) throws SQLException { // 10_28/4:22/test 하지 못했음
        String query = "SELECT * FROM edge_properties WHERE key_=\"" + key + "\" and value_=\"" + value + "\"";
        ResultSet rs = stmt.executeQuery(query);
        Collection<Edge> edgeCollection = new ArrayList<Edge>();
        while (rs.next()) {
            String edgeID = rs.getString(3);
            Edge e = getEdge(edgeID);
            edgeCollection.add(e);
        }
        return edgeCollection;
    }

    @Override
    public void shutdown() throws SQLException {
        connection.close();
    }
}
