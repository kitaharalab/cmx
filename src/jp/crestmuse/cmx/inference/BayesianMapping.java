package jp.crestmuse.cmx.inference;

public class BayesianMapping {

  final String layer;
  final int musRepPosition;
  final int option;
  final int bayesnetIndex;
    final boolean evidenceOnly;

  /*
   * 00000xxy xx = 00 => NORMAL xx = 01 => BY_TIED_LENGTH xx = 10 =>
   * MEASURE_HEAD y = 0 => NORMAL y = 1 => SET_ONLY
   */

    public static final int NORMAL = 0;
    //  public static final int SET_ONLY = 1;
    public static final int BY_TIED_LENGTH = 2;
    public static final int MEASURE_HEAD = 4;

    BayesianMapping(String layer, int musRepPosition, int option,
		    int bayesnetIndex, boolean evidenceOnly) {
	this.layer = layer;
	this.musRepPosition = musRepPosition;
	this.option = option;
	this.bayesnetIndex = bayesnetIndex;
	this.evidenceOnly = evidenceOnly;
    }

    public BayesianMapping(String layer, int musRepPosition, int option, 
			   int bayesnetIndex) {
	this(layer, musRepPosition, option, bayesnetIndex, true);
    }

    public BayesianMapping(String layer, int musRepPosition, int option, 
			   String bayesNodeName, BayesNetCompatible bn, 
			   boolean evidenceOnly) {
	this.layer = layer;
	this.musRepPosition = musRepPosition;
	this.option = option;
	this.bayesnetIndex = bn.getNode(bayesNodeName);
	this.evidenceOnly = evidenceOnly;
    }

    public BayesianMapping(String layer, int musRepPosition, int option, 
			   String bayesNodeName, BayesNetCompatible bn) {
	this(layer, musRepPosition, option, bayesNodeName, bn, true);
    }

  protected String labelToString(Object o) {
    return o.toString();
  }


    protected MusicElement mappedElement(MusicRepresentation mr, 
					 int currentMeasure, int currentTick) {
	if (musRepPosition == 0)
	    return mr.getMusicElement(layer, currentMeasure, currentTick);
	int inc = 1;
	if ((option & BY_TIED_LENGTH) == BY_TIED_LENGTH) 
	    inc = mr.getTiedLength(layer);
	int division = mr.getDivision();
	int newindex = currentMeasure * division + currentTick 
	    + musRepPosition * inc;
	System.err.println("newindex: " + newindex);
	if (newindex >= 0) {
	    int newmeasure = newindex / division;
	    int newtick;
	    if ((option & MEASURE_HEAD) == MEASURE_HEAD)
		newtick = 0;
	    else
		newtick = newindex % division;
	    System.err.println(layer + " " + newindex);
	    return mr.getMusicElement(layer, newmeasure, newtick);
	} else {
	    return null;
	}
    }

    /*
  protected MusicElement musicMappedElement(MusicRepresentation musRep,
					    int currentMeasure, int currentTick) {
    if (musRepPosition == 0)
	return musRep.getMusicElement(layer, currentMeasure, currentTick);
    int index = 0;
    int inc;
    if ((option & BY_TIED_LENGTH) == BY_TIED_LENGTH) {
      // inc = layer.getTiedLength();
      inc = musRep.getTiedLength(layer);
    } else if ((option & MEASURE_HEAD) == MEASURE_HEAD) {
      inc = musRep.getDivision();
      current = (current / musRep.getDivision()) * musRep.getDivision();
      if (musRepPosition < 0)
        current += musRep.getDivision();
    } else {
      inc = 1;
    }
    if ((option & SET_ONLY) == SET_ONLY) {
      int sgn = musRepPosition > 0 ? 1 : -1;
      current += sgn;
      for (int i = 0; i < sgn * musRepPosition && current >= 0; current += sgn) {
        // MusicElement<E> e = layer.getElement(current / musRep.getDivision(),
        // current % musRep.getDivision());
        MusicElement e = musRep.getMusicElement(layer, current
            / musRep.getDivision(), current % musRep.getDivision());
        if (e.set())
          i++;
      }
      current -= sgn;
      index = current;
    } else {
      index = current + musRepPosition * inc;
    }
    index = Math.max(index, 0);
    // return layer.getElement(index / musRep.getDivision(), index
    // % musRep.getDivision());
    return musRep.getMusicElement(layer, index / musRep.getDivision(), index
        % musRep.getDivision());
  }
    */
}
