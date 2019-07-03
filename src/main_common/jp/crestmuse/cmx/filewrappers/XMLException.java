package jp.crestmuse.cmx.filewrappers;

public class XMLException extends RuntimeException {
  Exception org;
  public XMLException(Exception e) {
    super(e.toString());
    org = e;
  }
}
