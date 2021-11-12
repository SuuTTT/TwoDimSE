package SE;

import graphdata.Graph;
import graphdata.PairNode;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * 节点（社区）间的交互
 * <p>
 * 目的在于把相关的数据封装在一起，方便阅读，
 * 但是经过实验测试发现会影响性能。所以在为注解放到原始代码中。
 */
public class Interactive {
    //节点之间的割
    private HashMap<PairNode, Double> cuts;
    //社区与其相联系的社区
    //connection是双向的，不同于pairNode，更新时应注意双向更新
    private HashMap<Integer, Set<Integer>> connections;
    // 节点之间的△H。使用了额外的存储，待优化（TreeMap）。
    // TreeSet可以为类按照一定规则排序，排序效率为O(logn)
    // private TreeMap<PairNode, Double> commDeltaHTreeMap;
    private HashMap<PairNode, CommDeltaH> commDeltaHMap;
    private TreeSet<CommDeltaH> commDeltaHSet;


    public Interactive(Graph graph) {
        this.cuts = graph.getWeights();
        this.connections = graph.getConnection();
        this.commDeltaHMap = new HashMap<>();
        this.commDeltaHSet = new TreeSet<>();
    }


    /**
     * 获取节点间最大的△H
     *
     * @return
     */
    public CommDeltaH maxCommDeltaH() {
        return commDeltaHSet.last();
    }

    /**
     * 是否还存在交互的节点
     *
     * @return
     */
    public boolean isEmpty() {
        return commDeltaHSet.isEmpty();
    }


    public double getCut(PairNode pairNode) {
        return cuts.get(pairNode);
    }

    /**
     * 两社区（结点）融合
     *
     * @param Comms
     * @param commDeltaH
     */
    public void merge(PairNode Comms, CommDeltaH commDeltaH) {
        int commLeft = Comms.getP1();
        int commRight = Comms.getP2();

        commDeltaHMap.remove(Comms);
        commDeltaHSet.remove(commDeltaH);
        connections.get(commLeft).remove(commRight);
        connections.get(commRight).remove(commLeft);
        cuts.remove(Comms);
    }


    public Set<Integer> getConnection(int comm) {
        return connections.get(comm);
    }

    public void remove(PairNode pairnode) {
        int p1 = pairnode.getP1();
        int k = pairnode.getP2();
        commDeltaHSet.remove(commDeltaHMap.get(pairnode));
        commDeltaHMap.remove(pairnode);
        cuts.remove(pairnode);
        connections.get(k).remove(p1);
    }

    /**
     * 节点融合更新相关信息，此处是对已存在交互的社区进行更新
     *
     * @param pairLeftAndK
     * @param cutIk
     * @param newDelta
     */
    public void update(PairNode pairLeftAndK, double cutIk, double newDelta) {
        cuts.put(pairLeftAndK, cutIk);
        commDeltaHSet.remove(commDeltaHMap.get(pairLeftAndK));
        CommDeltaH newDeltaH = new CommDeltaH(pairLeftAndK, newDelta);
        commDeltaHSet.add(newDeltaH);
        commDeltaHMap.put(pairLeftAndK, newDeltaH);
    }

    /**
     * 节点融合更新相关信息，此处是对未存在交互的社区进行更新，使其存在交互
     *
     * @param pairLeftAndK
     * @param pairRightAndK
     * @param cutJk
     * @param newDelta
     */
    public void update(PairNode pairLeftAndK, PairNode pairRightAndK, double cutJk, double newDelta) {
        cuts.put(pairLeftAndK, cutJk);
        cuts.remove(pairRightAndK);
        CommDeltaH commDeltaH = new CommDeltaH(pairLeftAndK, newDelta);
        commDeltaHSet.remove(commDeltaHMap.get(pairRightAndK));
        commDeltaHSet.add(commDeltaH);
        commDeltaHMap.remove(pairRightAndK);
        commDeltaHMap.put(pairLeftAndK, commDeltaH);
        int commLeft = pairLeftAndK.getP1();
        int commRight = pairRightAndK.getP1();
        int k = pairLeftAndK.getP2();
        connections.get(commLeft).add(k);
        connections.get(k).add(commLeft);
        connections.get(k).remove(commRight);
    }

    public HashMap<PairNode, Double> getCuts() {
        return cuts;
    }

    public void putComm(PairNode p, CommDeltaH commDeltaH) {
        commDeltaHMap.put(p, commDeltaH);
        commDeltaHSet.add(commDeltaH);
    }
}
