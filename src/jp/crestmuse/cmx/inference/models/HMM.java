package jp.crestmuse.cmx.inference.models;
import java.util.*;
import jp.crestmuse.cmx.inference.*;

public interface HMM<O extends Object> {
  int[] mostLikelyStateSequence(List<O> o, List<MusicElement> e);
  void calcForwardBackward(List<O> o, List<MusicElement> e);
  double getForwardProb(int t, int i);
  double getBackwardProb(int t, int i);
  double calcProb(List<O> o, int[] states, List<MusicElement> e);
  //  void setParamHook(HMMParamHook hook);
}
