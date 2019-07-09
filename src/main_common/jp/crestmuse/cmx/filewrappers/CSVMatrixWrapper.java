package jp.crestmuse.cmx.filewrappers;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.DoubleMatrix;
import jp.crestmuse.cmx.math.Operations;
import jp.crestmuse.cmx.misc.PrintStreamWrapper;
import jp.crestmuse.cmx.misc.PrintWriterWrapper;
import jp.crestmuse.cmx.misc.Printable;

public class CSVMatrixWrapper implements FileWrapperCompatible {

    private DoubleMatrix matrix;

    public CSVMatrixWrapper(DoubleMatrix matrix) {
	this.matrix = matrix;
    }

    public CSVMatrixWrapper(DoubleArray array) {
	this(Operations.toMatrixV(array));
    }

    public String getFileName() {
	throw new UnsupportedOperationException();
    }

    public void write(OutputStream out) throws IOException {
	write(new PrintStreamWrapper(new PrintStream(out)));
    }

    public void write(Writer writer) throws IOException {
	write(new PrintWriterWrapper(new PrintWriter(writer)));
    }

    public void writefile(File file) throws IOException {
	write(new PrintStreamWrapper(new PrintStream(file)));
    }

    public void writeGZippedFile(File file) throws IOException {
	write(new GZIPOutputStream(new BufferedOutputStream
				   (new FileOutputStream(file))));
    }

    private void write(Printable p) throws IOException {
	p.println(matrix.encode());
    }
}
							   
