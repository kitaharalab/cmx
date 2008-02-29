package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public interface FeatureExtractor {
  void extractFeatures(DoubleArray src);
  void nextFrame();
  DoubleArray getFeature(int index);
  String getFeatureType(int index);
  int nFeatureTypes();
//  int dim(int index);
}
