package jp.crestmuse.cmx.inference;

import java.util.LinkedList;


public class BayesianCalculator implements MusicLayerListener {

  private BayesNetCompatible bayesNet;
  private LinkedList<BayesianMapping> readMappings;
  private LinkedList<BayesianMapping> writeMappings;

  public BayesianCalculator(BayesNetCompatible bayesNet) {
    this.bayesNet = bayesNet;
    readMappings = new LinkedList<BayesianMapping>();
    writeMappings = new LinkedList<BayesianMapping>();
  }

  public void update(MusicRepresentation musRep, MusicElement me, int measure,
      int tick) {
      try {
    // int measure = indexInMusRep / musRep.getDivision() + 1;
    if (measure == musRep.getMeasureNum())
      return;
    for (BayesianMapping bm : readMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      System.err.println("measure: " + measure + "  tick: " + tick);
//      System.err.println(bm.layer);
//      System.err.println(e);
      if (e != null) {
//	  if (bm.evidenceOnly || e.hasEvidence()) {
	if (bm.evidenceOnly) {
	  if (e.hasEvidence()) {
	    int index = e.getHighestProbIndex();
//	    System.err.println(index);
	    if (index >= 0) {
	      Object label = e.getLabel(index);
	      int evidence = musRepLabelToBayesNetIndex(bm, label);
	      bayesNet.setEvidence(bm.bayesnetIndex, evidence);
	      System.err.println(label);
	    }
	  } else {
	    bayesNet.setEvidence(bm.bayesnetIndex, -1);
	  }
//	      bayesNet.setEvidence(bm.bayesnetIndex, e.getHighestProbIndex());
	} else {
	      // ネットワークの始点じゃないとうまくいかない？
	    throw new UnsupportedOperationException("not implmeneted yet.");
//	      double[] dd = e.getAllProbs();
//	      double[][] distr = new double[1][];
//	      distr[0] = dd;
//	      try {
//		  bayesNet.setDistribution(bm.bayesnetIndex, distr);
//	      } catch (Exception ex) {
//		  throw new IllegalStateException(ex.toString());
//	      }
	  }
      }
    }
    bayesNet.update();
    for (BayesianMapping bm : writeMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      if (e != null) {
	double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
	musRep.suspendUpdate();
//	System.err.println("measure: " + measure + "  tick: " + tick);
	System.err.println(bm.layer);
	for (int i = 0; i < margins.length; i++) {
	  e.removeEvidence();
	  int j = bayesNetIndexToMusRepIndex(bm, i, e);
	  e.setProb(j, margins[i]);
	  System.err.println(j + ":" + margins[i]);
//	  e.setProb(j, margins[i], false);
	}
	System.err.println();
	musRep.resumeUpdate();
	e.update();
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

  private int musRepLabelToBayesNetIndex(BayesianMapping bm, Object o) {
    String[] values = bayesNet.getValues(bm.bayesnetIndex);
    String label = bm.labelToString(o);
    for (int i = 0; i < values.length; i++) {
      if (values[i].equals(label))
	return i;
    }
    throw new IndexOutOfBoundsException("no element equivalent to the specified label.");
  }

  private int bayesNetIndexToMusRepIndex(BayesianMapping bm, 
					 int index, MusicElement e) {
    String value = bayesNet.getValueName(bm.bayesnetIndex, index);
    int length = e.getProbLength();
    for (int i = 0; i < length; i++) {
      if (bm.labelToString(e.getLabel(i)).equals(value))
	return i;
    }
    throw new IndexOutOfBoundsException("no label equivalent to the specified element.");
  }

  public void addReadMapping(BayesianMapping bm) {
    readMappings.add(bm);
  }

  public void addWriteMapping(BayesianMapping bm) {
    writeMappings.add(bm);
  }

}
