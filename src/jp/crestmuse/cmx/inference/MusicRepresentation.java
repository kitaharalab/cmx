package jp.crestmuse.cmx.inference;

public interface MusicRepresentation {
  int getMeasureNum();
  int getDivision();
  void addMusicLayer(String name, Object[] labels);
  void addMusicLayer(String name, Object[] labels, int tiedLength);
  MusicElement getMusicElement(String layer, int measure, int tick);
  int getTiedLength(String layer);


}