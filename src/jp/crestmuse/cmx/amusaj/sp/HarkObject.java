package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.*;
import java.io.*;
import java.nio.*;

public class HarkObject {

  public class HD_Header {
    public int type;
    public int advance;
    public int count;
    public long tv_sec;
    public long tv_usec;
    private HD_Header(int type, int advance, int count, 
                       long tv_sec, long tv_usec) {
      this.type = type;
      this.advance = advance;
      this.count = count;
      this.tv_sec = tv_sec;
      this.tv_usec = tv_usec;
    }
    public String toString() {
      return "type: " + type + ", advance: " + advance +
        ", count: " + count + 
        ", tv_sec: " + tv_sec + ", tv_usec: " + tv_usec;
    }
  }

  public class HDH_MicData {
    public int nch;
    public int length;
    public int data_bytes;
    private HDH_MicData(int nch, int length, int data_bytes) {
      this.nch = nch;
      this.length = length;
      this.data_bytes = data_bytes;
    }
    public String toString() {
      return "nch: " + nch + ", length: " + length + 
        ", data_bytes: " + data_bytes;
    }
  }

  public class HDH_SrcInfo {
    public int src_d;
    public float[] x;
    public float power;
    private HDH_SrcInfo(int src_d, float x, float y, float z, float power) {
      this.src_d = src_d;
      this.x = new float[3];
      this.x[0] = x;
      this.x[1] = y;
      this.x[2] = z;
      this.power = power;
    }
    public String toString() {
      return "src_d: " + src_d + ", x: [" + x[0] + ", " + x[1] + ", " + 
        x[2] + "], power:" + power;
    }
  }

  public class HDH_SrcData {
    public int length;
    public int data_bytes;
    private HDH_SrcData(int length, int data_bytes) {
      this.length = length;
      this.data_bytes = data_bytes;
    }
    public String toString() {
      return "length: " + length + ", data_bytes: " + data_bytes;
    }
  }

  public class SrcObject {
    public HDH_SrcInfo info;        // (h)
    public HDH_SrcData wave_head;   // (i)
    public float[] wave_data;       // (j)    // 表ではshort[]になっている。要注意
    public HDH_SrcData fft_head;    // (k)
    public float[] fft_real;        // (l)
    public float[] fft_imag;        // (m)
    public HDH_SrcData feature_head; // (n)
    public float[] feature_data;    // (o)
    public HDH_SrcData reliability_head; // (p)
    public float[] reliability_data; // (q)

    private SrcObject(InputStream in) throws IOException {
      info = new HDH_SrcInfo(readInt(in), readFloat(in), readFloat(in), 
                             readFloat(in), readFloat(in));
      if (hasSrcWave()) {
        wave_head = new HDH_SrcData(readInt(in), readInt(in));
        wave_data = readFloatArray(in, wave_head.data_bytes);
      } else {
        wave_head = null;
        wave_data = null;
      }
      if (hasSrcFFT()) {
        fft_head = new HDH_SrcData(readInt(in), readInt(in));
        fft_real = readFloatArray(in, fft_head.data_bytes);
        fft_imag = readFloatArray(in, fft_head.data_bytes);
      } else {
        fft_head = null;
        fft_real = null;
        fft_imag = null;
      }
      if (hasSrcFeature()) {
        feature_head = new HDH_SrcData(readInt(in), readInt(in));
        feature_data = readFloatArray(in, feature_head.data_bytes);
      } else {
        feature_head = null;
        feature_data = null;
      }
      if (hasSrcReliability()) {
        reliability_head = new HDH_SrcData(readInt(in), readInt(in));
        reliability_data = readFloatArray(in, reliability_head.data_bytes);
      } else {
        reliability_head = null;
        reliability_data = null;
      }
    }

    public DoubleArray getSrcWave() {
      return new MyDoubleArray(wave_head, wave_data);
    }

