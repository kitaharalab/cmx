package jp.crestmuse.cmx.inference;
import jp.crestmuse.cmx.inference.models.*;
import java.util.*;

public class MostSimpleHMMContCalculator implements MusicCalculator {

  private MusicRepresentation mr;
  private String obsLayer, hiddenLayer;
  private HMMCont hmm;
  private Object[] labels;
  private int div, nMeas;
  private int calclength = 0;
  private boolean usesViterbi = true;
  private double prob = 0.0;
  private boolean separateThread = false;
  
  public MostSimpleHMMContCalculator(String obsLayer, String hiddenLayer,
                                     HMMCont hmm,
                                     MusicRepresentation mr) {
    this.mr = mr;
    this.obsLayer = obsLayer;
    this.hiddenLayer = hiddenLayer;
    this.hmm = hmm;
    // obsLayer should be CONT
    if (!mr.getMusicLayerType(obsLayer).equals(MusicLayerType.CONT))
      throw new IllegalArgumentException
        ("observation layer shuold be CONT (ontinous)");
    if (!mr.getMusicLayerType(hiddenLayer).equals(MusicLayerType.STANDARD))
      throw new IllegalArgumentException
        ("hidden layer should be STANDARD");
    labels = mr.getLabels(hiddenLayer);
    div = mr.getDivision();
  }

  public MusicRepresentation getMusicRepresentation() {
    return mr;
  }

  public String getObservationLayer() {
    return obsLayer;
  }

  public String getHiddenLayer() {
    return hiddenLayer;
  }

  public void setCalcLength(int value) {
    calclength = value;
  }

  public void enableSeparateThread(boolean b) {
    separateThread = b;
  }
  
  //  public void setViterbiUsed(boolean b) {
  //    usesViterbi = b;
  //  }

  public void updated(final int measure, final int tick, final String layer,
                      final MusicRepresentation mr) {
    if (!obsLayer.equals(layer)) 
      new IllegalArgumentException("obsLayer and layer should be equivalent");
    if (separateThread) {
      Thread th = new Thread(new Runnable() {
          public void run() {
            updateElements(measure, tick, layer, mr);
          }
        });
      th.start();
      System.err.println("_________________thread started");
    } else {
      updateElements(measure, tick, layer, mr);
    }
  }

  // synchronizedは必要?
  //  synchronized
  private synchronized void updateElements(int measure, int tick, String layer,
                                         MusicRepresentation mr) {
    List<MusicElement> elemlist =
      mr.getMusicElementList(obsLayer, 0, 0, measure, tick+1);
    if (calclength > 0 && elemlist.size() > calclength)
      elemlist =
        elemlist.subList(elemlist.size() - calclength, elemlist.size());
    List<Double> oseq = new ArrayList<Double>();
    List<MusicElement> elemlist2 = new ArrayList<MusicElement>();
    elemlist2.addAll(elemlist);
    for (MusicElement e : elemlist2) {
      Double value = (Double)e.getMostLikely();
      if (value.isNaN()) 
        elemlist.remove(e);
      else
        oseq.add(value);
    }
    if (oseq.size() > 0) {
      int[] states = hmm.mostLikelyStateSequence(oseq, elemlist);
      for (int i = 0; i < states.length; i++) {
        MusicElement e = elemlist.get(i);
        MusicElement e2 = mr.getMusicElement(hiddenLayer, e.measure(), e.tick());
        e2.setEvidence(labels[states[i]]);
      }
    }
  }                                           

  /*
  public void updated(int measure, int tick, String layer,
                      MusicRepresentation mr) {
    if (!obsLayer.equals(layer))
      new IllegalArgumentException("obsLayer and layer should be equivalent");
    int last = measure * div + tick;
    int first = (calclength == 0 ? 0 : Math.max(last - calclength, 0));
    if (first < last) {
      List<Double> seq = new ArrayList<Double>();
      List<Integer> whereNaN = new ArrayList<Integer>();
      whereNaN.add(-1);
      for (int i = first; i < last; i++) {
        MusicElement me = mr.getMusicElement(obsLayer, i / div, i % div);
        Double value = (Double)me.getMostLikely();
        seq.add(value);
        if (Double.isNaN(value))
          whereNaN.add(i);
      }
      whereNaN.add(seq.size());
      prob = 1.0;
      for (int i = 0; i < whereNaN.size() - 1; i++) {
        int from = whereNaN.get(i);
        int thru = whereNaN.get(i+1);
        if (thru - from >= 2) {
          List<Double> subseq = seq.subList(from + 1, thru);
          if (usesViterbi) {
            int[] states = hmm.mostLikelyStateSequence(subseq);
            for (int ii = first + from + 1; ii < first + thru; ii++) {
              MusicElement me = mr.getMusicElement(hiddenLayer, ii/div, ii%div);
              me.setEvidence(labels[states[ii - first - from - 1]]);
            }
            prob *= hmm.calcProb(subseq, states);
          } else {
            hmm.calcForwardBackward(subseq);
            for (int ii = first + from + 1; ii < first + thru; ii++) {
              MusicElement me = mr.getMusicElement(hiddenLayer, ii/div, ii%div);
              me.suspendUpdate();
              int t = ii - first - from - 1;
              for (int j = 0; j < labels.length; j++) {
                me.setProb(labels[j], hmm.getForwardProb(t, j));
                prob *= hmm.getForwardProb(t, j);
              }
              me.resumeUpdate();
            }
          }
        }
      }
    }
  }
  */
  
  public double getProb() {
    return prob;
  }

}
