package imgseg;

import org.opencv.core.Mat;

import java.util.*;

public class ImgShow {
    /**
     * 红颜色的RGB表示
     */
    public static final double[] COLOR_RED = {0.0, 0.0, 255.0};
    /**
     * 2D分割区域的RGB平均值
     */
    public static double[][] commAverage;

    public static Mat deIIMG_2D(HashMap<Integer, Set<Integer>> communities, Mat img) {
        return deIIMG_2D(communities, img, false, true);
    }

    public static Mat deIIMG_2D(HashMap<Integer, Set<Integer>> communities, Mat img, boolean randomColor, boolean showBDY) {
        Mat outputImage = new Mat(img.rows(), img.cols(), img.type());
        commAverage = new double[communities.size()][3];
        int index = 0;
        for (Set<Integer> comm : communities.values()) {
            List<Integer> boundaries = findBoundary(comm, img.width());
            //分割区域的颜色表示
            commAverage[index++] = computeCommAve(img, comm);
            double[] regionColor = randomColor ? randomRegionColor() : commAverage[index - 1];

            for (int p : comm) {
                int loc = p - 1;
                int y = loc / img.width();
                int x = loc % img.width();
                //是否需要展示区域的边界条件
                if (showBDY && boundaries.contains(p)) {
                    outputImage.put(y, x, COLOR_RED);
                } else {
                    outputImage.put(y, x, regionColor);
                }
            }
        }

        return outputImage;
    }


    public static Mat deIIMG_3D(HashMap<Integer, Set<Integer>> communities2D, HashMap<Integer, Set<Integer>> communities3D, Mat img) {
        return deIIMG_3D(communities2D, communities3D, img, false, false, false);
    }

    public static Mat deIIMG_3D(HashMap<Integer, Set<Integer>> communities2D, HashMap<Integer, Set<Integer>> communities3D, Mat img,
                                boolean showBDY, boolean is2S, boolean randomColor) {
        Mat outputImage = new Mat(img.rows(), img.cols(), img.type());
        Set<Integer>[] comms2D = communities2D.values().toArray(new TreeSet[0]);
        for (Set<Integer> community : communities3D.values()) {
            //对应到图像像素点的社区
            Set<Integer> realCommunity = new TreeSet<>();
            //每个分割区域的颜色表示
            double[] regionColor;
            for (int comm2DIndex : community) {
                realCommunity.addAll(comms2D[comm2DIndex - 1]);
            }

            //分割区域边界
            List<Integer> boundaries = showBDY ? findBoundary(realCommunity, img.width()) : new ArrayList<>();

            if (randomColor) {
                regionColor = randomRegionColor();
            } else if (is2S) {
                regionColor = compute2SCommAve(community);
            } else {
                regionColor = computeCommAve(img, realCommunity);
            }

            //给每个分割区域中的像素点重新赋值
            for (int p : realCommunity) {
                int loc  = p -1;
                int y = loc / img.width();
                int x = loc % img.width();
                if (showBDY && boundaries.contains(p)) {
                    outputImage.put(y, x, COLOR_RED);
                } else {
                    outputImage.put(y, x, regionColor);
                }
            }
        }

        return outputImage;
    }

    private static double[] compute2SCommAve(Set<Integer> community) {
        double[] color = new double[3];
        for (int c : community) {
            for (int i = 0; i < 3; i++) {
                color[i] += commAverage[c - 1][i];
            }
        }

        for (int i = 0; i < 3; i++) {
            color[i] /= community.size();
        }

        return color;
    }

    /**
     * 根据原图像计算某个划分区域的像素RGB均值
     *
     * @param img
     * @param community
     * @return
     */
    private static double[] computeCommAve(Mat img, Set<Integer> community) {
        double[] color = new double[3];
        for (int pix : community) {
            int loc = pix - 1;
            int y = loc / img.width();
            int x = loc % img.width();
            for (int i = 0; i < 3; i++) {
                color[i] += img.get(y, x)[i];
            }
        }

        for (int i = 0; i < color.length; i++) {
            color[i] /= community.size();
        }

        return color;
    }

    /**
     * 随机生成一个RGB值
     *
     * @return
     */
    private static double[] randomRegionColor() {
        double[] rColor = new double[3];
        Random random = new Random();
        for (int i = 0; i < rColor.length; i++) {
            rColor[i] = random.nextDouble() * 255;
        }
        return rColor;
    }

    private static List<Integer> findBoundary(Set<Integer> community, int width) {
        List<Integer> boundaries = new ArrayList<>();
        for (int p : community) {
            if (!(community.contains(p + 1) && community.contains(p - 1)
                    && community.contains(p + width) && community.contains(p - width))) {
                boundaries.add(p);
            }
        }

        return boundaries;
    }
}
