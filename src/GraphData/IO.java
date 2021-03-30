package GraphData;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.*;

public class IO {
    /**
     * 从指定指定获取无向图
     *  注意文件的格式：
     *  第一行为节点的总数
     *  从第二行起，每一行为一个边：
     *  例如：1 2 3.44445
     *  中间用空格隔开
     * @param filePath
     * @throws Exception
     */
    public static Graph getUndirGraphFromFile(String filePath) throws Exception {
        FileInputStream file = new FileInputStream(filePath);
        BufferedReader bf = new BufferedReader(new InputStreamReader(file));

        //图的参数
        int numNodes = Integer.parseInt(bf.readLine());
        Graph g = new Graph(numNodes);
        double sumDegrees = 0.0;
        HashMap<PairNode, Double> weights = g.getWeights();
        HashMap<Integer, Set<Integer>> connection = g.getConnection();
        double[] nodeDegree = g.getNodeDegree();

        //从文件中遍历边的信息
        String line;
        while ((line = bf.readLine()) != null) {
            String[] edge = line.trim().split(" ");
            int start = Integer.parseInt(edge[0]);
            int end = Integer.parseInt(edge[1]);
            double weight = Double.parseDouble(edge[2]);
            PairNode pair = new PairNode(start, end);
            //将边信息保存到图中
            //**从算法高效的角度考虑，在生成无向图文件的时候应保证不会出现以下异常情况
            //**在保证无向图文件数据无异常的情况下，即可注释掉异常情况的判断
            if (!pair.isValid()) {
                System.out.println("edge is illegal, a node cannot connected with itself");
                continue;
            } else if (weight == 0) {
                System.out.println("edge break");
                continue;
            }

            //是否添加过此边
            if (!weights.containsKey(pair)) {
                //边及其对应的权重
                weights.put(pair, weight);
                putConnection(connection, start, end);
                //每一个节点的度数
                nodeDegree[start] += weight;
                nodeDegree[end] += weight;
                //无向图，所以权重翻倍
                sumDegrees += 2 * weight;
            }


        }

        //图的总度数
        g.setSumDegrees(sumDegrees);
        file.close();

        return g;
    }

    /**
     * 两点相连
     * @param connection
     * @param start
     * @param end
     */
    private static void putConnection(HashMap<Integer, Set<Integer>> connection, int start, int end) {
        if (!connection.containsKey(start)) {
            connection.put(start, new TreeSet<>() {{
                add(end);
            }});
        } else {
            connection.get(start).add(end);
        }
        //start end交换位置
        if (!connection.containsKey(end)) {
            connection.put(end, new TreeSet<>() {{
                add(start);
            }});
        } else {
            connection.get(end).add(start);
        }
    }


}
