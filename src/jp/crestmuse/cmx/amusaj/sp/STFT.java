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
public class STFT extends SPModule<DoubleArray,ComplexArray> {
  private int winsize = -1;
  private String wintype = null;
  private double[] window;
  private boolean paramSet = false;
  private static final FFTFactory factory = FFTFactory.getFactory();
  private FFT fft = factory.createFFT();
  private static final DoubleArrayFactory dfactory = 
    DoubleArrayFactory.getFactory();

  private boolean isStereo;

  public void changeWindow(String wintype, int winsize) {
    this.winsize = winsize;
    setParam("WINDOW_SIZE", winsize);
    this.wintype = wintype;
    setParam("WINDOW_TYPE", wintype);
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
       

  public void setParams(Map<String, String> params) {
    super.setParams(params);
    copyParamsFromConfigXML("param", "fft", "WINDOW_TYPE");
    paramSet = false;
  }

  private void setParams() {
    wintype = getParam("WINDOW_TYPE").toLowerCase();
    String stereo = getParam("STEREO");
    isStereo = 
      stereo != null 
      && (stereo.startsWith("Y") || stereo.startsWith("y") 
          || stereo.startsWith("T") || stereo.startsWith("t"));
    paramSet = true;
  }
  
//  public void setStereo(boolean b) {
//    isStereo = b;
//  }

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
  public void execute(List<QueueReader<DoubleArray>> src, 
                      List<TimeSeriesCompatible<ComplexArray>> dest) 
    throws InterruptedException {
    if (!paramSet) setParams();
    DoubleArray signal = src.get(0).take();
    if (winsize < 0 || winsize != signal.length())
      changeWindow(wintype, signal.length());
//    ComplexArray aa = fft.executeR2C(signal, window);
//    for (int i = 0; i < aa.length(); i++)
//      System.out.print(Math.hypot(aa.getReal(i), aa.getImag(i)) + " ");
//    System.out.println();
    dest.get(0).add(fft.executeR2C(signal, window));
    if (isStereo) {
      dest.get(1).add(fft.executeR2C(src.get(1).take(), window));
      dest.get(2).add(fft.executeR2C(src.get(2).take(), window));
    }
  }

  public int getInputChannels() {
    return isStereo ? 3 : 1;
  }

  public int getOutputChannels() {
    return isStereo ? 3 : 1;
  }

}
    
    