package jp.crestmuse.cmx.inference.models;
import java.util.List;

import jp.crestmuse.cmx.inference.MusicElement;

@Deprecated
interface HMMParamHook {
  public double getPi(int i, List<MusicElement> e);
  public double getAij(int i, int j, List<MusicElement> e);
}
