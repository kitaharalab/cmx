package jp.crestmuse.cmx.inference;

import be.ac.ulg.montefiore.run.jahmm.*;
import jp.crestmuse.cmx.math.*;

public class LtoRHMMChain<O extends Observation> {
  Hmm<O>[] hmms;
  String[] labels;
  DoubleArray initprob;
  DoubleMatrix transprob;

  public LtoRHMMChain(Hmm<O>[] hmms, String[] labels,
                  DoubleArray initprob, DoubleMatrix transprob) {
    this.hmms = hmms;
    this.labels = labels;
    this.initprob = initprob;
    this.transprob = transprob;

    
  }

  public int nHMMs() {
      return hmms.length;
  }

  public double[][] calcLogLikelihood(O o, double[][] prevLogLik) {
    double[][] ll = new double[hmms.length][];
    for (int k = 0; k < hmms.length; k++) {
      int nStates = hmms[k].nbStates();
      ll[k] = new double[nStates];
      double p = hmms[k].getOpdf(0).probability(o);
      double sum = 0;
      for (int i = 0; i < hmms.length; i++)
        sum += transprob.get(i, k)
          * Math.exp(prevLogLik[i][prevLogLik.length-1]);
      ll[k][0] = Math.log(p) + Math.log(sum);
      for (int j = 1; j < nStates; j++) {
        p = hmms[k].getOpdf(j).probability(o);
        sum = 0;
        for (int i = 0; i < nStates; i++) 
          sum += hmms[k].getAij(i, j) * Math.exp(prevLogLik[k][i]);
        ll[k][j] = Math.log(p) + Math.log(sum);
      }
    }
    return ll;
  }

  public double[][] calcLogLikelihood1st(O o) {
    double[][] ll = new double[hmms.length][];
    for (int k = 0; k < hmms.length; k++) {
      int nStates = hmms[k].nbStates();
      ll[k] = new double[nStates];
      double p = hmms[k].getOpdf(0).probability(o);
      ll[k][0] = Math.log(p) + Math.log(initprob.get(k));
      for (int j = 1; j < nStates; j++)
        ll[k][j] = Double.NEGATIVE_INFINITY;
    }
    return ll;
  }

}
