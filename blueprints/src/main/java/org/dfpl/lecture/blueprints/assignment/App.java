package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;
import org.dfpl.lecture.blueprints.persistent.PersistentEdge;
import org.dfpl.lecture.blueprints.persistent.PersistentGraph;
import org.dfpl.lecture.blueprints.persistent.PersistentVertex;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class App {

    static String id = "root";
    static String pw = "0210";

    public static void main(String[] args) throws SQLException {


        PersistentGraph g = new PersistentGraph("root", "1234", "db1007");
//        Vertex v1 = g.addVertex("v1");
//        Vertex v2 = g.addVertex("v2");
//        Vertex vv2 = g.addVertex("v2"); // duplicate check
//        Vertex v3 = g.addVertex("v3");
//
//        v3.setProperty("k1", "test1_value");
//        v3.setProperty("k2", "test2_value");
//        v3.setProperty("k2", "test2_value2");
//        v3.setProperty("k3", "test3_value");
//        Edge e1 = new PersistentEdge("v1|likes|v3", v1, v3, "likes");
//
//        System.out.println("\ngetPropertyKeys()");
//        Vertex check_v3 = g.getVertex("v3");
//        System.out.println("v3's keys: " + check_v3.getPropertyKeys());
//        check_v3.getPropertyKeys().forEach(e -> {
//            System.out.println("v3[" + e + "]: " + check_v3.getProperty(e));
//        });
//
//        System.out.println("\nremoveVertex()");
//        System.out.println("v2: " + g.getVertex("v2"));
//        g.removeVertex(g.getVertex("v2"));
//        System.out.println("v2: " + g.getVertex("v2"));
//
//
//        System.out.println("\ngetVertices()");
//        g.getVertices().forEach(e -> {
//            System.out.println("ID: " + e.getId());
//            e.getPropertyKeys().forEach(k -> {
//                System.out.println(e.getId() + "[" + k + "]: " + e.getProperty(k));
//            });
//        });
//
//        System.out.println("\ngetVertices(key, value)");
//        g.getVertices("k1", "test1_value").forEach(e -> {
//            System.out.println(e.getId());
//        });
//
//        System.out.println("\nremoveProperty()");
//        System.out.println("Remove Property v3.k1");
//        v3.removeProperty("k1");
//        v3.getPropertyKeys().forEach(k -> {
//            System.out.println(v3.getId() + "[" + k + "]: " + check_v3.getProperty(k));
//        });
//        v3.removeProperty("k");

        Vertex a = new PersistentVertex("a");
        Vertex b = g.addVertex("b");
        Vertex c = g.addVertex("c");
        Vertex d = g.addVertex("d");
        Vertex e = g.addVertex("e");


        Edge ab = g.addEdge(a, b, "k");
        Edge ac = g.addEdge(a, c, "l");
        Edge da = g.addEdge(d, a, "l");
        Edge ea = g.addEdge(e, a, "l");

        Collection<Vertex> verticies = new ArrayList<>();
        verticies.add(a);
        verticies.add(b);
        verticies.add(c);
        System.out.println(a.getVertices(Direction.OUT, "l").contains(b));
        System.out.println(a.getVertices(Direction.BOTH, "k").contains(b));
        System.out.println(a.getVertices(Direction.IN).contains(b));
        System.out.println(a.getVertices(Direction.IN).contains(d));

        b.setProperty("k1", true);
        d.setProperty("k1", false);
        System.out.println();
        System.out.println(a.getVertices(Direction.OUT, "k1", true, "k").contains(b)); // true
        // value가 String이면 label로 인식
        System.out.println(a.getVertices(Direction.IN, "k1", true).contains(d)); // false
        System.out.println(a.getVertices(Direction.BOTH, "k1", false, "l").contains(d)); // true



        g.shutdown();
    }
}
