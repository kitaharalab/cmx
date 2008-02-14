package jp.crestmuse.cmx.misc;

public class XMLException extends RuntimeException {
  Exception org;
  public XMLException(Exception e) {
    super(e.toString());
  }
}
