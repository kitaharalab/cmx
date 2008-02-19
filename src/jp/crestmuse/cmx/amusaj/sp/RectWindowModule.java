package jp.crestmuse.cmx.amusaj.sp;

public class RectWindowModule 
  implements ProducerConsuerCompatible<Object,TimeSeriesCompatible> {

  private Map<String,Object> params = null;
  private int winsize = 0;
  private double shift = Double.NaN;
  private int shift_;
  private int chTarget = 0;
  private boolean paramSet = false;

  private int channels;
  private int fs;
  private DoubleArray wavM = null, wavL = null, wavR = null;
  private boolean isStereo;

  private int t = 0;

  public void setParams(Map<String,Object> params) {
    this.params = params;
    paramSet = false;
  }

  private void setParams() {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
    if (params != null && params.containsKey("WINDOW_SIZE")) {
      winsize = (Integer)params.get("WINDOW_SIZE");
    } else {
      winsize = config.getParamInt("param", "fft", "WINDOW_SIZE");
      params.put("WINDOW_SIZE", winsize);
    }
    if (params != null && params.containsKey("SHIFT")) {
      shift = (Double)params.get("SHIFT");
    } else {
      shift = config.getParamDouble("param", "fft", "SHIFT");
      params.put("SHIFT", shift);
    }
    if (params != null && params.containsKey("TARGET_CHANNEL")) {
      chTarget = (Integer)params.get("TARGET_CHANNEL");
    } else {
      chTarget = 0;
      params.put("TARGET_CHANNEL", 0);
    }
    paramSet = true;
  }

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
                      List<TimeSeriesCompatible> dest) 
    throws InterruptedException {
    if (!paramSet) setParams();
    