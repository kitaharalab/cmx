package jp.crestmuse.cmx.inference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.inference.Calculator;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentation {

  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;

  public MusicRepresentation(int measureNum, int division) {
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

  public void addMusicLayer(String name, String[] labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, String[] labels, int tiedLength) {
    name2layer.put(name, new MusicLayer(labels, this));
    name2layer.get(name).setTiedLength(tiedLength);
  }

  public MusicElement getMusicElement(String layer, int index) {
    return name2layer.get(layer).getElement(index);
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

  public class MusicElement {

    private double prob[];
    private boolean isEvidence;
    private int evidence;
    private boolean set = false;

    private String[] labels;

    public MusicElement(String[] labels) {
      prob = new double[labels.length];
      this.labels = labels;
    }

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

    public void setProb(int i, double value) {
      set = true;
      isEvidence = false;
      prob[i] = value;
    }

    public String getLabel(int i) {
      return labels[i];
    }

    public int indexOf(String s) {
      for(int i=0; i<labels.length; i++)
        if(labels[i].equals(s))
          return i;
      return -1;
    }

    private void copy(MusicElement e) {
      prob = e.prob.clone();
      isEvidence = e.isEvidence;
      evidence = e.evidence;
      //isNote = e.isNote;
      // type = e.type;
      set = e.set;
      //chordLabel = e.chordLabel.clone();
      labels = labels.clone();
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
          elements[i].copy(me);
        }
    }

    void addCalculator(Calculator calc) {
      calculators.add(calc);
    }

    void addElement(int index) {
      MusicElement me = new MusicElement(labels);
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
