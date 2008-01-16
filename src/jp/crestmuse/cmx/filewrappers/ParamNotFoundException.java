package jp.crestmuse.cmx.filewrappers;

public class ParamNotFoundException extends IllegalStateException {

  public ParamNotFoundException() {
    super();
  }

  public ParamNotFoundException(String s) {
    super(s);
  }
}