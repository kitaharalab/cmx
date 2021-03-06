package jp.crestmuse.cmx.sound;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import jp.crestmuse.cmx.math.DoubleArray;

public interface AudioDataCompatible {
  int channels();
  int sampleRate();
  DoubleArray[] getDoubleArrayWaveform();
  byte[] getByteArrayWaveform();
  AudioFormat getAudioFormat();
  DoubleArray[] readNext(int sampleSize, int nOverlap) throws IOException;
//  DoubleArray[] readNext(int sampleSize);
//  void readNext(int sampleSize, DoubleArray[] wav, index);
//  DoubleArray[] getLast(int sampleSize);
//  void getLast(int sampleSize, DoubleArray[] wav, index);
  boolean hasNext(int sampleSize);

  DoubleArray[] read(long microsecond, int sampleSize) throws IOException;

  boolean supportsRandomAccess();
//  boolean supportsWholeWaveformGetter();
}
