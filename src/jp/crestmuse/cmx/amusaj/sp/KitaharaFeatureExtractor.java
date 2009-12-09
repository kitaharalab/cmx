package jp.crestmuse.cmx.amusaj.sp;

import org.apache.commons.math.distribution.*;
import org.apache.commons.math.optimization.*;
import org.apache.commons.math.optimization.general.*;
import org.apache.commons.math.optimization.fitting.*;
import org.apache.commons.math.analysis.polynomials.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import java.util.*;


public class KitaharaFeatureExtractor 
  implements FeatureExtractor<HarmonicsTimeSeries> {

  int ndiv;
  AbstractInterpolationModule interp = ExpInterpolationModule.getInstance();
  int maxFramesForInterpolation;
  private static DoubleArrayFactory factory = DoubleArrayFactory.getFactory();

  // if 10 harmonics, 3 divs, 
  DoubleArray featHarmonics;  // Nos. 1-11
  DoubleArray featLengthsOfHarmonics; 
  DoubleArray featRateOfLongHarmonics; // Nos. 12-20
  DoubleArray featGradOfPowerEnvelope; // Nos. 21
  DoubleArray featMedianOfDiffPower;   // Nos. 22-24
  DoubleArray featRatioMaxPower;       
  DoubleArray featAM;                  // Nos. 25-26
  DoubleArray featFM;                  // Nos. 27-28

  public KitaharaFeatureExtractor(int ndiv, 
//                                  AbstractInterpolationModule interp,
                                  int maxFramesForInterpolation) {
    this.ndiv = ndiv;
//    this.interp = interp;
    this.maxFramesForInterpolation = maxFramesForInterpolation;
  }

  public void extractFeatures(HarmonicsTimeSeries h) {
    extractFeatures(h, 0, h.nFrames());
  }

  private void extractFeatures(HarmonicsTimeSeries h, int from, int thru) {
    try {
      h.interpolate(interp, maxFramesForInterpolation);
      DoubleArray logPowerEnv = h.getLogPowerEnv();
      DoubleArray logSmoothPowerEnv = h.getLogSmoothPowerEnv();
      featHarmonics = calcHarmonicComponents(h, from, thru);
      featLengthsOfHarmonics 
        = calcLengthsOfHarmonicComponents(h, -60, from, thru);
      featRateOfLongHarmonics
        = calcRateOfLongHarmonicComponents(featLengthsOfHarmonics);
      featGradOfPowerEnvelope = factory.createArray(1);
      featGradOfPowerEnvelope.
        set(0, calcGradOfPowerEnvelope(logPowerEnv, from, thru));
      featMedianOfDiffPower 
        = calcMedianOfDiffPower(logSmoothPowerEnv, from, thru, ndiv);
      featAM = calcModulation(logPowerEnv, from, thru);
      featFM = calcModulation(h.getF0Envelope(), from, thru);
//      System.err.print(".");
    } catch (org.apache.commons.math.MathException e) {
      throw new SPException(e);
    }
  }

  private static DoubleArray calcHarmonicComponents(HarmonicsTimeSeries h, 
                                                    int from, int thru) 
    throws org.apache.commons.math.MathException {
    int nHarmonics = h.nHarmonics();
    DoubleArray feats = factory.createArray(nHarmonics + 1);
    DoubleArray medianPower = h.calcMedianPower(from, thru);
    DoubleArray ith = makeArithmeticSeries(nHarmonics);
    double ctrd = sum(mul(medianPower, ith));
    feats.set(0, ctrd);
    for (int i = 0; i < nHarmonics -  1; i++)
      feats.set(i + 1, sum(medianPower, 0, i+1));
    double ratioEvenOdd = (sumodd(medianPower) + Double.MIN_VALUE)
      / (sumeven(medianPower) + Double.MIN_VALUE);
    NormalDistributionImpl nd = new NormalDistributionImpl(0.0, 100.0);
    ratioEvenOdd = nd.cumulativeProbability(ratioEvenOdd) 
      - nd.cumulativeProbability(-ratioEvenOdd);
    feats.set(nHarmonics, ratioEvenOdd);
    return feats;
  }

  private static DoubleArray calcLengthsOfHarmonicComponents
  (HarmonicsTimeSeries h, double thrsPower, int from, int thru) {
      return h.calcLengthsOfHarmonicComponents(thrsPower, from, thru);
  }

  private static DoubleArray calcRateOfLongHarmonicComponents
    (DoubleArray featLengthsOfHarmonicComponents) {
    final double[] thrs = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    DoubleArray feats = factory.createArray(thrs.length);
    for (int i = 0; i < thrs.length; i++)
      feats.set(i, 
                nGreaterThan(featLengthsOfHarmonicComponents, thrs[i]));
    return feats;
  }

  private static double calcGradOfPowerEnvelope (DoubleArray powerenv, 
                                                 int from, int thru) 
    throws OptimizationException {
    PolynomialFitter f 
//      = new PolynomialFitter(1, new GaussNewtonOptimizer(false));
      = new PolynomialFitter(1, new LevenbergMarquardtOptimizer());
    for (int i = 0; i < thru - from; i++) 
      f.addObservedPoint(1.0, 1.0 + i, powerenv.get(from + i));
    PolynomialFunction pf = f.fit();
    return pf.getCoefficients()[1];
  }

  private static DoubleArray calcMedianOfDiffPower(DoubleArray powerenv, 
                                                   int from, int thru, int n) {
    DoubleArray feats = factory.createArray(n);
    for (int i = 0; i < n; i++) {
      int thru1 = from + (i + 1) * (thru - from) / n;
      feats.set(i, calcMedianOfDiffPower(powerenv, from, thru1));
    }
    return feats;
  }
  
  private static double calcMedianOfDiffPower(DoubleArray powerenv, 
                                              int from, int thru) {
//    System.err.println("powerenv: " + powerenv.length());
//    System.err.println("from: " + from + " thru: " + thru);
    return median(diff(powerenv.subarrayX(from, thru)));
  }

  private static DoubleArray calcRatioMaxPower
  (DoubleArray powerenv, int from, int thru, int n) {
    DoubleArray powerenv2 = powerenv.subarrayX(from, thru);
    double maxpower = max(powerenv2);
    DoubleArray feats = factory.createArray(n);
    for (int i = 0; i < n; i++) {
      int t = (i + 1) * (thru - from) / (n + 1);
      feats.set(i, powerenv2.get(t) - maxpower);
    }
    return feats;
  }

  private static DoubleArray calcRatioMaxPower
  (DoubleArray powerenv, int from, int thru, int[] index) {
    DoubleArray powerenv2 = powerenv.subarrayX(from, thru);
    double maxpower = max(powerenv2);
    DoubleArray feats = factory.createArray(index.length);
    for (int i = 0; i < index.length; i++)
      feats.set(i, powerenv2.get(index[i]) - maxpower);
    return feats;
  }

  private static DoubleArray calcModulation
  (DoubleArray env, int from, int thru) {
    DoubleArray env2 = env.subarrayX(from, thru);
    DoubleArray modulation = factory.createArray(2);
    modulation.set(0, calcFreqOfModulation(env2));
    modulation.set(1, calcAmpOfModulation(env2));
    return modulation;
  }

  private static double calcFreqOfModulation(DoubleArray env) {
    DoubleArray diffenv = sdiff(env, 3);
    double FM = (double)nZeroCross(diffenv) / (double)env.length();
    return FM;
  }

  private static double calcAmpOfModulation(DoubleArray env) {
    DoubleArray envSmooth = sgsmooth(sgsmooth(sgsmooth(env, 20), 20), 20);
    DoubleArray e = sub(env, envSmooth);
    double AM = iqr(e);
    return AM;
  }

//      DoubleArray powerenv2 = powerenv.subarrayX(from, thru);
//      DoubleArray time = makeArithmeticSeries(thru - from);


  public DoubleArray getFeature(int i) {
    if (i == 0)
      return concat(new DoubleArray[] 
        {featHarmonics, featRateOfLongHarmonics, 
         featGradOfPowerEnvelope, featMedianOfDiffPower, 
         featAM, featFM}
      );
    else
      return null;
  }

  public int nFeatureTypes() {
    return 1;
  }

  public String getFeatureType(int i) {
    if (i == 0)
      return "kitahara28dim";
    else
      return null;
  }

}


