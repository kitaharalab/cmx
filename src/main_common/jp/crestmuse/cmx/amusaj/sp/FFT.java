package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public interface FFT {
  ComplexArray executeR2C(DoubleArray x, double[] window);
//  double[] executeR2C(double[] x);
}
