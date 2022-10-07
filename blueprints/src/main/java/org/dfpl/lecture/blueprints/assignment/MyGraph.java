package org.dfpl.lecture.blueprints.assignment;

import com.fasterxml.jackson.core.JsonParser;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;
import org.codehaus.jettison.json.JSONObject;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;

public class MyGraph implements Graph {
    static String id = "root";
    static String pw = "1234";
    public static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306", id, pw);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Statement stmt;

    static {
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MyGraph(Statement stmt) {
        this.stmt = stmt;
    }

    // vertex_id | properties(KEY VALUE)

    @Override
    public Vertex addVertex(String id) throws IllegalArgumentException, SQLException {
        Vertex v = null;
        try{
            String query = "INSERT INTO verticies values('"+ id + "', null);";
            stmt.executeQuery(query); // id duplication check ?
            return new MyVertex(id);
        } catch (SQLException e) {
            v = this.getVertex(id);
        }
        return v;
    }

    @Override
    public Vertex getVertex(String id) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM verticies WHERE vertex_id=\'" + id + "\';");
        // SELECT vertex_id FROM verticies WHERE vertex_id="v1";
        rs.next();
        System.out.println(rs.getString("vertex_id"));
        System.out.println(rs.getString("properties"));

//        JSONParser parser = new JSONParser();
//        JSONObject json = (JSONObject) parser.parse(stringToParse);

        // {"k1": "test1_value"} -> HashMap<String, Object> properties;
        // how to convert .. :(
        //Vertex v = new MyVertex(rs.getString("vertex_id"), rs.getObject("properties"));
        Vertex v = new MyVertex(rs.getString("vertex_id"));
        return v;
    }

    @Override
    public void removeVertex(Vertex vertex) throws SQLException {
//        stmt.executeQuery("DELETE FROM verticies WHERE ");
    }

    @Override
    public Collection<Vertex> getVertices() throws SQLException {

//        ResultSet rs = stmt.executeQuery("SELECT vertex_id FROM vertices");
//        while (rs.next()) {
//
//        }
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(String key, Object value) {
        return null;
    }

    @Override
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws IllegalArgumentException, NullPointerException {
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

    }
}
