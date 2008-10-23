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
//public class STFT implements 
//                  ProducerConsumerCompatible<DoubleArray,ComplexArray> {
//  private Map<String,Object> params = null;
  private int winsize = -1;
  private String wintype = null;
  private double[] window;
//  private double shift = Double.NaN;
//  private int shift_;
//  private double powerthrs = 0;
//  private double rpowerthrs = 0;
  private boolean paramSet = false;
//  private int chTarget = 0;
////  private PeakExtractor peakext;
  private static final FFTFactory factory = FFTFactory.getFactory();
  private FFT fft = factory.createFFT();
  private static final DoubleArrayFactory dfactory = 
    DoubleArrayFactory.getFactory();

//  private WindowSlider winslider;

//  private double[] buff;
//  private DoubleArray wavM = null, wavL = null, wavR = null;
  private boolean isStereo;
//  private int channels;
//  private int fs;

//  private int t = 0;

/*
  public boolean setOptionsLocal(String option, String value) {
    if (option.equals("-winsize")) {
      winsize = Integer.parseInt(value);
      return true;
    } else if (option.equals("-wintype")) {
      wintype = value.toLowerCase();
      return true;
    } else if (option.equals("-shift")) {
      shift = Double.parseDouble(value);
      return true;
    } else if (option.equals("-ch")) {
      if (value.equals("mix"))
        chTarget = -1;
      else
        chTarget = Integer.parseInt(value);
      return true;
    } else {
      return false;
    }
  }
*/

  public void changeWindow(String wintype, int winsize) {
    this.winsize = winsize;
    setParam("WINDOW_SIZE", winsize);
//    if (params != null) params.put("WINDOW_SIZE", winsize);
//    buff = new double[winsize];
    this.wintype = wintype;
    setParam("WINDOW_TYPE", wintype);
//    if (params != null) params.put("WINDOW_TYPE", wintype);
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
//    this.params = params;
//    if (winslider == null) 
//      winslider = new WindowSlider();
//    winslider.setParams(params);
    paramSet = false;
  }

  private void setParams() {
    wintype = getParam("WINDOW_TYPE").toLowerCase();
    paramSet = true;
  }
  
/*
  private void setParams() {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
//    int winsize;
//    if (params != null && params.containsKey("WINDOW_SIZE")) {
//      winsize = (Integer)params.get("WINDOW_SIZE");
//    } else {
//      winsize = config.getParamInt("param", "fft", "WINDOW_SIZE");
//      params.put("WINDOW_SIZE", winsize);
//    }
//    buff = new double[winsize];
    if (containsParam("WINDOW_TYPE")) {
      wintype = getParam("WINDOW_TYPE").toLowerCase();
//    if (params != null && params.containsKey("WINDOW_TYPE")) {
//      wintype = ((String)params.get("WINDOW_TYPE")).toLowerCase();
    } else {
      wintype = config.getParam("param", "fft", "WINDOW_TYPE").toLowerCase();
      setParam("WINDOW_TYPE", wintype);
//      params.put("WINDOW_TYPE", wintype);
    }
//    changeWindow(wintype, winsize);
//    if (wintype.startsWith("ham"))
//      window = hamming(winsize);
//    else if (wintype.startsWith("hum"))
//      window = hanning(winsize);
//    else if (wintype.startsWith("gaus"))
//      window = gaussian(winsize);
//    else
//      throw new ConfigXMLException
//        ("WINDOW_TYPE is not found or wrong.");
//    if (params != null && params.containsKey("SHIFT")) {
//      shift = (Double)params.get("SHIFT");
//    } else {
//      shift = config.getParamDouble("param", "fft", "SHIFT");
//      params.put("SHIFT", shift);
//    }
//    if (params != null && params.containsKey("TARGET_CHANNEL")) {
//      chTarget = (Integer)params.get("TARGET_CHANNEL");
//    } else {
//      chTarget = 0;
//      params.put("TARGET_CHANNEL", 0);
//    }
//    powerthrs = config.getParamDouble("param", "fft", "POWER_THRESHOLD");
//    rpowerthrs = config.getParamDouble("param", "fft", 
//				       "RELATIVE_POWER_THRESHOLD");
//    peakext = new PeakExtractor();
//    fft = factory.createFFT();
    paramSet = true;
//  }
  }
*/

