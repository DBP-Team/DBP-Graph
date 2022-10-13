package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;
import org.dfpl.lecture.blueprints.persistent.PersistentEdge;
import org.dfpl.lecture.blueprints.persistent.PersistentGraph;

import java.sql.SQLException;

public class App {

    static String id = "root";
    static String pw = "1234";

    public static void main(String[] args) throws SQLException {


        PersistentGraph g = new PersistentGraph();
        Vertex v1 = g.addVertex("v1");
        Vertex v2 = g.addVertex("v2");
        Vertex vv2 = g.addVertex("v2"); // duplicate check
        Vertex v3 = g.addVertex("v3");

        v3.setProperty("k1", "test1_value");
        v3.setProperty("k2", "test2_value");
        v3.setProperty("k2", "test2_value2");
        v3.setProperty("k3", "test3_value");
        Edge e1 = new PersistentEdge("v1|likes|v3", v1, v3, "likes");

        System.out.println("\ngetPropertyKeys()");
        Vertex check_v3 = g.getVertex("v3");
        System.out.println("v3's keys: " + check_v3.getPropertyKeys());
        check_v3.getPropertyKeys().forEach(e -> {
            System.out.println("v3[" + e + "]: " + check_v3.getProperty(e));
        });

        System.out.println("\nremoveVertex()");
        System.out.println("v2: " + g.getVertex("v2"));
        g.removeVertex(g.getVertex("v2"));
        System.out.println("v2: " + g.getVertex("v2"));


        System.out.println("\ngetVertices()");
        g.getVertices().forEach(e -> {
            System.out.println("ID: " + e.getId());
            e.getPropertyKeys().forEach(k -> {
                System.out.println(e.getId() + "[" + k + "]: " + e.getProperty(k));
            });
        });

        System.out.println("\ngetVertices(key, value)");
        g.getVertices("k1", "test1_value").forEach(e -> {
            System.out.println(e.getId());
        });

        System.out.println("\nremoveProperty()");
        System.out.println("Remove Property v3.k1");
        v3.removeProperty("k1");
        v3.getPropertyKeys().forEach(k -> {
            System.out.println(v3.getId() + "[" + k + "]: " + check_v3.getProperty(k));
        });
        v3.removeProperty("k");

        g.shutdown();
    }
}
