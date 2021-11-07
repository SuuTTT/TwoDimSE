package imgseg;

import GraphData.Edge;
import GraphData.Graph;
import GraphData.IO;
import GraphData.PairNode;
import org.opencv.core.Mat;

import java.util.*;


public class SEAlgo {
    public static int k = 2;
    public static int t1 = 1;
    public static int t2 = 20;
    public static double cRatio = 0.2;

    public static PriorityQueue[] edgePQs;


    /**
     * 把图像转化为图结构的数据
     *
     * @param img
     * @return
     */
    public static Graph constructGraph(Mat img) {
        int height = img.height();
        int width = img.width();
        int[] boundary = {0, 0, width, height};

        Graph graph = new Graph(height * width);
        //图像中的坐标和对应到数学中的坐标轴是不一样的
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.printf("Processing at %d , %d%n", y, x);
                pixelINAV(graph, img, boundary, x, y);
            }
        }

        return graph;
    }

    /**
     * 图像构建为图结构的数据时，每个像素点的交互
     *
     * @param graph
     * @param img
     * @param boundary
     * @param x
     * @param y
     */
    private static void pixelINAV(Graph graph, Mat img, int[] boundary, int x, int y) {
        int leftBDY = boundary[0];
        int topBDY = boundary[1];
        int rightBDY = boundary[2];
        int bottomBDY = boundary[3];

        int width = img.width();
        int fromNode = y * width + x;

        PriorityQueue<Edge> edges = new PriorityQueue<>();
        for (int i = 1; i <= k; i++) {
            int toNode;
            double weight;
            // x + i, y
            if (x < rightBDY - i) {
                toNode = y * width + (x + i);
                weight = metric(img, x, y, x + i, y);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x, y + i
            if (y < bottomBDY - i) {
                toNode = (y + i) * width + x;
                weight = metric(img, x, y, x, y + i);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x + i, y + i
            if ((x < rightBDY - i) && (y < bottomBDY - i)) {
                toNode = (y + i) * width + (x + i);
                weight = metric(img, x, y, x + i, y + i);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x + i, y - i
            if ((x < rightBDY - i) && (y > i + topBDY - 1)) {
                toNode = (y - i) * width + (x + i);
                weight = metric(img, x, y, x + i, y - i);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x , y - i
            if (y > i + topBDY - 1) {
                toNode = (y - i) * width + x;
                weight = metric(img, x, y, x, y - i);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x - i, y - i
            if ((x > i + leftBDY - 1) && (y > i + topBDY - 1)) {
                toNode = (y - i) * width + (x - i);
                weight = metric(img, x, y, x - i, y - i);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x - i, y
            if (x > i + leftBDY - 1) {
                toNode = y * width + (x - i);
                weight = metric(img, x, y, x - i, y);
                edges.add(new Edge(fromNode, toNode, weight));
            }

            // x - i, y + i
            if ((x > i + leftBDY - 1) && (y < bottomBDY - i)) {
                toNode = (y + i) * width + (x - i);
                weight = metric(img, x, y, x - i, y + i);
                edges.add(new Edge(fromNode, toNode, weight));
            }
        }


        selectNMostSimilar(graph, edges, edges.size() / 2);
    }

    /**
     * 选取权重较大的边，数量为原来的一半
     *
     * @param graph
     * @param edges
     */
    private static void selectNMostSimilar(Graph graph, PriorityQueue<Edge> edges, int n) {
        int size = edges.size();
        int pivot = size - n;
        for (int i = 0; i < pivot; i++) {
            edges.poll();
        }

        double sumDegrees = graph.getSumDegrees();
        HashMap<PairNode, Double> weights = graph.getWeights();
        HashMap<Integer, Set<Integer>> connection = graph.getConnection();
        double[] nodeDegree = graph.getNodeDegree();

        while (!edges.isEmpty()) {
            Edge edge = edges.poll();
            //加一为了使其从1开始编码
            int start = edge.getStart() + 1;
            int end = edge.getEnd() + 1;
            double weight = edge.getWeight();
            PairNode pair = new PairNode(start, end);
            if (!weights.containsKey(pair)) {
                //边及其对应的权重
                weights.put(pair, edge.getWeight());
                IO.putConnection(connection, start, end);
                //每一个节点的度数
                nodeDegree[start] += weight;
                nodeDegree[end] += weight;
                //无向图，所以权重翻倍
                sumDegrees += 2 * weight;
            }
        }

        graph.setSumDegrees(sumDegrees);
    }

    public static Graph constructGraphBy2D(HashMap<Integer, Set<Integer>> communities2D, int n) {
        System.out.println("construct graph G**");

        int size = communities2D.size();
        Graph graph = new Graph(size);
        edgePQs = new PriorityQueue[size];

        for (int fromNode = 0; fromNode < size; fromNode++) {
            if (edgePQs[fromNode] != null) {
                selectNMostSimilar(graph, edgePQs[fromNode], n);
            }

            PriorityQueue<Edge> edges = new PriorityQueue<>();
            for (int toNode = 0; toNode < size; toNode++) {
                if (fromNode != toNode) {
                    double weight = metric(fromNode, toNode);
                    Edge e = new Edge(fromNode, toNode, weight);
                    edges.offer(e);
                }
            }
            edgePQs[fromNode] = new PriorityQueue(edges);
            selectNMostSimilar(graph, edges, n);
        }

        System.out.println("done!");
        return graph;
    }


    private static double metric(Mat img, int x1, int y1, int x2, int y2) {
        double dst = Math.sqrt(
                square(img.get(y1, x1)[0] - img.get(y2, x2)[0])
                        + square(img.get(y1, x1)[1] - img.get(y2, x2)[1])
                        + square(img.get(y1, x1)[2] - img.get(y2, x2)[2])
        );

        return Math.pow(2, -dst / t1);
    }

    private static double metric(int node1, int node2) {
        double[] c1 = ImgShow.commAverage[node1];
        double[] c2 = ImgShow.commAverage[node2];

        double dst = Math.sqrt(
                square(c1[0] - c2[0])
                        + square(c1[1] - c2[1])
                        + square(c1[2] - c2[2])
        );

        return Math.pow(2, -square(dst / t2));
    }

    private static int square(int value) {
        return value * value;
    }

    private static double square(double value) {
        return value * value;
    }


}
