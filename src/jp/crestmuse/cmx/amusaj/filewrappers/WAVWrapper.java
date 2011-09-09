package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.sound.*;
import java.io.*;
import java.nio.*;
import javax.sound.sampled.*;

/*********************************************************************
 *The <tt>WAVWrapper</tt> class wraps a WAV file.
 *********************************************************************/
public class WAVWrapper implements FileWrapperCompatible,AudioDataCompatible {
 
  private long filesize;
  private FmtChunk fmt;
  private DataChunk data;
  private String filename = null;

  private static final DoubleArrayFactory dafactory = 
    DoubleArrayFactory.getFactory();

  static final int LINEAR_PCM_FORMAT = 1;

  private WAVWrapper() {
    fmt = new FmtChunk();
    data = new DataChunk();
  }

  public WAVWrapper(DoubleArray[] wav, int sampleRate) throws IOException {
    this();
    setWaveform(wav, sampleRate);
  }

    public WAVWrapper(AudioDataCompatible wav) 
	throws IOException {
	this(wav.getDoubleArrayWaveform(), wav.sampleRate());
    }

  private void setWaveform(DoubleArray[] waveform, int sampleRate) {
      fmt.setDefault(waveform.length, sampleRate);
      data.setWaveform(waveform);
      filesize = 4 + 4 + fmt.size + 4 + data.size;
  }

  /*******************************************************************
   *Returns the sampling rate.
   *******************************************************************/
  public int sampleRate() {
    return fmt.samplesPerSec;
  }

//  public boolean supportsWholeWaveformGetter() {
//    return true;
//  }

  public boolean supportsRandomAccess() {
    return true;
  }

  /*******************************************************************
   *Returns the waveform.
   *******************************************************************/
  public DoubleArray[] getDoubleArrayWaveform() {
    return data.getWaveform();
  }

  public int channels() {
    return data.getWaveform().length;
  }

  public byte[] getByteArrayWaveform() {
    return data.getByteArray();
//    return data.bytearray;
  }

