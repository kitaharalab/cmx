package jp.crestmuse.cmx.inference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.inference.Calculator;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentation {
  /*
    public enum Type {
      Melody, Chord, Bass, Voicing
    };
  */
  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;
  /*
    private MusicElement[] melodyElements;
    private MusicElement[] chordElements;
    private MusicElement[] bassElements;
    private MusicElement[] voicingHighElements;
    private MusicElement[] voicingMidHighElements;
    private MusicElement[] voicingMidLowElements;
    private MusicElement[] voicingLowElements;

    private List<Calculator> melodyCalculators;
    private List<Calculator> chordCalculators;
    private List<Calculator> bassCalculators;
    private List<Calculator> voicingCalculators;

    private int tiedMelodyLength;
    private int tiedChordLength;
    private int tiedBassLength;
    private int tiedVoicingLength;
  */
  public MusicRepresentation(int measureNum, int division) {
    this.measureNum = measureNum;
    this.division = division;
    name2layer = new HashMap<String, MusicLayer>();
    /*
    melodyElements = new MusicElement[measureNum * division];
    chordElements = new MusicElement[measureNum * division];
    bassElements = new MusicElement[measureNum * division];
    voicingHighElements = new MusicElement[measureNum * division];
    voicingMidHighElements = new MusicElement[measureNum * division];
    voicingMidLowElements = new MusicElement[measureNum * division];
    voicingLowElements = new MusicElement[measureNum * division];
    melodyCalculators = new LinkedList<Calculator>();
    chordCalculators = new LinkedList<Calculator>();
    bassCalculators = new LinkedList<Calculator>();
    voicingCalculators = new LinkedList<Calculator>();
    tiedMelodyLength = 1;
    tiedChordLength = 1;
    tiedBassLength = 1;
    tiedVoicingLength = 1;
    */
  }

  public int getMeasureNum() {
    return measureNum;
  }

  public int getDivision() {
    return division;
  }
/*
  public void addMusicLayer(String name) {
    addMusicLayer(name, 1);
  }

  public void addMusicLayer(String name, int tiedLength) {
    addMusicLayer(name, tiedLength, true);
  }

  public void addMusicLayer(String name, int tiedLength, boolean isNote) {
    name2layer.put(name, new MusicLayer(isNote));
    name2layer.get(name).setTiedLength(tiedLength);
  }
*/
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

  public void setTiedLength(String layer, int tiedLength) {
    name2layer.get(layer).setTiedLength(tiedLength);
  }

  public void addCalculator(String layer, Calculator calc) {
    name2layer.get(layer).addCalculator(calc);
  }

  /*
    public MusicElement getMelodyElement(int i) {
      if (melodyElements[i] == null)
        addMelodyElement(i);
      return melodyElements[i];
    }

    private void addMelodyElement(int index) {
      addMusicElement(Type.Melody, index, tiedMelodyLength, melodyElements);
    }

    public MusicElement getChordElement(int i) {
      if (chordElements[i] == null)
        addChordElement(i);
      return chordElements[i];
    }

    private void addChordElement(int index) {
      addMusicElement(Type.Chord, index, tiedChordLength, chordElements);
    }

    public MusicElement getBassElement(int i) {
      if (bassElements[i] == null)
        addBassElement(i);
      return bassElements[i];
    }

    private void addBassElement(int index) {
      addMusicElement(Type.Bass, index, tiedBassLength, bassElements);
    }

    public MusicElement getVoicingHighElement(int i) {
      if (voicingHighElements[i] == null)
        addVoicingHighElement(i);
      return voicingHighElements[i];
    }

    private void addVoicingHighElement(int index) {
      addMusicElement(Type.Voicing, index, tiedVoicingLength, voicingHighElements);
    }

    public MusicElement getVoicingMidHighElement(int i) {
      if (voicingMidHighElements[i] == null)
        addVoicingMidHighElement(i);
      return voicingMidHighElements[i];
    }

    private void addVoicingMidHighElement(int index) {
      addMusicElement(Type.Voicing, index, tiedVoicingLength,
          voicingMidHighElements);
    }

    public MusicElement getVoicingMidLowElement(int i) {
      if (voicingMidLowElements[i] == null)
        addVoicingMidLowElement(i);
      return voicingMidLowElements[i];
    }

    private void addVoicingMidLowElement(int index) {
      addMusicElement(Type.Voicing, index, tiedVoicingLength,
          voicingMidLowElements);
    }

    public MusicElement getVoicingLowElement(int i) {
      if (voicingLowElements[i] == null)
        addVoicingLowElement(i);
      return voicingLowElements[i];
    }

    private void addVoicingLowElement(int index) {
      addMusicElement(Type.Voicing, index, tiedVoicingLength, voicingLowElements);
    }

    private void addMusicElement(Type type, int index, int tiedLength,
        MusicElement[] elements) {
      MusicElement me = new MusicElement(type, index);
      int head = (index / tiedLength) * tiedLength;
      for (int i = head; i < head + tiedLength && i < division * measureNum; i++)
        elements[i] = me;
    }

    public int getTiedMelodyLength() {
      return tiedMelodyLength;
    }

    public void setTiedMelodyLength(int tiedMelodyLength) {
      this.tiedMelodyLength = tiedMelodyLength;
      for (int i = 0; i < division * measureNum; i += tiedMelodyLength)
        if (melodyElements[i] == null)
          addMelodyElement(i);
        else {
          MusicElement me = melodyElements[i];
          addMelodyElement(i);
          melodyElements[i].copy(me);
        }
    }

    public int getTiedChordLength() {
      return tiedChordLength;
    }

    public void setTiedChordLength(int tiedChordLength) {
      this.tiedChordLength = tiedChordLength;
      for (int i = 0; i < division * measureNum; i += tiedChordLength)
        if (chordElements[i] == null)
          addChordElement(i);
        else {
          MusicElement me = chordElements[i];
          addChordElement(i);
          chordElements[i].copy(me);
        }
    }

    public int getTiedBassLength() {
      return tiedBassLength;
    }

    public void setTiedBassLength(int tiedBassLength) {
      this.tiedBassLength = tiedBassLength;
      for (int i = 0; i < division * measureNum; i += tiedBassLength)
        if (bassElements[i] == null)
          addBassElement(i);
        else {
          MusicElement me = bassElements[i];
          addBassElement(i);
          bassElements[i].copy(me);
        }
    }

    public int getTiedVoicingLength() {
      return tiedVoicingLength;
    }

    public void setTiedVoicingLength(int tiedVoicingLength) {
      this.tiedVoicingLength = tiedVoicingLength;
      MusicElement me;
      for (int i = 0; i < division * measureNum; i += tiedVoicingLength) {
        if (voicingHighElements[i] == null)
          addVoicingHighElement(i);
        else {
          me = voicingHighElements[i];
          addVoicingHighElement(i);
          voicingHighElements[i].copy(me);
        }

        if (voicingMidHighElements[i] == null)
          addVoicingMidHighElement(i);
        else {
          me = voicingMidHighElements[i];
          addVoicingMidHighElement(i);
          voicingMidHighElements[i].copy(me);
        }

        if (voicingMidLowElements[i] == null)
          addVoicingMidLowElement(i);
        else {
          me = voicingMidLowElements[i];
          addVoicingMidLowElement(i);
          voicingMidLowElements[i].copy(me);
        }

        if (voicingLowElements[i] == null)
          addVoicingLowElement(i);
        else {
          me = voicingLowElements[i];
          addVoicingLowElement(i);
          voicingLowElements[i].copy(me);
        }
      }
    }


  public void setEvidence(int trackNum, long tick, MusicElement me){ int
    measureTick = SequencerManager.TICKS_PER_BEAT 4; int measure = (int)(tick /
    measureTick); int index = (int)(tick % measureTick) / (measureTick /
    division); setEvidence(trackNum, measuredivision + index, me); }

  public void setEvidence(int trackNum, int index, MusicElement me) {
    setPredict(trackNum, index, me);
    for (Calculator c : calculators)
      if (c.isCalc(me, trackNum, index))
        c.update(me, trackNum, index);
  }

  public void setPredict(int trackNum, int index, MusicElement me) {
    musicElements[trackNum][index] = me;
  }
  */

  public int getIndex(long tick) {
    return getIndex(tick, SequencerManager.TICKS_PER_BEAT);
  }

  public int getIndex(long tick, int ticksPerBeat) {
    int measureTick = ticksPerBeat * 4;
    int measure = (int) (tick / measureTick);
    int index = (int) (tick % measureTick) / (measureTick / division);
    return measure * division + index;
  }
  /*
    public void addCalculator(Calculator calc) {
      for (Type t : calc.drivenBy()) {
        if (t == Type.Melody)
          melodyCalculators.add(calc);
        else if (t == Type.Chord)
          chordCalculators.add(calc);
        else if (t == Type.Bass)
          bassCalculators.add(calc);
        else if (t == Type.Voicing)
          voicingCalculators.add(calc);
      }
    }

    public void update(Type type, MusicElement me, int index) {
      List<Calculator> list;
      if (type == Type.Melody)
        list = melodyCalculators;
      else if (type == Type.Chord)
        list = chordCalculators;
      else if (type == Type.Bass)
        list = bassCalculators;
      else
        list = voicingCalculators;
      for (Calculator c : list)
        c.update(me, index);
    }
  */

  public void update(String layer, int index) {
    name2layer.get(layer).update(index);
  }

  public class MusicElement {

    private double prob[];
    private boolean isEvidence;
    private int evidence;
    //private boolean isNote;
    // private Type type;
    private boolean set = false;
    //private String[] chordLabel = { "C", "Dm", "Em", "F", "G", "Am", "Bm(b5)" };
    private String[] labels;

    public MusicElement(String[] labels) {
      prob = new double[labels.length];
      this.labels = labels;
    }
/*
    public MusicElement(boolean isNote) {
      this.isNote = isNote;
      if (isNote)
        prob = new double[128];
      else
        prob = new double[7];
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

    public int getRankedProbIndex(int rank) {
      double[] cloneArray = prob.clone();
      Arrays.sort(cloneArray);
      for (int i = 0; i < prob.length; i++) {
        if (prob[i] == cloneArray[cloneArray.length - 1 - rank])
          return i;
      }
      return 0;
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
      /*
      if (isNote)
        return i + "";
      return chordLabel[i];
      */
      return labels[i];
    }

    public int indexOf(String s) {
      /*
      for (int i = 0; i < chordLabel.length; i++)
        if (chordLabel[i].equals(s))
          return i;
      return Integer.parseInt(s);
      */
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
/*
    MusicLayer(boolean isNote) {
      elements = new MusicElement[division * measureNum];
      calculators = new LinkedList<Calculator>();
      this.isNote = isNote;
    }
*/

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
