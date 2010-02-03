package jp.crestmuse.cmx.math;
import java.util.*;
import java.math.*;
import org.apache.commons.math.linear.*;
import java.nio.*;

public class Utils {
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final DoubleMatrixFactory mfactory = 
    DoubleMatrixFactory.getFactory();
  private static final ComplexArrayFactory cfactory = 
      ComplexArrayFactory.getFactory();

  public Utils() { }

  public static DoubleArray cloneArray(DoubleArray x) {
    try {
      return (DoubleArray)x.clone();
    } catch (CloneNotSupportedException e) {
      int length = x.length();
      DoubleArray z = factory.createArray(length);
      for (int i = 0; i < length; i++)
        z.set(i, x.get(i));
      return z;
    }
  }

  public static String toString(DoubleArray x, String sep, 
                                String left, String right) {
    StringBuilder sb = new StringBuilder();
    sb.append(left);
    int length = x.length();
    if (length >= 1) {
      sb.append(String.valueOf(x.get(0)));
      for (int i = 1; i < length; i++) {
        sb.append(sep);
        sb.append(String.valueOf(x.get(i)));
      }
    }
    sb.append(right);
    return sb.toString();
  }

    public static String toString(DoubleArray x) {
	return toString1(x);
    }

  public static String toString1(DoubleArray x) {
    return toString(x, ", ", "{", "}");
  }
         
  public static String toString2(DoubleArray x) {
    return toString(x, " ", "", "");
  }

  public static String toString(ComplexArray x, String sep, 
                                String left, String right) {
    StringBuilder sb = new StringBuilder();
    sb.append(left);
    int length = x.length();
    if (length >= 1) {
      sb.append(String.valueOf(x.getReal(0))).append(" + ");
      sb.append(String.valueOf(x.getImag(0))).append(" i");
      for (int i = 1; i < length; i++) {
        sb.append(sep);
        sb.append(String.valueOf(x.getReal(i))).append(" + ");
        sb.append(String.valueOf(x.getImag(i))).append(" i");
      }
    }
    sb.append(right);
    return sb.toString();
  }

    public static String toString(ComplexArray x) {
	return toString1(x);
    }

  public static String toString1(ComplexArray x) {
    return toString(x, ", ", "{", "}");
  }

  public static String toString2(ComplexArray x) {
    return toString(x, " ", "", "");
  }

    public static String toString(DoubleMatrix x) {
	return toString1(x);
    }

  public static String toString(DoubleMatrix x, String sep1, String sep2, 
                                String left, String right) {
    StringBuilder sb = new StringBuilder();
    sb.append(left);
    int nrows = x.nrows();
    if (nrows >= 1) {
      appendRowString(x, 0, sep1, sb);
      for (int i = 1; i < nrows; i++) {
        sb.append(sep2);
        appendRowString(x, i, sep1, sb);
      }
    }
    sb.append(right);
    return sb.toString();
  }

  private static void appendRowString(DoubleMatrix x, int i, 
                                        String sep, StringBuilder sb) {
    int ncols = x.ncols();
    if (ncols >= 1) {
      sb.append(String.valueOf(x.get(i, 0)));
      for (int j = 1; j < ncols; j++) {
        sb.append(sep);
        sb.append(String.valueOf(x.get(i, j)));
      }
    }
  }
    
  public static String toString1(DoubleMatrix x) {
    return toString(x, ", ", ";\n", "{", "}");
  }

  public static String toString2(DoubleMatrix x) {
    return toString(x, " ", "\n", "", "");
  }

  public static DoubleArray parseArray(String text, String sep) {
    String[] ss = text.trim().split(sep);
    int dim = ss.length;
    DoubleArray array = factory.createArray(dim);
    for (int i = 0; i < dim; i++)
      array.set(i, Double.parseDouble(ss[i]));
    return array;
  }

  public static DoubleArray parseArray(String text) {
    return parseArray(text, " ");
  }

  public static DoubleMatrix parseMatrix(String text, String sep1, 
                                         String sep2) {
    String[] ss = text.trim().split(sep2);
    int nrows = ss.length;
    int ncols = ss[0].trim().split(sep1).length;
    DoubleMatrix matrix = mfactory.createMatrix(nrows, ncols);
    for (int i = 0; i < nrows; i++) {
      String[] sss = ss[i].split(sep1);
      if (ncols != sss.length) 
        throw new IllegalStateException("inconsistent data size");
      for (int j = 0; j < ncols; j++)
        matrix.set(i, j, Double.parseDouble(sss[j]));
    }
    return matrix;
  }

