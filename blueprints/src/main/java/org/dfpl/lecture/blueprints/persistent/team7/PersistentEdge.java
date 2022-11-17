package org.dfpl.lecture.blueprints.persistent.team7;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Vertex;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PersistentEdge implements Edge {
    private String id;
    private Vertex outV;
    private Vertex inV;
    private String label;
//    private HashMap<String, Object> properties;

    public PersistentEdge(String id, Vertex outV, Vertex inV, String label) {
        this.id = id;
        this.outV = outV;
        this.inV = inV;
        this.label = label;
    }

    @Override
    public String toString() {
        return id + "/" + outV.getId() + "/" + inV.getId() + "/" + label;
    }

    @Override
    public Vertex getVertex(Direction direction) {
        if (direction.equals(Direction.OUT))
            return outV;
        else if (direction.equals(Direction.IN))
            return inV;
        else throw new IllegalArgumentException("Direction.BOTH is not allowed");

    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void remove() {
        String query = "DELETE FROM edges WHERE edge_id='" + this.id + "'";
        try {
            PersistentGraph.stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Edge){
            Edge eObj = (PersistentEdge) obj;

            if(this.id.equals(eObj.getId()))
                return true;
//            if(this.id.equals(eObj.getId())
//                    && this.outV.equals(eObj.getVertex(Direction.OUT))
//                    && this.inV.equals(eObj.getVertex(Direction.IN))) {
//                return true;
//            }

//                System.out.println("\n" + "id : " + eObj.getId());
//                System.out.println("outV : " + (eObj.getVertex(Direction.OUT)).getId());
//                System.out.println("inV : " + (eObj.getVertex(Direction.IN)).getId());

        }
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getProperty(String key) {
        String query = "SELECT value_, value_type FROM edge_properties WHERE key_ = '" + key + "';";
        Object value = null;
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            while(rs.next()) {
                if (rs.getString(2).equals("Boolean"))
                    value = rs.getBoolean(1);
                else if (rs.getString(2).equals("Integer"))
                    value = rs.getInt(1);
                else if (rs.getString(2).equals("Double"))
                    value = rs.getDouble(1);
                else // == "String"
                    value = rs.getString(1);
            }

        }catch (SQLException e){
            System.out.println(e);
        }
        return value;
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> keySet = new HashSet<>();

        String query = "SELECT key_ FROM edge_properties;";
        try {
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            while(rs.next()){
                String key = rs.getString(1);
                keySet.add(key);
            }
        }catch(SQLException e){
            System.out.println(e);
        }

        return keySet;
    }

    @Override
    public void setProperty(String key, Object value) {

        String edgesUpdateQuery = "UPDATE edges SET properties=JSON_SET(properties," +
                " \'$." + key + "\', \'" + value + "\') WHERE edge_id=\'" + this.id + "\'";
        String propertiesInsertQuery = "INSERT IGNORE INTO edge_properties VALUES('" + key + "', '" + value + "', '" + this.id + "', '" + value.getClass().getSimpleName() + "');";

        try {
            PersistentGraph.stmt.executeUpdate(edgesUpdateQuery);
            PersistentGraph.stmt.executeUpdate(propertiesInsertQuery);
            // 1. insert 에서 exception 발생 시 update
            // 2. select 로 이미 있는지 알아낸 다음 update
        } catch (SQLException e) {
            String propertiesUpdateQuery = "UPDATE edge_properties SET value_='" + value + "' WHERE edge_id = '"
                    + this.id + "' AND key_ ='" + key + "'";
            try {
                PersistentGraph.stmt.executeUpdate(propertiesUpdateQuery);
            } catch (SQLException e2) {
                System.out.println(e2);
            }
        }
    }

    @Override
    public Object removeProperty(String key) {

        String updateVerticiesQuery = "UPDATE edges SET properties=" +
                "JSON_REMOVE(properties, \'$." + key + "\') WHERE edge_id=\'" + this.id + "\';";
        String deletePropertiesQuery = "DELETE FROM edge_properties WHERE edge_id = '"
                + this.id + "' AND key_ = '" + key + "'";

        try {
            PersistentGraph.stmt.executeUpdate(updateVerticiesQuery);
            PersistentGraph.stmt.executeUpdate(deletePropertiesQuery);
        } catch (SQLException e) {
            System.out.println("Exception Occur: " + e);
        }

        HashMap<String, Object> map = null;
        Object returnObj = null;
        String query = "SELECT properties FROM edges;";
        try{
            ResultSet rs = PersistentGraph.stmt.executeQuery(query);
            while (rs.next())
                map = new ObjectMapper().readValue(rs.getString(1), HashMap.class);
            returnObj = map.remove(key);
        }catch (SQLException e){
            System.out.println(e);
        } catch (Exception e1){
            System.out.println(e1);
        }

        return returnObj;
    }
}
