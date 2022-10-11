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

        v3.setProperty("k1", "test1_value");
        v3.setProperty("k2", "test2_value");
        v3.setProperty("k2", "test2_value2");
        v3.setProperty("k3", "test3_value");
        Edge e1 = new MyEdge("v1|likes|v3", v1, v3, "likes");

        Vertex check_v3 = g.getVertex("v3");
        System.out.println("v3's keys: " + check_v3.getPropertyKeys());
        check_v3.getPropertyKeys().forEach(e -> {
            System.out.println("v3[" + e + "]: " + check_v3.getProperty(e));
        });
        System.out.println("v2: " + g.getVertex("v2"));
        g.removeVertex(g.getVertex("v2"));
        System.out.println("v2: " + g.getVertex("v2"));

        connection.close();
    }
}
