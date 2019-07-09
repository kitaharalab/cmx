package jp.crestmuse.cmx.inference.models;

import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;

import java.util.List;

import jp.crestmuse.cmx.inference.MusicElement;

public interface GACalculator<E,O> {
  List<E> createInitial(int size);
  double calcFitness(List<E> s, List<O> o, List<MusicElement> e);
  void populationUpdated(Population pop, int generation, List<MusicElement> e);
  StoppingCondition getStoppingCondition();
}
