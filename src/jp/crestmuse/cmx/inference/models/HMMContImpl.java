package jp.crestmuse.cmx.inference.models;
import jp.crestmuse.cmx.inference.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;
import org.apache.commons.math3.distribution.*;
import java.io.*;
import java.util.*;

class HMMContImpl implements HMMCont {
  //  Hmm<ObservationReal> hmm;
  MyHMM hmm;
  private HMMParamHook hook = null;
  private ForwardBackwardCalculator fbcalc = null;
  private List<MusicElement> elemlist;

  HMMContImpl(double[] pi, double[][] a, List<? extends RealDistribution> b) {
    if (pi == null || a == null || b == null) {
      hmm = null;
    } else if (b.get(0) instanceof NormalDistribution) {
      List<OpdfGaussian> bb = new ArrayList<OpdfGaussian>();
      for (RealDistribution bi : b) {
        NormalDistribution nd = (NormalDistribution)bi;
        bb.add(new OpdfGaussian(nd.getNumericalMean(),
                                nd.getNumericalVariance()));
      }
      hmm = new MyHMM(pi, a, bb);
    } else {
      throw new UnsupportedOperationException
        ("only single gaussians are supported at the moment.");
    }
  }

  /*
  HMMContImpl(Hmm<ObservationReal> hmm) {
    this.hmm = hmm;
  }
  */
  
  /*
  static HMMContImpl readfile(String filename, HMMCont.Distr distr)
    throws IOException,FileFormatException {
    BufferedReader r = new BufferedReader(new FileReader(filename));
    Hmm<ObservationReal> hmm;
    if (distr.equals(HMMCont.Distr.GAUSS)) 
      hmm = HmmReader.read(r, new OpdfGaussianReader());
    else if (distr.equals(HMMCont.Distr.GMM)) 
      hmm = HmmReader.read(r, new OpdfGaussianMixtureReader());
    else
      throw new IllegalArgumentException(distr + ": unsupported distribution");
    return new HMMContImpl(hmm);
  }
  */

  /*
  public void setParamHook(HMMParamHook h) {
    hook = h;
  }
  */
  
  public synchronized int[] mostLikelyStateSequence(List<Double> o,
                                                    List<MusicElement> e) {
    List<ObservationReal> l = new ArrayList<ObservationReal>();
    for (Double d : o)
      l.add(new ObservationReal(d));
    elemlist = e;
    return hmm.mostLikelyStateSequence(l);
  }

  public synchronized void calcForwardBackward(List<Double> o,
                                               List<MusicElement> e) {
    List<ObservationReal> l = new ArrayList<ObservationReal>();
    for (Double d : o)
      l.add(new ObservationReal(d));
    elemlist = e;
    fbcalc = new ForwardBackwardCalculator(l, hmm);
  }

  public double getForwardProb(int t, int i) {
    return fbcalc.alphaElement(t, i);
  }

  public double getBackwardProb(int t, int i) {
    return fbcalc.betaElement(t, i);
  }

  public synchronized double calcProb(List<Double> o, int[] states,
                                      List<MusicElement> e) {
    List<ObservationReal> l = new ArrayList<ObservationReal>();
    for (Double d : o)
      l.add(new ObservationReal(d));
    elemlist = e;
    return hmm.probability(l, states);
  }

  private class MyHMM extends Hmm<ObservationReal> {
    MyHMM(double[] pi, double[][] a,
          List<? extends Opdf<ObservationReal>> opdfs) {
      super(pi, a, opdfs);
    }

    public double getPi(int i) {
      if (hook == null) 
        return super.getPi(i);
      else
        return hook.getPi(i, elemlist);
    }

    public double getAij(int i, int j) {
      if (hook == null)
        return super.getAij(i, j);
      else
        return hook.getAij(i, j, elemlist);
    }
  }
}
