package GraphData;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Graph {
    private int numNodes;
    private double sumDegrees;

    private HashMap<PairNode, Double> weights;
    private HashMap<Integer, Set<Integer>> connection;
    //此处使用数组而非HashMap的原因是：图的节点的编号连续，如从1-55555，共55555个节点
    private double[] nodeDegree;


    public Graph(int numNodes) {
        this.numNodes = numNodes;
        int initialCap = 3 * numNodes / 4 + 1;
        this.weights = new HashMap<>(initialCap);
        this.connection = new HashMap<>(initialCap);
        this.nodeDegree = new double[numNodes + 1]; //节点从1开始而非0
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public double getSumDegrees() {
        return sumDegrees;
    }

    public void setSumDegrees(double sumDegrees) {
        this.sumDegrees = sumDegrees;
    }

    public HashMap<PairNode, Double> getWeights() {
        return weights;
    }

    public void setWeights(HashMap<PairNode, Double> weights) {
        this.weights = weights;
    }

    public double[] getNodeDegree() {
        return nodeDegree;
    }

    public void setNodeDegree(double[] nodeDegree) {
        this.nodeDegree = nodeDegree;
    }

    public HashMap<Integer, Set<Integer>> getConnection() {
        return connection;
    }

    public void setConnection(HashMap<Integer, Set<Integer>> connection) {
        this.connection = connection;
    }
}

