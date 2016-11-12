package jp.crestmuse.cmx.inference;

import java.util.LinkedList;

public class BayesianCalculator implements MusicCalculator {

  private BayesNetCompatible bayesNet;
  private LinkedList<BayesianMapping> readMappings;
  private LinkedList<BayesianMapping> writeMappings;

  public BayesianCalculator(BayesNetCompatible bayesNet) {
    this.bayesNet = bayesNet;
    readMappings = new LinkedList<BayesianMapping>();
    writeMappings = new LinkedList<BayesianMapping>();
  }

  public void updated(int measure, int tick, String layer, 
                     MusicRepresentation musRep) {
    for (BayesianMapping bm : readMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      if (e != null) {
        Object evidence = e.getMostLikely();
        if (e.getProb(evidence) > 0.5) {
          Object[] labels = musRep.getLabels(layer);
          for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(evidence)) {
              bayesNet.setEvidence(bm.bayesnetIndex, i);
              break;
            }
          }
        }
      }
    }
    try {
      bayesNet.update();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BayesNetException(e.toString());
    }
    for (BayesianMapping bm : writeMappings) {
      MusicElement e = bm.mappedElement(musRep, measure, tick);
      if (e != null) {
        double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
        for (int i = 0; i < margins.length; i++) {
          e.setProb(i, margins[i]);
        }
      }
    }
  }


/*
  public void update(MusicRepresentation2 musRep, 
                     MusicRepresentation2.MusicElement me, int measure,
                     int tick) {
    try {
      // int measure = indexInMusRep / musRep.getDivision() + 1;
      if (measure == musRep.getMeasureNum())
        return;
      for (BayesianMapping bm : readMappings) {
        MusicRepresentation2.MusicElement e = 
          bm.mappedElement(musRep, measure, tick);
        if (e != null) {
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
        MusicRepresentation2.MusicElement e = 
          bm.mappedElement(musRep, measure, tick);
        if (e != null) {
	  double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
	  for (int i = 0; i < margins.length; i++) {
            e.removeEvidence(i);
            e.setProb(i, margins[i]);
	  }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
*/

  public void addReadMapping(BayesianMapping bm) {
    readMappings.add(bm);
  }

  public void addWriteMapping(BayesianMapping bm) {
    writeMappings.add(bm);
  }

}
