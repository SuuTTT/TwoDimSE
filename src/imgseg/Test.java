package imgseg;

import graphdata.Edge;
import graphdata.Graph;
import graphdata.IO;
import graphdata.PairNode;
import SE.NewTwoDimSE;
import SE.TwoDimSE;
import filter.Filter;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;
import java.util.*;

public class Test {
    private static final String imgPath = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\data\\86000.jpg";
    public static final String smoothFile = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\smooth";
    private static final String resultPath = "src" + File.separator;
    private static final String imgGraph = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\image_graph";
    private static final String twoLayerGraph = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\2layer_graph";
    private static final String graph1 = "test_graph1";
    private static final String graph2 = "test_graph2";
    private static final String res2D = "2D";
    private static final String res3D = "3D";
    private static int n = 20;

    public static void main(String[] args) throws Exception {
        System.load("D:\\opencv\\opencv\\build\\java\\x64\\opencv_java454.dll");
//        edgesTest();
//        communityTest();
//        graphSaveTest();
//        graphReadTest();
//        filterTest();
//        hashTest();
//        constructGraph();
//        compare2Graph();
//        deiimg2D();
//        diimg_3D();
//        autodeiimg3D();
//        twoLayer2D();
//        smoothImageTest();

    }

    private static void autodeiimg3D() {
        Mat img = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(img);
        Graph g = SEAlgo.constructGraph(smoothImage);
//        Graph g = null;
//        try {
//            g = IO.getUndirGraphFromFile(imgGraph);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        TwoDimSE se = new TwoDimSE(g);
        se.min2dSE(true);
        Mat res2D = ImgShow.deIIMG_2D(se.getCommunities(), img);
        Graph g2;
        TwoDimSE se2;
        g2 = SEAlgo.constructGraphBy2D(se.getCommunities(), 50);
        se2 = new TwoDimSE(g2);
        se2.min2dSE(true);

        Mat res3D = ImgShow.deIIMG_3D(se.getCommunities(), se2.getCommunities(), img);
        HighGui.imshow("origin", img);
        HighGui.imshow("2d", res2D);
        HighGui.imshow("3D", res3D);
        HighGui.waitKey();
    }

