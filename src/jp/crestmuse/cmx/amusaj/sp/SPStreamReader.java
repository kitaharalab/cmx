package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import java.io.*;
import java.util.*;

public class SPStreamReader extends SPModule {
    private AmusaDecoder decoder = AmusaDecoder.getInstance();
    private BufferedReader reader;
    private String fmt;
    private int dim;

    public SPStreamReader(BufferedReader reader, String fmt, int dim) {
	this.reader = reader;
	this.fmt = fmt;
	this.dim = dim;
    }

    public SPStreamReader(Reader reader, String fmt, int dim) {
	this(new BufferedReader(reader), fmt, dim);
    }

    public SPStreamReader(InputStream in, String fmt, int dim) {
	this(new BufferedReader(new InputStreamReader(in)), fmt, dim);
    }

    public void execute(Object[] src, TimeSeriesCompatible[] dest) 
    throws InterruptedException {
	try {
	    String line = reader.readLine();
	    if (line.equals("")) {
		dest[0].add(SPTerminator.getInstance());
		return;
	    }
	    StringTokenizer st = new StringTokenizer(line);
	    Object o = decoder.decode(st, fmt, dim);
	    dest[0].add(o);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public Class[] getInputClasses() {
	return new Class[0];
    }

    public Class[] getOutputClasses() {
	return new Class[] {decoder.getClassFor(fmt)};
    }
}
	
	