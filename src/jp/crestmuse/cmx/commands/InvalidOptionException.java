package jp.crestmuse.cmx.commands;

/*********************************************************************
 *Thrown when the user speficied invalid option(s). <br>
 *ユーザが不正なオプションを指定したときにスローされる例外です. 
 *********************************************************************/ 
public class InvalidOptionException extends Exception {

  InvalidOptionException() {
    super("Invalid option(s).");
  }

  InvalidOptionException(String message) {
    super(message);
  }
}
