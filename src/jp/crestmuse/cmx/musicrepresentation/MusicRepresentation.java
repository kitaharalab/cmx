package jp.crestmuse.cmx.musicrepresentation;

import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentation {

  private int measureNum;
  private int division;
  private MusicElement[][] musicElements;
  private List<Calculator> calculators;

  public MusicRepresentation(int measureNum, int division, int trackNum){
    this.measureNum = measureNum;
    this.division = division;
    musicElements = new MusicElement[trackNum][measureNum*division];
    calculators = new LinkedList<Calculator>();
  }

  public int getTrackNum() { return musicElements.length; }

  public int getMeasureNum() { return measureNum; }

  public int getDivision() { return division; }

  public MusicElement getMusicElement(int trackNum, int index){
    return musicElements[trackNum][index];
  }

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

  public void addCalculator(Calculator calc){
    calculators.add(calc);
  }

}
