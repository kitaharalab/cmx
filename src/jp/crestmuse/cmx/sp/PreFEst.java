package jp.crestmuse.cmx.sp;
import jp.crestmuse.cmx.filewrappers.amusaj.*;
import jp.crestmuse.cmx.math.*;

public interface PreFEst {
  DoubleArray calcWeights(PeakSet peakset);
}
