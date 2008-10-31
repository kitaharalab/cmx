package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import java.util.*;

public class WindowSlider extends SPModule<Object,DoubleArray> {

  private int winsize = 0;
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

  public void setParams(Map<String,String> params) {
    super.setParams(params);
    copyParamsFromConfigXML("param", "fft", "WINDOW_SIZE", "SHIFT");
    paramSet = false;
    setParams();
  }

  private void setParams() {
    winsize = getParamInt("WINDOW_SIZE");
    shift = getParamDouble("SHIFT");
    if (containsParam("TARGET_CHANNEL")) {
      chTarget = getParamInt("TARGET_CHANNEL");
    } else {
      chTarget = 0;
      setParam("TARGET_CHANNEL", 0);
    }
    paramSet = true;
  }

  public void setInputData(AudioDataCompatible audiodata) {
    if (!paramSet) setParams();
    channels = audiodata.channels();
    setParam("CHANNELS", channels);
    fs = audiodata.sampleRate();
    setParam("SAMPLE_RATE", fs);
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
    if (isStereo)
      setParam("STEREO", "Yes");
    t = 0;
  }

//  public boolean isStereo() {
//    return isStereo;
//  }

  public int getInputChannels() {
    return 0;
  }

  public int getOutputChannels() {
    return isStereo ? 3 : 1;
  }

  public int getAvailableFrames() {
    return 
      Math.max(0, 
               1 + (int)Math.floor((double)(wavM.length() - winsize) / shift_));
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
                    
}
