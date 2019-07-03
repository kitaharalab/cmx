package jp.crestmuse.cmx.inference;
import java.util.*;

public interface MusicRepresentation {
  int getMeasureNum();
  int getDivision();
  void addMusicLayer(String name, Object[] labels);
  void addMusicLayer(String name, Object[] labels, int tiedLength);
  void addMusicLayer(String name, List<Object> labels);
  void addMusicLayer(String name, List<Object> labels, int tiedLength);
  void addMusicLayerCont(String name);
  void addMusicLayerCont(String name, int tiedLength);
  boolean containsMusicLayer(String name);
  MusicLayerType getMusicLayerType(String name);
  MusicElement getMusicElement(String layer, int measure, int tick);
  List<MusicElement> getMusicElementList(String layer);
  List<MusicElement> getMusicElementList(String layer,
                                         int measureFrom, int tickFrom,
                                         int measureThru, int tickThru);
  int getTiedLength(String layer);
  Object[] getLabels(String layer);
  void addMusicCalculator(String layer, MusicCalculator calc);


}
