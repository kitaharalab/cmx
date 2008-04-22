package jp.crestmuse.cmx.amusaj.filewrappers;

public class MIDINoteArray implements Deltable {
  public int delta(){
    return delta;
  }
  public static final int DIM = 128;
  public boolean noteArray[] = new boolean[DIM];
  public int delta;
  public String chord = "";
  public String toString(){
    String result = "delta:"+delta+", note:";
    for(int i=0; i<DIM; i+=1) if(noteArray[i])result += i + " ";
    result += ", chord:"+chord;
    return result;
  }
}