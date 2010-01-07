package jp.crestmuse.cmx.inference;

import be.ac.ulg.montefiore.run.jahmm.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class HMMViterbiDecoder extends SPModule {
  private Hmm<ObservationVector> hmm;
  private List<ObservationVector>[] lists;
  private Class[] inClasses, outClasses;
  private int dim1, dim2;

  public HMMViterbiDecoder(Hmm<ObservationVector> hmm, int dim1, int dim2) {
    this.hmm = hmm;
    this.dim1 = dim1;
    this.dim2 = dim2;
    lists = new List[dim2];
    for (int i = 0; i < dim2; i++)
      lists[i] = new ArrayList<ObservationVector>();
    inClasses = new Class[dim1];
    for (int i = 0; i < dim1; i++)
      inClasses[i] = DoubleArray.class;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)  
    throws InterruptedException {
    for (int j = 0; j < dim2; j++) {
      DoubleArray x = (DoubleArray)src[0];
      ObservationVector v = new ObservationVector(x.toArray());
      lists[j].add(v);
    }
  }
  
  public void terminated(TimeSeriesCompatible[] dest) {
    try {
      int[][] states = new int[dim2][];
      for (int j = 0; j < dim2; j++)
        states[j] = hmm.mostLikelyStateSequence(lists[j]);
      for (int t = 0; t < states[0].length; t++)
        dest[0].add(new MyIntArray(states, t));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public Class[] getInputClasses() {
    return inClasses;
  }

  public Class[] getOutputClasses() {
    return new Class[] { IntArray.class };
  }

  static class MyIntArray implements IntArray {
    private int[][] values;
    private int index;
    MyIntArray(int[][] values, int index) {
      this.values = values;
      this.index = index;
    }
    public final int length() {
      return values.length;
    }
    public final void set(int i, int value) {
      values[i][index] = value;
    }
    public final int get(int i) {
      return values[i][index];
    }
    public String encode() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length(); i++) 
        sb.append(get(i)).append(" ");
      return sb.toString();
    }
    public int[] toArray() {
      throw new UnsupportedOperationException();
    }
  }

/*
  private class MyIntMatrix implements IntMatrix {
    int[][] values;
    MyIntMatrix(int[][] values) {
      this.values = values;
    }
    public int nrows() {
      return values.length;
    }
    public int ncols() {
      return values[0].length;
    }
    public int get(int i, int j) {
      return values[i][j];
    }
    public void set(int i, int j, int value) {
      values[i][j] = value;
    }
  }
*/
}