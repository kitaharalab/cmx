package jp.crestmuse.cmx.inference;

import be.ac.ulg.montefiore.run.jahmm.*;
import jp.crestmuse.cmx.math.*;

public class HMMChain<O extends Observation> {
  Hmm<O>[] hmms;
  String[] labels;
  DoubleArray transprob1st;
  DoubleMatrix transprob;

  public HMMChain(Hmm<O>[] hmms, String[] labels,
                  DoubleArray transprob1st, DoubleMatrix transprob) {
    this.hmms = hmms;
    this.labels = labels;
    this.transprob1st = transprob1st;
    this.transprob = transprob;

    
  }

  public double[][] calcLogLikelihood(O o, double[][] prevLogLik) {
    double[][] ll = new double[hmms.length][];
    for (int k = 0; k < hmms.length; k++) {
      int nStates = hmms[k].nbStates();
      double[] ll[k] = new double[nStates];
      for (int i = 0; i <  


  }
}
