package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;
import jp.crestmuse.cmx.math.*;

public interface F0PredominanceCalculator {
  DoubleArray calcWeights(PeakSet peakset);
}
