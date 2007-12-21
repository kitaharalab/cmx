package jp.crestmuse.cmx.amusaj.sp;

public abstract class FFTFactory extends SPFactory {
  public static FFTFactory getFactory() {
    return (FFTFactory)getFactory("fftFactory", "FFTFactoryImpl");
  }

  public abstract FFT createFFT();
}
