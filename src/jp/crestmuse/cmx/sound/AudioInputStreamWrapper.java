package jp.crestmuse.cmx.sound;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.*;
import jp.crestmuse.cmx.math.*;

public class AudioInputStreamWrapper implements AudioDataCompatible {
  private TargetDataLine line;
  private AudioInputStream audioin;
  private AudioFormat fmt;
  private int channels;
  private int sampleRate;
  private int bitsPerSample;
  private int bytesPerSample;
  private byte[] buff = null;
  private double[] cache = null;
  private int next = 0;
  private static final int CACHE_SIZE = 65536;

  private static final DoubleArrayFactory factory =
    DoubleArrayFactory.getFactory();
  

  AudioInputStreamWrapper(TargetDataLine line) {
    this.line = line;
    audioin = new AudioInputStream(line);
    fmt = audioin.getFormat();
    channels = fmt.getChannels();
    sampleRate = (int)fmt.getSampleRate();
    bitsPerSample = fmt.getSampleSizeInBits();
    bytesPerSample = bitsPerSample / 8;
  }

  public AudioInputStream getAudioInputStream() {
    return audioin;
  }

  public TargetDataLine getLine() {
    return line;
  }

  public int channels() {
    return channels;
  }

  public int sampleRate() {
    return sampleRate;
  }

  public AudioFormat getAudioFormat() {
    return fmt;
  }

  public DoubleArray[] getDoubleArrayWaveform() {
    throw new UnsupportedOperationException();
  }

  public byte[] getByteArrayWaveform() {
    throw new UnsupportedOperationException();
  }

  public boolean supportsRandomAccess() {
    return false;
  }

  public DoubleArray[] read(long microsecond, int sampleSize) {
    throw new UnsupportedOperationException();
  }

//  public boolean supportsWholeWaveformGetter() {
//    return false;
//  }

  /** 8 bit only */


  public DoubleArray[] readNext(int sampleSize, int nOverlap) 
    throws IOException {
    DoubleArray[] array = new DoubleArray[channels];
    for (int i = 0; i < array.length; i++)
      array[i] = factory.createArray(sampleSize);
    if (cache == null) {
      cache = new double[CACHE_SIZE];
      nOverlap = 0;
    }
    int n = sampleSize - nOverlap;
    if (buff == null || buff.length < n * channels)
      buff = new byte[bytesPerSample * n * channels];
    for (int i = 0; i < nOverlap; i++) 
      for (int ch = 0; ch < channels; ch++) 
        array[ch].set(i, cache[next + (i - nOverlap) * channels + ch]);
    audioin.read(buff, 0, bytesPerSample * n * channels);
    ByteBuffer bb = ByteBuffer.wrap(buff);
    for (int i = 0; i < n; i++) {
      for (int ch = 0; ch < channels; ch++) {
        if (bitsPerSample == 8) {
          int b = bb.get() + 128;
//          int b = buff[i * channels + ch] + 128;
          if (b >= 128) b -= 256;
          cache[next + i * channels + ch] = (double)b / 256;
          //  = (double)(buff[i * channels + ch] ) ;
        } else if (bitsPerSample == 16) {
          cache[next + i * channels + ch] = (double)bb.getShort();
        } else {
          throw new UnsupportedOperationException("Unsupported audio format: " + fmt);
        }
        array[ch].set(nOverlap + i, cache[next + i * channels + ch]);
      }
    }
    next += n * channels;
      if (next >= CACHE_SIZE - sampleSize) {
        System.arraycopy(cache, next - sampleSize, cache, 0, sampleSize);
        next = sampleSize;
      }
    return array;
  }
          

/*
  public DoubleArray[] readNext(int sampleSize, int nOverlap) 
    throws IOException {
    DoubleArray[] array = new DoubleArray[channels];
    for (int i = 0; i < array.length; i++)
      array[i] = factory.createArray(sampleSize);
    if (cache == null) {
      if (buff == null || buff.length < sampleSize * channels)
        buff = new byte[sampleSize * channels];
      cache = new double[CACHE_SIZE];
      audioin.read(buff, 0, sampleSize * channels);
      for (int i = 0; i < sampleSize; i++) {
	  for (int ch = 0; ch < channels; ch++) {
	      int b = buff[i * channels + ch] + 128;
	      if (b >= 128) b -= 256;
	      cache[next + i * channels + ch] = (double)b/256;
		  //= (double)(buff[i * channels + ch] ) ;
          array[ch].set(i, cache[next + i * channels + ch]);
        }
      }
      next += sampleSize * channels;
      return array;
    } else {
      int n = sampleSize - nOverlap;
      if (buff == null || buff.length < n * channels)
        buff = new byte[n * channels];
      for (int i = 0; i < nOverlap; i++) 
        for (int ch = 0; ch < channels; ch++) 
          array[ch].set(i, cache[next + (i - nOverlap) * channels + ch]);
      audioin.read(buff, 0, n * channels);
      for (int i = 0; i < n; i++) {
        for (int ch = 0; ch < channels; ch++) {
	    int b = buff[i * channels + ch] + 128;
	    if (b >= 128) b -= 256;
	    cache[next + i * channels + ch] = (double)b / 256;
	      //  = (double)(buff[i * channels + ch] ) ;
          array[ch].set(nOverlap + i, cache[next + i * channels + ch]);
        }
      }
      next += n * channels;
      if (next >= CACHE_SIZE - sampleSize) {
        System.arraycopy(cache, next - sampleSize, cache, 0, sampleSize);
        next = sampleSize;
      }
    }
    return array;
  }
*/

          
  public boolean hasNext(int sampleSize) {
    return line.isOpen();
  }


  public static AudioInputStreamWrapper createWrapper8(int fs) 
    throws LineUnavailableException {
    AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 
                                      (float)fs, 8, 1, 1, (float)fs, true);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
    TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
    line.open(fmt);
    AudioInputStreamWrapper audio = new AudioInputStreamWrapper(line);
    return audio;
  }

  public static AudioInputStreamWrapper createWrapper16(int fs)
    throws LineUnavailableException {
    AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                                      (float)fs, 16, 1, 2, (float)fs, true);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
    TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
    line.open(fmt);
    AudioInputStreamWrapper audio = new AudioInputStreamWrapper(line);
    return audio;
  }
}