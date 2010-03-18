package jp.crestmuse.cmx.inference;

public interface PriorProbCalculator {

  public double calcPriorProb(Object label, Object prevlabel, 
			    MusicRepresentation2 mr, 
			    int measure, int tick);

}