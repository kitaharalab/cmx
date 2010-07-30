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
    System.err.println(measure + ":" + tick + "...");
    if (measure == musRep.getMeasureNum())
      return;
    for (BayesianMapping bm : readMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      System.err.println(e);
      if (e != null) {
	  if (bm.evidenceOnly || e.hasEvidence()) {
	    Object label = e.getLabel(e.getHighestProbIndex());
	    int evidence = musRepLabelToBayesNetIndex(bm, label);
	    bayesNet.setEvidence(bm.bayesnetIndex, evidence);
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
    System.err.println(writeMappings.size());
    for (BayesianMapping bm : writeMappings) {
      System.err.println("MAPPING: " + bm.layer);
      System.err.println("BAYESINDEX: " + bm.bayesnetIndex);
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      System.err.println(e);
      if (e != null) {
	double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
	for (int i = 0; i < margins.length; i++) {
	  e.removeEvidence();
	  int j = bayesNetIndexToMusRepIndex(bm, i, e);
	  System.err.println(j + ":" + margins[i]);
	  e.setProb(j, margins[i], false);
	  System.err.println("@@@");
	}
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
      System.err.println(values[i] + ":" + label);
      if (values[i].equals(label))
	return i;
    }
    throw new IndexOutOfBoundsException("no element equivalent to the specified label.");
  }

  private int bayesNetIndexToMusRepIndex(BayesianMapping bm, 
					 int index, MusicElement e) {
    String value = bayesNet.getValueName(bm.bayesnetIndex, index);
    int length = e.getProbLength();
    System.err.println(length);
    for (int i = 0; i < length; i++) {
      System.err.println(value + ":" + bm.labelToString(e.getLabel(i)));
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
