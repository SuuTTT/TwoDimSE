import graphdata.Graph;
import SE.TwoDimSE;
import filter.Filter;
import imgseg.ImgShow;
import imgseg.SEAlgo;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.HashMap;
import java.util.Set;

public class deIIMG {
    public static void main(String[] args) throws Exception {
        imageSeg();
    }



    public static void printTimeLog(long startTime, long endTime) {
        long cost = endTime - startTime;
        long second = cost / 1000;
        long min = second / 60;
        second = second % 60;
        long ms = cost % 1000;
        System.out.printf("time cost is %dm %ds %dms", min,second, ms);
    }

    public static String getImagePath() {
        String imgPath = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\data\\butterfly.jpg";
        return imgPath;
    }

    public static String getPath() {
        String path = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\image_graph";
//        String path = "/Users/gem/PyProject/SE_image_seg/image_graph";
//        String path = "test_graph";
        return path;
    }

    private static void imageSeg() {
        //因为使用了opencv库读取图像信息所以要先加载配置文件
        System.load("D:\\opencv\\opencv\\build\\java\\x64\\opencv_java454.dll");
        long startTime = System.currentTimeMillis();
        String imgPath = getImagePath();
        Mat img = Imgcodecs.imread(imgPath);
        Filter filter = new Filter(0.1);
        Mat smoothImage = filter.getSmoothImage(img);
        Graph g = SEAlgo.constructGraph(smoothImage);

        TwoDimSE se = new TwoDimSE(g);
        se.min2dSE(true);
        Mat res2D = ImgShow.deIIMG_2D(se.getCommunities(), img);
        //自动选择参数部分，仍需完善
        int n = 30;
        Graph g2;
        TwoDimSE se2;
        HashMap<Integer, Set<Integer>> commResult3d;
        do {
            g2 = SEAlgo.constructGraphBy2D(se.getCommunities(), n);
            se2 = new TwoDimSE(g2);
            se2.min2dSE(true);
            commResult3d = se2.getCommunities();
            n+=5;
        } while (se2.getCompressionRatio() > SEAlgo.cRatio);

        Mat res3D = ImgShow.deIIMG_3D(se.getCommunities(), commResult3d, img);
        long endTime = System.currentTimeMillis();
        printTimeLog(startTime, endTime);
        HighGui.imshow("origin", img);
        HighGui.imshow("2d", res2D);
        HighGui.imshow("3D", res3D);
        HighGui.waitKey();
    }
}
