package jp.crestmuse.cmx.inference.models;
import jp.crestmuse.cmx.inference.*;
import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.distribution.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import java.util.*;

class HMMContWithGAImpl extends HMMContImpl {
  private GeneticAlgorithm ga;
  private int num;
  private int popLimit;
  private double elitRate;
  private GACalculator gacalc;

  HMMContWithGAImpl(GACalculator<Integer,ObservationReal> gacalc,
                    int initialNum, int popLimit, double elitRate,
                    CrossoverPolicy crossoverPolicy, double crossoverRate,
                    MutationPolicy mutationPolicy, double mutationRate,
                    SelectionPolicy selectionPolicy) {
    this(null, null, null, gacalc, initialNum, popLimit, elitRate,
         crossoverPolicy, crossoverRate, mutationPolicy, mutationRate,
         selectionPolicy);
  }

  HMMContWithGAImpl(double[] pi, double[][] a,
                     List<? extends RealDistribution> b,
                    GACalculator<Integer,ObservationReal> gacalc,
                    int initialNum, int popLimit, double elitRate,
                    CrossoverPolicy crossoverPolicy, double crossoverRate,
                    MutationPolicy mutationPolicy, double mutationRate,
                    SelectionPolicy selectionPolicy) {
    super(pi, a, b);
    ga = new GeneticAlgorithm(crossoverPolicy, crossoverRate,
                              mutationPolicy, mutationRate,
                              selectionPolicy);
    num = initialNum;
    this.popLimit = popLimit;
    this.elitRate = elitRate;
    this.gacalc = gacalc;
  }
    
  public int[] mostLikelyStateSequence(List<Double> o,
                                       List<MusicElement> e) {
    //    List<ObservationReal> obsList = new ArrayList<ObservationReal>();
    //    for (Double d : o)
    //      obsList.add(new ObservationReal(d));
    List<Chromosome> chromList = new ArrayList<Chromosome>();
    for (int i = 0; i < num; i++) 
      chromList.add(new MyChromosome(gacalc.createInitial(o.size()), o, e));
    Population pop1 = new ElitisticListPopulation(chromList, popLimit, elitRate);
    Population pop2 =
      ga.evolve(pop1, new MyStoppingCondition(gacalc.getStoppingCondition(),e));
    MyChromosome ch = (MyChromosome)pop2.getFittestChromosome();
    Integer[] array = ch.getRepresentation().toArray(new Integer[0]);
    int[] array2 = new int[array.length];
    for (int i = 0; i < array.length; i++)
      array2[i] = array[i];
    return array2;
  }

  public void calcForwardBackward(List<Double> o, List<MusicElement> e) {
    if (hmm != null)
      super.calcForwardBackward(o, e);
    else
      throw new UnsupportedOperationException();
  }

  public double getForwardProb(int t, int i) {
    if (hmm != null)
      return super.getForwardProb(t, i);
    else
      throw new UnsupportedOperationException();
  }

  public double getBackwardProb(int t, int i) {
    if (hmm != null)
      return super.getBackwardProb(t, i);
    else
      throw new UnsupportedOperationException();
  }

  public double calcProb(List<Double> o, int[] states, List<MusicElement> e) {
    if (hmm != null)
      return super.calcProb(o, states, e);
    else
      throw new UnsupportedOperationException();
  }
  
  private class MyChromosome extends BinaryChromosome {
    List<Double> obs;
    List<MusicElement> e;
    MyChromosome(List<Integer> states, List<Double> obs,
                 List<MusicElement> e) {
      super(states);
      this.obs = obs;
      this.e = e;
    }
    protected void checkValidity(List<Integer> representation) {
      // do nothing
    }
    public MyChromosome newFixedLengthChromosome(List<Integer> representation) {
      return new MyChromosome(representation, obs, e);
    }
    public double fitness() {
      return gacalc.calcFitness(getRepresentation(), obs, e);
    }
    public List<Integer> getRepresentation() {
      return super.getRepresentation();
    }
  }

  private class MyStoppingCondition implements StoppingCondition {
    private StoppingCondition mystop;
    int generation = 0;
    List<MusicElement> e;
    MyStoppingCondition(StoppingCondition stop, List<MusicElement> e) {
      mystop = stop;
      this.e = e;
    }
    public boolean isSatisfied(Population pop) {
      gacalc.populationUpdated(pop, ++generation, e);
      return mystop.isSatisfied(pop);
    }
  }
      

  
}
