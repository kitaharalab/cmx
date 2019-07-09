package jp.crestmuse.cmx.misc;
import java.util.NoSuchElementException;

public class EmptyQueueException extends NoSuchElementException {
  public EmptyQueueException() {
    super();
  }
  public EmptyQueueException(String s) {
    super(s);
  }
}