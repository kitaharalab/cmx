package jp.crestmuse.cmx.inference;

import org.apache.commons.math3.distribution.RealDistribution;

import java.util.Map;

public interface MusicElement {
  //  /** @deprecated */
  //  void setProb(int index, double value);

  void setDistribution(RealDistribution d);
  
  void setProb(Object label, double value);

  void setProb(Map<Object,Double> map);

  //  /** @deprecated */
  //  double getProb(int index);

  double getProb(Object label);


//  double[] getAllProbs();

//  /** @deprcated */
//  int getHighestProbIndex();

  Object getMostLikely();


  //  /** @deprecated */
  //  Object getLabel(int index);
  //  /** @deprecated */
  //  int indexOf(Object label);
  
  //  /** @deprecated */
  //  int getProbLength();

  //  /** @deprecated */
  //  void setEvidence(int index);
  void setEvidence(Object label);

  Object generate();

  void suspendUpdate();

  void resumeUpdate();

  boolean rest();

  void setRest(boolean b);

  boolean tiedFromPrevious();

  void setTiedFromPrevious(boolean b);

  MusicElement next();

  MusicElement prev();

  int measure();

  int tick();

  int duration();
}