    public ComplexArray getSrcFFT() {
      return new MyComplexArray(fft_head, fft_real, fft_imag);
    }

    public DoubleArray getSrcFeature() {
      return new MyDoubleArray(feature_head, feature_data);
    }

    public DoubleArray getSrcReliability() {
      return new MyDoubleArray(reliability_head, reliability_data);
    }
  }

  private class MyDoubleArray extends AbstractDoubleArrayImpl {
    private HDH_SrcData head;
    private float[] data;
    private MyDoubleArray(HDH_SrcData head, float[] data) {
      this.head = head;
      this.data = data;
    }
    public int length() {
      return head.length;
    }
    public double get(int index) {
      return data[index];
    }
    public void set(int index, double value) {
      data[index] = (float)value;
    }
  }

  private class MyComplexArray extends AbstractComplexArrayImpl {
    private HDH_SrcData head;
    private float[] real, imag;
    private MyComplexArray(HDH_SrcData head, float[] real, float[] imag) {
      this.head = head;
      this.real = real;
      this.imag = imag;
    }
    public int length() {
      return head.length;
    }
    public double getReal(int index) {
      return real[index];
    }
    public double getImag(int index) {
      return imag[index];
    }
    public void setReal(int index, double value) {
      real[index] = (float)value;
    }
    public void setImag(int index, double value) {
      imag[index] = (float)value;
    }
  }

  private class MyDoubleArray2 extends AbstractDoubleArrayImpl {
    private HDH_MicData head;
    private int ch;
    private float[] data;
    private MyDoubleArray2(HDH_MicData head, int ch, float[] data) {
      this.head = head;
      this.ch = ch;
      this.data = data;
    }
    public int length() {
      return head.length;
    }
    public double get(int index) {
      return data[index * head.nch + ch];
    }
    public void set(int index, double value) {
      data[index * head.nch + ch] = (float)value;
    }
  }

  private class MyComplexArray2 extends AbstractComplexArrayImpl {
    private HDH_MicData head;
    private int ch;
    private float[] real, imag;
    private MyComplexArray2(HDH_MicData head, int ch, float[] real, float[] imag) {
      this.head = head;
      this.ch = ch;
      this.real = real;
      this.imag = imag;
    }
    public int length() {
      return head.length;
    }
    public double getReal(int index) {
      return real[index * head.nch + ch];
    }
    public double getImag(int index) {
      return imag[index * head.nch + ch];
    }
    public void setReal(int index, double value) {
      real[index * head.nch + ch] = (float)value;
    }
    public void setImag(int index, double value) {
      imag[index * head.nch + ch] = (float)value;
    }
  }
    
  
  public HD_Header header;           // (a)
  public HDH_MicData mic_wave_head;  // (b)
  public float[] mic_wave_data;      // (c)
  public HDH_MicData mic_fft_head;   // (d)
  public float[] mic_fft_real;       // (e)
  public float[] mic_fft_imag;       // (f)
  public int src_num;                // (g)
  public SrcObject[] src;

  public long music_position = 0;
  
  public HarkObject(InputStream in) throws IOException {
//    System.err.println("hark");
    bytebuff.order(ByteOrder.LITTLE_ENDIAN);
    header = new HD_Header(readInt(in), readInt(in), readInt(in),
                            readLong(in), readLong(in));
    System.err.println(header);
    if (hasMicWave()) {
      mic_wave_head = new HDH_MicData(readInt(in), readInt(in), readInt(in));
      System.err.println("mic_wave_head:" + mic_wave_head);
      mic_wave_data = readFloatArray(in, mic_wave_head.data_bytes);
      System.err.println("mic_wave_data:" + mic_wave_data.length);
    } else {
      mic_wave_head = null;
      mic_wave_data = null;
    }
    if (hasMicFFT()) {
      mic_fft_head = new HDH_MicData(readInt(in), readInt(in), readInt(in));
      System.err.println("mic_fft_head:" + mic_fft_head);
      mic_fft_real = readFloatArray(in, mic_fft_head.data_bytes);
      System.err.println("mic_fft_real:" + mic_fft_real.length);
      mic_fft_imag = readFloatArray(in, mic_fft_head.data_bytes);
      System.err.println("mic_fft_imag:" + mic_fft_imag.length);
    } else {
      mic_fft_head = null;
      mic_fft_real = null;
      mic_fft_imag = null;
    }
    if (hasSrcInfo()) {
      src_num = readInt(in);
      System.err.println(src_num);
      src = new SrcObject[src_num];
      for (int i = 0; i < src_num; i++)
        src[i] = new SrcObject(in);
    } else {
      src_num = 0;
      src = new SrcObject[0];
    }
  }

