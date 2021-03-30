package GraphData;


/**
 * 无向图无需严格区分起点和终点
 */
public class PairNode {
    private int p1;
    private int p2;

    public PairNode(int p1, int p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    /**
     * 判断是否为自环
     *
     * @return
     */
    public boolean isValid() {
        if (this.p1 != this.p2) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        return Math.max(p1, p2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        PairNode p = (PairNode) o;
        if (p1 == p.p1 && p2 == p.p2) {
            return true;
        } else if (p1 == p.p2 && p2 == p.p1) {
            return true;
        }

        return false;
    }

    @Override
    public String toString(){
        return String.format("%d -> %d", p1, p2);
    }
}