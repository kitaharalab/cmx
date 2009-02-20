package jp.crestmuse.cmx.inference;

import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.inference.Calculator;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentation {

  public enum Type {Melody, Chord, Bass};
  private int measureNum;
  private int division;
  //private MusicElement[][] musicElements;
  private MusicElement[] melodyElements;
  private MusicElement[] chordElements;
  private MusicElement[] bassElements;
  //private List<Calculator> calculators;
  private List<Calculator> melodyCalculators;
  private List<Calculator> chordCalculators;
  private List<Calculator> bassCalculators;
  private int tiedMelodyLength;
  private int tiedChordLength;
  private int tiedBassLength;

  public MusicRepresentation(int measureNum, int division){
    this.measureNum = measureNum;
    this.division = division;
    //musicElements = new MusicElement[trackNum][measureNum*division];
    //calculators = new LinkedList<Calculator>();
    melodyElements = new MusicElement[measureNum*division];
    chordElements = new MusicElement[measureNum*division];
    bassElements = new MusicElement[measureNum*division];
    melodyCalculators = new LinkedList<Calculator>();
    chordCalculators = new LinkedList<Calculator>();
    bassCalculators = new LinkedList<Calculator>();
    tiedMelodyLength = 1;
    tiedChordLength = 1;
    tiedBassLength = 1;
  }

  public int getMeasureNum() { return measureNum; }

  public int getDivision() { return division; }
/*
  public MusicElement getMusicElement(int trackNum, int index){
    return musicElements[trackNum][index];
  }
*/
  public MusicElement getMelodyElement(int i){
    return melodyElements[i];
  }

  public MusicElement addMelodyElement(int i){
    melodyElements[i] = new MusicElement(Type.Melody, i);
    return melodyElements[i];
  }

  public MusicElement getChordElement(int i){
    return chordElements[i];
  }

  public MusicElement addChordElement(int i){
    chordElements[i] = new MusicElement(Type.Chord, i);
    return chordElements[i];
  }

  public MusicElement getBassElement(int i){
    return bassElements[i];
  }

  public MusicElement addBassElement(int i){
    bassElements[i] = new MusicElement(Type.Bass, i);
    return bassElements[i];
  }

  public int getTiedMelodyLength() {
    return tiedMelodyLength;
  }

  public void setTiedMelodyLength(int tiedMelodyLength) {
    this.tiedMelodyLength = tiedMelodyLength;
  }

  public int getTiedChordLength() {
    return tiedChordLength;
  }

  public void setTiedChordLength(int tiedChordLength) {
    this.tiedChordLength = tiedChordLength;
  }

  public int getTiedBassLength() {
    return tiedBassLength;
  }

  public void setTiedBassLength(int tiedBassLength) {
    this.tiedBassLength = tiedBassLength;
  }
/*
  public void setEvidence(int trackNum, long tick, MusicElement me){
    int measureTick = SequencerManager.TICKS_PER_BEAT * 4;
    int measure = (int)(tick / measureTick);
    int index = (int)(tick % measureTick) / (measureTick / division);
    setEvidence(trackNum, measure*division + index, me);
  }

  public void setEvidence(int trackNum, int index, MusicElement me){
    setPredict(trackNum, index, me);
    for(Calculator c : calculators)
      if(c.isCalc(me, trackNum, index))
        c.update(me, trackNum, index);
  }

  public void setPredict(int trackNum, int index, MusicElement me){
    musicElements[trackNum][index] = me;
  }
*/
  public int getIndex(long tick){
    int measureTick = SequencerManager.TICKS_PER_BEAT * 4;
    int measure = (int)(tick / measureTick);
    int index = (int)(tick % measureTick) / (measureTick / division);
    return measure*division + index;
  }

  public void addCalculator(Calculator calc){
    for(Type t : calc.drivenBy()){
      if(t == Type.Melody) melodyCalculators.add(calc);
      else if(t == Type.Chord) chordCalculators.add(calc);
      else bassCalculators.add(calc);
    }
    //calculators.add(calc);
  }

  public class MusicElement{

    private double prob[];
    private boolean isEvidence;
    private int evidence;
    private boolean isNote;
    private Type type;
    private int index;
    private String[] chordLabel = {"C", "Dm", "Em", "F", "G", "Am", "Bm(b5)"};

    public MusicElement(Type type, int index){
      this.type = type;
      if(type == Type.Chord) isNote = false;
      else isNote = true;
      this.index = index;
      prob = new double[128];
    }

    public double getProb(int i){
      if(isEvidence){
        if(evidence == i) return 1.0;
        return 0.0;
      }
      return prob[i];
    }

    public int getHighestProbIndex(){
      if(isEvidence) return evidence;
      double max = Double.MIN_VALUE;
      int index = 0;
      for(int i=0; i<prob.length; i++){
        if(prob[i] > max){
          max = prob[i];
          index = i;
        }
      }
      return index;
    }

    public void setEvidence(int i){
      isEvidence = true;
      evidence = i;
      if(type == Type.Melody){
        for(Calculator c : melodyCalculators) c.update(this, index);
      }else if(type == Type.Chord)
        for(Calculator c : chordCalculators) c.update(this, index);
      else
        for(Calculator c : bassCalculators) c.update(this, index);
    }

    public void setProb(int i, double value){
      isEvidence = false;
      prob[i] = value;
    }

    public String getLabel(int i){
      if(isNote) return i + "";
      return chordLabel[i];
    }

    public int indexOf(String s){
      for(int i=0; i<chordLabel.length; i++)
        if(chordLabel[i].equals(s)) return i;
      return Integer.parseInt(s);
    }

  }

}