  private byte[] bytearray = new byte[8];
//  private byte[] bytearray = new byte[4];
  private ByteBuffer bytebuff = ByteBuffer.wrap(bytearray);

  private int readInt(InputStream in) throws IOException {
//    in.read(bytearray);
    in.read(bytearray, 0, 4);
    return bytebuff.getInt(0);
  }

  private long readLong(InputStream in) throws IOException {
//    in.read(bytearray);
    in.read(bytearray, 0, 8);
    return bytebuff.getLong(0);
  }

  private float readFloat(InputStream in) throws IOException {
//    in.read(bytearray);
    in.read(bytearray, 0, 4);
    return bytebuff.getFloat(0);
  }
    

  private float[] readFloatArray(InputStream in, int data_bytes) 
    throws IOException {
//    System.err.println(data_bytes);
      float[] data = new float[data_bytes / 4];
      byte[] bytearray = new byte[data_bytes];
      for (int read_bytes = 0; read_bytes < data_bytes; ) {
//        System.err.println(read_bytes);
        read_bytes += in.read(bytearray, read_bytes, data_bytes - read_bytes);
      }
//      System.err.println("#bytes: " + in.read(bytearray));
//      for (int i = 0; i < bytearray.length; i++)
//        System.err.print(bytearray[i] + "\t" );
//      FloatBuffer floatbuff = FloatBuffer.wrap(bytearray);
      FloatBuffer floatbuff = ByteBuffer.wrap(bytearray).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
//      floatbuff.order(ByteOrder.LITTLE_ENDIAN);
      floatbuff.get(data);
//      ByteBuffer bytebuff = ByteBuffer.wrap(bytearray);
//      bytebuff.order(ByteOrder.LITTLE_ENDIAN);
//      for (int i = 0; i < data.length; i++) 
//        data[i] = bytebuff.getFloat();
      return data;
  }

  public boolean hasMicWave() {
    return (header.type & 1) == 1;
  }

  public boolean hasMicFFT() {
    return (header.type & 2) == 2;
  }

  public boolean hasSrcInfo() {
    return (header.type & 4) == 4;
  }

  public boolean hasSrcWave() {
    return (header.type & 8) == 8;
  }

  public boolean hasSrcFFT() {
    return (header.type & 16) == 16;
  }

  public boolean hasSrcFeature() {
    return (header.type & 32) == 32;
  }

  public boolean hasSrcReliability() {
    return (header.type & 64) == 64;
  }

  public DoubleArray[] getMicWave() {
//    System.err.println("mc_wave_head" + mic_wave_head);
    DoubleArray[] waves = new DoubleArray[mic_wave_head.nch];
    for (int i = 0; i < waves.length; i++)
      waves[i] = new MyDoubleArray2(mic_wave_head, i, mic_wave_data);
    return waves;
  }

  public ComplexArray[] getMicFFT() {
    ComplexArray[] ffts = new ComplexArray[mic_fft_head.nch];
    for (int i = 0; i < ffts.length; i++)
      ffts[i] = new MyComplexArray2(mic_fft_head, i, mic_fft_real, mic_fft_imag);
    return ffts;
  }

}
