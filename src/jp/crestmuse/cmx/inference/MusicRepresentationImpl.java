package jp.crestmuse.cmx.inference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.crestmuse.cmx.inference.Calculator;
import jp.crestmuse.cmx.sound.SequencerManager;

/** @deprecated 
 This class remains just for the compatibility to legacy software. 
 Don't use this class if you are developing new software. */
public class MusicRepresentationImpl implements MusicRepresentation {

  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;

  public MusicRepresentationImpl(int measureNum, int division) {
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

  public void addMusicLayer(String name, int notenum) {
    addMusicLayer(name, notenum, 1);
  }

  public void addMusicLayer(String name, int notenum, int tiedLength) {
    String[] arr = new String[notenum];
    for(int i=0; i<notenum; i++) arr[i] = i + "";
    addMusicLayer(name, arr, tiedLength);
  }

  public void addMusicLayer(String name, Object[] labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, Object[] labels, int tiedLength) {
    name2layer.put(name, new MusicLayer((String[])labels, this));
    name2layer.get(name).setTiedLength(tiedLength);
  }

  public MusicElement getMusicElement(String layer, int index) {
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

  public void addCalculator(String layer, Calculator calc) {
    name2layer.get(layer).addCalculator(calc);
  }

  /** NOTE: always throws UnsupportedOperationException */
  public void addMusicLayerListener(String layer, MusicLayerListener listener) {
    throw new UnsupportedOperationException();
  }

    /** NOTE: always throws UnsupportedOperationException */
    public void suspendUpdate() {
      throw new UnsupportedOperationException();
    }

    
    /** NOTE: always throws UnsupportedOperationException */
    public void resumeUpdate() {
      throw new UnsupportedOperationException();
    }



  public int getIndex(long tick) {
    return getIndex(tick, SequencerManager.TICKS_PER_BEAT);
  }

  public int getIndex(long tick, int ticksPerBeat) {
    int measureTick = ticksPerBeat * 4;
    int measure = (int) (tick / measureTick);
    int index = (int) (tick % measureTick) / (measureTick / division);
    return measure * division + index;
  }

  public void update(String layer, int index) {
    name2layer.get(layer).update(index);
  }

  /** NOTE: always throws UnsupportedOperationExceoption */
  public boolean isChanged() {
    throw new UnsupportedOperationException();
  }

  /** NOTE: always throws UnsupportedOperationExceoption */
  public void resetChangeFlag() {
    throw new UnsupportedOperationException();
  }

    

  class MusicElementImpl implements MusicElement {

    private double prob[];
    private boolean isEvidence;
    private int evidence;
    private boolean set = false;

    private String[] labels;

    public MusicElementImpl(String[] labels) {
      prob = new double[labels.length];
      this.labels = labels;
    }

    /** Always throws UnsupportedOperationException */
    public int tiedLength() {
      throw new UnsupportedOperationException();
    }

    /** Always throws UnsupportedOperationException */
    public int measure() {
      throw new UnsupportedOperationException();
    }

    /** Always throws UnsupportedOperationException */
    public int tick() {
      throw new UnsupportedOperationException();
    }
    
    /** Always throws UnsupportedOperationException */
    public void setBackPointerTo(MusicElement e) {
      throw new UnsupportedOperationException();
    }
    /** Always throws UnsupportedOperationException */
    public void setBackPointer(int index, int value) {
      throw new UnsupportedOperationException();
    }
    /** Always throws UnsupportedOperationException */
    public int getBackPointer(int index) {
      throw new UnsupportedOperationException();
    }
    /** Always throws UnsupportedOperationException */
    public void update() {
      throw new UnsupportedOperationException();
    }


    /** NOTE: always throws UnsupportedOperationException */
    public boolean tiedFromPrevious() {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public void setTiedFromPrevious(boolean b) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public boolean rest() {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public void setRest(boolean b) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public void setAttribute(String key, String value) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public String getAttribute(String key) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public boolean hasAttribute(String key) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public void removeAttribute(String key) {
      throw new UnsupportedOperationException();
    }

    /** NOTE: always throws UnsupportedOperationException */
    public Map<String,String> getAllAttributes() {
      throw new UnsupportedOperationException();
    }
    
    /** NOTE: always throws UnsupportedOperationException */
    public void setAllAttributes(Map<String,String> map) {
      throw new UnsupportedOperationException();
    }

    public double[] getAllProbs() {
      return prob;
    }

    public double getLogLikelihood(int i) {
      return Math.log(getProb(i));
    }

    public double getProb(int i) {
      if (isEvidence) {
        if (evidence == i)
          return 1.0;
        return 0.0;
      }
      return prob[i];
    }

    public void removeEvidence() {
      evidence = -1;
    }
    
    public boolean hasEvidence() {
      return isEvidence;
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

    public int getRankedProbIndex(int rank) {
      double[] cloneArray = prob.clone();
      Arrays.sort(cloneArray);
      for (int i = 0; i < prob.length; i++) {
        if (prob[i] == cloneArray[cloneArray.length - 1 - rank])
          return i;
      }
      return 0;
    }

    public int getProbLength() {
      return prob.length;
    }

    public boolean set() {
      return set;
    }

    public void setEvidence(int i) {
      set = true;
      isEvidence = true;
      evidence = i;
    }

    public void setLogLikelihood(int i, double value, boolean update) {
      setLogLikelihood(i, value);
    }

    public void setLogLikelihood(int i, double value) {
      setProb(i, Math.exp(value));
    }

    public void setProb(int i, double value, boolean update) {
      setProb(i, value);
    }
    
    public void setProb(int i, double value) {
      set = true;
      isEvidence = false;
      prob[i] = value;
    }

    public String getLabel(int i) {
      return labels[i];
    }

    public int getIndexOf(Object s) {
      return indexOf((String)s);
    }

    public int indexOf(String s) {
      for(int i=0; i<labels.length; i++)
        if(labels[i].equals(s))
          return i;
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
      labels = ee.labels.clone();
    }

  }

  private class MusicLayer {

    MusicElement[] elements;
    List<Calculator> calculators;
    int tiedLength = 1;
    // boolean isNote;
    String[] labels;
    MusicRepresentation musRep;

    MusicLayer(String[] labels, MusicRepresentation musRep) {
      elements = new MusicElement[division * measureNum];
      calculators = new LinkedList<Calculator>();
      this.labels = labels;
      this.musRep = musRep;
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

    void addCalculator(Calculator calc) {
      calculators.add(calc);
    }

    void addElement(int index) {
      MusicElement me = new MusicElementImpl(labels);
      int head = (index / tiedLength) * tiedLength;
      for (int i = head; i < head + tiedLength && i < division * measureNum; i++)
        elements[i] = me;
    }

    void update(int index) {
      for (Calculator c : calculators)
        c.update(musRep, elements[index], index);
    }

  }

}
