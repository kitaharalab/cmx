package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import java.util.*;

//public class HarmonicsExtractor<D extends SPElement>
//  extends SPModule<D, PeakSet> {
public class HarmonicsExtractor<D extends SPElement> extends SPModule {

  private double f0range;
  private double freqRange;
  private double powerthrs;
  private int nHarmonics;
  private int nHarmsForF0Calc;
  private boolean setParams = false;

  protected String getParamCategory() {
    return "harmonics";
  }
  protected String[] getUsedParamNames() {
    return new String[]{"F0_SEARCH_RANGE", "HARMONICS_SEARCH_RANGE", 
                        "POWER_THRESHOLD", "NUM_OF_HARMONICS", 
                        "NUM_OF_HARMONICS_FOR_F0_CALC"};
  }
  private void setParams() {
    f0range = getParamDouble("F0_SEARCH_RANGE");
    freqRange = getParamDouble("HARMONICS_SEARCH_RANGE");
    powerthrs = getParamDouble("POWER_THRESHOLD");
    nHarmonics = getParamInt("NUM_OF_HARMONICS");
    nHarmsForF0Calc = getParamInt("NUM_OF_HARMONICS_FOR_F0_CALC");
    setParams = true;
  }
/*
  public void execute(List<QueueReader<D>> src,
                      List<TimeSeriesCompatible<PeakSet>> dest)
    throws InterruptedException {
    if (!setParams) setParams();
    PeakSet peakset = (PeakSet)src.get(0).take();
    SPDoubleArray f0array = (SPDoubleArray)src.get(1).take();
    double f0 = f0array.get(0);
    dest.get(0).add(extractHarmonics(peakset, f0, peakset.hasNext() && f0array.hasNext()));
  }
*/
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    if (!setParams) setParams();
    PeakSet peakset = (PeakSet)src[0];
    SPDoubleArray f0array = (SPDoubleArray)src[1];
    double f0 = f0array.get(0);
    dest[0].add(extractHarmonics(peakset, f0, peakset.hasNext() && f0array.hasNext()));
  }

  private PeakSet extractHarmonics(PeakSet peakset, double f0, boolean hasNext) {
    PeakSet harmonics = new PeakSet(nHarmonics, hasNext);
    DoubleArray ff = peakset.freq();
    DoubleArray pp = div(peakset.power(), sum(peakset.power()));
    double accurateF0 = calcAccurateF0(f0, ff, pp);
    for (int i = 0; i < nHarmonics; i++) {
      BooleanArray mask = 
        or(lessThan(ff, (i+1-freqRange)*accurateF0), 
           greaterThan(ff, (i+1+freqRange)*accurateF0));
      DoubleArray p = mask(pp, mask, 0.0);
//      DoubleArray p = mask(pp, mask, Double.NEGATIVE_INFINITY);
      MaxResult maxresult = max(p);
//      System.err.println("   " + maxresult.max);
//      System.err.println("      " + maxresult.argmax);
      if (maxresult.argmax > 0) {
        harmonics.setFreq(i, ff.get(maxresult.argmax));
        harmonics.setPower(i, maxresult.max);
      } else {
        harmonics.setFreq(i, 0.0);
        harmonics.setPower(i, 0.0);
      }
      System.out.print(harmonics.power(i) + " ");
    }
    System.out.println();
    return harmonics;
  }

  private double calcAccurateF0(double f0, DoubleArray freq, 
                                DoubleArray power) {
    double accurateF0 = 0.0;
    for (int i = 1; i < nHarmsForF0Calc; i++) {
      BooleanArray mask = 
        or(lessThan(freq, i*f0/f0range), greaterThan(freq, i*f0*f0range));
      DoubleArray p = mask(power, mask, Double.NEGATIVE_INFINITY);
      MaxResult maxresult = max(p);
      if (maxresult.max > powerthrs) {
        accurateF0 = freq.get(maxresult.argmax) / i;
        break;
      }
    }
    return accurateF0;
  }
/*
  public int getInputChannels() {
    return 2;
  }

  public int getOutputChannels() {
    return 1;
  }
*/
  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ PeakSet.class, SPDoubleArray.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    return new Class[]{ PeakSet.class };
  }
}

/*
public class HarmonicsExtractor
  implements ProducerConsumerCompatible<PeaksCompatible, 
  PeaksCompatible> {

  private static ConfigXMLWrapper config;
  private static double F0_SEARCH_RANGE;
  private static double HARMONICS_SEARCH_RANGE;
  private static int NUM_OF_FRAMES_FOR_INTERPOLATE;
  private static double POWER_THRESHOLD;
  private static int NUM_OF_HARMONICS_FOR_F0_CALC;
  private static String INTERPOLATION;
  private static AbstractInterpolationModule interp;
  private static int SGSMOOTH_RANGE = 20;
  private static boolean paramSet = false;

  private static void setParams() {
    config = CMXCommand.getConfigXMLWrapper();
    F0_SEARCH_RANGE = config.getParamDouble("param", "harmonics", 
                                            "F0_SEARCH_RANGE");
    HARMONICS_SEARCH_RANGE = config.getParamDouble("param", "harmonics", 
                                                   "HARMONICS_SEARCH_RANGE");
    NUM_OF_FRAMES_FOR_INTERPOLATE = 
      config.getParamInt("param", "harmonics", 
                         "NUM_OF_FRAMES_FOR_INTERPOLATE");
    POWER_THRESHOLD = config.getParamDouble("param", "harmonics", 
                                            "POWER_THRESHOLD");
    NUM_OF_HARMONICS_FOR_F0_CALC = 
      config.getParamInt("param", "harmonics", 
                         "NUM_OF_HARMONICS_FOR_F0_CALC");
    INTERPOLATION = config.getParam("param", "harmonics", "INTERPOLATION");
    if (INTERPOLATION.equals("none"))
      interp = null;
    else if (INTERPOLATION.equals("linear"))
      interp = LinearInterpolationModule.getInstance();
    else if (INTERPOLATION.equals("exp"))
      interp = ExpInterpolationModule.getInstance();
    else
      throw new ParamNotFoundException
        ("INTERPOLATION not found or wrong.");
    paramSet = true;
  }

}

*/