package jp.crestmuse.cmx.inference;

import java.util.LinkedList;
import jp.crestmuse.cmx.inference.MusicRepresentation2.*;


public class BayesianCalculator implements MusicLayerListener {

  private BayesNetCompatible bayesNet;
  private LinkedList<BayesianMapping> readMappings;
  private LinkedList<BayesianMapping> writeMappings;

  public BayesianCalculator(BayesNetCompatible bayesNet) {
    this.bayesNet = bayesNet;
    readMappings = new LinkedList<BayesianMapping>();
    writeMappings = new LinkedList<BayesianMapping>();
  }

  public void update(MusicRepresentation2 musRep, MusicElement me, int measure,
      int tick) {
      try {
    // int measure = indexInMusRep / musRep.getDivision() + 1;
    System.err.println(measure + ":" + tick + "...");
    if (measure == musRep.getMeasureNum())
      return;
    for (BayesianMapping bm : readMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      if (e != null) {
	  //	  System.out.println("e.getAllProbs()");
	  //	  double[] ddd = e.getAllProbs();
	  //	  for (int i = 0; i < ddd.length; i++)
	  //	  System.out.println(ddd[i] + " ");
	  //	  System.out.println();
	  // bayesNet.setMargin(bm.bayesnetIndex, e.getAllProbs());
	  if (bm.evidenceOnly || e.hasEvidence()) {
	      bayesNet.setEvidence(bm.bayesnetIndex, e.getHighestProbIndex());
	  } else {
	      // ネットワークの始点じゃないとうまくいかない？
	      double[] dd = e.getAllProbs();
	      double[][] distr = new double[1][];
	      distr[0] = dd;
	      try {
		  bayesNet.setDistribution(bm.bayesnetIndex, distr);
	      } catch (Exception ex) {
		  throw new IllegalStateException(ex.toString());
	      }
	  }
      }
    }
    bayesNet.update();
    for (BayesianMapping bm : writeMappings) {
	//	System.err.println("MAPPING: " + bm.layer);
	//	System.err.println("BAYESINDEX: " + bm.bayesnetIndex);
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      if (e != null) {
	  double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
	  for (int i = 0; i < margins.length; i++) {
	      e.removeEvidence(i);
	      e.setProb(i, margins[i]);
	  }
	  // System.err.println("Rank0: " + e.getProb(e.getRankedProbIndex(0)));
	  // System.err.println("Rank1: " + e.getProb(e.getRankedProbIndex(1)));
	  // System.err.println("Rank2: " + e.getProb(e.getRankedProbIndex(2)));
	  //	  System.err.print(e.getLabel(e.getHighestProbIndex()) + ":"
	  //			   + e.getProb(e.getHighestProbIndex()) + " ");
      }
    }
    //    System.err.println();
      } catch (Exception e) {
	  e.printStackTrace();
      }
  }

  public void addReadMapping(BayesianMapping bm) {
    readMappings.add(bm);
  }

  public void addWriteMapping(BayesianMapping bm) {
    writeMappings.add(bm);
  }

}