    private static void smoothImageTest() throws FileNotFoundException {
        Mat img = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(img);

        FileInputStream file = new FileInputStream(smoothFile);
        BufferedReader bf = new BufferedReader(new InputStreamReader(file));

        for (int y = 0; y < img.height(); y++) {
            for (int x = 0; x < img.width(); x++) {
                boolean isSame = true;
                String[] color;
                try {
                    color = bf.readLine().split("\t");
                    for (int i = 0; i < 3; i++) {
                        if (Double.parseDouble(color[2 - i]) != smoothImage.get(y, x)[i]) {
                            isSame = false;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!isSame) {
                    System.out.println(y + " " + x);
                }
            }
        }
    }

    private static void hashTest() {
        Mat img = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        SEAlgo.k = 4;
        Mat smoothImage = filter.getSmoothImage(img);
        Graph g = SEAlgo.constructGraph(smoothImage);
        TwoDimSE se = new TwoDimSE(g);
        se.min2dSE(true);

    }

    private static void filterTest() {
        Mat img = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(img);
        HighGui.imshow("smooth", smoothImage);
        HighGui.waitKey();
    }

    private static void twoLayer2D() {
        try {
            Graph g = IO.getUndirGraphFromFile("C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\2layer_graph");
            NewTwoDimSE se = new NewTwoDimSE(g);
            se.min2dSE("2layer_2D", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void communityTest() {
        HashMap<Integer, Set<Integer>> map = new HashMap<>();
        Set<Integer> set = new TreeSet<>() {{
            add(1);
            add(2);
            add(3);
        }};
        map.put(1, set);
        Set<Integer>[] sets = map.values().toArray(new TreeSet[0]);
        sets[0].size();
    }

    private static void diimg_3D() throws Exception {
        //读取图像
        Mat image = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(image);
        Graph g = SEAlgo.constructGraph(smoothImage);
//        Graph g = IO.getUndirGraphFromFile(imgGraph);
//        g.write2File("test_graph1");

        TwoDimSE se = new TwoDimSE(g);
        se.min2dSE(true);
        Mat res2D = ImgShow.deIIMG_2D(se.getCommunities(), image);
        Graph g2 = SEAlgo.constructGraphBy2D(se.getCommunities(), n);
//        Graph g2 = IO.getUndirGraphFromFile(twoLayerGraph);
//        Graph.write2File(g2, "test_graph2");
        TwoDimSE se2 = new TwoDimSE(g2);
        se2.min2dSE(true);
        TwoDimSE bestSE2 = se2;

        while (se2.getCompressionRatio() > SEAlgo.cRatio) {
            n += 5;
            Graph g3 = SEAlgo.constructGraphBy2D(se.getCommunities(), n);
            TwoDimSE se3 = new TwoDimSE(g3);
            se3.min2dSE(true);
            bestSE2 = se3;
            System.out.println(n);
        }


        Mat res3D = ImgShow.deIIMG_3D(se.getCommunities(), bestSE2.getCommunities(), image);
        HighGui.imshow("origin", image);
        HighGui.imshow("2d", res2D);
        HighGui.imshow("3D", res3D);
        HighGui.waitKey();
    }

    private static void deiimg2D() {
        //读取图像
        Mat image = Imgcodecs.imread(imgPath);

        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(image);
        Graph g = SEAlgo.constructGraph(smoothImage);
        TwoDimSE se = new TwoDimSE(g);
        se.min2dSE(true);
        Mat res = ImgShow.deIIMG_2D(se.getCommunities(), image);
        HighGui.imshow("2d", res);
        HighGui.waitKey();
    }

    private static void compare2Graph() {
        try {
            Graph g1 = IO.getUndirGraphFromFile("C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\image_graph");
            Mat image = Imgcodecs.imread(imgPath);
            Filter filter = new Filter(0.1);
            Mat smoothImage = filter.getSmoothImage(image);
            Graph g2 = SEAlgo.constructGraph(smoothImage);
            HashMap<PairNode, Double> ws1 = g1.getWeights();
            HashMap<PairNode, Double> ws2 = g2.getWeights();
            int count = 0;
            if (ws1.size() != ws2.size()) {
                System.out.println(ws1.size() + "\t" + ws2.size());
                return;
            }
            for (Map.Entry<PairNode, Double> e : ws1.entrySet()) {
                PairNode k = e.getKey();
                Double v = e.getValue();

                if (!ws2.containsKey(k)) {
                    System.out.println("缺失：" + k.toString());
                    count++;
                } else if (!ws2.get(k).equals(v)) {
//                    System.out.println(String.format("出现不一致： %s : %s\t%s", k.toString(), v, ws2.get(k)));
                    count++;
                }

            }
            System.out.println("不一致的覆盖率：" + (double) count / ws1.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Graph constructGraph() {
        Mat image = Imgcodecs.imread(imgPath);

        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(image);
        Graph g = SEAlgo.constructGraph(smoothImage);
//        Graph.write2File(g, "test1");
        System.out.println(g);
        return g;
    }

    private static void graphReadTest() {
        Graph g = Graph.readFromFile("test1");
        System.out.println(g.getSumDegrees());
    }

    private static void graphSaveTest() {
        Graph g = new Graph(5);
        g.setSumDegrees(223);
//        Graph.write2File(g, "test1");
        g.write2File("test1");
    }

    private static void edgesTest() {
        PriorityQueue<Edge> edges = new PriorityQueue<>(SEAlgo.edgeDescComparator);
        Edge e1 = new Edge(1, 2, 1.2);
        Edge e2 = new Edge(1, 3, 1.1);
        Edge e3 = new Edge(2, 3, 2.1);
        Edge e4 = new Edge(4, 2, 1.9);
        Edge e5 = new Edge(3, 7, 1.2);
        edges.add(e1);
        edges.add(e2);
        edges.add(e3);
        edges.add(e4);
        edges.add(e5);
        while (!edges.isEmpty()) {
            System.out.println(edges.poll());
        }
    }
}
