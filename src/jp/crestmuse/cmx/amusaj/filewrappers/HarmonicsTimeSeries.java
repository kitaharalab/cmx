package jp.crestmuse.cmx.amusaj.filewrappers;

import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.amusaj.sp.*;

public class HarmonicsTimeSeries implements SPElement {
  private PeakSet[] peaks;
  private int nHarmonics;

  public HarmonicsTimeSeries(int nFrames, int nHarmonics) {
    peaks = new PeakSet[nFrames];
    this.nHarmonics = nHarmonics;
  }

  public void set(int i, PeakSet peakset) {
    if (peakset.nPeaks() != nHarmonics)
      throw new IllegalStateException("Num of peaks incompatible.");
    peaks[i] = peakset;
  }

  public PeakSet get(int i) {
    return peaks[i];
  }

  public int nFrames() {
    return peaks.length;
  }

  public int nHarmonics() {
    return nHarmonics;
  }

  public void interpolate(AbstractInterpolationMoudle i) {
    for (int k = 0; k < nHarmonics; k++) {
      i.run(new FreqDoubleArray(k));
      i.run(new PowerDoubleArray(k));
    }
  }
  


  private abstract class MyDoubleArray implements DoubleArray {
    int k;
    MyDoubleArray(int k) {
      this.k = k;
    }
    public int length() {
      return peaks.length;
    }
    public Object clone() {
      throw new UnsupportedOperationException();
    }
    public double[] toArray() {
      throw new UnsupportedOperationException();
    }
    public DoubleArray subarrayX(int from, int thru) {
      throw new UnsupportedOperationException();
    }
  }

  private class FreqDoubleArray extends MyDoubleArray  {
    FreqDoubleArray(int k) {
      super(k);
    }
    public double get(int i) {
      return peaks[i].freq(k);
    }
    public void set(int i, double value) {
      peaks[i].setFreq(k, value);
    }
  }

  private class PowerDoubleArray extends MyDoubleArray {
    PowerDoubleArray(int k) {
      super(k);
    }
    public double get(int i) {
      return peaks[i].power(k);
    }
    public void set(int i, double value) {
      peaks[i].setPower(k, value);
    }
  }
    

}

    
