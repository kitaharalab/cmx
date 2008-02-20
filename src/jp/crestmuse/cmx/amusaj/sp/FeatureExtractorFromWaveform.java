package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public interface FeatureExtractorFromWaveform {
  DoubleArray extractFeatures(DoubleArray wav, int index);
  String getFeatureType(int index);
  int nFeatureTypes();
}
