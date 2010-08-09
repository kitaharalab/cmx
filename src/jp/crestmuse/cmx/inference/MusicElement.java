package jp.crestmuse.cmx.inference;
import java.util.*;

public interface MusicElement {
  void setProb(int index, double value);
//  void setProb(int index, double value, boolean update);
  void setLogLikelihood(int index, double value);
//  void setLogLikelihood(int index, double value, boolean update);
  double getLogLikelihood(int index);
  double getProb(int index);
  double[] getAllProbs();
  int getHighestProbIndex();
  Object getLabel(int index);
  int getIndexOf(Object label);
  int getProbLength();
  void setEvidence(int index);
  boolean hasEvidence();
  void removeEvidence();
  void update();
  int getBackPointer(int index);
  void setBackPointer(int index, int value);
  void setBackPointerTo(MusicElement e);
  int measure();
  int tick();
  int tiedLength();
  boolean tiedFromPrevious();
  void setTiedFromPrevious(boolean b);
  boolean rest();
  void setRest(boolean b);
  void setAttribute(String key, String value);
  String getAttribute(String key);
  boolean hasAttribute(String key);
  void removeAttribute(String key);
  Map<String,String> getAllAttributes();
  void setAllAttributes(Map<String,String> map);
}
