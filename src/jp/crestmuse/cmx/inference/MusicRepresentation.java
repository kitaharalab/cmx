package jp.crestmuse.cmx.inference;

public interface MusicRepresentation {
  int getMeasureNum();
  int getDivision();
  void addMusicLayer(String name, Object[] labels);
  void addMusicLayer(String name, Object[] labels, int tiedLength);
  MusicElement getMusicElement(String layer, int measure, int tick);
  int getTiedLength(String layer);
  Object[] getLabels(String layer);

    /** deprecated */
  void addMusicLayerListener(String layername, MusicLayerListener listener);

    void addVerticalMusicCalculator(String layername, MusicCalculator calc);


  boolean isChanged();
  void resetChangeFlag();
  void suspendUpdate();
  void resumeUpdate();

}