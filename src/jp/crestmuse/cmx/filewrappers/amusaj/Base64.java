package jp.crestmuse.cmx.filewrappers.amusaj;

public class Base64 {
  private static final org.apache.commons.codec.binary.Base64 base64 = 
    new org.apache.commons.codec.binary.Base64();

  public static String encode(byte[] array) {
    return new String(base64.encodeBase64Chunked(array));
  }

  public static byte[] decode(String text) {
    return base64.decodeBase64(text.getBytes());
  }
}