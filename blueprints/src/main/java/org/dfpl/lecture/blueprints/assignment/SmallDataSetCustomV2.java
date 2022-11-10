package org.dfpl.lecture.blueprints.assignment;

import com.tinkerpop.blueprints.revised.Direction;
import com.tinkerpop.blueprints.revised.Edge;
import com.tinkerpop.blueprints.revised.Graph;
import com.tinkerpop.blueprints.revised.Vertex;
import org.dfpl.lecture.blueprints.persistent.PersistentGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;

public class SmallDataSetCustomV2 {
    // isEvenTag, addTag, subTag, divTag 의 문자열은 바뀔 수 있음
    public static String isEvenTag = "isEven";
    public static String addTag = "add";
    public static String subTag = "sub";
    public static String divTag = "div";
    public static long[] resultForTest = new long[24];
    public static void main(String[] args) throws Exception {

        // 참고: 평가를 위한 데이터셋은 보다 작은 데이터4을 활용 (예: CollegeMsg.txt, http://snap.stanford.edu/data/index.html)
        // 참고: 데이터셋 변경 가능
        String fileName = "./blueprints/data/CollegeMsg.txt";
        String delimiter = "\\s";
        String dbID = "root";
        String dbPW = "1234";
        String dbName = "team7";

        BufferedReader r = new BufferedReader(new FileReader(fileName));

        Graph g = new PersistentGraph(dbID, dbPW, dbName);
        // PersistentGraph의 생성자는 빈 생성자 (id는 root, pw는 1234, db name은 팀 이름으로) 혹은
        // (String dbID, String dbPW, String dbName) 의 생성자를 가질 수 있음
        //Graph g = new PersistentGraph(dbID, dbPW, dbName);

        int cnt = 0;
        while(true){
            String line = r.readLine();
            if(line == null)
                break;
            if (line.startsWith("#"))
                continue;
            if(++cnt % 1000 == 0){
                System.out.println(cnt + " lines read...");
            }
            String[] arr = line.split(delimiter);
            Vertex v1 = g.addVertex(arr[0]);
            int v1Int = Integer.parseInt(v1.getId());
            Vertex v2 = g.addVertex(arr[1]);
            int v2Int = Integer.parseInt(v2.getId());
            Edge e12 = g.addEdge(v1, v2, "l");
            e12.setProperty(isEvenTag, ((v1Int + v2Int) % 2 == 0));
            e12.setProperty(addTag, v1Int + v2Int);
            e12.setProperty(subTag, v1Int - v2Int);
            e12.setProperty(divTag, (v1Int / (double) v2Int));

        }

        r.close();
        System.out.println("Data loaded");

        // finding a vertex that has a maximum out-degree
        int maxOutDegree = Integer.MIN_VALUE;
        String maxOutDegreeID = null;
        int dummy = 0;
        for(Vertex v: g.getVertices()){
            if(++dummy%1000 == 0)
                System.out.print(".");
            int d = v.getVertices(Direction.OUT).size();
            if(d > maxOutDegree){
                maxOutDegree = d;
                maxOutDegreeID = v.getId();
            }
        }
        System.out.println();
        // finding a vertex that has a maximum in-degree
        int maxInDegree = Integer.MIN_VALUE;
        String maxInDegreeID = null;
        dummy = 0;
        for(Vertex v: g.getVertices()){
            if(++dummy%1000 == 0)
                System.out.print(".");
            int d = v.getVertices(Direction.IN).size();
            if(d > maxInDegree){
                maxInDegree = d;
                maxInDegreeID = v.getId();
            }
        }
        System.out.println("Start to evaluate");

        resultForTest[1] = Integer.parseInt(maxOutDegreeID);
        System.out.println("[1] " + resultForTest[1]);

        resultForTest[2] = maxOutDegree;
        System.out.println("[2] " + resultForTest[2]);

        resultForTest[3] = Integer.parseInt(maxInDegreeID);
        System.out.println("[3] " + resultForTest[3]);

        resultForTest[4] = maxInDegree;
        System.out.println("[4] " + resultForTest[4]);

        resultForTest[5] = g.getVertices().size();
        System.out.println("[5] " + resultForTest[5]);

        resultForTest[6] = g.getEdges().size();
        System.out.println("[6] " + resultForTest[6]);
        resultForTest[7] = g.getEdges(isEvenTag, true).size();
        System.out.println("[7] " + resultForTest[7]);

        resultForTest[8] = g.getEdges(isEvenTag, false).size();
        System.out.println("[8] " + resultForTest[8]);

        resultForTest[9] = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT).size();
        System.out.println("[9] " + resultForTest[9]);

