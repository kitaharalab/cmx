package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import java.util.*;

public class WindowSlider 
  implements ProducerConsumerCompatible<Object, DoubleArray> {

  private Map<String,Object> params = null;
  private int winsize = 0;
//  private String wintype = null;
//  private double[] window;
  private double shift = Double.NaN;
  private int shift_;
  private int chTarget = 0;
  private boolean paramSet = false;

  private double[] buff;
  private int channels;
  private int fs;
  private DoubleArray wavM = null, wavL = null, wavR = null;
  private boolean isStereo;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private int t = 0;
//  private int t0 = 0, t1 = 0, t2 = 0;

  public void setParams(Map<String,Object> params) {
    this.params = params;
//    paramSet = false;
    setParams();
  }

  private void setParams() {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
//    int winsize;
    if (params != null && params.containsKey("WINDOW_SIZE")) 
      winsize = (Integer)params.get("WINDOW_SIZE");
    else {
      winsize = config.getParamInt("param", "fft", "WINDOW_SIZE");
      params.put("WINDOW_SIZE", winsize);
    }
//    String wintype;
//    if (params != null && params.containsKey("WINDOW_TYPE")) 
//      wintype = ((String)params.get("WINDOW_TYPE")).toLowerCase();
//    else
//      wintype = config.getParam("param", "fft", "WINDOW_TYPE");
//    changeWindow(wintype, winsize);
    
    if (params != null && params.containsKey("SHIFT")) {
      shift = (Double)params.get("SHIFT");
    } else {
      shift = config.getParamDouble("param", "fft", "SHIFT");
      if (params != null) params.put("SHIFT", shift);
    }
    if (params != null && params.containsKey("TARGET_CHANNEL")) {
      chTarget = (Integer)params.get("TARGET_CHANNEL");
    } else {
      chTarget = 0;
      if (params != null) params.put("TARGET_CHANNEL", 0);
    }
    paramSet = true;
  }

/*
  public void changeWindow(String wintype, int winsize) {
    this.winsize = winsize;
    if (params != null) params.put("WINDOW_SIZE", winsize);
    buff = new double[winsize];
    this.wintype = wintype;
    if (params != null) params.put("WINDOW_TYPE", wintype);
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
*/

  public void setInputData(AudioDataCompatible audiodata) {
    if (!paramSet) setParams();
    channels = audiodata.channels();
    if (params != null) params.put("CHANNELS", channels);
    fs = audiodata.sampleRate();
    if (params != null) params.put("SAMPLE_RATE", fs);
    if (shift < 1)
      shift = shift * fs;
    shift_ = (int)shift;
    DoubleArray[] w = audiodata.getDoubleArrayWaveform();
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
//    t0 = t1 = t2 = 0;
  }

  public boolean isStereo() {
    return isStereo;
  }

  public int getInputChannels() {
    return 0;
  }

  public int getOutputChannels() {
    return isStereo ? 3 : 1;
  }

  public TimeSeriesCompatible createOutputInstance(int nFrames, 
                                                   int timeunit) {
    return new MutableTimeSeries(nFrames, timeunit);
  }

  public int getAvailableFrames() {
    return 
      Math.max(0, 
               1 + (int)Math.floor((double)(wavM.length() - winsize) / shift));
  }

  public int getTimeUnit() {
    return 1000 * shift_ / fs;
  }

  public void execute(List<QueueReader<Object>> src,
                      List<TimeSeriesCompatible<DoubleArray>> dest)
    throws InterruptedException {
    dest.get(0).add(wavM.subarrayX(t, t + winsize));
    if (isStereo) {
      dest.get(1).add(wavL.subarrayX(t, t + winsize));
      dest.get(2).add(wavR.subarrayX(t, t + winsize));
    }
    t += shift_;
  }
                    
/*
  private void calcWindowedSignal(DoubleArray wav, int index) {
    if (window == null)
      for (int i = 0; i < buff.length; i++)
        buff[i] = wav.get(index + i);
    else
      for (int i = 0; i < buff.length; i++)
        buff[i] = wav.get(index + i) * window[i];
  }

  public double[] getNextWindowedSignalMixed() {
    calcWindowedSignal(wavM, t0);
    t0 += shift_;
    return buff;
  }

  public double[] getNextWindowedSignalLeft() {
    calcWindowedSignal(wavL, t1);
    t1 += shift_;
    return buff;
  }

  public double[] getNextWindowedSignalRight() {
    calcWindowedSignal(wavR, t2);
    t2 += shift_;
    return buff;
    }
*/

}
