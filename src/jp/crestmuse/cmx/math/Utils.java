package jp.crestmuse.cmx.math;
import java.util.*;
import java.math.*;

public class Utils {
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final DoubleMatrixFactory mfactory = 
    DoubleMatrixFactory.getFactory();

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
    return toString(x, ", ", "{", "}");
  }

  public static String toString2(ComplexArray x) {
    return toString(x, " ", "", "");
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
        appendRowString(x, 0, sep1, sb);
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

    public static final DoubleArray create1dimDoubleArray(double x) {
	DoubleArray array = factory.createArray(1);
	array.set(0, x);
	return array;
    }

}