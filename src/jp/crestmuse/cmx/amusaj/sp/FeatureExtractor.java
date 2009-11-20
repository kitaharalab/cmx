package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public interface FeatureExtractor<E extends SPElement> {
  void extractFeatures(E src);
  void nextFrame();
  DoubleArray getFeature(int index);
  String getFeatureType(int index);
  int nFeatureTypes();
//  int dim(int index);
}
