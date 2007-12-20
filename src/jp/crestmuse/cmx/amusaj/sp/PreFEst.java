package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;
import jp.crestmuse.cmx.filewrappers.amusaj.*;
import jp.crestmuse.cmx.math.*;

public interface PreFEst {
  DoubleArray calcWeights(PeakSet peakset);
}
