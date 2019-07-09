package jp.crestmuse.cmx.misc;
import java.io.PrintStream;

public class PrintStreamWrapper implements Printable {
    private PrintStream ps;
    public PrintStreamWrapper(PrintStream s) {
	this.ps = s;
    }
    public void print(String s) {
	ps.print(s);
    }
    public void println(String s) {
	ps.println(s);
    }
}
