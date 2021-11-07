package filter;

import org.opencv.core.Mat;

public class Filter {

    private double sigma;
    private final static double WIDTH = 4.0;

    public Filter(double sigma) {
        this.sigma = sigma;
    }

    /**
     * 获取平滑过后的图像
     * @param img
     * @return
     */
    public Mat getSmoothImage(Mat img) {
        double[] mask = makeFgauss(img);
        normalizeMask(mask);

        Mat temp = convolveEven(img, mask);
        img = convolveEven(temp, mask);
        temp.release();

        return img;
    }

    /**
     * 使用mask对图像进行卷积
     * @param img
     * @param mask
     * @return
     */
    private Mat convolveEven(Mat img, double[] mask) {
        double max = 0.0;
        int height = img.height();
        int width = img.width();

        Mat outputImg = new Mat(height, width, img.type());
        int lenMask = mask.length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //像素点y,x的处理
                double[] colorVector = img.get(y, x);
                double[] sumVector = new double[img.channels()];
                //遍历颜色通道
                for (int i = 0; i < img.channels(); i++) {
                    sumVector[i] = colorVector[i] * mask[0];
                    //遍历mask向量
                    for (int j = 1; j < lenMask; j++) {
                        sumVector[i] += mask[j] * (img.get(y, Math.max(x - j, 0))[i] +
                                img.get(y, Math.min(x + j, width - 1))[i]);
//                        max = Math.max(max, sumVector[i]);
                    }
                }
                //输出图像中y,x像素点的颜色向量
                outputImg.put(y, x, sumVector);
            }
        }

        return outputImg;
    }


    /**
     * 正则化mask向量
     * @param mask
     */
    private void normalizeMask(double[] mask) {
        double sum = 0.0;
        for (double m : mask) {
            sum += Math.abs(m);
        }
        sum = sum * 2 + Math.abs(mask[0]);

        for (int i = 0; i < mask.length; i++) {
            mask[i] = mask[i] / sum;
        }
    }

    /**
     * 高斯掩码
     * @param img
     * @return
     */
    private double[] makeFgauss(Mat img) {
        int length = (int) (Math.ceil(sigma * WIDTH) + 1);
        double[] mask = new double[length];
        for (int i = 0; i < length; i++) {
            mask[i] = Math.exp(-0.5 * Math.pow(i / sigma, 2));
        }
        return mask;
    }

    public static void matrixMulti(Mat img, double fac) {
        for (int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                double[] colors = img.get(i,j);
                for (int k = 0; k < colors.length; k++) {
                    colors[k] *= fac;
                }
                img.put(i,j,colors);
            }
        }
    }


}
