package jp.crestmuse.cmx.inference;

public interface MusicCalculator {
  public void updated(int measure, int tick, String layer, 
                      MusicRepresentation mr);
}
