package jp.crestmuse.cmx.filewrappers;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

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
							   
