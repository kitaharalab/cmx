package jp.crestmuse.cmx.inference;

/** @deprecated */
public interface MusicLayerListener {
  //OrpheusBB用Calculator
    public void update(MusicRepresentation musRep, MusicElement me, 
		       int measure, int tick);
    //  public void update(MusicRepresentation musRep, MusicElement me, int indexInMusRep);
}
