package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.math.*;
import javax.sound.sampled.*;

public interface AudioDataCompatible {
  int channels();
  int sampleRate();
  DoubleArray[] getDoubleArrayWaveform();
  byte[] getByteArrayWaveform();
  AudioFormat getAudioFormat();
  double[] next();
  boolean hasNext();
  boolean supportsWholeWaveformGetter();
}
