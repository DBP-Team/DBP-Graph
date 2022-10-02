package org.dfpl.lecture.blueprints.memory2;

import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

public class App {
    public static void main(String[] args) {
        MyGraph g = new MyGraph();

        Vertex a = g.addVertex("A");
        Vertex b = g.addVertex("B");
        Vertex c = g.addVertex("C");
        System.out.println(g.getVertex("A"));
        System.out.println(g.getVertex("B"));
        System.out.println(g.getVertex("C"));
        System.out.println(g.getVertices());

        Edge e1 = g.addEdge(a, b, "likes");
        Edge e2 = g.addEdge(a, c, "likes");

        System.out.println(g.getEdge(a, b, "likes"));
        System.out.println(g.getEdge(a, b, "loves"));
        System.out.println(g.getEdges());
    }
}