        resultForTest[10] = g.getVertex(maxInDegreeID).getVertices(Direction.IN).size();
        System.out.println("[10] " + resultForTest[10]);

        resultForTest[11] = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT).stream()
                .flatMap(v -> v.getVertices(Direction.OUT).stream()).toList().size();
        System.out.println("[11] " + resultForTest[11]);

        resultForTest[12] = g.getVertex(maxInDegreeID).getVertices(Direction.IN).stream()
                .flatMap(v -> v.getVertices(Direction.IN).stream()).toList().size();
        System.out.println("[12] " + resultForTest[12]);

        resultForTest[13] = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT, isEvenTag, true).stream()
                .flatMap(v -> v.getVertices(Direction.OUT, isEvenTag, false).stream()).toList().size();
        System.out.println("[13] " + resultForTest[13]);

        resultForTest[14] = g.getVertex(maxInDegreeID).getVertices(Direction.IN, isEvenTag, true).stream()
                .flatMap(v -> v.getVertices(Direction.IN, isEvenTag, false).stream()).toList().size();
        System.out.println("[14] " + resultForTest[14]);

        resultForTest[15] = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT).stream()
                .flatMap(v -> v.getVertices(Direction.OUT).stream()).flatMap(v -> v.getVertices(Direction.OUT).stream())
                .toList().size();
        System.out.println("[15] " + resultForTest[15]);

        resultForTest[16] = g.getVertex(maxInDegreeID).getVertices(Direction.IN).stream()
                .flatMap(v -> v.getVertices(Direction.IN).stream()).flatMap(v -> v.getVertices(Direction.IN).stream())
                .toList().size();
        System.out.println("[16] " + resultForTest[16]);

        long min7 = Long.MAX_VALUE;
        int min7C = 0;
        for (int i = 0; i < 10; i++) {
            long pre = System.nanoTime();
            min7C = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT).stream().flatMap(v -> v.getVertices(Direction.OUT).stream())
                    .toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min7) {
                System.out.println("\t" + min7 + " -> " + elapsedTime);
                min7 = elapsedTime;

            }
        }
        resultForTest[17] = min7C;
        System.out.println("[17] " + resultForTest[17]);
        System.out.println("[P1] " + min7);

        long min8 = Long.MAX_VALUE;
        int min8C = 0;
        for (int i = 0; i < 10; i++) {
            long pre = System.nanoTime();
            min8C = g.getVertex(maxInDegreeID).getVertices(Direction.IN).stream().flatMap(v -> v.getVertices(Direction.IN).stream())
                    .toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min8) {
                System.out.println("\t" + min8 + " -> " + elapsedTime);
                min8 = elapsedTime;
            }
        }
        resultForTest[18] = min8C;
        System.out.println("[18] " + resultForTest[18]);
        System.out.println("[P2] " + min8);

        long min9 = Long.MAX_VALUE;
        int min9C = 0;
        for (int i = 0; i < 10; i++) {
            long pre = System.nanoTime();
            min9C = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT, isEvenTag, true).stream()
                    .flatMap(v -> v.getVertices(Direction.OUT, isEvenTag, false).stream()).toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min9) {
                System.out.println("\t" + min9 + " -> " + elapsedTime);
                min9 = elapsedTime;
            }
        }
        resultForTest[19] = min9C;
        System.out.println("[19] " + resultForTest[19]);
        System.out.println("[P3] " + min9);

        long min10 = Long.MAX_VALUE;
        int min10C = 0;
        for (int i = 0; i < 10; i++) {
            long pre = System.nanoTime();
            min10C = g.getVertex(maxInDegreeID).getVertices(Direction.IN, isEvenTag, true).stream()
                    .flatMap(v -> v.getVertices(Direction.IN, isEvenTag, false).stream()).toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min10) {
                System.out.println("\t" + min10 + " -> " + elapsedTime);
                min10 = elapsedTime;
            }
        }
        resultForTest[20] = min10C;
        System.out.println("[20] " + resultForTest[20]);
        System.out.println("[P4] " + min10);

        long min11 = Long.MAX_VALUE;
        int min11C = 0;
        for (int i = 0; i < 5; i++) {
            long pre = System.nanoTime();
            min11C = g.getVertex(maxOutDegreeID).getVertices(Direction.OUT).stream().flatMap(v -> v.getVertices(Direction.OUT).stream())
                    .flatMap(v -> v.getVertices(Direction.OUT).stream()).toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min11) {
                System.out.println("\t" + min11 + " -> " + elapsedTime);
                min11 = elapsedTime;
            }
        }
        resultForTest[21] = min11C;
        System.out.println("[21] " + resultForTest[21]);
        System.out.println("[P5] " + min11);

        long min12 = Long.MAX_VALUE;
        int min12C = 0;
        for (int i = 0; i < 5; i++) {
            long pre = System.nanoTime();
            min12C = g.getVertex(maxInDegreeID).getVertices(Direction.IN).stream().flatMap(v -> v.getVertices(Direction.IN).stream())
                    .flatMap(v -> v.getVertices(Direction.IN).stream()).toList().size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min12) {
                System.out.println("\t" + min12 + " -> " + elapsedTime);
                min12 = elapsedTime;
            }
        }
        resultForTest[22] = min12C;
        System.out.println("[22] " + resultForTest[22]);
        System.out.println("[P6] " + min12);

        long min13 = Long.MAX_VALUE;
        int min13C = 0;
        for (int i = 0; i < 5; i++) {
            long pre = System.nanoTime();
            min13C = g.getVertex(maxOutDegreeID).getTwoHopVertices(Direction.OUT).size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min13) {
                System.out.println("\t" + min13 + " -> " + elapsedTime);
                min13 = elapsedTime;
            }
        }
        //  [23] should be same with [21]
        resultForTest[23] = min13C;
        System.out.println("[23] " + resultForTest[23]);
        System.out.println("[P7] " + min13);

        long min14 = Long.MAX_VALUE;
        int min14C = 0;
        for (int i = 0; i < 5; i++) {
            long pre = System.nanoTime();
            min14C = g.getVertex(maxInDegreeID).getTwoHopVertices(Direction.IN).size();
            long elapsedTime = System.nanoTime() - pre;
            if (elapsedTime < min14) {
                System.out.println("\t" + min14 + " -> " + elapsedTime);
                min14 = elapsedTime;
            }
        }
        // [24] should be same with [22]
        resultForTest[24] = min14C;
        System.out.println("[24] " + resultForTest[24]);
        System.out.println("[P8] " + min14);
        testForAssignment();
    }

    @SuppressWarnings("unused")
    private static void scanDegree(Graph g, Direction direction) throws SQLException {
        String maxID = null;
        Integer maxDegree = Integer.MIN_VALUE;
        for (Vertex v : g.getVertices()) {
            int size = v.getVertices(direction).size();
            if (size > maxDegree) {
                maxID = v.getId();
                maxDegree = size;
                System.out.println(maxID + " : " + maxDegree);
            }
        }
    }
    private static void testForAssignment() {
        int correctCnt = 0;
        int incorrectCnt = 0;
        long[] originalResult = new long[] {0, 9, 237, 32, 137, 1899, 20296, 10188, 10108, 237, 137, 4615, 4115, 982, 1024, 190569, 149967, 4615, 4115, 982, 1024, 190569, 149967, 190569, 149967};

        for (int i = 1; i < resultForTest.length; i++) {
            System.out.println("Our TEST : " + i);
            if (originalResult[i] == resultForTest[i]) {
                correctCnt += 1;
            }
            else {
                System.out.println("Fail : " + i);
                System.out.println("Original : " + originalResult[i]);
                System.out.println("OurResult : " + resultForTest[i]);
                incorrectCnt += 1;
            }
        }
        System.out.println("Correct : " + correctCnt);
        System.out.println("Incorrect : " + incorrectCnt);
    }
}