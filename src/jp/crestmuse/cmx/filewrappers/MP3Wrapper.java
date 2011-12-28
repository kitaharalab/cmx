package jp.crestmuse.cmx.filewrappers;

import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.math.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.sound.sampled.*;
import javazoom.jl.decoder.*;
import static jp.crestmuse.cmx.math.MathUtils.*;
import static javazoom.jl.decoder.Header.*;

public class MP3Wrapper implements AudioDataCompatible {

  public List<short[]> data = new ArrayList<short[]>();
  public DoubleArray[] dataInDouble;
  public byte[] bytearray;
  public Header header = null;
  public int ch;
  public int sampleRate = -1;

  public MP3Wrapper(InputStream input) throws DecoderException,BitstreamException {
    Bitstream stream = new Bitstream(input);
    Decoder decoder = new Decoder();
    Header header;
    while ((header = stream.readFrame()) != null) {
      if (this.header == null) this.header = header;
      SampleBuffer buff = (SampleBuffer)decoder.decodeFrame(header, stream);
      data.add(buff.getBuffer().clone());
      stream.closeFrame();
    } 

    int nFrames = data.size();
    int blocksize = decoder.getOutputBlockSize();
    int mode = this.header.mode();
    if (mode == SINGLE_CHANNEL) {
      ch = 1;
    } else if (mode == STEREO || mode == JOINT_STEREO || 
               mode == DUAL_CHANNEL) {
      ch = 2;
    } else {
      throw new IllegalArgumentException("Unsupported MP3");
    }
    dataInDouble = new DoubleArray[ch];
    for (int i = 0; i < ch; i++)
      dataInDouble[i] = createDoubleArray(nFrames * blocksize / ch);
    bytearray = new byte[nFrames * blocksize * 2];
    ByteBuffer bb = ByteBuffer.wrap(bytearray);
    bb.order(ByteOrder.LITTLE_ENDIAN);
//    ByteBuffer bb = ByteBuffer.allocate(nFrames * blocksize * 2);

    int k = 0;
    for (int i = 0; i < nFrames; i++) {
      for (int j = 0; j < blocksize; j++) {
        dataInDouble[k % ch].set(k / ch, (double)data.get(i)[j] / 32768);
        bb.putShort(data.get(i)[j]);
//        bytearray[2 * k] = (byte)(data.get(i)[j] / 256);
//        bytearray[2 * k + 1] = (byte)(data.get(i)[j] % 256);
        k++;
      }
    }
  }

  public static MP3Wrapper readfile(String filename) throws IOException,DecoderException,BitstreamException {
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(filename));
    return new MP3Wrapper(input);
  }

  public static MP3Wrapper read(InputStream input) throws IOException, DecoderException,BitstreamException {
    BufferedInputStream input2 = new BufferedInputStream(input);
    return new MP3Wrapper(input2);
  }
    

  public int channels() {
    return ch;
  }

  public int sampleRate() {
    if (sampleRate < 0) {
      String s = header.sample_frequency_string();
      if (s.endsWith("kHz")) {
        sampleRate = 
          (int)(Double.parseDouble(
                  s.substring(0, s.indexOf("kHz")).trim()) * 1000);
      } else {
        throw new IllegalArgumentException();
      }
    }
    return sampleRate;
  }

  public DoubleArray[] getDoubleArrayWaveform() {
    return dataInDouble;
  }

  public byte[] getByteArrayWaveform() {
    return bytearray;
  }
 
  public DoubleArray[] read(long microsecond, int sampleSize) {
    DoubleArray[] array = new DoubleArray[channels()];
    int from = (int)(sampleRate() * microsecond / 1000000);
    for (int i = 0; i < array.length; i++)
      array[i] = getDoubleArrayWaveform()[i].subarrayX(from, from+sampleSize);
    return array;
  }

  private int next = 0;

  public DoubleArray[] readNext(int sampleSize, int nOverlap) {
    DoubleArray[] array = new DoubleArray[channels()];
    for (int i = 0; i < array.length; i++)
      array[i] = getDoubleArrayWaveform()[i].subarrayX(next, next + sampleSize);
    next += sampleSize - nOverlap;
    return array;
  }

  public boolean hasNext(int sampleSize) {
    return next + sampleSize <= getDoubleArrayWaveform()[0].length();
  }

  public boolean supportsRandomAccess() {
    return true;
  }

  public AudioFormat getAudioFormat() {
    return new AudioFormat(sampleRate(), 16, ch, true, false);
  }
}