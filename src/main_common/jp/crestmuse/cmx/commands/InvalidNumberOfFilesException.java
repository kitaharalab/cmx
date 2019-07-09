package jp.crestmuse.cmx.commands;
import java.io.IOException;

public class InvalidNumberOfFilesException extends IOException {
  InvalidNumberOfFilesException() {
    super();
  }
  InvalidNumberOfFilesException(String s) {
    super(s);
  }
}