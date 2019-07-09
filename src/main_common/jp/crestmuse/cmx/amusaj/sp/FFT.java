package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.ComplexArray;
import jp.crestmuse.cmx.math.DoubleArray;

public interface FFT {
  ComplexArray executeR2C(DoubleArray x, double[] window);
//  double[] executeR2C(double[] x);
}
