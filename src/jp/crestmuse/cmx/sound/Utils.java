package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.math.*;
import javax.sound.sampled.*;

public class Utils {
  public static AudioDataCompatible excerpt(AudioDataCompatible audiodata,int from,int thru){
    return new MyAudioData(audiodata, from, thru);
  }

  private static class MyAudioData implements AudioDataCompatible {
    AudioDataCompatible org;
    int from;
    int thru;
    DoubleArray[] waveform = null;
    MyAudioData(AudioDataCompatible audiodata, int from, int thru) {
      if (!audiodata.supportsWholeWaveformGetter())
        throw new IllegalStateException();
      org = audiodata;
      this.from = from;
      this.thru = thru;
      DoubleArray[] w = org.getDoubleArrayWaveform();
      waveform = new DoubleArray[w.length];
      for (int i = 0; i < w.length; i++)
        waveform[i] = w[i].subarrayX(from, thru);
    }
    public int channels() {
      return org.channels();
    }
    public int sampleRate() {
      return org.sampleRate();
    }
    public AudioFormat getAudioFormat() {
      return org.getAudioFormat();
    }
    public DoubleArray[] getDoubleArrayWaveform() {
      return waveform;
    }
    public byte[] getByteArrayWaveform() {
      throw new UnsupportedOperationException();
    }
    private int next = 0;
    public DoubleArray[] readNext(int sampleSize, int nOverlap) {
      DoubleArray[] array = new DoubleArray[channels()];
      for (int i = 0; i < array.length; i++)
        array[i]=waveform[i].subarrayX(next, next+sampleSize);
      next += sampleSize - nOverlap;
      return array;
    }
    public boolean hasNext(int sampleSize) {
      return next + sampleSize <= waveform[0].length();
    }
    public boolean supportsWholeWaveformGetter() {
      return true;
//      return org.supportsWholeWaveformGetter();
    }
  }
  
}