package jp.crestmuse.cmx.inference;
import jp.crestmuse.cmx.inference.models.*;
import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.exception.*;
import java.util.*;

public class BasicGACalculator<E,O> implements MusicCalculator {


  private MusicRepresentation mr;
  private String targetLayer;
  private int targetTie;
  private List<Object> targetLabels;
  private Map<Object,Integer> targetLabelMap;
  private GACalculator<E,O> gacalc;
  private int length;
  private GeneticAlgorithm ga;
  private int initNum;
  private int popLimit;
  private double elitRate;
  
  public BasicGACalculator(String targetLayer,
                           GACalculator<E,O> gacalc,
                           int length,
                           int initialNum, int popLimit, double elitRate,
                           CrossoverPolicy crossPolicy, double crossRate,
                           double mutRate,
                           SelectionPolicy selPolicy, 
                           MusicRepresentation mr) {
    this.mr = mr;
    this.targetLayer = targetLayer;
    this.gacalc = gacalc;
    this.length = length;
    ga = new GeneticAlgorithm(crossPolicy, crossRate,
                              new MyMutation(), mutRate,
                              selPolicy);
    this.initNum = initialNum;
    this.popLimit = popLimit;
    this.elitRate = elitRate;
    targetTie = mr.getTiedLength(targetLayer);
    targetLabels = Arrays.asList(mr.getLabels(targetLayer));
    targetLabelMap =  new HashMap<Object,Integer>();
    for (int i = 0; i < targetLabels.size(); i++) {
      targetLabelMap.put(targetLabels.get(i), i);
    }
  }

  public void updated(int measure, int tick, String layer,
                      MusicRepresentation mr) {
    List<O> observed = new ArrayList<O>();
    List<MusicElement> me = new ArrayList<MusicElement>();
    int obsTie = mr.getTiedLength(layer);
    for (int i = 0; i < length; i += obsTie) {
      MusicElement e = mr.getMusicElement(layer, measure, tick+i);
      me.add(e);
      observed.add((O)e.getMostLikely());
    }
    List<Chromosome> chroms = new ArrayList<Chromosome>();
    for (int i = 0; i < initNum; i++) {
      List<E> initial = gacalc.createInitial(length / targetTie);
      if (initial == null)
        initial = createInitialDefault(length / targetTie);
      chroms.add(new MyChromosome(initial, observed, me, gacalc));
    }
    Population pop1 = new ElitisticListPopulation(chroms, popLimit, elitRate);
    Population pop2 =
      ga.evolve(pop1, new MyStoppingCondition(gacalc.getStoppingCondition(),
                                              me, gacalc));
    MyChromosome ch = (MyChromosome)pop2.getFittestChromosome();
    List<E> rep = ch.getRepresentation();
    for (int i = 0; i < length; i += targetTie) {
      MusicElement e = mr.getMusicElement(targetLayer, measure, tick+i);
      e.setEvidence(rep.get(i / targetTie));
    }
  }
  
  private E getRandomLabel() {
    return (E)targetLabels.get((int)(Math.random() * targetLabels.size()));
  }
    
  private List<E> createInitialDefault(int length) {
    List<E> list = new ArrayList<E>();
    for (int i = 0; i < length; i++) {
      list.add(getRandomLabel());
    }
    return list;
  }


  private class MyChromosome extends AbstractListChromosome<E> {
    List<O> observed;
    List<MusicElement> me;
    GACalculator<E,O> gacalc;
    MyChromosome(List<E> rep, List<O> observed, List<MusicElement> me,
                 GACalculator<E,O> gacalc) {
      super(rep);
      this.observed = observed;
      this.me = me;
      this.gacalc = gacalc;
    }
    protected void checkValidity(List<E> rep) {
      // do nothing
    }
    public MyChromosome newFixedLengthChromosome(List<E> rep) {
      return new MyChromosome(rep, observed, me, gacalc);
    }
    public double fitness() {
      return gacalc.calcFitness(getRepresentation(), observed, me);
    }
    public List<E> getRepresentation() {
      return super.getRepresentation();
    }
  }

  private class MyStoppingCondition implements StoppingCondition {
    private StoppingCondition mystop;
    int generation = 0;
    List<MusicElement> me;
    GACalculator<E,O> gacalc;
    MyStoppingCondition(StoppingCondition stop, List<MusicElement> me,
                        GACalculator<E,O> gacalc) {
      mystop = stop;
      this.me = me;
      this.gacalc = gacalc;
    }
    public boolean isSatisfied(Population pop) {
      gacalc.populationUpdated(pop, ++generation, me);
      return mystop.isSatisfied(pop);
    }
  }

  private class MyMutation implements MutationPolicy {
    public Chromosome mutate(Chromosome original)
      throws MathIllegalArgumentException {
      MyChromosome c = (MyChromosome)original;
      List<E> newrep = new ArrayList<E>(c.getRepresentation());
      int i = GeneticAlgorithm.getRandomGenerator().nextInt(c.getLength());
      newrep.set(i, getRandomLabel());
      return c.newFixedLengthChromosome(newrep);
    }
  }
  
}
