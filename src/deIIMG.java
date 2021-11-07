import GraphData.Graph;
import GraphData.IO;
import SE.NewTwoDimSE;
import SE.TwoDimSE;
import SE.TwoDimSE2;

public class deIIMG {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        String path = getPath();
        Graph g = IO.getUndirGraphFromFile(path);
//        NewTwoDimSE twoDimSE = new NewTwoDimSE(g);
        TwoDimSE twoDimSE = new TwoDimSE(g);
        twoDimSE.min2dSE("2D", true);
        long endTime = System.currentTimeMillis();
        printTimeLog(startTime, endTime);
    }

    private static void printTimeLog(long startTime, long endTime) {
        long cost = endTime - startTime;
        long second = cost / 1000;
        long min = second / 60;
        second = second % 60;
        long ms = cost % 1000;
        System.out.printf("time cost is %dm %ds %dms", min,second, ms);
    }

    public static String getPath() {
        String path = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\image_graph";
//        String path = "/Users/gem/PyProject/SE_image_seg/image_graph";
//        String path = "test_graph";
        return path;
    }
}
