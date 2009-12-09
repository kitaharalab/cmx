package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import java.util.*;

public class WindowSlider extends SPModule {

  private int winsize = 0;
  private double shift = Double.NaN;
  private int shift_;
  private int[] chTarget = null;

  private double[] buff;
  private int fs;
  private DoubleArray[] wav;
//  private DoubleArray wavM = null, wavL = null, wavR = null;
//  private boolean isStereo;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private int t = 0;

  public WindowSlider(boolean isStereo) {
    if (isStereo) {
      chTarget = new int[]{-2, 0, 1}; // -2 means the mixture of all channels
      wav = new DoubleArray[3];
    } else {
      chTarget = new int[]{-2};
      wav = new DoubleArray[1];
    }
  }

  public WindowSlider(int[] chTarget) {
    this.chTarget = chTarget;
    wav = new DoubleArray[chTarget.length];
  }
  
  protected String getParamCategory() {
    return "fft";
  }

  protected String[] getUsedParamNames() {
    return new String[]{"WINDOW_SIZE", "SHIFT"};
  }

  /** "from" and "thru" in milli sec. */
  public void setInputData(AudioDataCompatible audiodata, int from, 
                           int thru) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    winsize = params.getParamInt("fft", "WINDOW_SIZE");
    int channels = audiodata.channels();
    params.setParam("fft", "CHANNELS", channels);
    fs = audiodata.sampleRate();
    params.setParam("fft", "SAMPLE_RATE", fs);
    shift = params.getParamDouble("fft", "SHIFT");
    if (shift < 1)
      shift = shift * fs;
    shift_ = (int)shift;
    DoubleArray[] w = audiodata.getDoubleArrayWaveform();
    for (int i = 0; i < chTarget.length; i++) {
      int idxFrom = (int)((long)from * (long)fs / 1000);
      int idxThru = (int)((long)thru * (long)fs / 1000);
      if (chTarget[i] == -2)
        wav[i] = mean(w, idxFrom, idxThru);
      else
        wav[i] = w[chTarget[i]].subarrayX(idxFrom, idxThru);
    }
    t = 0;
  }


  public void setInputData(AudioDataCompatible audiodata) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    winsize = params.getParamInt("fft", "WINDOW_SIZE");
    int channels = audiodata.channels();
    params.setParam("fft", "CHANNELS", channels);
    fs = audiodata.sampleRate();
    params.setParam("fft", "SAMPLE_RATE", fs);
    shift = params.getParamDouble("fft", "SHIFT");
    if (shift < 1)
      shift = shift * fs;
    shift_ = (int)shift;
    DoubleArray[] w = audiodata.getDoubleArrayWaveform();
    for (int i = 0; i < chTarget.length; i++) {
      if (chTarget[i] == -2)
        wav[i] = mean(w);
      else
        wav[i] = w[chTarget[i]];
    }
//    if (!params.containsParam("fft", "TARGET_CHANNEL")) {
//      if (channels == 2) params.setParam("fft", 
//                                         "TARGET_CHANNEL", "stereo");
//      else params.setParam("fft", "TARGET_CHANNEL", "0");
//    }
//    if (channels == 2 && 
//        params.getParam("fft", "TARGET_CHANNEL").equalsIgnoreCase("mix")) {
//      wavM = add(w[0], w[1]);
//      divX(wavM, 2);
//      setWaveform(wavM, null, null, false);
//    } else if (channels == 2 
//               && params.getParam("fft", "TARGET_CHANNEL").equalsIgnoreCase("stereo")) {
//      wavM = add(w[0], w[1]);
//      divX(wavM, 2);
//      setWaveform(wavM, w[0], w[1], true);
//    } else {
//      try {
//        setWaveform(w[params.getParamInt("fft", "TARGET_CHANNEL")], null, null, false);
//      } catch (NumberFormatException e) {
//        throw new IllegalStateException("TARGET_CHANNEL should be an integer, 'mix', or 'stereo'.");
//      }
//    }
    t = 0;
  }
        
//  private void setWaveform(DoubleArray wM, DoubleArray wL, DoubleArray wR, 
//                           boolean isStereo) {
//    wavM = wM;
//    wavL = wL;
//    wavR = wR;
//    this.isStereo = isStereo;
//  }


  public int getTimeUnit() {
    return 1000 * shift_ / fs;
  }

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
    //    boolean hasNext = (t + winsize < wavM.length());
    boolean hasNext = (t + shift_ + winsize < wav[0].length());
    for (int i = 0; i < wav.length; i++) {
      SPDoubleArray a = new SPDoubleArray(wav[i].subarrayX(t, t + winsize));
      dest[i].add(a);
    }
//    SPDoubleArray a = new SPDoubleArray(wavM.subarrayX(t, t + winsize));
//    dest[0].add(a);
//    if (isStereo) {
//	dest[1].add(new SPDoubleArray(wavL.subarrayX(t, t + winsize)));
//	dest[2].add(new SPDoubleArray(wavR.subarrayX(t, t + winsize)));
//    } else {
//      dest[1].add(a);
//      dest[2].add(a);
//    }
    if(!hasNext) {
      for (int i = 0; i < wav.length; i++)
        dest[i].add(SPTerminator.getInstance());
//	dest[0].add(SPTerminator.getInstance());
//	dest[1].add(SPTerminator.getInstance());
//	dest[2].add(SPTerminator.getInstance());
    }
    t += shift_;
  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[0];
  }

  public Class<SPElement>[] getOutputClasses() {
    Class[] cc = new Class[chTarget.length];
    for (int i = 0; i < cc.length; i++)
      cc[i] = SPDoubleArray.class;
    return cc;
//    return new Class[]{ SPDoubleArray.class, SPDoubleArray.class, SPDoubleArray.class };
  }
                    
}
