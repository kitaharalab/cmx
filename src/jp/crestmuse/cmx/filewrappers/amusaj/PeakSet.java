package jp.crestmuse.cmx.filewrappers.amusaj;

public class PeakSet {
  private int nPeaks;
  private DoubleArray freq;
  private DoubleArray power;
  private DoubleArray phase;
  private DoubleArray iid;
  private DoubleArray ipd;
  private DoubleArrayFactory factory;

  public PeakSet(int nPeaks, DoubleArrayFactory factory) {
    this.nPeaks = nPeaks;
    this.factory = factory;
    freq = factory.createArray(nPeaks);
    power = factory.createArray(nPeaks);
    phase = factory.createArray(nPeaks);
    iid = factory.createArray(nPeaks);
    ipd = factory.createArray(nPeaks);
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

}