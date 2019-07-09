package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.DoubleArray;

public interface FeatureExtractor<E> {
  void extractFeatures(E src);
//  void nextFrame();
  DoubleArray getFeature(int index);
//  DoubleArray getFeature(String type);
  String getFeatureType(int index);
  int nFeatureTypes();
//  int dim(int index);
}
