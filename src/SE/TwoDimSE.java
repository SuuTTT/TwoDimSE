package SE;

import GraphData.Graph;
import GraphData.PairNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class TwoDimSE {
    private double sumDegrees;
    private double oneDimSE;
    private double twoDimSE;
    //社区及其包含的节点
    private HashMap<Integer, Set<Integer>> communities;
    //节点的度和割边数
    private double[] volumes;
    private double[] gs;
    //节点之间的割
    private HashMap<PairNode, Double> cuts;
    //社区与其相联系的社区
    //connection是双向的，不同于pairNode，更新时应注意双向更新
    private HashMap<Integer, Set<Integer>> connections;
    // 节点之间的△H。使用了额外的存储，待优化（TreeMap）。
    // TreeSet可以为类按照一定规则排序，排序效率为O(logn)
    // private TreeMap<PairNode, Double> commDeltaHTreeMap;
    private HashMap<PairNode, Double> commDeltaHMap;
    private TreeSet<CommDeltaH> commDeltaHSet;
    //todo 维护每一个社区对应的最大△H，减小commDeltaHSet的负担，commDeltaH只需存储每一个社区对应最大的△H即可

    /**
     * 初始化二维结构熵所需的编码树信息
     * 非构造树方法，仅适用于二维结构熵，
     * 因此将graph中保存的原始节点信息直接拿来使用，不再额外使用存储空间
     *
     * @param graph
     */
    public TwoDimSE(Graph graph) {
        this.oneDimSE = 0.0;
        this.twoDimSE = 0.0;

        this.sumDegrees = graph.getSumDegrees();
        //内存换时间，初始化HashMap的容量能够提高速度
        int initialCap = 3 * graph.getNumNodes() / 4 + 1;
        this.volumes = graph.getNodeDegree();
        this.gs = graph.getNodeDegree().clone();
        this.communities = new HashMap<>(initialCap);
        //以下四个数据的增删是同步的
        this.cuts = graph.getWeights();
        this.connections = graph.getConnection();
        this.commDeltaHMap = new HashMap<>();
        this.commDeltaHSet = new TreeSet<>();

    }


    /**
     * 二维结构熵极小化
     */
    public void min2dSE() {
        initEncodingTree();
        twoDimSE = oneDimSE;
        CommDeltaH maxCommDeltaH = commDeltaHSet.last();

        //找到最大的△H，merge这两个节点，直到不满足merge的条件
        while (maxCommDeltaH.getDeltaH() > 0 && !commDeltaHSet.isEmpty()) {
            PairNode comms = maxCommDeltaH.getPairComms();
            double deltaH = maxCommDeltaH.getDeltaH();
            twoDimSE -= deltaH;
            updateCommunities(maxCommDeltaH);
            maxCommDeltaH = commDeltaHSet.last();
        }
        //输出解码信息
        ndiInfo();
    }


    /**
     * 节点merge以后更新社区信息
     *
     * @param commDeltaH
     */
    private void updateCommunities(CommDeltaH commDeltaH) {
        PairNode comms = commDeltaH.getPairComms();
        double deltaH = commDeltaH.getDeltaH();
        int commLeft = comms.getP1();
        int commRight = comms.getP2();

        //更新新社区的度和割
        double vi = volumes[commLeft];
        double gi = gs[commLeft];
        double vj = volumes[commRight];
        double gj = gs[commRight];
        volumes[commLeft] = vi + vj;
        gs[commLeft] = gi + gj - 2 * cuts.get(comms);
        volumes[commRight] = 0.0;
        gs[commRight] = 0.0;

        //两社区融合为一个新的社区，同时切断两社区之间的联系
        communities.get(commLeft).addAll(communities.get(commRight));
        communities.remove(commRight);
        commDeltaHMap.remove(comms);
        commDeltaHSet.remove(commDeltaH);
        connections.get(commLeft).remove(commRight);
        connections.get(commRight).remove(commLeft);
        cuts.remove(comms);

        //更新与comLeft和comRight相关社区的△H和cut
        updateCutAndDeltaH(commLeft, commRight);

    }


    /**
     * 更新与comLeft和comRight相关社区的△H以及cut
     * 同时也会更新connection的信息
     *
     * @param commLeft
     * @param commRight
     */
    private void updateCutAndDeltaH(int commLeft, int commRight) {
        Set<Integer> connLeft = connections.get(commLeft);
        Set<Integer> connRight = connections.get(commRight);

        double Vi = volumes[commLeft];
        double Gi = gs[commLeft];
        double Gk;
        double Vk;
        double Gx;
        double newDelta;
        //遍历与社区commLeft相关联的社区
        for (int k : connLeft) {
            double cutIk;
            PairNode pairLeftAndK = new PairNode(commLeft, k);
            if (connRight.contains(k)) {    //若社区k与commLeft和commRight均有关联
                PairNode pairRightAndK = new PairNode(commRight, k);
                cutIk = cuts.get(pairLeftAndK) + cuts.get(pairRightAndK);
                connRight.remove(k);
                commDeltaHSet.remove(new CommDeltaH(pairRightAndK, commDeltaHMap.get(pairRightAndK)));
                commDeltaHMap.remove(pairRightAndK);
                cuts.remove(pairRightAndK);
                connections.get(k).remove(commRight);
            } else {
                cutIk = cuts.get(pairLeftAndK);
            }
            Gk = gs[k];
            Vk = volumes[k];
            Gx = Gi + Gk - 2 * cutIk;
            newDelta = computeDeltaH(Vi, Vk, Gi, Gk, Gx, sumDegrees);
            //更新与cuts和△H相关的存储

            cuts.put(pairLeftAndK, cutIk);
            commDeltaHSet.remove(new CommDeltaH(pairLeftAndK, commDeltaHMap.get(pairLeftAndK)));
            commDeltaHSet.add(new CommDeltaH(pairLeftAndK, newDelta));
            commDeltaHMap.put(pairLeftAndK, newDelta);
            //此处connection不用更新

        }
        //遍历融合前与社区commRight相关联但与commLeft不关联的社区
        for (int k : connRight) {
            PairNode pairRightAndK = new PairNode(commRight, k);
            double cutJk = cuts.get(pairRightAndK);
            Vk = volumes[k];
            Gk = gs[k];
            Gx = Gi + Gk - 2 * cutJk;
            newDelta = computeDeltaH(Vi, Vk, Gi, Gk, Gx, sumDegrees);
            //更新与cuts和△H相关的存储
            cuts.put(new PairNode(commLeft, k), cutJk);
            cuts.remove(pairRightAndK);
            commDeltaHSet.remove(new CommDeltaH(pairRightAndK, commDeltaHMap.get(pairRightAndK)));
            commDeltaHSet.add(new CommDeltaH(new PairNode(commLeft, k), newDelta));
            commDeltaHMap.remove(pairRightAndK);
            commDeltaHMap.put(new PairNode(commLeft, k), newDelta);
            connections.get(commLeft).add(k);
            connections.get(k).add(commLeft);
            connections.get(k).remove(commRight);
        }


    }

    /**
     * 初始化编码树
     * 即一维的编码树，每个叶子结点只包含一个点
     */
    private void initEncodingTree() {
        //计算社区节点之间的deltaH
        for (PairNode p : cuts.keySet()) {
            double vi = volumes[p.getP1()];
            double vj = volumes[p.getP2()];
            double gi = vi;
            double gj = vj;
            double gx = vi + vj - 2 * cuts.get(p);
            double deltaH = computeDeltaH(vi, vj, gi, gj, gx, sumDegrees);
            commDeltaHMap.put(p, deltaH);
            commDeltaHSet.add(new CommDeltaH(p, deltaH));
        }

        //计算一维结构熵，并初始化社区
        for (int i = 1; i < volumes.length; i++) {
            if (volumes[i] > 0.0) {
                //一维编码树的的每个社区就是图中的每个顶点
                int finalI = i;
                communities.put(i, new TreeSet<>() {{
                    add(finalI);
                }});
                oneDimSE -= (volumes[i] / sumDegrees) * (Math.log(volumes[i] / sumDegrees)) / Math.log(2);
            }
        }

    }

    /**
     * 输出ndi的相关信息
     */
    private void ndiInfo() {
        System.out.println(String.format("The One and Two dimension SE: %f, %f\nDecoding Information : %f",
                oneDimSE, twoDimSE, oneDimSE - twoDimSE));
        double ndi = (oneDimSE - twoDimSE) / oneDimSE;
        System.out.println(String.format("The Normalized Decoding Information is %f", ndi));
    }

    /**
     * 将二维结构熵极小化划分的社区保存起来
     *
     * @param fileName
     * @throws IOException
     */
    public void saveResult(String fileName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        for (Set<Integer> res : communities.values()) {
            for (int i : res) {
//                System.out.print(i + "\t");
                bw.write(i + "\t");
            }
//            System.out.println();
            bw.write("\n");
        }

        bw.close();
    }

    /**
     * 计算两个节点之间的△H
     *
     * @param vi
     * @param vj
     * @param gi
     * @param gj
     * @param gx
     * @param sumDegrees
     * @return
     */
    private double computeDeltaH(double vi, double vj, double gi, double gj, double gx, double sumDegrees) {
        BigDecimal a1 = new BigDecimal(vi * (Math.log(vi) / (Math.log(2) + 0.0)));
        BigDecimal a2 = new BigDecimal(vj * (Math.log(vj) / (Math.log(2) + 0.0)));
        BigDecimal a3 = new BigDecimal((vi + vj) * (Math.log(vi + vj) / (Math.log(2) + 0.0)));
        BigDecimal a4 = new BigDecimal(gi * (Math.log(vi / (sumDegrees + 0.0)) / Math.log(2)));
        BigDecimal a5 = new BigDecimal(gj * (Math.log(vj / (sumDegrees + 0.0)) / Math.log(2)));
        BigDecimal a6 = new BigDecimal(gx * (Math.log((vi + vj) / (sumDegrees + 0.0)) / Math.log(2)));
//        System.out.println(String.format("a1, a2, a3, a4, a5, a6: %f, %f, %f, %f, %f, %f", a1, a2, a3, a4, a5, a6));
        BigDecimal b1 = a1.add(a2);
        BigDecimal b2 = b1.subtract(a3);
        BigDecimal b3 = b2.subtract(a4);
        BigDecimal b4 = b3.subtract(a5);
        BigDecimal b5 = b4.add(a6);
        return b5.doubleValue() / (sumDegrees + 0.0);
    }
}