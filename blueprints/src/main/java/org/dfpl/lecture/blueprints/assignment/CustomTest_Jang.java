package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;
import org.dfpl.lecture.blueprints.persistent.PersistentGraph;

import java.sql.SQLException;

public class CustomTest_Jang {
    public static void main(String[] args) throws SQLException {
        PersistentGraph g = new PersistentGraph("root", "1234", "db1007");

        Vertex v1 = g.addVertex("1");
        Vertex v2 = g.addVertex("2");
        Vertex v3 = g.addVertex("3");
        g.addEdge(v1, v2, "like");
        Edge e1 = g.getEdge("1|like|2");
        Edge e2 = g.getEdge(v1, v2, "like");
        Edge e3 = g.getEdge(v1, v2, "like2");
        Edge e4 = g.getEdge("1|like|3");
        System.out.println(e2);
    }
}
