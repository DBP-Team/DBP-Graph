package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class MyVertex implements Vertex {

    private String id;
    private HashMap<String, Object> properties;

    public MyVertex(String id) {
        this.id = id;
        this.properties = new HashMap<>();
    }

    public MyVertex(String id, HashMap<String, Object> properties) {
        this.id = id;
        this.properties = properties;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }


    @Override
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    @Override
    public void setProperty(String key, Object value) throws SQLException {
        /*
            jsonObject를 update 할 때는 2가지 경우의 수를 나눠야 합니다.
                a. jsonObject가 비어있을 때 => SET properties=JSON_OBJECT('key', 'value')
                b. jsonObject안에 값이 있을 때 => SET properties=JSON_SET(properties, '$.key', 'value')
            그래서 SELECT 로 비어있는지 확인하고 알맞게 query를 사용해야 합니다.

            값이 존재하는데 JSON_OBJECT를 쓰면 이전 값들이 날아가버리고
            값이 없는데 JSON_SET을 쓰면 업데이트가 되지 않습니다.

            (더 좋은 방법이 있을 순 있는데 일단 이렇게 해놨습니다.)
         */
        String executeQuery;
        String selectQuery = "SELECT COUNT(properties) FROM verticies WHERE vertex_id=\'" + this.id + "\';";
        ResultSet rs = MyGraph.stmt.executeQuery(selectQuery);
        rs.next();
        if (rs.getInt(1) == 0) {
            executeQuery = "UPDATE verticies SET properties=JSON_OBJECT(\'" + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\';";
        } else {
            executeQuery = "UPDATE verticies SET properties=JSON_SET(properties, \'$." + key + "\', \'" + value + "\') WHERE vertex_id=\'" + this.id + "\';";
        }
        System.out.println(executeQuery);
        MyGraph.stmt.executeUpdate(executeQuery);
        this.properties.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {

        return properties.remove(key);
    }

    @Override
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void remove() {

    }
}
