package jp.crestmuse.cmx.inference;
import java.util.*;

public interface MusicRepresentation {
  int getMeasureNum();
  int getDivision();
  void addMusicLayer(String name, Object[] labels);
  void addMusicLayer(String name, Object[] labels, int tiedLength);
  void addMusicLayer(String name, List<Object> labels);
  void addMusicLayer(String name, List<Object> labels, int tiedLength);
  MusicElement getMusicElement(String layer, int measure, int tick);
  int getTiedLength(String layer);
  Object[] getLabels(String layer);
  void addMusicCalculator(String layer, MusicCalculator calc);


}