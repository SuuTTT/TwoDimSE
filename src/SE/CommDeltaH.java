package SE;

import graphdata.PairNode;

public class CommDeltaH implements Comparable {
    private PairNode pairComms;
    private double deltaH;

    public CommDeltaH(PairNode pairComms, double deltaH) {
        this.pairComms = pairComms;
        this.deltaH = deltaH;
    }

    public PairNode getPairComms() {
        return pairComms;
    }

    public void setPairComms(PairNode pairComms) {
        this.pairComms = pairComms;
    }

    public double getDeltaH() {
        return deltaH;
    }

    public void setDeltaH(double deltaH) {
        this.deltaH = deltaH;
    }

    @Override
    public int hashCode() {
        return pairComms.hashCode();
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
        CommDeltaH c = (CommDeltaH) o;
        return this.pairComms.equals(c.pairComms) && this.deltaH == c.deltaH;
    }

    /**
     * 社区之间的不确定性的比较函数，
     * 注意：可能会影响所分割社区的结果，但不改变总体消除的不确定性的量
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        CommDeltaH c = (CommDeltaH) o;
        int cmp = Double.compare(this.deltaH, c.deltaH);
        return cmp == 0 ? Integer.compare(this.pairComms.getP1(), c.pairComms.getP1()) : cmp;
    }

    @Override
    public String toString() {
        return String.format("%d->%d: %f", pairComms.getP1(), pairComms.getP2(), deltaH);
    }
}
