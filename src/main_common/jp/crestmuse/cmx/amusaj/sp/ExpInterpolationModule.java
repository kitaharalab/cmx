package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.math.Utils.*;

class ExpInterpolationModule extends AbstractInterpolationModule {

  private static final ExpInterpolationModule interp = 
    new ExpInterpolationModule();
  private double c;

  boolean isMissing(double x) {
    return Double.isInfinite(x) || Double.isNaN(x) || (x == 0);
  }
  
  void calcCoefficient(int leftindex, double leftvalue, 
                       int rightindex, double rightvalue) {
    c = (Math.log(rightvalue)-Math.log(leftvalue))/(rightindex-leftindex);
  }

  double calcValue(int index, int leftindex, 
                   double leftvalue, int rightindex, double rightvalue) {
    return leftvalue * Math.exp(c * (index - leftindex));
  }

  static ExpInterpolationModule getInstance() {
    return interp;
  }

  static void interpolateX(DoubleArray x, int leftbound, 
                           int rightbound, int maxlength) {
    interp.run(x, leftbound, rightbound, maxlength);
  }

  static void interpolateX(DoubleArray x, int maxlength) {
    interp.run(x, maxlength);
  }

  static DoubleArray interpolate(DoubleArray x, int leftbound, 
                                 int rightbound, int maxlength) {
    DoubleArray z = cloneArray(x);
    interpolateX(z, leftbound, rightbound, maxlength);
    return z;
  }

  static DoubleArray interpolate(DoubleArray x, int maxlength) {
    DoubleArray z = cloneArray(x);
    interpolateX(z, maxlength);
    return z;
  }
}