package jp.crestmuse.cmx.inference;
import java.util.*;
import static java.lang.Math.*;

public class NaiveMusicCalculator {
  private String stateLayer, emitLayer;
  private int m, n;
  private Object[] stateLabels, emitLabels;
  private double[] probs;

  public NaiveMusicCalculator(String stateLayer, Object[] stateLables, 
                              String emitLayer, Object[] emitLabels) {
    this.stateLayer = stateLayer;
    this.emitLayer = emitLayer;
    this.stateLabels = stateLabels;
    this.emitLabels = emitLabels;
    m = stateLabels.length;
    n = stateLabels.length;
    probs = new double[m * n];
    for (int i = 0; i < m * n; i++)
      probs[i] = 1.0 / (double)n;
  }


//  public double getLog(Object state, Object emit) {
//    return log(getLog(state, emit));
//  }

//  public double get(Object state, Object emit) {
//    return get(indexOfState(state), indexOfEmission(emit));
//  }

  public void set(int stateIndex, double[] values) {
    for (int j = 0; j < n; j++)
      probs[stateIndex * m + j] = values[j];
  }

  public void setLog(int stateIndex, double[] values) {
    for (int j = 0; j < n; j++)
      probs[stateIndex * m + j] = log(values[j]);
  }

  double get(int stateIndex, int emitIndex) {
    return probs[stateIndex * m + emitIndex];
  }

  double getLog(int stateIndex, int emitIndex) {
    return log(probs[stateIndex * m + emitIndex]);
  }

  private int indexOfState(Object label) {
    for (int i = 0; i < m; i++)
      if (label.equals(stateLabels[i]))
        return i;
    throw new IllegalStateException("no label: " + label);
  }

  private int indexOfEmission(Object label) {
    for (int i = 0; i < n; i++)
      if (label.equals(emitLabels[i]))
        return i;
    throw new IllegalStateException("no label: " + label);
  }
                              
  public void updated(int measure, int tick, String layer, 
                      MusicRepresentation mr) {
    
  }
}