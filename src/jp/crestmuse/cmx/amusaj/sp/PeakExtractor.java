package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.math.Utils.*;
import jp.crestmuse.cmx.misc.*;
import static java.lang.Math.*;
import java.util.*;

//public class PeakExtractor extends SPModule<SPComplexArray,PeakSet> {
public class PeakExtractor extends SPModule {

    private static double powerthrs = 0;
    private static double rpowerthrs = 0;

    private int fs;
    private int winsize;

    private int size = 0;
    private double[] power0;
    private DoubleArray freq, power, phase, iid, ipd;
    private int t;

  private DoubleArrayFactory factory = DoubleArrayFactory.getFactory();
  private ConfigXMLWrapper config;

    public PeakExtractor() {
      config = CMXCommand.getConfigXMLWrapper();
      powerthrs = config.getParamDouble("param", "fft", "POWER_THRESHOLD");
      rpowerthrs = config.getParamDouble("param", "fft", 
                                         "RELATIVE_POWER_THRESHOLD");
    }

  private void reset(int size) {
	this.size = size;
	power0 = new double[size];
	freq = factory.createArray(size);
	power = factory.createArray(size);
	phase = factory.createArray(size);
	iid = factory.createArray(size);
	ipd = factory.createArray(size);
    }
/*
  public int getInputChannels() {
    return 3;
  }

  public int getOutputChannels() {
    return 1;
  }
*/
  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ SPComplexArray.class, SPComplexArray.class, SPComplexArray.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    return new Class[]{ PeakSet.class };
  }
/*
  public void execute(List<QueueReader<SPComplexArray>> src, 
                      List<TimeSeriesCompatible<PeakSet>> dest) 
    throws InterruptedException{
    SPComplexArray fftresult = src.get(0).take();
    SPComplexArray fftresultL, fftresultR;
    boolean isStereo;
    if (src.size() > 1 && src.get(1) != null) {
      fftresultL = src.get(1).take();
      fftresultR = src.get(2).take();
      isStereo = true;
    } else {
      fftresultL = null;
      fftresultR = null;
      isStereo = false;
    }
    double maxpower = Double.NEGATIVE_INFINITY;
    int length = fftresult.length();
    if (size != length)
      reset(length);
    for (int i = 0; i < size; i++) {
      double pp = hypot(fftresult.getReal(i), fftresult.getImag(i));
      power0[i] = pp;
      if (pp > maxpower)
        maxpower = pp;
    }
    double powerthrs2 = maxpower * rpowerthrs;
    int k = 0, i = 2;
    while (i < size-2) {
      if (power0[i] > power0[i+1]) {
        if (power0[i+1] > power0[i+2]) {
          if (power0[i] > power0[i-1]
              && power0[i-1] > power0[i-2]
              && power0[i] > powerthrs
              && power0[i] > powerthrs2) {
            freq.set(k, (double)(i * fs) / (double)winsize);
            power.set(k, power0[i]);
            phase.set(k, atan2(fftresult.getImag(i), fftresult.getReal(i)));
            if (isStereo) {
              iid.set(k, hypot(fftresultR.getReal(i), fftresultR.getImag(i))
                      / hypot(fftresultL.getReal(i), fftresultL.getImag(i)));
              ipd.set(k, atan2(fftresultR.getImag(i), fftresultR.getReal(i))
                      - atan2(fftresultL.getImag(i), fftresultL.getReal(i)));
            } else {
              iid.set(k, 1.0);
              ipd.set(k, 0.0);
            }
            k++;
            i += 4;
          } else {
            i += 4;
          }
        } else {
          i += 3;
        }
      } else if (power0[i] <= power0[i-1]) {
        i += 2;
      } else {
        i++;
      }
    }
    dest.get(0).add
      (new PeakSet(subarray(freq, 0, k), subarray(power, 0, k), 
                   subarray(phase, 0, k), subarray(iid, 0, k), 
                   subarray(ipd, 0, k), fftresult.hasNext()));
  }
*/
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    SPComplexArray fftresult = (SPComplexArray)src[0];
    SPComplexArray fftresultL, fftresultR;
    boolean isStereo;
    if (src.length > 1 && src[1] != null) {
      fftresultL = (SPComplexArray)src[1];
      fftresultR = (SPComplexArray)src[2];
      isStereo = true;
    } else {
      fftresultL = null;
      fftresultR = null;
      isStereo = false;
    }
    double maxpower = Double.NEGATIVE_INFINITY;
    int length = fftresult.length();
    if (size != length)
      reset(length);
    for (int i = 0; i < size; i++) {
      double pp = hypot(fftresult.getReal(i), fftresult.getImag(i));
      power0[i] = pp;
      if (pp > maxpower)
        maxpower = pp;
    }
    double powerthrs2 = maxpower * rpowerthrs;
    int k = 0, i = 2;
    while (i < size-2) {
      if (power0[i] > power0[i+1]) {
        if (power0[i+1] > power0[i+2]) {
          if (power0[i] > power0[i-1]
              && power0[i-1] > power0[i-2]
              && power0[i] > powerthrs
              && power0[i] > powerthrs2) {
            freq.set(k, (double)(i * fs) / (double)winsize);
            power.set(k, power0[i]);
            phase.set(k, atan2(fftresult.getImag(i), fftresult.getReal(i)));
            if (isStereo) {
              iid.set(k, hypot(fftresultR.getReal(i), fftresultR.getImag(i))
                      / hypot(fftresultL.getReal(i), fftresultL.getImag(i)));
              ipd.set(k, atan2(fftresultR.getImag(i), fftresultR.getReal(i))
                      - atan2(fftresultL.getImag(i), fftresultL.getReal(i)));
            } else {
              iid.set(k, 1.0);
              ipd.set(k, 0.0);
            }
            k++;
            i += 4;
          } else {
            i += 4;
          }
        } else {
          i += 3;
        }
      } else if (power0[i] <= power0[i-1]) {
        i += 2;
      } else {
        i++;
      }
    }
    dest[0].add
      (new PeakSet(subarray(freq, 0, k), subarray(power, 0, k), 
                   subarray(phase, 0, k), subarray(iid, 0, k), 
                   subarray(ipd, 0, k), fftresult.hasNext()));
  }
                         
  public void setParams(Map<String,String> params) {
    super.setParams(params);
    fs = getParamInt("SAMPLE_RATE");
    winsize = getParamInt("WINDOW_SIZE");
  }

}
