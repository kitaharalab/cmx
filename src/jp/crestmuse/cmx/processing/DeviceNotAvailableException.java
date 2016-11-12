package jp.crestmuse.cmx.processing;

public class DeviceNotAvailableException extends RuntimeException {
  public DeviceNotAvailableException() {
    super();
  }

  public DeviceNotAvailableException(String s) {
    super(s);
  }
}
