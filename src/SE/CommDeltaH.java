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
        return this.pairComms.equals(c.pairComms)  && this.deltaH == c.deltaH;
    }

    @Override
    public int compareTo(Object o) {
        CommDeltaH c = (CommDeltaH) o;
        int cp = Double.compare(this.deltaH, c.deltaH);
        if (cp != 0) {
            return cp;
        } else {
            return Integer.compare(this.pairComms.getP1(), c.pairComms.getP1());
        }
    }

    @Override
    public String toString() {
        return String.format("%d->%d: %f", pairComms.getP1(), pairComms.getP2(), deltaH);
    }
}
