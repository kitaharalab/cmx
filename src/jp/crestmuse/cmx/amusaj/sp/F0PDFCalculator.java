package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;
import jp.crestmuse.cmx.math.*;

public interface F0PDFCalculator {
  DoubleArray calcWeights(PeakSet peakset);
}
