package jp.crestmuse.cmx.amusaj.sp;

public class SPException extends RuntimeException {
  Exception e;

  public SPException() {
    super();
  }
  public SPException(String s) {
    super(s);
  }
  public SPException(Exception e) {
    super(e.toString());
    this.e = e;
  }
  public void printStackTrace() {
    if (e == null)
      super.printStackTrace();
    else
      e.printStackTrace();
  }
}
