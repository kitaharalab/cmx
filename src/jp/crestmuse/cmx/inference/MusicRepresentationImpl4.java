package jp.crestmuse.cmx.inference;

import java.util.*;
import static java.lang.Math.*;

import jp.crestmuse.cmx.sound.SequencerManager;

class MusicRepresentationImpl4 implements MusicRepresentation {

  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;
  private boolean changeFlag;
  private boolean updateEnabled = true;

  MusicRepresentationImpl4(int measureNum, int division) {
    this.measureNum = measureNum;
    this.division = division;
    name2layer = new HashMap<String, MusicLayer>();
  }

  public int getMeasureNum() {
    return measureNum;
  }

  public int getDivision() {
    return division;
  }

  MusicLayer getMusicLayer(String name) {
    return name2layer.get(name);
  }

  public void addMusicLayer(String name, Object[] labels) {
    addMusicLayer(name, labels, 1); 
  }

  public void addMusicLayer(String name, Object[] labels, int tiedLength) { 
    MusicLayer mlb = new MusicLayer(labels, tiedLength);
    //	mlb.setTiedLength(tiedLength); 
    name2layer.put(name, mlb); 
  }

  public int getTiedLength(String layername) {
    return getMusicLayer(layername).getTiedLength();
  }

  /** NOTE: Always throws UnsupportedOperationException */
  public void addMusicLayerListener(String layername, MusicLayerListener l) {
    throw new UnsupportedOperationException();
  }

  public void addVerticalMusicCalculator(String layername, MusicCalculator calc) {
    getMusicLayer(layername).addMusicCalculator(calc);
  }

//  public void addMusicLayerListener(String layername, MusicLayerListener calc) {
//    getMusicLayer(layername).addListener(calc);
//  }

//  public void setUpdateChainLimit(String layername, int updateChainLimit) {
//    getMusicLayer(layername).setUpdateChainLimit(updateChainLimit);
//  }

  public MusicElement getMusicElement(String layername, int measure, int tick) {
    return getMusicLayer(layername).getElement(measure, tick);
  }

  public Object[] getLabels(String layername) {
    return getMusicLayer(layername).labels;
  }

/*    
  public Object getLabel(String layername, int index) {
    return getMusicLayer(layername).labels[index];
  }

  public int getNumOfLabels(String layername) {
    return getMusicLayer(layername).labels.length;
  }
*/

  public boolean isChanged() {
    return changeFlag;
  }
    
  public void resetChangeFlag() {
    changeFlag = false;
  }

/** Probably not tested yet. */
//  public void applyBackPointers(String layer) {
//    getMusicLayer(layer).applyBackPointers();
//  }

//  public void update(String layer, int measure, int tick) {
//    MusicLayer l = name2layer.get(layer);
//    l.update(l.getElement(measure, tick));
//  }

///  public void updateAll(String layer) {
//    MusicLayer l = name2layer.get(layer);
//    int m = getMeasureNum();
//    int n = getDivision();
//    int d = getTiedLength(layer);
//    for (int i = 0; i < m; i++)
//      for (int j = 0; j < n; j += d) 
//	l.update(l.getElement(i, j));
//  }

  public void suspendUpdate() {
    updateEnabled = false;
  }

  public void resumeUpdate() {
    updateEnabled = true;
  }

  private class MusicLayer {
    Object[] labels;

    protected int tiedLength;
    protected MusicElement[] elements;
    private List<MusicCalculator> calcs;
//    private List<MusicLayerListener> listeners;
//    private int updateChainLimit = Integer.MAX_VALUE;
//    private int updateChainCount = 0;

    MusicLayer(Object[] labels, int tiedLength) {
      this.tiedLength = tiedLength;
      this.elements = new MusicElement[getMeasureNum() * getDivision()];
      this.calcs = new LinkedList<MusicCalculator>();
      this.labels = labels;
      MusicElement e = null;
      int k = 0;
      for (int i = 0; i < getMeasureNum() * getDivision(); i++) {
	if (i % tiedLength == 0)
//	if (k % tiedLength == 0)
	  e = new MusicElementImpl(this, i);
	elements[i] = e;
      }
    }

    int getTiedLength() {
      return tiedLength;
    }

    void addMusicCalculator(MusicCalculator calc) {
      calcs.add(calc);
    }

//    void addListener(MusicLayerListener listener) {
//      listeners.add(listener);
//    }


    /* TODO 要検討*/
    private void update(MusicElement me) {
      throw new UnsupportedOperationException("not implemented yet");
    }
/*
    private void update(MusicElement me) {
      changeFlag = true;
      if (updateChainCount >= updateChainLimit)
        return;
      updateChainCount++;
      for (MusicLayerListener listener : listeners) {
	listener.update(MusicRepresentation3.this, me, 
			me.measure(), me.tick());
//        listener.update(MusicRepresentation3.this, me, 
//			me.indexInMusRep 
//			/ MusicRepresentation3.this.getDivision(), 
//			me.indexInMusRep 
//			% MusicRepresentation3.this.getDivision());
      }
      updateChainCount = 0;
    }
*/

