package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import java.util.*;

//public class WindowSlider extends SPModule<SPDummyObject,SPDoubleArray> {
public class WindowSlider extends SPModule {

  private int winsize = 0;
  private double shift = Double.NaN;
  private int shift_;
  private int chTarget = 0;
//  private boolean paramSet = false;

  private double[] buff;
//  private int channels;
  private int fs;
  private DoubleArray wavM = null, wavL = null, wavR = null;
  private boolean isStereo;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private int t = 0;

  protected String getParamCategory() {
    return "fft";
  }

  protected String[] getUsedParamNames() {
    return new String[]{"WINDOW_SIZE", "SHIFT"};
  }

/*
  public void setParams(Map<String,String> params) {
    super.setParams(params);
    copyParamsFromConfigXML("param", "fft", "WINDOW_SIZE", "SHIFT");
//    paramSet = false;
//    setParams();
  }
*/

/*
  private void setParams() {
    winsize = getParamInt("WINDOW_SIZE");
    shift = getParamDouble("SHIFT");
    paramSet = true;
  }
*/

  public void setInputData(AudioDataCompatible audiodata) {
    winsize = getParamInt("WINDOW_SIZE");
    int channels = audiodata.channels();
    setParam("CHANNELS", channels);
    fs = audiodata.sampleRate();
    setParam("SAMPLE_RATE", fs);
    shift = getParamDouble("SHIFT");
    if (shift < 1)
      shift = shift * fs;
    shift_ = (int)shift;
    DoubleArray[] w = audiodata.getDoubleArrayWaveform();
    if (!containsParam("TARGET_CHANNEL")) {
      if (channels == 2) setParam("TARGET_CHANNEL", "stereo");
      else setParam("TARGET_CHANNEL", "0");
    }
    if (channels == 2 && getParam("TARGET_CHANNEL").equalsIgnoreCase("mix")) {
      wavM = add(w[0], w[1]);
      divX(wavM, 2);
      setWaveform(wavM, null, null, false);
    } else if (channels == 2 
               && getParam("TARGET_CHANNEL").equalsIgnoreCase("stereo")) {
      wavM = add(w[0], w[1]);
      divX(wavM, 2);
      setWaveform(wavM, w[0], w[1], true);
    } else {
      try {
        setWaveform(w[getParamInt("TARGET_CHANNEL")], null, null, false);
      } catch (NumberFormatException e) {
        throw new IllegalStateException("TARGET_CHANNEL should be an integer, 'mix', or 'stereo'.");
      }
    }
    t = 0;
  }
        
  private void setWaveform(DoubleArray wM, DoubleArray wL, DoubleArray wR, 
                           boolean isStereo) {
    wavM = wM;
    wavL = wL;
    wavR = wR;
    this.isStereo = isStereo;
  }


//  public boolean isStereo() {
//    return isStereo;
//  }
/*
  public int getInputChannels() {
    return 0;
  }

  public int getOutputChannels() {
    return 3;
//    return isStereo ? 3 : 1;
  }
*/
/*
  public int getAvailableFrames() {
    return 
      Math.max(0, 
               1 + (int)Math.floor((double)(wavM.length() - winsize) / shift_));
  }
*/

  public int getTimeUnit() {
    return 1000 * shift_ / fs;
  }
/*
  public void execute(List<QueueReader<SPDummyObject>> src,
                      List<TimeSeriesCompatible<SPDoubleArray>> dest)
    throws InterruptedException {
    boolean hasNext = (t + shift_ + winsize < wavM.length());
    SPDoubleArray a = new SPDoubleArray(wavM.subarrayX(t, t + winsize), 
                                        hasNext);
    dest.get(0).add(a);
    if (isStereo) {
      dest.get(1).add(new SPDoubleArray(wavL.subarrayX(t, t + winsize), 
                                        hasNext));
      dest.get(2).add(new SPDoubleArray(wavR.subarrayX(t, t + winsize), 
                                        hasNext));
    } else {
      dest.get(1).add(a);
      dest.get(2).add(a);
    }
    t += shift_;
  }
*/
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    boolean hasNext = (t + shift_ + winsize < wavM.length());
    SPDoubleArray a = new SPDoubleArray(wavM.subarrayX(t, t + winsize), 
                                        hasNext);
    dest[0].add(a);
    if (isStereo) {
      dest[1].add(new SPDoubleArray(wavL.subarrayX(t, t + winsize), 
                                        hasNext));
      dest[2].add(new SPDoubleArray(wavR.subarrayX(t, t + winsize), 
                                        hasNext));
    } else {
      dest[1].add(a);
      dest[2].add(a);
    }
    if(!hasNext) {
      dest[0].add(new SPTerminator());
      dest[1].add(new SPTerminator());
      dest[2].add(new SPTerminator());
    }
    t += shift_;
  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[0];
  }

  public Class<SPElement>[] getOutputClasses() {
    return new Class[]{ SPDoubleArray.class, SPDoubleArray.class, SPDoubleArray.class };
  }
                    
}
