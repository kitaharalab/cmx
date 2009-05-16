package jp.crestmuse.cmx.misc;
import java.io.*;

public class PrintWriterWrapper implements Printable {
    private PrintWriter w;
    public PrintWriterWrapper(PrintWriter w) {
	this.w = w;
    }
    public void print(String s) {
	w.print(s);
    }
    public void println(String s) {
	w.println(s);
    }
}