  public static DoubleMatrix parseMatrix(String text) {
    return parseMatrix(text, " ", "\n");
  }

    public static final DoubleArray createDoubleArray(int length) {
	return factory.createArray(length);
    }

    public static final DoubleArray createDoubleArray(double[] x) {
    	return factory.createArray(x);
    }

    public static final DoubleArray createDoubleArray(List<BigDecimal> x) {
	int length = x.size();
	DoubleArray array = createDoubleArray(length);
	for (int i = 0; i < length; i++)
	    array.set(i, x.get(i).doubleValue());
	return array;
    }

    public static final ComplexArray createComplexArray(int length) {
	return cfactory.createArray(length);
    }

    public static final ComplexArray createComplexArray(double[] re, 
							  double[] im) {
	return cfactory.createArray(re, im);
    }

    public static final ComplexArray 
	createComplexArray(List<BigDecimal> re, List<BigDecimal> im) {
	int length = re.size();
	ComplexArray array = createComplexArray(length);
	for (int i = 0; i < length; i++) 
	    array.set(i, re.get(i).doubleValue(), im.get(i).doubleValue());
	return array;
    }

    public static final DoubleMatrix createDoubleMatrix(int nrows, int ncols) {
	return mfactory.createMatrix(nrows, ncols);
    }

    public static final DoubleMatrix createDoubleMatrix(List<List<BigDecimal>> x){
	int nrows = x.size();
	int ncols = x.get(0).size();
	DoubleMatrix matrix = createDoubleMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++) {
	    List<BigDecimal> l = x.get(i);
	    for (int j = 0; j < ncols; j++) 
		matrix.set(i, j, l.get(j).doubleValue());
	}
	return matrix;
    }

    public static final DoubleArray create1dimDoubleArray(double x) {
	DoubleArray array = factory.createArray(1);
	array.set(0, x);
	return array;
    }

    public static final RealMatrix toRealMatrix(DoubleMatrix x) {
	return new MyRealMatrix(x);
    }

    private static class MyRealMatrix extends AbstractRealMatrix { 
	private DoubleMatrix x;
	private MyRealMatrix(DoubleMatrix x) {
	    super();
	    this.x = x;
	}
	public void addToEntry(int row, int column, double increment) {
	    x.set(row, column, x.get(row, column) + increment);
	}
	public RealMatrix copy() {
	    try {
		return new MyRealMatrix((DoubleMatrix)x.clone());
	    } catch (CloneNotSupportedException e) {
		throw new UnsupportedOperationException();
	    }
	}
	public RealMatrix createMatrix(int rowDimension, int columnDimension) {
	    return new Array2DRowRealMatrix(rowDimension, columnDimension);
	}
	public int getColumnDimension() {
	    return x.ncols();
	}
	public double getEntry(int row, int column) {
	    return x.get(row, column);
	}
	public int getRowDimension() {
	    return x.nrows();
	}
	public void multiplyEntry(int row, int column, 
				  double factor){
	    x.set(row, column, x.get(row, column) * factor);
	}
	public void setEntry(int row, int column, double value) {
	    x.set(row, column, value);
	}
    }

/*
  public static void copyTo(DoubleArray array, 
						DoubleBuffer buff) {
    if (array instanceof DefaultDoubleArray) {
      ((DefaultDoubleArray)array).copyTo(DoubleBuffer buff);
    } else {
      int length = array.length();
      for (int i = 0; i < length; i++)
	buff.put(array.get(i));
    }
  }
*/

    public static DoubleMatrix toDoubleMatrix(RealMatrix x) {
	return new MyDoubleMatrix(x);
    }

    private static class MyDoubleMatrix extends AbstractDoubleMatrixImpl {
	private RealMatrix x;
	private MyDoubleMatrix(RealMatrix x) {
	    this.x = x;
	}
	public int nrows() {
	    return x.getRowDimension();
	}
	public int ncols() {
	    return x.getColumnDimension();
	}
	public double get(int i, int j) {
	    return x.getEntry(i, j);
	}
	public void set(int i, int j, double value) {
	    try {
		x.setEntry(i, j, value);
	    } catch (MatrixIndexException e) {
		throw new MathException(e);
	    }
	}
    }
}
	
	