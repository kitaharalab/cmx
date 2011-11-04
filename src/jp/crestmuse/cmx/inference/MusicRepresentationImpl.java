package jp.crestmuse.cmx.inference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.crestmuse.cmx.sound.SequencerManager;

/** @deprecated 
 This class remains just for the compatibility to legacy software. 
 Don't use this class if you are developing new software. */
class MusicRepresentationImpl implements MusicRepresentation {

  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;

  MusicRepresentationImpl(int measureNum, int division) {
    this.measureNum = measureNum;
    this.division = division;
    name2layer = new HashMap<String, MusicLayer>();
  }

  public int getMeasureNum() {
    return measureNum;
  }

  public int getDivision() {
    return division;
  }

/*
  public void addMusicLayer(String name, int notenum) {
    addMusicLayer(name, notenum, 1);
  }

  public void addMusicLayer(String name, int notenum, int tiedLength) {
    String[] arr = new String[notenum];
    for(int i=0; i<notenum; i++) arr[i] = i + "";
    addMusicLayer(name, arr, tiedLength);
  }
*/

  public void addMusicLayer(String name, Object[] labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, Object[] labels, int tiedLength) {
    name2layer.put(name, new MusicLayer(name, labels));
    name2layer.get(name).setTiedLength(tiedLength);
  }

  public void addMusicLayer(String name, List<Object> labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, List<Object> labels, 
                            int tiedLength) {
    addMusicLayer(name, labels.toArray(), tiedLength);
  }

  public Object[] getLabels(String layer) {
    return name2layer.get(layer).labels;
  }

  private MusicElement getMusicElement(String layer, int index) {
    return name2layer.get(layer).getElement(index);
  }

  public MusicElement getMusicElement(String layer, int measure, int tick) {
    return getMusicElement(layer, measure * division + tick);
  }

  public int getTiedLength(String layer) {
    return name2layer.get(layer).getTiedLength();
  }

//  public void setTiedLength(String layer, int tiedLength) {
//    name2layer.get(layer).setTiedLength(tiedLength);
//  }

  public void addMusicCalculator(String layer, MusicCalculator calc) {
    name2layer.get(layer).addMusicCalculator(calc);
  }

  class MusicElementImpl implements MusicElement {

    private double prob[];
    private boolean isEvidence;
    private int evidence;
    private boolean set = false;
    private MusicLayer parent;
    private int index;

    MusicElementImpl(MusicLayer parent, int index) {
      this.parent = parent;
      this.index = index;
      prob = new double[parent.labels.length];
    }

/*
    private Object[] labels;

    public MusicElementImpl(Object[] labels) {
      prob = new double[labels.length];
      this.labels = labels;
    }
*/

/*
    public double[] getAllProbs() {
      if (isEvidence) {
        double[] prob = new double[parent.labels.length];
        prob[evidence] = 1.0;
        return prob;
      } else {
        return (double[])prob.clone();
      }
    }
*/

    public double getProb(int i) {
      if (isEvidence) {
        if (evidence == i)
          return 1.0;
        return 0.0;
      }
      return prob[i];
    }

    public int getHighestProbIndex() {
      if (isEvidence)
        return evidence;
      double max = Double.MIN_VALUE;
      int index = 0;
      for (int i = 0; i < prob.length; i++) {
        if (prob[i] > max) {
          max = prob[i];
          index = i;
        }
      }
      return index;
    }

/*
    public int getRankedProbIndex(int rank) {
      double[] cloneArray = prob.clone();
      Arrays.sort(cloneArray);
      for (int i = 0; i < prob.length; i++) {
        if (prob[i] == cloneArray[cloneArray.length - 1 - rank])
          return i;
      }
      return 0;
    }
*/

    public int getProbLength() {
      return prob.length;
    }

/*
    public boolean set() {
      return set;
    }
*/

    public void setEvidence(Object label) {
      setEvidence(indexOf(label));
    }

    public void setEvidence(int i) {
      set = true;
      isEvidence = true;
      evidence = i;
      System.err.println("UPDATE: " + parent.name + " " + index);
      parent.update(index);
    }

    public void setProb(int i, double value) {
      set = true;
      isEvidence = false;
      prob[i] = value;
      parent.update(index);
    }

    public Object getLabel(int i) {
      return parent.labels[i];
    }

    public int indexOf(Object s) {
      for(int i=0; i<parent.labels.length; i++) {
        System.err.println(parent.labels[i] + "--" + s + ":" + parent.labels[i].equals(s));
        if(parent.labels[i].equals(s))
          return i;
      }
      return -1;
    }

    private void copy(MusicElement e) {
      MusicElementImpl ee = (MusicElementImpl)e;
      prob = ee.prob.clone();
      isEvidence = ee.isEvidence;
      evidence = ee.evidence;
      //isNote = e.isNote;
      // type = e.type;
      set = ee.set;
      //chordLabel = e.chordLabel.clone();
      parent = ee.parent;
      index = ee.index;
//      labels = ee.labels.clone();
    }

    public double getProb(Object label) {
      return getProb(indexOf(label));
    }

    public void setProb(Object label, double value) {
      setProb(indexOf(label), value);
    }

    public Object getMostLikely() {
      return getLabel(getHighestProbIndex());
    }

    public Object generate() {
      if (isEvidence) {
        return getLabel(evidence);
      } else {
        double sum = 0;
        for (int i = 0; i < prob.length; i++) {
          sum += prob[i];
        }
        double rand = Math.random();
        double cum = 0;
        for (int i = 0; i < prob.length; i++) {
          if (cum / sum <= rand && rand <= (cum + prob[i]) / sum)
            return getLabel(i);
          cum += prob[i];
        }
        return null;
      }
    }


  }

  private class MusicLayer {

    String name;
    MusicElement[] elements;
    List<MusicCalculator> calculators;
    int tiedLength = 1;
    // boolean isNote;
    Object[] labels;
//    MusicRepresentation musRep;

    MusicLayer(String name, Object[] labels) {
      this.name = name;
      elements = new MusicElement[division * measureNum];
      calculators = new LinkedList<MusicCalculator>();
      this.labels = labels;
//      this.musRep = musRep;
    }

    MusicElement getElement(int index) {
      if (elements[index] == null) {
        addElement(index);
      }
      return elements[index];
    }

    int getTiedLength() {
      return tiedLength;
    }

    void setTiedLength(int length) {
      tiedLength = length;
      for (int i = 0; i < division * measureNum; i += tiedLength)
        if (elements[i] == null)
          addElement(i);
        else {
          MusicElement me = elements[i];
          addElement(i);
          ((MusicElementImpl)elements[i]).copy(me);
        }
    }

    void addMusicCalculator(MusicCalculator calc) {
      calculators.add(calc);
    }

    void addElement(int index) {
      MusicElement me = new MusicElementImpl(this, index);
      int head = (index / tiedLength) * tiedLength;
      for (int i = head; i < head + tiedLength && i < division * measureNum; i++)
        elements[i] = me;
    }

    void update(int index) {
      for (MusicCalculator c : calculators)
        c.updated(index/division, index%division, name, MusicRepresentationImpl.this);
//        c.updated(musRep, elements[index], index);
    }

    


  }

}
