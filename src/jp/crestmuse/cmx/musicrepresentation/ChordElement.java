package jp.crestmuse.cmx.musicrepresentation;

import jp.crestmuse.cmx.misc.Chord;

public class ChordElement extends Chord implements MusicElement {

  public ChordElement(String chordname){
    super(chordname);
  }
  
  public String getName() {
    return getChordName();
  }

  public int[] getNums() {
   int[] a = new int[getNotesList().size()];
   for(int i=0; i<getNotesList().size(); i++){
     a[i] = getNotesList().get(i);
   }
   return a;
  }

}
