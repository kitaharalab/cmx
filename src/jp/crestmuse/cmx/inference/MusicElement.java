package jp.crestmuse.cmx.inference;
import java.util.*;

public interface MusicElement {
  /** @deprecated */
  void setProb(int index, double value);

  void setProbV(Object label, double value);
//  void setProbH(Object label, double value);
  void setPriorProb(Object label, double value);

  void setLogProbV(Object label, double value);
//  void setLogProbH(Object label, double value);
  void setLogPriorProb(Object label, double value);

  double getProb(Object label);
//  double getProbV(Object label);
//  double getProbH(Object label);
//  double getPriorProb(Object label);

  double getLogProb(Object label);
//  double getLogProbV(Object label);
//  double getLogProbH(Object label);
//  double getLogPriorProb(Object label);


  /** @deprecated */
  void setLogLikelihood(int index, double value);

  /** @deprecated */
  double getLogLikelihood(int index);

  /** @deprecated */
  double getProb(int index);
  /** @deprecated */
  double[] getAllProbs();

  /** @deprcated */
  int getHighestProbIndex();

  Object getMostLikelyLabel();
//  Object getHighestProbLabel();

  Object[] getLabels();
  /** @deprecated */
  int getNumOfLabels();
  /** @deprecated */
  Object getLabel(int index);
  /** @deprecated */
  int getIndexOf(Object label);
  
  /** @deprecated */
  int getProbLength();

  /** @deprecated */
  void setEvidence(int index);
  void setEvidence(Object label);
  boolean hasEvidence();
  void removeEvidence();

  /** @deprecated */
  void update();
//  int getBackPointer(int index);
//  void setBackPointer(int index, int value);
//  void setBackPointerTo(MusicElement e);
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
  MusicElement next();
  MusicElement previous();
}