    MusicElement getElement(int measure, int index) {
      if (index >= getDivision())
        throw new IndexOutOfBoundsException();
      return elements[getDivision() * measure + index];
//      return elements[(getDivision() * measure + index) / getTiedLength()];
    }

/*
    Object getLabel(int index) {
      // kari
      return labels[index];
      //	    return index >= 0 ? labels[index] : "";
    }
    
    int getNumOfLabels() {
      return labels.length;
    }
*/

    int getIndexOf(Object label) {
      for (int i = 0; i < labels.length; i++)
	if (labels[i].equals(label))
	  return i;
      return -1;
      //	    return Arrays.binarySearch(labels, label);
    }

//    void setUpdateChainLimit(int updateChainLimit) {
//      this.updateChainLimit = updateChainLimit;
//    }

/*
    void applyBackPointers() {
      MusicElementImpl e = (MusicElementImpl)getElement(getMeasureNum()-1, getDivision()-1);
      int lastindex = e.argmax;
      e.evidenceIndex = lastindex;
      while (e.backpoint != null) {
	e.backpoint.setEvidence(e.getBackPointer(lastindex));
//	e.backpoint.evidenceIndex = e.getBackPointer(lastindex);
//	System.err.println(e.backpoint.evidenceIndex);
	e = (MusicElementImpl)e.backpoint;
      }
    }
*/

  }



  private class Value {
    int index;
    double logPriorProb = Double.NEGATIVE_INFINITY;
    double logProbV = Double.NEGATIVE_INFINITY;
    double logProbH = Double.NEGATIVE_INFINITY;
    Value(int i) {
      index = i;
    }
  }


  private class MusicElementImpl implements MusicElement {
    private Value[] values;

//    private ElementProbPair[] pairs;
//    private ElementProbPair[] sortedPairs;
//    private ArrayList<ElementProbPair> pairs;
    private int evidence = -1;
//    private boolean setflag = false;
    // TODO 固定の要素の場合最初にペアを突っ込むことになりそうなので一応フラグ
    private MusicLayer parent;
    private int indexInMusRep;
//    private double maxLL = Double.NEGATIVE_INFINITY;
//    private int argmax = -1;
      //      private int bpmeasure = -1;
      //      private int bptick = -1;
//    private MusicElement backpoint = null;
//    private int duration;
    private Map<String,String> attrs = new HashMap<String,String>();
    private boolean tiedFromPrevious = false;
    private boolean rest = false;

    private MusicElementImpl(MusicLayer parent, int indexInMusRep) {
      this.parent = parent;
      this.indexInMusRep = indexInMusRep;
//      pairs = new ElementProbPair[parent.labels.length];
      values = new Value[parent.labels.length];
      for (int i = 0; i < parent.labels.length; i++)
        values[i] = new Value(i);
//	pairs[i] = new ElementProbPair(i, Double.NEGATIVE_INFINITY);
//      sortedPairs = null;
//      duration = tiedLength();
    }
			   
/*
    private MusicElement(MusicLayer parent, int indexInMusRep) {
	if (parent instanceof MusicLayerBasic) {
	    MusicLayerBasic l = ((MusicLayerBasic)parent);
	    pairs = new ArrayList<ElementProbPair>();
	    for (int i = 0; i < l.labels.length; i++)
		pairs.add(new ElementProbPair(i, Double.NEGATIVE_INFINITY));
	} else {
	    MusicLayerMeasurewise l 
		= ((MusicLayerMeasurewise)parent);
	    pairs = new ArrayList<ElementProbPair>();
	    for (int i = 0; i < l.labels.size(); i++)
		pairs.add(new ElementProbPair(i, Double.NEGATIVE_INFINITY));
	}

	//      pairs = new ArrayList<ElementProbPair>();
      this.parent = parent;
      this.indexInMusRep = indexInMusRep;
    }
*/

    public int measure() {
      return indexInMusRep / division;
    }

    public int tick() {
      return indexInMusRep % division;
    }

    public int division() {
      return division;
    }

    public int tiedLength() {
      return parent.getTiedLength();
    }

//    public int duration() {
//      return duration;
//    }

//    public void setDuration(int d) {
//      this.duration = d;
//    }

    public void setAttribute(String key, String value) {
      attrs.put(key, value);
    }

    public void setAllAttributes(Map<String,String> map) {
      attrs.putAll(map);
    }

