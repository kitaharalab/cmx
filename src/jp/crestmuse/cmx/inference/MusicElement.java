package jp.crestmuse.cmx.inference;

public interface MusicElement {
  void setProb(int index, double value);
  void setProb(int index, double value, boolean update);
  void setLogLikelihood(int index, double value);
  void setLogLikelihood(int index, double value, boolean update);
  double getLogLikelihood(int index);
  double[] getAllProbs();
  int getHighestProbIndex();
  Object getLabel(int index);
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
//  boolean tiedFromPrevious();
}