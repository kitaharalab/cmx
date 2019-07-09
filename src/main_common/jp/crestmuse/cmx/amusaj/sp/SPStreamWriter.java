package jp.crestmuse.cmx.amusaj.sp;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jp.crestmuse.cmx.misc.Encodable;

public class SPStreamWriter extends SPModule {
    private BufferedWriter writer;

    public SPStreamWriter(BufferedWriter writer) {
	this.writer = writer;
    }

    public SPStreamWriter(Writer writer) {
	this(new BufferedWriter(writer));
    }

    public SPStreamWriter(OutputStream out) {
	this(new BufferedWriter(new OutputStreamWriter(out)));
    }	     

    public void execute(Object[] src, TimeSeriesCompatible[] dest) {
	try {
	    if (src[0] instanceof Encodable) {
		writer.write(((Encodable)src[0]).encode());
		writer.newLine();
		writer.flush();
	    } else {
		throw new IllegalStateException();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	
	}
    }

    public Class[] getInputClasses() {
	return new Class[] {Encodable.class };
    }

    public Class[] getOutputClasses() {
	return new Class[0];
    }
}
	
	