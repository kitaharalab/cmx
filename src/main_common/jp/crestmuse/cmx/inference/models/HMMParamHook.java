package jp.crestmuse.cmx.inference.models;
import jp.crestmuse.cmx.inference.*;
import java.util.*;

@Deprecated
interface HMMParamHook {
  public double getPi(int i, List<MusicElement> e);
  public double getAij(int i, int j, List<MusicElement> e);
}