    public String getAttribute(String key) {
      return attrs.get(key);
    }

    public boolean hasAttribute(String key) {
      return attrs.containsKey(key);
    }

    public void removeAttribute(String key) {
      attrs.remove(key);
    }

    public Map<String,String> getAllAttributes() {
      return attrs;
    }

    public boolean tiedFromPrevious() {
      return tiedFromPrevious;
    }

    public boolean rest() {
      return rest;
    }

    public void setTiedFromPrevious(boolean b) {
      tiedFromPrevious = b;
    }

    public void setRest(boolean b) {
      rest = b;
    }

    /** @deprecated */
    public void setProb(int index, double value) {
      setLogLikelihood(index, log(value));
    }

    public void setProbV(Object label, double value) {
      setLogProbV(label, log(value));
    }

    public void setProbH(Object label, double value) {
      setLogProbH(label, log(value));
    }

    public void setPriorProb(Object label, double value) {
      setLogPriorProb(label, value);
    }

    public void setLogProbV(Object label, double value) {
      values[getIndexOf(label)].logProbV = value;
    }

    public void setLogProbH(Object label, double value) {
      values[getIndexOf(label)].logProbH = value;
    }

    public void setLogPriorProb(Object label, double value) {
      values[getIndexOf(label)].logPriorProb = value;
    }

    public double getProb(Object label) {
      return exp(getLogProb(label));
    }

    public double getProbV(Object label) {
      return exp(getLogProbV(label));
    }

    public double getProbH(Object label) {
      return exp(getLogProbH(label));
    }

    public double getPriorProb(Object label) {
      return exp(getLogPriorProb(label));
    }

    public double getLogProb(Object label) {
      int i = getIndexOf(label);
      if (evidence >= 0)
        return evidence == i ? 0.0 : Double.NEGATIVE_INFINITY;
      else
        return values[i].logProbV + values[i].logProbH + values[i].logPriorProb;
    }

    public double getLogProbV(Object label) {
      return values[getIndexOf(label)].logProbV;
    }

    public double getLogProbH(Object label) {
      return values[getIndexOf(label)].logProbH;
    }

    public double getLogPriorProb(Object label) {
      int i = getIndexOf(label);
      if (evidence >= 0)
        return evidence == i ? 0.0 : Double.NEGATIVE_INFINITY;
      else
        return values[getIndexOf(label)].logPriorProb;
    }

    /** @deprecated 仮 */
    public void setLogLikelihood(int i, double value) {
      values[i].logProbV = 0;
      values[i].logProbH = 0;
      values[i].logPriorProb = value;
    }

    /** @deprecated */
    public double getLogLikelihood(int i) {
      if (evidence >= 0)
        return evidence == i ? 0.0 : Double.NEGATIVE_INFINITY;
      else
        return values[i].logProbV + values[i].logProbH + values[i].logPriorProb;
    }

    /** @deprecated */
    public double getProb(int i) {
      return exp(getLogLikelihood(i));
    }

    /** @deprecated */
    public double[] getAllProbs() {
      double[] allprobs = new double[values.length];
      for (int i = 0; i < values.length; i++)
        allprobs[i] = getProb(i);
      return allprobs;
    }

    
/*
    public double[] getAllProbs() {
      double[] allprobs = new double[pairs.length];
      for (int i = 0; i < pairs.length; i++)
	allprobs[i] = getProb(i);
      //  System.err.println(allprobs[i] = pairs.get(i).prob);
      return allprobs;
    }
*/

/*
    public double getLogLikelihood(int index) {
      if (evidenceIndex != -1)
	return evidenceIndex == index ? 0.0 : Double.NEGATIVE_INFINITY;
      else
	return pairs[index].loglik;
    }
*/


    
/*
    public double getProb(int index) {
      return exp(values[i].logPriorProb + values[i].logProvH + values[i].logProbV);
    }
*/

/*	  
    public double getProb(int index) {
      if (evidenceIndex != -1) 
	return evidenceIndex == index ? 1.0 : 0.0;
      else
	return Math.exp(pairs[index].loglik);
      //      return pairs.get(index).prob;
    }

      //    public E getElement(int index) {
      //      return pairs.get(index).element;
      //    }
      */

    /** @deprecated */
    public int getHighestProbIndex() {
      if (evidence >= 0)
        return evidence;
//      else if (argmax != -1)
//	return argmax;
      else {
        double max = Double.NEGATIVE_INFINITY;
        int argmax = -1;
        for (int i = 0; i < values.length; i++) {
          double logP = 
            values[i].logProbV + values[i].logProbH + values[i].logPriorProb;
          if (logP > max) {
            max = logP;
            argmax = i;
          }
          
        }
        return argmax;
//	return getRankedProbIndex(0);
        //	    return getRankedProbIndex(0);
      }
    }

