package jp.crestmuse.cmx.inference;

public interface PriorProbCalculator {

  public double calcPriorProb(Object label, Object prevlabel, 
			    MusicRepresentation mr, 
			    int measure, int tick);

}