/*
  public void setInputData(AudioDataCompatible audiodata) {
    if (winslider == null) 
      winslider = new WindowSlider();
    winslider.setParams(params);
    winslider.setInputData(audiodata);
    isStereo = winslider.isStereo();
    if (fft == null)
      fft = factory.createFFT();
  }
*/

/*
  public void setInputData(WAVXMLWrapper wavxml) {
    if (!paramSet) setParams();
    channels = wavxml.getFmtChunk().channels();
    if (params != null) params.put("CHANNEL", channels);
    fs = (int)wavxml.getFmtChunk().sampleRate();
    if (params != null) params.put("SAMPLE_RATE", fs);
    if (shift < 1)
      shift = shift * fs;
    shift_ = (int)shift;
    DoubleArray[] w = 
      wavxml.getDataChunkList()[0].getAudioData().getDoubleArrayWaveform();
    if (chTarget == 0 && channels == 2) {
      wavM = add(w[0], w[1]);
      divX(wavM, 2);
      wavL = w[0];
      wavR = w[1];
      isStereo = true;
    } else if (chTarget == -1 && channels == 2) {
      wavM = add(w[0], w[1]);
      divX(wavM, 2);
      wavL = null;
      wavR = null;
      isStereo = false;
    } else {
      wavM = w[chTarget];
      wavL = null;
      wavR = null;
      isStereo = false;
    }
    t = 0;
  }
*/

/*
  public int getAvailableFrames() {
    return winslider.getAvailableFrames();
  }
*/

  /*
  public int getAvailableFrames() {
    return 
      Math.max(0, 
               1 + (int)Math.floor((double)(wavM.length() - winsize) / shift));
  }
  */

/*

  public int getTimeUnit() {
    return winslider.getTimeUnit();
  }
*/

/*
  public int getTimeUnit() {
    return 1000 * shift_ / fs;
  }
*/

  public void setStereo(boolean b) {
    isStereo = b;
  }

  /*********************************************************************
   *あらかじめsetInputDataメソッドでセットしたwaveformに対してSTFTを実行します. 
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
//    double[] buff = winslider.getNextWindowedSignalMixed();
    DoubleArray signal = src.get(0).take();
    if (winsize < 0 || winsize != signal.length())
      changeWindow(wintype, signal.length());
    dest.get(0).add(fft.executeR2C(signal, window));
//    dest.get(0).add(dfactory.createArray(fft.executeR2C(buff)));
//    stft1ch(wavM, dest.get(0), t);
    if (isStereo) {
//      buff = winslider.getNextWindowedSignalLeft();
//      dest.get(1).add(dfactory.createArray(fft.executeR2C(buff)));
      dest.get(1).add(fft.executeR2C(src.get(1).take(), window));
//      buff = winslider.getNextWindowedSignalRight();
//      dest.get(2).add(dfactory.createArray(fft.executeR2C(buff)));
      dest.get(2).add(fft.executeR2C(src.get(2).take(), window));
//      stft1ch(wavL, dest.get(1), t);
//      stft1ch(wavR, dest.get(2), t);
    }
//    t += shift_;
  }

  public int getInputChannels() {
    return isStereo ? 3 : 1;
  }

  public int getOutputChannels() {
    return isStereo ? 3 : 1;
  }

//  public TimeSeriesCompatible<ComplexArray> 
//      createOutputInstance(int nFrames, int timeunit) {
//    return new MutableComplexTimeSeries(nFrames, timeunit);
//  }

/*
  private void stft1ch(DoubleArray wav, TimeSeriesCompatible dest, 
                       int index) throws InterruptedException {
    for (int i = 0; i < buff.length; i++)
      buff[i] = wav.get(index + i) * window[i];
    dest.add(dfactory.createArray(fft.executeR2C(buff)));
  }
*/
}
    
    