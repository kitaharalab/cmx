package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import java.util.*;

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
  

  /*********************************************************************
   *（古い）あらかじめsetInputDataメソッドでセットしたwaveformに対してSTFTを実行します. 
   *destはTimeSeriesCompatibleを要素とするListで, セットされたwaveformが
   *モノラルの場合は要素数=1でdest.get(0)がSTFT結果を表し, セットされたwaveformが
   *ステレオの場合は要素数=3で, dest.get(0)が左右混合信号に対するSTFT結果, 
   *dest.get(1)が左信号に対するSTFT結果, dest.get(2)が右信号に対するSTFT結果を
   *表します. 
   *@param src 常にnullを指定します(何を指定しても無視されます)
   *@param dest STFT実行結果格納用リスト
   *********************************************************************/
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
  throws InterruptedException {
    setParams();
    SPDoubleArray signal = (SPDoubleArray)src[0];
    if (winsize < 0 || winsize != signal.length())
      changeWindow(wintype, signal.length());
    SPComplexArray fftresult = 
      new SPComplexArray(fft.executeR2C(signal, window));
    dest[0].add(fftresult);
    if (isStereo) {
      dest[1].add(new SPComplexArray(fft.executeR2C((SPDoubleArray)src[1],
                                                    window)));
      dest[2].add(new SPComplexArray(fft.executeR2C((SPDoubleArray)src[2], 
                                                    window)));
    } else {
      dest[1].add(fftresult);
      dest[2].add(fftresult);
    }
  }
                
  public Class<SPElement>[] getInputClasses() {
    if (isStereo)
      return new Class[]{ SPDoubleArray.class, SPDoubleArray.class, SPDoubleArray.class };
    else
      return new Class[] { SPDoubleArray.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    return new Class[]{ SPComplexArray.class, SPComplexArray.class, SPComplexArray.class };
  }

}
    
    