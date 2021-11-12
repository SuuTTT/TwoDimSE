package imgseg;

import graphdata.Edge;
import graphdata.Graph;
import graphdata.IO;
import graphdata.PairNode;
import org.opencv.core.Mat;

import java.util.*;


public class SEAlgo {
    public static int k = 2;
    public static int t1 = 1;
    /**
     * 论文中是20，此处为30是因为python计算的浮点数和java计算的浮点数精度有差异
     * t2值对图像分割结果影响较大，如果有更好的选取标准或许会使图像分割结果更加完美
     */
    public static int t2 = 20;
    /**
     * 压缩信息率。要求构建的G**的压缩信息率要小于此值
     */
    public static double cRatio = 0.2;

    /**
     * 构建G**时每个节点所对应的边的集合
     * 这样在构建G**时就不必每次都计算一遍
     */
    public static PriorityQueue[] edgePQs;
    /**
     * 边的比较器，使其从小到大排列
     */
    public static final Comparator<Edge> edgeDescComparator = (e1, e2) -> {
        int weightComp = Double.compare(e2.getWeight(),e1.getWeight());
        return weightComp == 0 ? Integer.compare(e1.getSeqID(), e2.getSeqID()) : weightComp;
    };

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
     * 一般我们以图像左上角为原点建立坐标系，水平为x，垂直为y
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
        int seqID = 0;

        PriorityQueue<Edge> edges = new PriorityQueue<>(edgeDescComparator);
        for (int i = 1; i <= k; i++) {
            int toNode;
            double weight;
            // x + i, y
            if (x + i < rightBDY) {
                toNode = y * width + (x + i);
                weight = metric(img, x, y, x + i, y);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x, y + i
            if (y + i < bottomBDY) {
                toNode = (y + i) * width + x;
                weight = metric(img, x, y, x, y + i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x + i, y + i
            if ((x + i < rightBDY) && (y + i < bottomBDY)) {
                toNode = (y + i) * width + (x + i);
                weight = metric(img, x, y, x + i, y + i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x + i, y - i
            if ((x + i < rightBDY) && (y - i > topBDY - 1)) {
                toNode = (y - i) * width + (x + i);
                weight = metric(img, x, y, x + i, y - i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x , y - i
            if (y - i > topBDY - 1) {
                toNode = (y - i) * width + x;
                weight = metric(img, x, y, x, y - i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x - i, y - i
            if ((x - i > leftBDY - 1) && (y - i > topBDY - 1)) {
                toNode = (y - i) * width + (x - i);
                weight = metric(img, x, y, x - i, y - i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x - i, y
            if (x - i > leftBDY - 1) {
                toNode = y * width + (x - i);
                weight = metric(img, x, y, x - i, y);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

            // x - i, y + i
            if ((x - i > leftBDY - 1) && (y + i < bottomBDY)) {
                toNode = (y + i) * width + (x - i);
                weight = metric(img, x, y, x - i, y + i);
                edges.add(new Edge(fromNode, toNode, weight, seqID++));
            }

        }

        selectNMostSimilar(graph, edges, edges.size() / 2);
    }

    /**
     * 选取权重较大的边，数量为n
     *
     * @param graph 信息系统G
     * @param edges 某个节点在图中的边集合
     * @param n     选取边的数量
     */
    private static void selectNMostSimilar(Graph graph, PriorityQueue<Edge> edges, int n) {
        double sumDegrees = graph.getSumDegrees();
        HashMap<PairNode, Double> weights = graph.getWeights();
        HashMap<Integer, Set<Integer>> connection = graph.getConnection();
        double[] nodeDegree = graph.getNodeDegree();

        for (int i = 0; i < n && !edges.isEmpty(); i++) {
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

    /**
     * 构建G**
     *
     * @param communities2D G*的划分结果
     * @param n             构建图时每个节点有其他节点建立联系的数量
     * @return 返回G**
     */
    public static Graph constructGraphBy2D(HashMap<Integer, Set<Integer>> communities2D, int n) {
        System.out.println("construct graph G**");

        int size = communities2D.size();
        Graph graph = new Graph(size);
        edgePQs = new PriorityQueue[size];

        for (int fromNode = 0; fromNode < size; fromNode++) {
            if (edgePQs[fromNode] != null) {
                //经过首次计算后过无需再次计算，大大缩短运行时间。以空间换时间
                selectNMostSimilar(graph, edgePQs[fromNode], n);
            }

            PriorityQueue<Edge> edges = new PriorityQueue<>(edgeDescComparator);
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


        return StrictMath.pow(2, -dst / t1);
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
