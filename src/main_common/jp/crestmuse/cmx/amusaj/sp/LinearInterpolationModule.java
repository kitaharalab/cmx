package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.DoubleArray;

import static jp.crestmuse.cmx.math.MathUtils.cloneArray;

class LinearInterpolationModule extends AbstractInterpolationModule {

  private static final LinearInterpolationModule interp = 
    new LinearInterpolationModule();
  private double c;

  boolean isMissing(double x) {
    return Double.isInfinite(x) || Double.isNaN(x);
  }

  void calcCoefficient(int leftindex, double leftvalue,
                       int rightindex, double rightvalue) {
    c = (rightvalue - leftvalue) / (rightindex - leftindex);
  }

  double calcValue(int index, int leftindex, 
                   double leftvalue, int rightindex, double rightvalue) {
    return leftvalue + c * (index - leftindex);
  }

  static LinearInterpolationModule getInstance() {
    return interp;
  }

  static void interpolateX(DoubleArray x, int maxlength) {
    interp.run(x, maxlength);
  }

  static DoubleArray interpolate(DoubleArray x, int maxlength) {
    DoubleArray z = cloneArray(x);
    interpolateX(z, maxlength);
    return z;
  }
}