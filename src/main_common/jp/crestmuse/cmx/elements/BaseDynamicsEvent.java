package jp.crestmuse.cmx.elements;

public class BaseDynamicsEvent extends MutableMusicEvent {

  private double value;
  
  public BaseDynamicsEvent(long timeStamp, double value, int ticksPerBeat){
//  public BaseDynamicsEvent(int timeStamp, int ticksPerBeat, double value){
  super(Type.CONTROL_CHANGE, timeStamp, timeStamp, ticksPerBeat);
    this.value = value;
  }

  public double getValue() { return value; }

}
