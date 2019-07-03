package jp.crestmuse.cmx.amusaj.sp;

public class FFTFactoryImpl extends FFTFactory {
  public FFT createFFT() {
    return new FFTImpl();
  }
}
