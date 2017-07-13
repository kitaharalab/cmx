package jp.crestmuse.cmx.inference.models;
import java.util.*;
import jp.crestmuse.cmx.inference.*;
import org.apache.commons.math3.genetics.*;

public interface GACalculator<E,O> {
  List<E> createInitial(int size);
  double calcFitness(List<E> s, List<O> o, List<MusicElement> e);
  void populationUpdated(Population pop, int generation, List<MusicElement> e);
  StoppingCondition getStoppingCondition();
}
