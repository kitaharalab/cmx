package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import java.io.*;
import java.nio.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import javax.sound.sampled.*;
import org.xml.sax.*;

public class WAVXMLWrapper extends CMXFileWrapper {
  public static final String TOP_TAG = "riff-wave";

  private FmtChunk fmt;
  private DataChunk[] datalist;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  public FmtChunk getFmtChunk() {
    if (fmt == null)
      fmt = new FmtChunk(selectSingleNode("/riff-wave/fmt-chunk"));
    return fmt;
  }

  public DataChunk[] getDataChunkList(){
    if (datalist == null) {
      NodeList nl = selectNodeList("/riff-wave/data-chunk");
      int size = nl.getLength();
      datalist = new DataChunk[size];
      for (int i = 0; i < size; i++)
        datalist[i] = new DataChunk(nl.item(i));
    }
    return datalist;
  }

  public void addFmtChunk(int format, int channels, int sampleRate, 
                     int byteRate, int blockAlign, int bitsPerSample) {
    addChild("fmt-chunk");
    addChildAndText("format", format);
    addChildAndText("channels", channels);
    addChildAndText("sample-rate", sampleRate);
    addChildAndText("byte-rate", byteRate);
    addChildAndText("block-align", blockAlign);
    addChildAndText("bits-per-sample", bitsPerSample);
    returnToParent();
  }

  public void addDataChunk(byte[] data) {
    addChild("data-chunk");
    ByteArrayNodeInterface.addByteArrayToWrapper(data, "audio-data", this);
    returnToParent();
    returnToParent();
  }

  public static WAVXMLWrapper readWAV(String filename) 
    throws IOException, ParserConfigurationException, 
    SAXException, TransformerException {
    WAVXMLWrapper wavxml = 
      (WAVXMLWrapper)createDocument(TOP_TAG);
    DataInputStream datain = new DataInputStream
      (new BufferedInputStream(new FileInputStream(filename)));
    wavxml.readWAV(datain);
    wavxml.finalizeDocument();
    return wavxml;
  }

  private void readWAV(DataInputStream datain) throws IOException {
    byte[] buffID = new byte[4];
    datain.read(buffID);
    if (!(new String(buffID)).equals("RIFF"))
      throw new InvalidFileTypeException();
    readUnsignedInt(datain);
    datain.read(buffID);
    if (!(new String(buffID)).equals("WAVE"))
      throw new InvalidFileTypeException();
    while (datain.read(buffID) != -1) {
      String chunkID = new String(buffID);
      int size = readUnsignedInt(datain);
      byte[] bb = new byte[size];
      datain.read(bb);
      ByteBuffer buffData = ByteBuffer.wrap(bb);
      buffData.order(ByteOrder.LITTLE_ENDIAN);
      if (chunkID.equals("fmt "))
        readFmtChunk(size, buffData);
      else if (chunkID.equals("data"))
        readDataChunk(size, buffData);
    }
  }

  private static int readUnsignedInt(DataInputStream datain) 
						throws IOException {
    int b0 = datain.readUnsignedByte();
    int b1 = datain.readUnsignedByte();
    int b2 = datain.readUnsignedByte();
    int b3 = datain.readUnsignedByte();
    return b0 + 256 * b1 + 65536 * b2 + 16777216 * b3;
  }

  private void readFmtChunk(int size, ByteBuffer buff) throws IOException {
    int format = readUnsignedShort(buff);
    int channels = readUnsignedShort(buff);
    int sampleRate = readUnsignedInt(buff);
    int byteRate = readUnsignedInt(buff);
    int blockAlign = readUnsignedShort(buff);
    int bitsPerSample = readUnsignedShort(buff);
    addFmtChunk(format, channels, sampleRate, byteRate, 
                blockAlign, bitsPerSample);
  }

  private void readDataChunk(int size, ByteBuffer buff) throws IOException {
    byte[] b = new byte[size];
    buff.get(b);
    addDataChunk(b);
  }
    
  private int readUnsignedShort(ByteBuffer buff) throws IOException {
    short s = buff.getShort();
    return s < 0 ? s + 32768 : s;
  }

  private int readUnsignedInt(ByteBuffer buff) throws IOException {
    int i = buff.getInt();
    return i < 0 ? i + 256 * 256 * 256 * 128 : i;
  }

  public void writefileAsWAV(String filename) throws IOException {
    DataOutputStream dataout = new DataOutputStream 
      (new BufferedOutputStream(new FileOutputStream(filename)));
    write(dataout);
  }

