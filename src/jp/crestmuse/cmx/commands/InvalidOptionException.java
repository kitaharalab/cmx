package jp.crestmuse.cmx.commands;

/*********************************************************************
 *Thrown when the user speficied invalid option(s). <br>
 *$B%f!<%6$,IT@5$J%*%W%7%g%s$r;XDj$7$?$H$-$K%9%m!<$5$l$kNc30$G$9(B. 
 *********************************************************************/ 
class InvalidOptionException extends Exception {

  InvalidOptionException() {
    super("Invalid option(s).");
  }

  InvalidOptionException(String message) {
    super(message);
  }
}