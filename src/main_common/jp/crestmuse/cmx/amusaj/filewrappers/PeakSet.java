package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.DoubleArrayFactory;
import jp.crestmuse.cmx.misc.Encodable;

public class PeakSet implements Encodable {
  private int nPeaks;
  private DoubleArray freq;
  private DoubleArray power;
  private DoubleArray phase;
  private DoubleArray iid;
  private DoubleArray ipd;
  public long music_position;             // is 'public' really ok?

    //  private boolean hasNext = true;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  public PeakSet(int nPeaks) {
    this.nPeaks = nPeaks;
    freq = factory.createArray(nPeaks);
    power = factory.createArray(nPeaks);
    phase = factory.createArray(nPeaks);
    iid = factory.createArray(nPeaks);
    ipd = factory.createArray(nPeaks);
    //    this.hasNext = hasNext;
  }

  public PeakSet(DoubleArray freq, DoubleArray power, DoubleArray phase, 
                 DoubleArray iid, DoubleArray ipd) {
    nPeaks = freq.length();
    this.freq = freq;
    this.power = power;
    this.phase = phase;
    this.iid = iid;
    this.ipd = ipd;
    //    this.hasNext = hasNext;
  }

  public void setPeak(int i, double freq, double power, double phase, 
                      double iid, double ipd) {
    this.freq.set(i, freq);
    this.power.set(i, power);
    this.phase.set(i, phase);
    this.iid.set(i, iid);
    this.ipd.set(i, ipd);
  }

  public int nPeaks() {
    return nPeaks;
  }

  public void setFreq(int i, double value) {
    freq.set(i, value);
  }

  public void setPower(int i, double value) {
    power.set(i, value);
  }

  public double freq(int i) {
    return freq.get(i);
  }

  public double power(int i) {
    return power.get(i);
  }

  public double phase(int i) {
    return phase.get(i);
  }

  public double iid(int i) {
    return iid.get(i);
  }

  public double ipd(int i) {
    return ipd.get(i);
  }

  public DoubleArray freq() {
    return freq;
  }

  public DoubleArray power() {
    return power;
  }

  public DoubleArray phase() {
    return phase;
  }

  public DoubleArray iid() {
    return iid;
  }

  public DoubleArray ipd() {
    return ipd;
  }
  
  public void filter(Filter filt) {
    if (filt != null && (filt.usesLCF || filt.usesHCF)) {
      for (int i = 0; i < nPeaks; i++) {
        double f = freq.get(i);
        if (filt.usesLCF) {
          if (f < filt.LCFbtm) {
            power.set(i, 0.0);
          } else if (f < filt.LCFtop) {
            double a = (f - filt.LCFbtm) / (filt.LCFtop - filt.LCFbtm);
            power.set(i, a * power.get(i));
          }
        }
        if (filt.usesHCF) {
          if (f > filt.HCFbtm) {
            power.set(i, 0.0);
          } else if (f > filt.HCFtop) {
            double a = (f - filt.HCFbtm) / (filt.HCFtop - filt.HCFbtm);
            power.set(i, a * power.get(i));
          }
        }
      }
    }
  }

  public String encode() {
    StringBuilder sb = new StringBuilder();
    sb.append(nPeaks).append(" ");
    for (int i = 0; i < nPeaks; i++) {
      sb.append(freq(i)).append(" ");
      sb.append(power(i)).append(" ");
      sb.append(phase(i)).append(" ");
      sb.append(iid(i)).append(" ");
      sb.append(ipd(i)).append(" ");
    }
    return sb.toString();
  }

    //  public boolean hasNext() {
    //    return hasNext;
    //  }

  public static class Filter {
    private boolean usesLCF, usesHCF;
    private double LCFbtm, LCFtop, HCFbtm, HCFtop;
  }

  public static final Filter getFilter(boolean usesLCF, double LCFbtm, 
                                       double LCFtop, boolean usesHCF, 
                                       double HCFtop, double HCFbtm) {
    Filter f = new Filter();
    f.usesLCF = usesLCF;
    f.LCFbtm = LCFbtm;
    f.LCFtop = LCFtop;
    f.usesHCF = usesHCF;
    f.HCFtop = HCFtop;
    f.HCFbtm = HCFbtm;
    return f;
  }


}