  private void write(DataOutputStream dataout) throws IOException {
    int bytesize = 4;
    FmtChunk fmtchunk = getFmtChunk();
    bytesize += fmtchunk.bytesize() + 8;
    DataChunk[] datalist = getDataChunkList();
    for (DataChunk datachunk : datalist)
      bytesize += datachunk.bytesize() + 8;
    dataout.writeBytes("RIFF");
    writeUnsignedInt(bytesize, dataout);
    dataout.writeBytes("WAVE");
    fmtchunk.write(dataout);
    for (DataChunk datachunk : datalist)
      datachunk.write(dataout);
    dataout.close();
  }
                                       
  private static void writeUnsignedShort(int value, DataOutputStream dataout) 
						throws IOException {
    byte b1 = (byte)(value / 256);
    byte b0 = (byte)(value % 256);
    dataout.writeByte(b0);
    dataout.writeByte(b1);
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


  public class FmtChunk extends NodeInterface {
    private int format;
    private int channels;
    private int sampleRate;
    private int byteRate;
    private int blockAlign;
    private int bitsPerSample;

    private FmtChunk(Node node) {
      super(node);
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      for (int i = 0; i < size; i++) {
        Node node1 = nl.item(i);
        String nodename = node1.getNodeName();
        String value = getText(node1);
        if (nodename.equals("format"))
          format = Integer.parseInt(value);
        else if (nodename.equals("channels"))
          channels = Integer.parseInt(value);
        else if (nodename.equals("sample-rate"))
          sampleRate = Integer.parseInt(value);
        else if (nodename.equals("byte-rate"))
          byteRate = Integer.parseInt(value);
        else if (nodename.equals("block-align"))
          blockAlign = Integer.parseInt(value);
        else if (nodename.equals("bits-per-sample"))
          bitsPerSample = Integer.parseInt(value);
      }
    }
    protected String getSupportedNodeName() {
      return "fmt-chunk";
    }
    public final int format() {
      return format;
    }
    public final int channels() {
      return channels;
    }
    public final int sampleRate() {
      return sampleRate;
    }
    public final int byteRate() {
      return byteRate;
    }
    public final int blockAlign() {
      return blockAlign;
    }
    public final int bitsPerSample() {
      return bitsPerSample;
    }
    public AudioFormat getAudioFormat() {
      return new AudioFormat((float)sampleRate, bitsPerSample, 
                             channels, 
                             bitsPerSample == 8 ? false : true, false);
    }
    private int bytesize() {
      return 16;
    }
    private void write(DataOutputStream dataout) throws IOException {
      dataout.writeBytes("fmt ");
      writeUnsignedInt(bytesize(), dataout);
      writeUnsignedShort(format, dataout);
      writeUnsignedShort(channels, dataout);
      writeUnsignedInt(sampleRate, dataout);
      writeUnsignedInt(byteRate, dataout);
      writeUnsignedShort(blockAlign, dataout);
      writeUnsignedShort(bitsPerSample, dataout);
    }
  }

  public class DataChunk extends NodeInterface {
    private AudioData data;
    private DataChunk(Node node) {
      super(node);
      data = new AudioData(getChildByTagName("audio-data"));
    }
    protected String getSupportedNodeName() {
      return "data-chunk";
    }
    public AudioData getAudioData() {
      return data;
    }
    private int bytesize() {
      return data.bytesize();
    }
    private void write(DataOutputStream dataout) throws IOException {
      dataout.writeBytes("data");
      byte[] bb = data.getByteArray();
      dataout.write(bb, 0, bb.length);
    }
  }

  public class AudioData extends ByteArrayNodeInterface {
    private AudioData(Node node) {
      super(node);
      setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }
    protected String getSupportedNodeName() {
      return "audio-data";
    }
    public byte[] getByteArray() {
      return super.getByteArray();
    }
    public short[] getWaveform() {
      FmtChunk fmt = getFmtChunk();
      if (fmt.bitsPerSample() == 8)
        return getUnsignedByteArray();
      else if (fmt.bitsPerSample() == 16)
        return getShortArray();
      else
        throw new InvalidElementException("Unsupported bits/sample");
    }
    public int getZeroValue() {
      if (fmt.bitsPerSample() == 8)
        return 128;
      else if (fmt.bitsPerSample() == 16)
        return 0;
      else
        throw new InvalidElementException("Unsupported bits/sample");
    }
    private int bytesize() {
      return lengthInByte();
    }
    public DoubleArray[] getDoubleArrayWaveform() {
      short[] wav = getWaveform();
      FmtChunk fmt = getFmtChunk();
      int ch = fmt.channels();
      int length = (int)(wav.length / ch);
      DoubleArray[] data = new DoubleArray[ch];
      for (int i = 0; i < ch; i++)
        data[i] = factory.createArray(length);
      for (int t = 0; t < length; t++)
        for (int i = 0; i < ch; i++)
          data[i].set(t, (double)(wav[t*ch+i] - getZeroValue()) / 
                      (double)(Math.pow(2, fmt.bitsPerSample())));
      return data;
    }
  }
    
}