    public Object getHighestProbLabel() {
      return parent.labels[getHighestProbIndex()];
    }

/*
    public int getRankedProbIndex(int rank) {
      if (sortedPairs == null) {
	sortedPairs = pairs.clone();
	Arrays.sort(sortedPairs);
      }
      return sortedPairs[rank].index;
//      List<ElementProbPair> rankedpairs = 
//	(ArrayList<ElementProbPair>) pairs.clone();
//      Collections.sort(rankedpairs);
      //	for (ElementProbPair p : rankedpairs)
      //	    System.err.print(p.prob + " ");
//      return rankedpairs.get(rank).index;
      //      return labels.indexOf(rankedprobs.get(rank));
    }

    public void getNBestIndices(int[] nbest) {
      if (sortedPairs == null) {
	sortedPairs = pairs.clone();
	Arrays.sort(sortedPairs);
      }
      for (int i = 0; i < nbest.length; i++)
	nbest[i] = sortedPairs[i].index;
//	  List<ElementProbPair> rankedpairs
//	      = (ArrayList<ElementProbPair>)pairs.clone();
//	  Collections.sort(rankedpairs);
//      for (int i = 0; i < nbest.length; i++)
//	nbest[i] = rankedpairs.get(i).index;
    }

      public int[] getNBestIndices(int n) {
	if (sortedPairs == null) {
	  sortedPairs = pairs.clone();
	  Arrays.sort(sortedPairs);
	}
//	  List<ElementProbPair> rankedpairs 
//	      = (ArrayList<ElementProbPair>) pairs.clone();
//	  Collections.sort(rankedpairs);
	  int[] nbest = new int[n];
	  for (int i = 0; i < n; i++)
	    nbest[i] = sortedPairs[i].index;
//	      nbest[i] = rankedpairs.get(i).index;
	  return nbest;
      }
*/

    /** @deprecated */
    public int getProbLength() {
      return values.length;
    }

/*
    public boolean set() {
      return setflag;
    }
*/

    public boolean hasEvidence() {
      return evidence >= 0;
    }

    public void setEvidence(int i) {
      evidence = i;
//      sortedPairs = null;
      if (updateEnabled)
	parent.update(this);
    }

    public void removeEvidence() {
//      sortedPairs = null;
      evidence = -1;
    }

/*
    private void setLogLikelihood(int i, double value, boolean update) {
      sortedPairs = null;
      evidenceIndex = -1;
      if (pairs[i].loglik == maxLL && value < maxLL) {
	argmax = -1;
      }
      pairs[i].loglik = value;
//      System.err.println("### " + i + " " + pairs[i].loglik);
      if (argmax != -1 && value > maxLL) {
	maxLL = value;
	argmax = i;
      } else if (argmax == -1) {
	argmax = getHighestProbIndex();
	maxLL = getLogLikelihood(argmax);
      }
      setflag = true;
      if (updateEnabled && update) parent.update(this);
//      System.err.println("@@@ " + argmax + "  " + maxLL);
    }

    public void setLogLikelihood(int i, double value) {
      setLogLikelihood(i, value, true);
    }


    private void setProb(int i, double value, boolean update) {
      setLogLikelihood(i, Math.log(value), update);
	//	pairs.get(i).dist = -Math.log(value);
	//      setflag = true;
	//      parent.update(this);
    }

    public void setProb(int i, double value) {
      setProb(i, value, true);
    }
*/

    /** @deprecated */
    public void update() {
      parent.update(this);
    }

/*
    public int getBackPointer(int i) {
      return pairs[i].backpointer;
    }

    public void setBackPointer(int i, int value) {
      pairs[i].backpointer = value;
    }

    public void setBackPointerTo(MusicElement e) {
      backpoint = e;
    }
*/

    public Object[] getLabels() {
      return parent.labels;
    }

    public int getNumOfLabels() {
      return parent.labels.length;
    }

    public Object getLabel(int index) {
      return parent.labels[index];
    }
      
    public int getIndexOf(Object label) {
      return parent.getIndexOf(label);
    }

/*
    private class ElementProbPair implements Comparable<ElementProbPair> {
      private int index;
      //	  private double prob;
      private double loglik;
      private int backpointer = -1;
      private ElementProbPair(int index, double loglik) {
	this.index = index;
	this.loglik = loglik;
      }
      public int compareTo(ElementProbPair arg0) {
	if (loglik > arg0.loglik)
	  return -1;
	else if (loglik < arg0.loglik)
	  return 1;
	else
	  return 0;  
//        
//         たぶんこれ昇順 if(this.prob == arg0.prob) return 0; return (this.prob -
//         arg0.prob) > 0 ? 1 : -1 ;
//           
      }
    }
*/
  }



}
