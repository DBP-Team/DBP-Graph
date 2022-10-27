package org.dfpl.lecture.blueprints.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

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
        try{
            stmt.executeUpdate("CREATE OR REPLACE DATABASE " + dbName);
            stmt.executeUpdate("USE "+ dbName);
            stmt.executeUpdate("CREATE OR REPLACE TABLE verticies (vertex_id varchar(50) PRIMARY KEY, properties json)");
            stmt.executeUpdate("CREATE OR REPLACE TABLE edge (id varchar(50), outV varchar(50), inV varchar(50), label varchar(50), properties json);");
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
            String query = "INSERT IGNORE INTO verticies values('" + id + "', null);";
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
    public Collection<Vertex> getVertices(String key, Object value) {
        Collection<Vertex> arrayList = new ArrayList<Vertex>();
        try {
            String query = "SELECT vertex_id FROM verticies WHERE JSON_VALUE(properties, \'$." + key + "\')= \'" + value + "\';";
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                arrayList.add(this.getVertex(rs.getString(1)));
            }
        } catch (Exception e){
            System.out.println("Exception Occur: " + e);
        }
        return arrayList;
    }

    @Override
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws IllegalArgumentException, NullPointerException {
        String query = "INSERT INTO edge ('" + inVertex.getId() + "|" + label + "|" + inVertex.getId() + "', '" + inVertex.getId() + "', '" + inVertex.getId() + "', '" + label + "', null);";
        return null;
    }

    @Override
    public Edge getEdge(Vertex outVertex, Vertex inVertex, String label) {
        return null;
    }

    @Override
    public Edge getEdge(String id) {
        return null;
    }

    @Override
    public void removeEdge(Edge edge) {

    }

    @Override
    public Collection<Edge> getEdges() {
        return null;
    }

    @Override
    public Collection<Edge> getEdges(String key, Object value) {
        return null;
    }

    @Override
    public void shutdown() throws SQLException {
        connection.close();
    }
}
