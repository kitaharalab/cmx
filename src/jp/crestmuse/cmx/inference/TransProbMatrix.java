package jp.crestmuse.cmx.inference;
import static java.lang.Math.*;
import java.util.*;

public class TransProbMatrix {
  private double[] probs;
  private Object[] labels;
  private int n;

  public TransProbMatrix(Object[] labels) {
    this.labels = labels;
    n = labels.length;
    probs = new double[n * n];
    for (int i = 0; i < n * n; i++)
      probs[i] = 1.0 / (double)n;
  }

  public TransProbMatrix(List<Object> labels) {
    this(labels.toArray());
  }

//  public void set(Object from, Object to, double value) {
//    probs[indexOf(from) * n + indexOf(to)] = value;
//  }

  public void set(Object from, double[] values) {
    int i = indexOf(from);
    for (int j = 0; j < n; j++)
      probs[i * n + j] = values[j];
  }

//  public void setLog(Object from, Object to, double value) {
//    set(from, to, exp(value));
//  }

  public void setLog(Object from, double[] values) {
    int i = indexOf(from);
    for (int j = 0; j < n; j++)
      probs[i * n + j] = exp(values[i]);
  }

//  public double get(Object from, Object to) {
//    return probs[indexOf(from) * n + indexOf(to)];
//  }

//  public double[] get(Object from) {
//    int i = indexOf(from);
//    double[] values = new double[n];
//    for (int j = 0; j < n; j++)
//      values[j] = probs[i * n + j];
//    return values;
//  }

//  public double getLog(Object from, Object to) {
//    return log(get(from, to));
//  }

//  public double[] getLog(Object from) {
//    int i = indexOf(from);
//    double[] values = new double[n];
//    for (int j = 0; j < n; j++)
//      values[j] = log(probs[i * n + j]);
//    return values;
//  }

  double getLog(int i, int j) {
    return log(probs[i * n + j]);
  }

  double get(int i, int j) {
    return probs[i * n + j];
  }

  private int indexOf(Object label) {
    for (int i = 0; i < n; i++) {
      if (label.equals(labels[i])) 
        return i;
    }
    throw new IllegalStateException("no label: " + label);
  }
}

  