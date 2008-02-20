package jp.crestmuse.cmx.math;

class DefaultComplexArrayFactory extends ComplexArrayFactory {
  public ComplexArray createArray(int length) {
    return new DefaultComplexArray(length);
  }

  public ComplexArray createArray(double[] re, double[] im) {
    return new DefaultComplexArray(re, im);
  }

//  public ComplexArray createArray(double[] array) {
//    return new DefaultComplexArray(array, null);
//  }

//  public ComplexArray createArray2(double[] array) {
//    return new DefaultComplexArray2(array);
//  }
}