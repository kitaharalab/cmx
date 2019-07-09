package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.ComplexArray;
import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.DoubleArrayFactory;

import static jp.crestmuse.cmx.amusaj.sp.SPUtils.gaussian;
import static jp.crestmuse.cmx.amusaj.sp.SPUtils.hamming;
import static jp.crestmuse.cmx.amusaj.sp.SPUtils.hanning;

/*********************************************************************
 *FFTFactoryクラスのファクトリを通じて得られたFFTオブジェクトを用いて, 
 *短時間フーリエ変換を行います. 
 *********************************************************************/

public class STFT extends SPModule {

  int winsize = -1;
  String wintype = null;
  double[] window;
  private boolean paramSet = false;
  private static final FFTFactory factory = FFTFactory.getFactory();
  FFT fft = factory.createFFT();
  private static final DoubleArrayFactory dfactory = 
    DoubleArrayFactory.getFactory();

  private boolean isStereo;

  public STFT(boolean isStereo) {
    super();
    this.isStereo = isStereo;
  }

  public void changeWindow(String wintype, int winsize) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    this.winsize = winsize;
    params.setParam("fft", "WINDOW_SIZE", winsize);
    this.wintype = wintype;
    params.setParam("fft", "WINDOW_TYPE", wintype);
    if (wintype.startsWith("ham"))
      window = hamming(winsize);
    else if (wintype.startsWith("han"))
      window = hanning(winsize);
    else if (wintype.startsWith("gaus"))
      window = gaussian(winsize);
    else if (wintype.startsWith("rect"))
      window = null;
    else
      throw new IllegalStateException("Unsupported window type");
  }

  protected String getParamCategory() {
    return "fft";
  }

  protected String[] getUsedParamNames() {
    return new String[]{"WINDOW_TYPE"};
  }

  void setParams() {
    if (paramSet) return;
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    wintype = params.getParam("fft", "WINDOW_TYPE").toLowerCase();
    paramSet = true;
  }
  

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
  throws InterruptedException {
    setParams();
    if (src[0] instanceof DoubleArrayWithTicktime) {
      DoubleArrayWithTicktime signal = (DoubleArrayWithTicktime)src[0];
      if (winsize < 0 || winsize != signal.length())
        changeWindow(wintype, signal.length());
      ComplexArrayWithTicktime fftresult = 
        new ComplexArrayWithTicktime(fft.executeR2C(signal, window), signal.music_position);
      dest[0].add(fftresult);
      if (isStereo) {
        dest[1].add(new ComplexArrayWithTicktime(
                      fft.executeR2C((DoubleArray)src[1], window), signal.music_position));
        dest[2].add(new ComplexArrayWithTicktime(
                      fft.executeR2C((DoubleArray)src[2], window), signal.music_position));
      } else {
        dest[1].add(fftresult);
        dest[2].add(fftresult);
      }
    } else {
      DoubleArray signal = (DoubleArray)src[0];
      if (winsize < 0 || winsize != signal.length())
        changeWindow(wintype, signal.length());
      ComplexArray fftresult = fft.executeR2C(signal, window);
      dest[0].add(fftresult);
      if (isStereo) {
        dest[1].add(fft.executeR2C((DoubleArray)src[1], window));
        dest[2].add(fft.executeR2C((DoubleArray)src[2], window));
      } else {
        dest[1].add(fftresult);
        dest[2].add(fftresult);
      }
    }
  }
                
  public Class[] getInputClasses() {
    if (isStereo)
      return new Class[]{ DoubleArray.class, DoubleArray.class, DoubleArray.class };
    else
      return new Class[] { DoubleArray.class };
  }

  public Class [] getOutputClasses() {
    return new Class[]{ ComplexArray.class, ComplexArray.class, ComplexArray.class };
  }

}
    
    