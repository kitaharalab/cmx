package jp.crestmuse.cmx.inference;
import java.util.*;

public interface MusicElement {
  /** @deprecated */
  void setProb(int index, double value);

  void setProb(Object label, double value);

  /** @deprecated */
  double getProb(int index);

  double getProb(Object label);


//  double[] getAllProbs();

  /** @deprcated */
  int getHighestProbIndex();

  Object getMostLikely();


  /** @deprecated */
  Object getLabel(int index);
  /** @deprecated */
  int indexOf(Object label);
  
  /** @deprecated */
  int getProbLength();

  /** @deprecated */
  void setEvidence(int index);
  void setEvidence(Object label);

  Object generate();
}
