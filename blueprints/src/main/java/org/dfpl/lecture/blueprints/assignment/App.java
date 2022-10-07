package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

    static String id = "root";
    static String pw = "1234";

    public static void main(String[] args) throws SQLException {
        Connection connection = MyGraph.connection;
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("CREATE OR REPLACE DATABASE db1007");
        stmt.executeUpdate("USE db1007");
        stmt.executeUpdate("CREATE OR REPLACE TABLE verticies (vertex_id varchar(50), properties json)");

        MyGraph g = new MyGraph(stmt);
        Vertex v1 = g.addVertex("v1");
        Vertex v2 = g.addVertex("v2");
        Vertex vv2 = g.addVertex("v2"); // duplicate check
        Vertex v3 = g.addVertex("v3");
        
        // insert into json_test values (2 , json_object('k1' , 'test1_value')) ;
        v3.setProperty("k1", "test1_value");
        Edge e1 = new MyEdge("v1|likes|v3", v1, v3, "likes");

        g.getVertex("v3");

        connection.close();
    }
}