  public AudioFormat getAudioFormat() {
    return fmt.getAudioFormat();
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

/*  
  public DoubleArray[] readNext(int sampleSize) {
    DoubleArray[] array = new DoubleArray[channels()];
    for (int i = 0; i < array.length; i++)
      array[i] = data.getWaveform()[i].subarrayX(next, next + sampleSize);
    next += sampleSize;
    return array;
//    double[] array = new double[channels()];
//    for (int i = 0; i < array.length; i++)
//      array[i] = data.getWaveform()[i].get(next);
//    next++;
//    return array;
  }

  public void readNext(int sampleSize, DoubleArray[] x, int index) {
    DoubleArray[] w = data.getWaveform();
    for (int ch = 0; ch < x.length; ch++) 
      for (int t = 0; t < sampleSize; t++) 
        x[ch].set(t + index, w[ch].get(t + next));
  }

  public boolean hasNext(int sampleSize) {
    return next + sampleSize <= data.getWaveform()[0].length();
  }

  public DoubleArray[] getLast(int sampleSize) {
    DoubleArray[] array = new DoubleArray[channels()];
    for (int i = 0; i < array.length; i++)
      array[i] = data.getWaveform()[i].subarrayX(next - sampleSize, next);
    return array;
  }

  public void getLast(int sampleSize, DoubleArray[] x, int index) {
    DoubleArray[] w = data.getWaveform();
    for (int ch = 0; ch < x.length; ch++)
      for (int t = 0; t < sampleSize; t++)
        x[ch].set(t + index, w[ch].get(t + next - sampleSize));
  }
*/


/*
  public AmusaDoubleArray getDoubleArrayWaveform(int ch) {
    return data.getWaveform()[ch];
  }

  public AmusaDoubleArray[] getWaveformInFs(int fs) {
    AmusaDoubleArray[] w = new AmusaDoubleArray[data.getWaveform().length];
    for (int i = 0; i < data.getWaveform().length; i++)
      w[i] = data.getWaveform()[i].changeRate((int)getSampleRate(), fs);
    return w;
  }

  public AmusaDoubleArray getWaveformInFs(int ch, int fs) {
    return data.getWaveform()[ch].changeRate((int)getSampleRate(), fs);
  }
*/

  public String getFileName() {
    return filename;
  }

  /*******************************************************************
   *Reads the specified file.
   *******************************************************************/
  public static WAVWrapper readfile(String filename) throws IOException {
    WAVWrapper wav = new WAVWrapper();
    DataInputStream datain = new DataInputStream(
        new BufferedInputStream(new FileInputStream(filename)));
    wav.filename = filename;
    wav.read2(datain);
    return wav;
  }

  public static WAVWrapper read(InputStream input) throws IOException {
    WAVWrapper wav = new WAVWrapper();
    DataInputStream datain = new DataInputStream(
      new BufferedInputStream(input));
    wav.filename = null;
    wav.read2(datain);
    return wav;
  }

  private void read2(DataInputStream datain) throws IOException {
//      System.err.print("Reading the wave file ");
    readRIFFHeader(datain);
    byte[] buff = new byte[4];
    while (datain.read(buff) != -1) {
      String header = new String(buff);
      if (header.equals("fmt ")) {
        fmt.read(datain);
      } else if (header.equals("data")) {
        data.read(datain);
      } else {
        readDummyData(datain);
      }
    }
    datain.close();
    //  prog.finish();
//      System.err.println("complete.");
  }

  private void readRIFFHeader(DataInputStream datain) throws IOException {
    byte[] buff = new byte[4];
    datain.read(buff);
    if (!(new String(buff)).equals("RIFF")) {
      throw new InvalidFileTypeException();
    }
    filesize = readUnsignedInt(datain);
    datain.read(buff);
    if (!(new String(buff)).equals("WAVE")) {
      throw new InvalidFileTypeException();
    }
  }

  private static int readUnsignedShort(DataInputStream datain) 
						throws IOException {
    int b0 = datain.readUnsignedByte();
    int b1 = datain.readUnsignedByte();
    return b0 + 256 * b1;
  }

  private static int readSignedShort(DataInputStream datain)
						throws IOException {
    int b0 = datain.readUnsignedByte();
    int b1 = datain.readUnsignedByte();
    int n = b0 + 256 * b1;
    return (n >= 32768) ? n - 65536 : n;
  }

  private static long readUnsignedInt(DataInputStream datain) 
						throws IOException {
    int b0 = datain.readUnsignedByte();
    int b1 = datain.readUnsignedByte();
    int b2 = datain.readUnsignedByte();
    int b3 = datain.readUnsignedByte();
    return b0 + 256 * b1 + 65536 * b2 + 16777216 * b3;
  }

  private void readDummyData(DataInputStream datain) throws IOException {
    long size = readUnsignedInt(datain);
    byte[] buff = new byte[(int)size];
    datain.read(buff);
  }

  /*******************************************************************
   *Writes the waveform to the specified file.
   ******************************************************************/
  public void writefile(String filename) throws IOException {
    writefile(new File(filename));
  }

  public void writefile(File file) throws IOException {
//    DataOutputStream dataout = new DataOutputStream(
//      new BufferedOutputStream(new FileOutputStream(file)));
////    DataOutputStream dataout = 
////      new DataOutputStream(new FileOutputStream(filename));
    write(new FileOutputStream(file));
//    System.err.println("complete.");
  }

  public void write(OutputStream out) throws IOException {
    write(new DataOutputStream(new BufferedOutputStream(out)));
  }
  
  private void write(DataOutputStream dataout) throws IOException {
    writeRIFFHeader(dataout);
    fmt.write(dataout);
    data.write(dataout);
    dataout.close();
  }

  public void write(Writer writer) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void writeGZippedFile(File file) throws IOException {
    throw new UnsupportedOperationException();
  }

  private void writeRIFFHeader(DataOutputStream dataout) throws IOException {
    dataout.writeBytes("RIFF");
    writeUnsignedInt(filesize, dataout);
    dataout.writeBytes("WAVE");
  }

  private static void writeUnsignedShort(int value, DataOutputStream dataout) 
						throws IOException {
    byte b1 = (byte)(value / 256);
    byte b0 = (byte)(value % 256);
    dataout.writeByte(b0);
    dataout.writeByte(b1);
  }

  private static void writeSignedShort(int value, DataOutputStream dataout)
						throws IOException {
    if (value < 0) value += 65536;
    writeUnsignedShort(value, dataout);
  }

  private static void writeUnsignedInt(long value, DataOutputStream dataout)
						throws IOException {
    byte b3 = (byte)(value / 16777216);
    byte b2 = (byte)((value % 16777216) / 65536);
    byte b1 = (byte)((value % 65536) / 256);
    byte b0 = (byte)(value % 256);
    dataout.writeByte(b0);
    dataout.writeByte(b1);
    dataout.writeByte(b2);
    dataout.writeByte(b3);
  }

/*
  public WAVAccessorForStream getAccessorForStream() {
    short[] dataInShort = data.getShortArray();
    AudioFormat fmt = new AudioFormat(
        (float)(this.fmt.samplesPerSec), 
        16, this.fmt.channels, true, true);
    return new WAVAccessorForStream(dataInShort, fmt);
  }
*/

  private class FmtChunk {
    private long size;
    private int format;
    private int channels;
    private int samplesPerSec;
    private long bytesPerSec;
    private int blocksize;
    private int bitsPerSample;
    private byte[] ext;

    private void setDefault(int ch, int sampleRate) {
      size = 16;
      format = LINEAR_PCM_FORMAT;
      channels = ch;
      samplesPerSec = sampleRate;
      bitsPerSample = 16;
      bytesPerSec = samplesPerSec * channels * (bitsPerSample / 8);
      blocksize = channels * (bitsPerSample / 8);
    }

    private AudioFormat getAudioFormat() {
      return new AudioFormat((float)samplesPerSec, bitsPerSample, 
                             channels, 
                             bitsPerSample == 8 ? false : true, false);
    }

    private void read(DataInputStream datain) throws IOException {
      size = readUnsignedInt(datain);
      format = readUnsignedShort(datain);
      channels = readUnsignedShort(datain);
      samplesPerSec = (int)readUnsignedInt(datain);
      bytesPerSec = readUnsignedInt(datain);
      blocksize = readUnsignedShort(datain);
      bitsPerSample = readUnsignedShort(datain);
      if (size > 16) {
        ext = new byte[(int)size - 16];
        datain.read(ext);
      }
    }

    private void write(DataOutputStream dataout) throws IOException {
      dataout.writeBytes("fmt ");
      writeUnsignedInt(size, dataout);
      writeUnsignedShort(format, dataout);
      writeUnsignedShort(channels, dataout);
      writeUnsignedInt(samplesPerSec, dataout);
      writeUnsignedInt(bytesPerSec, dataout);
      writeUnsignedShort(blocksize, dataout);
      writeUnsignedShort(bitsPerSample, dataout);
      if (size > 16) {
        dataout.write(ext);
      }
    }
  }

  private class DataChunk {
    private long size;
    private DoubleArray[] data = null;
    private short[] dataInShort = null;
    private byte[] bytearray= null;

    private void setWaveform(DoubleArray[] waveform) {
      data = waveform;
      size = fmt.blocksize * waveform[0].length();
    }

    private DoubleArray[] getWaveform() {
      if (data == null) {
        int length = (int)(size / fmt.blocksize);
        data = new DoubleArray[fmt.channels];
        for (int i = 0; i < fmt.channels; i++)
          data[i] = dafactory.createArray(length);
        for (int t = 0; t < length; t++) 
          for (int i = 0; i < fmt.channels; i++) 
            data[i].set(t, (double)dataInShort[t*fmt.channels+i] / 32768.0);
      }
      return data;
    }

    private void read(DataInputStream datain) throws IOException {
      if (fmt.format != LINEAR_PCM_FORMAT)
        throw new InvalidFileTypeException("unsupported waveform type");
      size = readUnsignedInt(datain);
      int length = (int)(size / fmt.blocksize);
      bytearray = new byte[(int)size];
      datain.read(bytearray);
      ByteBuffer buff = ByteBuffer.wrap(bytearray);
      buff.order(ByteOrder.LITTLE_ENDIAN);
      dataInShort = new short[length * fmt.channels];
      if (fmt.bitsPerSample == 8)
        for (int t = 0; t < length; t++) 
          for (int i = 0; i < fmt.channels; i++) {
            byte b = buff.get();
            if (b >= 0)
              dataInShort[t * fmt.channels + i] = (short)(b - 128);
            else
              dataInShort[t * fmt.channels + i] = (short)(b + 128);
//            dataInShort[t * fmt.channels + i] = (short)(buff.get() - 128);
          }
      else if (fmt.bitsPerSample == 16)
        for (int t = 0; t < length; t++)
          for (int i = 0; i < fmt.channels; i++)
            dataInShort[t * fmt.channels + i] = buff.getShort();
//    
////      prog.setTotalLength(length / 66536);
////      data = new AmusaDoubleArray[fmt.channels];
//      dataInShort = new short[length * fmt.channels];
////      for (int i = 0; i < fmt.channels; i++)
////        data[i] = AmusaDoubleArray.createArray(length);
//      for (int t = 0; t < length; t++) {
////        if (t % 65536 == 0) prog.next();
////        if (t % 65536 == 0) System.err.print(".");
//        for (int i = 0; i < fmt.channels; i++) {
//          int sample;
//          if (fmt.bitsPerSample == 8) {
//            sample = 256 * (datain.readUnsignedByte() - 128);
////            data[i].set(t, (double)(datain.readUnsignedByte()-128) / 128.0);
//          } else if (fmt.bitsPerSample == 16) {
//            sample = readSignedShort(datain);
////            data[i].set(t, (double)readSignedShort(datain) / 32768.0);
//          } else {
//            throw new InvalidFileTypeException("unsupported waveform type");
//          }
//          dataInShort[t * fmt.channels + i] = (short)sample;
////          data[i].set(t, (double)sample / 32768.0);
//        }
//      }
    }

    private short[] getShortArray() {
      if (dataInShort == null) {
        dataInShort = new short[getWaveform()[0].length() * data.length];
        for (int t = 0; t < getWaveform()[0].length(); t++) 
          for (int i = 0; i < getWaveform().length; i++) 
            dataInShort[t * i] = (short)(getWaveform()[i].get(i) * 32768.0);
      }
      return dataInShort;
    }

    private byte[] getByteArray() {
      if (bytearray == null) {
        short[] shortarray = getShortArray();
        bytearray = new byte[shortarray.length * 2];
        ByteBuffer bb = ByteBuffer.wrap(bytearray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < shortarray.length; i++) 
          bb.putShort(shortarray[i]);
      }
      return bytearray;
    }

    private void write(DataOutputStream dataout) throws IOException {
      if (fmt.format != LINEAR_PCM_FORMAT)
        throw new InvalidFileTypeException("unsupported waveform type");
      dataout.writeBytes("data");
      writeUnsignedInt(size, dataout);
      int length = (int)(size / fmt.blocksize);
      for (int t = 0; t < length; t++) {
//        if (t % 65536 == 0) System.err.print(".");
        for (int i = 0; i < fmt.channels; i++) {
          if (fmt.bitsPerSample == 8)
            dataout.writeByte((int)((getWaveform()[i].get(t) * 128) + 128));
          else if (fmt.bitsPerSample == 16)
            writeSignedShort((int)(getWaveform()[i].get(t) * 32768), dataout);
          else
            throw new InvalidFileTypeException();
        }
      }
    }
  }

/*
  public class WAVAccessorForStream {
    private short[] dataInShort;
    private AudioFormat fmt;

    private WAVAccessorForStream(short[] dataInShort, AudioFormat fmt) {
      this.dataInShort = dataInShort;
      this.fmt = fmt;
    }

    public short[] getShortArray() {
      return dataInShort;
    }

    public AudioFormat getAudioFormat() {
      return fmt;
    }
  }

  public class UnsupportedWAVFileException extends IOException {
    public UnsupportedWAVFileException() {
      super();
    }
    public UnsupportedWAVFileException(String s) {
      super(s);
    }
  }
*/
}