package jp.crestmuse.cmx.filewrappers;
import java.util.*;
import java.nio.*;
import org.w3c.dom.*;
import org.apache.commons.codec.binary.*;

public abstract class ByteArrayNodeInterface extends NodeInterface {
  private int bytesize;
  private ByteBuffer buff;
  private static final Base64 base64 = new Base64();

  protected ByteArrayNodeInterface(Node node) {
    super(node);
    bytesize = getAttributeInt("bytesize");
    buff = ByteBuffer.wrap(base64.decodeBase64(getText().getBytes()));
  }

  protected final int lengthInByte() {
    return bytesize;
  }

  protected final int lengthInShort() {
    return bytesize / 2;
  }

  protected final int lengthInInt() {
    return bytesize / 4;
  }

  protected byte[] getByteArray() {
    byte[] array = new byte[lengthInByte()];
    buff.get(array);
//    for (int i = 0; i < array.length; i++)
//      array[i] = buff.get();
    return array;
  }

  protected short[] getUnsignedByteArray() {
    short[] array = new short[lengthInByte()];
    for (int i = 0; i < array.length; i++) {
      byte b = buff.get();
      array[i] = b < 0 ? (short)(b + 128) : b;
    }
    return array;
  }

  protected short[] getShortArray() {
    short[] array = new short[lengthInShort()];
    for (int i = 0; i < array.length; i++)
      array[i] = buff.getShort();
    return array;
  }

  protected int[] getUnsignedShortArray() {
    int[] array = new int[lengthInShort()];
    for (int i = 0; i < array.length; i++) {
      short s = buff.getShort();
      array[i] = s < 0 ? s + 32768 : s;
    }
    return array;
  }

  protected int[] getIntArray() {
    int[] array = new int[lengthInInt()];
    for (int i = 0; i < array.length; i++)
      array[i] = buff.getInt();
    return array;
  }

  protected void setByteOrder(ByteOrder bo) {
    buff.order(bo);
  }

  public static void addByteArrayToWrapper(byte[] data, String nodename, 
                                           CMXFileWrapper wrapper) {
    String s = new String(base64.encodeBase64Chunked(data));
    wrapper.addChild(nodename);
    wrapper.setAttribute("bytesize", data.length);
    wrapper.addText(s);
  }
}