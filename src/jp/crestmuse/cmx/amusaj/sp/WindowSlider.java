package jp.crestmuse.cmx.amusaj.sp;
import static jp.crestmuse.cmx.math.Operations.mean;
import static jp.crestmuse.cmx.sound.SoundUtils.excerpt;

import java.io.IOException;

import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.DoubleArrayFactory;
import jp.crestmuse.cmx.sound.AudioDataCompatible;
import jp.crestmuse.cmx.sound.TickTimer;

public class WindowSlider extends SPModule {

  int winsize = 0;
  private double shift = Double.NaN;
  private int shift_;
  int[] chTarget = null;

  private double[] buff;
  private int fs;
  AudioDataCompatible audiodata;
//  private DoubleArray[] wav;
//  private DoubleArray wavM = null, wavL = null, wavR = null;
//  private boolean isStereo;

  TickTimer ticktimer = null;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

//  private int t = 0;

  public WindowSlider(boolean isStereo) {
    if (isStereo) {
      chTarget = new int[]{-2, 0, 1}; // -2 means the mixture of all channels
//      wav = new DoubleArray[3];
    } else {
      chTarget = new int[]{-2};
//      wav = new DoubleArray[1];
    }
  }

  public WindowSlider(int[] chTarget) {
    this.chTarget = chTarget;
//    wav = new DoubleArray[chTarget.length];
  }
  
  protected String getParamCategory() {
    return "fft";
  }

  protected String[] getUsedParamNames() {
    return new String[]{"WINDOW_SIZE", "SHIFT"};
  }

  /** "from" and "thru" in milli sec. */
  public void setInputData(AudioDataCompatible audiodata, 
                           int from, int thru) {
    long fs = audiodata.sampleRate();
    setInputData(excerpt(audiodata, 
                         (int)((long)from * fs / 1000), 
                         (int)((long)thru * fs / 1000)));
  }
  
  public AudioDataCompatible getTargetWaveform() {
    return audiodata;
  }

  public void setTickTimer(TickTimer timer) {
    ticktimer = timer;
  }

/*public void setInputData(AudioDataCompatible audiodata, int from, 
                           int thru) {
    if (!audio.supportsWholeWaveformGetter())
      throw new IllegalStateException();
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
    this.audiodata = audiodata;
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
*/

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
    this.audiodata = audiodata;
//    if (audiodata.supportsWholeWaveformGetter()) {
//      DoubleArray[] w = audiodata.getDoubleArrayWaveform();
//      for (int i = 0; i < chTarget.length; i++) {
//        if (chTarget[i] == -2)
//          wav[i] = mean(w);
//        else
//          wav[i] = w[chTarget[i]];
//      }
//    } else {
////      for (int i = 0; i < chTarget.length; i++)
////        wav[i] = factory.createArray(60 * fs);
//    }
//    t = 0;
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

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    long ticktime = ticktimer != null ? ticktimer.getTickPosition() : -1;
      if (audiodata.hasNext(winsize)) {
      try {
        DoubleArray[] wav = audiodata.readNext(winsize, winsize - shift_);
        
        for (int i = 0; i < chTarget.length; i++) {
          DoubleArrayWithTicktime w;
          if (chTarget[i] == -2)
            w = new DoubleArrayWithTicktime(mean(wav), ticktime);
          else
            w = new DoubleArrayWithTicktime(wav[chTarget[i]], ticktime);
          dest[i].add(w);
        }
      } catch (IOException e) {
        throw new SPException(e);
      }
    } else { 
      for (int i = 0; i < chTarget.length; i++)
        dest[i].add(SPTerminator.getInstance());
    }
  }
  
/*
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
    boolean hasNext = (t + shift_ + winsize < wav[0].length());
    if (audiodata.supportsWholeWaveformGetter()) {
      for (int i = 0; i < wav.length; i++) {
        SPDoubleArray a = new SPDoubleArray(wav[i].subarrayX(t, t + winsize));
        dest[i].add(a);
      }
    } else {
      for (int i = 0; i < chTarget.length; i++) {
        audiodata.next(winsize
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
*/


  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    Class[] cc = new Class[chTarget.length];
    for (int i = 0; i < cc.length; i++)
      cc[i] = DoubleArray.class;
    return cc;
//    return new Class[]{ SPDoubleArray.class, SPDoubleArray.class, SPDoubleArray.class };
  }
                    
}
