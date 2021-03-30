import GraphData.Graph;
import GraphData.IO;
import GraphData.PairNode;
import SE.CommDeltaH;
import SE.TwoDimSE;

import java.util.TreeSet;

public class main {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        String path = "C:\\Users\\Lenovo\\Desktop\\SE_image_seg\\image_graph";
        Graph g = IO.getUndirGraphFromFile(path);
        TwoDimSE twoDimSE = new TwoDimSE(g);
        twoDimSE.min2dSE();
        twoDimSE.saveResult("2D");
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("time cost is %dms",endTime - startTime));
    }
}
