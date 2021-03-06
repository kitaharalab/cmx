package jp.crestmuse.cmx.math;

import org.apache.commons.math.linear.AbstractRealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.MatrixIndexException;
import org.apache.commons.math.linear.RealMatrix;

import java.math.BigDecimal;
import java.util.List;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.type.OctaveDouble;

public class MathUtils {
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final DoubleMatrixFactory mfactory = 
    DoubleMatrixFactory.getFactory();
  private static final ComplexArrayFactory cfactory = 
      ComplexArrayFactory.getFactory();
  private static final ComplexMatrixFactory cmfactory = 
    ComplexMatrixFactory.getFactory();

  public MathUtils() { }

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

  public static String toString(BooleanArray x, String sep, 
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

//  public static void println(DoubleArray x) {
//    System.out.println(toString(x));
//  }

  public static void println(Array x) {
    System.out.println(toString(x));
  }

  public static String toString(Array x) {
    if (x instanceof DoubleArray)
      return toString((DoubleArray)x);
    else if (x instanceof BooleanArray)
      return toString((BooleanArray)x);
    else if (x instanceof ComplexArray)
      return toString((ComplexArray)x);
    else
      throw new UnsupportedOperationException(x.getClass() + ": toString not supported");
  }

  public static String toString(DoubleArray x) {
    return toString1(x);
  }

//  public static void println(BooleanArray x) {
//    System.out.println(toString(x));
//  }

  public static String toString(BooleanArray x) {
    return toString1(x);
  }

  public static String toString1(DoubleArray x) {
    return toString(x, ", ", "{", "}");
  }
         
  public static String toString1(BooleanArray x) {
    return toString(x, ", ", "{", "}");
  }
         
  public static String toString2(DoubleArray x) {
    return toString(x, " ", "", "");
  }

  public static String toString2(BooleanArray x) {
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

//  public static void println(ComplexArray x) {
//    System.out.println(toString(x));
//  }

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

  public static String toString1(ComplexMatrix x) {
    return toString(x, ", ", ";\n", "{", "}");
  }

  public static String toString2(ComplexMatrix x) {
    return toString(x, " ", "\n", "", "");
  }

  public static String toString(ComplexMatrix x, String sep1, String sep2, 
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

  private static void appendRowString(ComplexMatrix x, int i, 
                                      String sep, StringBuilder sb) {
    int ncols = x.ncols();
    if (ncols >= 1) {
      sb.append(String.valueOf(x.getReal(i, 0))).append(" + ").
        append(String.valueOf(x.getImag(i, 0))).append(" i");
      for (int j = 1; j < ncols; j++) {
        sb.append(sep);
        sb.append(String.valueOf(x.getReal(i, j))).append(" + ").
          append(String.valueOf(x.getImag(i, j))).append(" i");
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

  //  public static final DoubleArray array(int length) {
  //    return createDoubleArray(length);
  //  }
  
  public static final DoubleArray createDoubleArray(double[] x) {
    return factory.createArray(x);
  }

  public static final DoubleArray array(double[] x) {
    return createDoubleArray(x);
  }
  
  public static final DoubleArray createDoubleArray(List<? extends Number> x) {
    int length = x.size();
    DoubleArray array = createDoubleArray(length);
    for (int i = 0; i < length; i++) {
      array.set(i, x.get(i).doubleValue());
//          Object x1 = x.get(i);
//          if (x1 instanceof Double) 
//	    array.set(i, ((Double)x1).doubleValue());
//          else if (x1 instanceof BigDecimal)
//            array.set(i, ((BigDecimal)x1).doubleValue());
//          else
//            throw new IllegalArgumentException("Not double value: " + x1);
    }
    return array;
  }

  public static final DoubleArray array(List<? extends Number> x) {
    return createDoubleArray(x);
  }

  public static final DoubleArray createDoubleArrayWithNegativeIndex(int length) {
    return new DefaultDoubleArrayWithNegativeIndex(length);
  }

  public static final DoubleArray createDoubleArrayWithNegativeIndex(double[] x) {
    return new DefaultDoubleArrayWithNegativeIndex(x);
  }

  public static final DoubleArray createDoubleArrayWithNegativeIndex(int length, double value) {
    return new DefaultDoubleArrayWithNegativeIndex(length, value);
  }

  public static final DoubleArray createDoubleArrayWithNegativeIndex(List<? extends Number> x) {
    double[] array = new double[x.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = x.get(i).doubleValue();
    return createDoubleArrayWithNegativeIndex(array);
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

  //  public static final DoubleMatrix mat(int nrows, int ncols) {
  //    return createDoubleMatrix(nrows, ncols);
  //  }
  
  public static final DoubleMatrix
    createDoubleMatrix(List<List<? extends Number>> x){
    int nrows = x.size();
    int ncols = x.get(0).size();
    DoubleMatrix matrix = createDoubleMatrix(nrows, ncols);
    for (int i = 0; i < nrows; i++) {
      List<? extends Number> l = x.get(i);
      for (int j = 0; j < ncols; j++) 
        matrix.set(i, j, l.get(j).doubleValue());
    }
    return matrix;
  }

  public static final DoubleMatrix mat(List<List<? extends Number>> x) {
    return createDoubleMatrix(x);
  }
  
  public static final ComplexMatrix createComplexMatrix(int nrows, int ncols) {
    return cmfactory.createMatrix(nrows, ncols);
  }

  public static final DoubleArray create1dimDoubleArray(double x) {
    DoubleArray array = factory.createArray(1);
    array.set(0, x);
    return array;
  }

  //  public static final DoubleArray arrayOf(double x) {
  //    return create1dimDoubleArray(x);
  //  }
  
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

  public static DoubleMatrix createDoubleMatrixFromArrays(List<DoubleArray> list) {
    return createDoubleMatrixFromArrays(list.toArray(new DoubleArray[0]));
  }

  public static DoubleMatrix array2mat(List<DoubleArray> list) {
    return createDoubleMatrixFromArrays(list);
  }

  public static DoubleMatrix createDoubleMatrixFromArrays(DoubleArray[] list) {
    int dim = list[0].length();
    for (int i = 1; i < list.length; i++) {
      if (list[i].length() != dim)
        throw new MathException("Dimension doesn't match");
    }
    return new MyDoubleMatrix2(list);
  }

  public static DoubleMatrix array2mat(DoubleArray[] list) {
    return createDoubleMatrixFromArrays(list);
  }

  private static class MyDoubleMatrix2 extends AbstractDoubleMatrixImpl {
    private DoubleArray[] list;
    private MyDoubleMatrix2(DoubleArray[] list) {
      this.list = list;
    }
    public int nrows() {
      return list[0].length();
    }
    public int ncols() {
      return list.length;
    }
    public double get(int i, int j) {
      return list[j].get(i);
    }
    public void set(int i, int j, double value) {
      list[j].set(i, value);
    }
  }

  public static OctaveDouble toOctave(DoubleMatrix x) {
    int nrows = x.nrows();
    int ncols = x.ncols();
    double[] array = new double[nrows * ncols];
    for (int i = 0; i < nrows; i++) {
      for (int j = 0; j < ncols; j++) {
        array[i + nrows * j] = x.get(i, j);
      }
    }
    return new OctaveDouble(array, nrows, ncols);
  }

  /*
  public static void toOctave(OctaveEngine octave, String name, DoubleMatrix x) {
    octave.put(name, toOctave(x));
  }
  */
  
  public static DoubleMatrix fromOctave(OctaveDouble x) {
    return new DoubleMatrixImplAsArray(x.getSize()[0], x.getSize()[1], x.getData());
  }

  public static DoubleMatrix fromOctave(OctaveEngine octave, String name) {
    return fromOctave(octave.get(OctaveDouble.class, name));
  }
  
}
	
	
