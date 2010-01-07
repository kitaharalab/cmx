package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.*;
//import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;

public class HarmonicsTimeSeries {
  private PeakSet[] peaks;
  private int nHarmonics;
//  private DoubleArray f0env;
  private DoubleArray powerenv;
  private DoubleArray logPowerEnv;
  private DoubleArray logSmoothPowerEnv;
  private boolean analyzed = false;

  private DoubleArrayFactory factory = DoubleArrayFactory.getFactory();

  public HarmonicsTimeSeries(int nFrames, int nHarmonics) {
    peaks = new PeakSet[nFrames];
    this.nHarmonics = nHarmonics;
    powerenv = factory.createArray(nFrames);
  }

  public void set(int i, PeakSet peakset) {
    if (peakset.nPeaks() != nHarmonics)
      throw new IllegalStateException("Num of peaks incompatible.");
    peaks[i] = peakset;
    // interpolationの前に全体powerを出すべきかどうか要検討
    powerenv.set(i, sum(peakset.power()));
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

  // ExpInterpolationModule is prefered.
  public void interpolate(AbstractInterpolationModule i, int nFrames) {
    for (int k = 0; k < nHarmonics; k++) {
      i.run(new FreqDoubleArray(k, 0, peaks.length), nFrames);
      i.run(new PowerDoubleArray(k, 0, peaks.length), nFrames);
    }
    i.run(powerenv, nFrames);
    logPowerEnv = calcLogPowerEnv(powerenv);
    logSmoothPowerEnv = calcLogSmoothPowerEnv(powerenv);
    analyzed = true;
  }
  
  private static DoubleArray calcLogPowerEnv(DoubleArray powerenv) {
    DoubleArray logPowerEnv = add(powerenv, 1.0 / 32768.0);
    logX(logPowerEnv, 10);
    mulX(logPowerEnv, 20);
    subX(logPowerEnv, max(logPowerEnv));
    return logPowerEnv;
  }

  private static DoubleArray calcLogSmoothPowerEnv(DoubleArray powerenv) {
    DoubleArray logSmoothPowerEnv = sgsmooth(sgsmooth(powerenv, 20), 20);
    addX(logSmoothPowerEnv, -min(logSmoothPowerEnv) + 1.0/32768.0);
    logX(logSmoothPowerEnv, 10);
    mulX(logSmoothPowerEnv, 20);
    subX(logSmoothPowerEnv, max(logSmoothPowerEnv));
    return logSmoothPowerEnv;
  }
    
  public DoubleArray getLogPowerEnv() {
    if (!analyzed)
      throw new IllegalStateException();
    return logPowerEnv;
  }

  public DoubleArray getLogSmoothPowerEnv() {
    if (!analyzed)
      throw new IllegalStateException();
    return logSmoothPowerEnv;
  }

  public DoubleArray calcMedianPower(int from, int thru) {
    if (!analyzed)
      throw new IllegalStateException();
    DoubleArray median = factory.createArray(nHarmonics);
    for (int k = 0; k < nHarmonics; k++) {
      DoubleArray power = new PowerDoubleArray(k, from, thru);
      median.set(k, median(power));
    }
    double sumpower = sum(median);
    if (sumpower != 0.0)
      divX(median, sumpower);
    return median;
  }

  public DoubleArray calcLengthsOfHarmonicComponents(double thrsPower, 
                                                     int from, int thru) {
    if (!analyzed)
      throw new IllegalStateException();
    DoubleArray lengths = factory.createArray(nHarmonics);
    double thrs2 = Math.pow(10, thrsPower / 20);
    for (int k = 0; k < nHarmonics; k++) {
      DoubleArray power = new PowerDoubleArray(k, from, thru);
      BooleanArray isPowerHigher = greaterThan(power, thrs2);
      lengths.set(k, ratioTrue(isPowerHigher));
    }
    return lengths;
  }

  public DoubleArray getF0Envelope() {
    return new FreqDoubleArray(0, 0, peaks.length);
  }

  private abstract class MyDoubleArray implements DoubleArray {
    int k, from, thru;
    MyDoubleArray(int k, int from, int thru) {
      this.k = k;
      this.from = from;
      this.thru = thru;
    }
    public int length() {
      return thru - from;
    }
    public Object clone() {
      throw new UnsupportedOperationException();
    }
    public double[] toArray() {
      double[] array = new double[length()];
      for (int i = 0; i < array.length; i++)
        array[i] = get(i);
      return array;
//      throw new UnsupportedOperationException();
    }
    public DoubleArray subarrayX(int from, int thru) {
      this.from += from;
      this.thru = this.from + thru;
      return this;
//      throw new UnsupportedOperationException();
    }
    public String encode() {
      return jp.crestmuse.cmx.math.Utils.toString2(this);
    }
  }

  private class FreqDoubleArray extends MyDoubleArray  {
    FreqDoubleArray(int k, int from, int thru) {
      super(k, from, thru);
    }
    public double get(int i) {
      return peaks[i + from].freq(k);
    }
    public void set(int i, double value) {
      peaks[i + from].setFreq(k, value);
    }
  }

  private class PowerDoubleArray extends MyDoubleArray {
    PowerDoubleArray(int k, int from, int thru) {
      super(k, from, thru);
    }
    public double get(int i) {
      return peaks[i + from].power(k);
    }
    public void set(int i, double value) {
      peaks[i + from].setPower(k, value);
    }
  }
    

}

    
