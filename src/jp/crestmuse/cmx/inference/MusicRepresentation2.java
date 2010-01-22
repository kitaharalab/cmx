package jp.crestmuse.cmx.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentation2 {

  private int measureNum;
  private int division;
  private HashMap<String, MusicLayer> name2layer;
  private boolean changeFlag;

  public MusicRepresentation2(int measureNum, int division) {
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

    //  public void addMusicLayer(String name, MusicLayer ml) {
    //    name2layer.put(name, ml);
    //  }

    MusicLayer getMusicLayer(String name) {
	return name2layer.get(name);
    }

    public void addMusicLayerBasic(String name, int notenum) {
	addMusicLayerBasic(name, notenum, 1); 
    }
    
    public void addMusicLayerBasic(String name, int notenum, int tiedLength) {
	String[] arr = new String[notenum]; 
	for(int i=0; i<notenum; i++) arr[i] = i + ""; 
	addMusicLayerBasic(name, arr, tiedLength); 
    }

    public void addMusicLayerBasic(String name, Object[] labels) {
	addMusicLayerBasic(name, labels, 1); 
    }

    public void addMusicLayerBasic(String name, Object[] labels, int tiedLength) { 
	MusicLayerBasic mlb = new MusicLayerBasic(labels, tiedLength);
	//	mlb.setTiedLength(tiedLength); 
	name2layer.put(name, mlb); 
    }

    public void addMusicLayerMeasurewise(String name, int measureWidth){
	name2layer.put(name, new MusicLayerMeasurewise(measureWidth)); 
    }

    public int getTiedLength(String layername) {
	return getMusicLayer(layername).getTiedLength();
    }

    public void addMusicLayerListener(String layername, MusicLayerListener calc) {
	getMusicLayer(layername).addListener(calc);
    }


    public void setUpdateChainLimit(String layername, int updateChainLimit) {
	getMusicLayer(layername).setUpdateChainLimit(updateChainLimit);
    }

    public MusicElement getMusicElement(String layername, int measure, int tick) {
	return getMusicLayer(layername).getElement(measure, tick);
    }
    
    public Object getLayerLabel(String layername, int index) {
      return getMusicLayer(layername).getLabel(index);
    }

    //    public int getElementsLength(String layername) {
    //	return getMusicLayer(layername).getElementsLength();
    //    }

    public boolean isMeasurewiseLayer(String layername) {
	return getMusicLayer(layername) instanceof MusicLayerMeasurewise;
    }

    public int getLayerMeasureWidth(String layername) {
	return ((MusicLayerMeasurewise)getMusicLayer(layername)).getMeasureWidth();
    }
    
    public boolean isChanged() {
      return changeFlag;
    }
    
    public void resetChangeFlag() {
      changeFlag = false;
    }

    public void applyBackPointers(String layer) {
	getMusicLayer(layer).applyBackPointers();
    }

  @Deprecated
  public int getIndex(long tick) {
    return getIndex(tick, SequencerManager.TICKS_PER_BEAT);
  }

  @Deprecated
  public int getIndex(long tick, int ticksPerBeat) {
    int measureTick = ticksPerBeat * 4;
    int measure = (int) (tick / measureTick);
    int index = (int) (tick % measureTick) / (measureTick / division);
    return measure * division + index;
  }

  /*
   * public void update(String layername, int index) {
   * getMusicLayer(layername).update(index); }
   * 
   */

    /*
   public void update(String layer, int measure, int tick) {
       MusicLayer l = name2layer.get(layer);
       l.update(l.getElement(measure, tick));
   }
    */

  abstract class MusicLayer {
    protected int tiedLength;
    protected List<MusicElement> elements;
    private List<MusicLayerListener> listeners;
    private int updateChainLimit = Integer.MAX_VALUE;
    private int updateChainCount = 0;

    MusicLayer(int tiedLength) {
      this.tiedLength = tiedLength;
      this.elements = new ArrayList<MusicElement>();
//      for (int i = 0; i < getMeasureNum() * getDivision() / tiedLength; i++) {
//        elements.add(new MusicElement(this, i * tiedLength));
//      }
      this.listeners = new LinkedList<MusicLayerListener>();
    }

    int getTiedLength() {
      return tiedLength;
    }

    void addListener(MusicLayerListener listener) {
      listeners.add(listener);
    }
    
      //    int getElementsLength(){
      //      return elements.size();
      //    }

    private void update(MusicElement me) {
      changeFlag = true;
      if (updateChainCount >= updateChainLimit)
        return;
      updateChainCount++;
      for (MusicLayerListener listener : listeners) {
        listener.update(MusicRepresentation2.this, me, me.indexInMusRep / MusicRepresentation2.this.getDivision(), me.indexInMusRep % MusicRepresentation2.this.getDivision());
      }
      updateChainCount = 0;
    }

    abstract MusicElement getElement(int measure, int index);

      abstract Object getLabel(int index);

      abstract int getIndexOf(Object label);

    void setUpdateChainLimit(int updateChainLimit) {
      this.updateChainLimit = updateChainLimit;
    }

      void applyBackPointers() {
	  MusicElement e = getElement(getMeasureNum()-1, getDivision()-1);
	  int lastindex = e.argmax;
	  e.evidenceIndex = lastindex;
	  while (e.backpoint != null) {
	      e.backpoint.evidenceIndex = e.getBackPointer(lastindex);
	      System.err.println(e.backpoint.evidenceIndex);
	      e = e.backpoint;
	  }
      }


  }

    class MusicLayerBasic extends MusicLayer {
	Object[] labels;
	MusicLayerBasic(Object[] labels, int tiedLength) {
	    super(tiedLength);
	    this.labels = labels;
	    for (int i = 0; i < getMeasureNum() * getDivision() / tiedLength; i++) {
        elements.add(new MusicElement(this, i * tiedLength));
      }
	}
      //    MusicLayerBasic(int tiedLength) {
      //      super(tiedLength);
      //    }

	//    int getTiedLength() {
	//      return tiedLength;
	//    }

    /**
     * 小節番号、小節内のindexから要素を返します。小節番号、index共に最初の要素は0番目としてカウントします。
     * indexはtightLengthを考慮した値ではなく、親のMusicRepresentationでの位置を示します。
     * 
     * @param measure
     *          小節番号
     * @param index
     *          小節内の要素のindex
     */
    MusicElement getElement(int measure, int index) {
      if (index >= getDivision())
        throw new IndexOutOfBoundsException();
       return elements.get((getDivision() * measure + index) / getTiedLength());
    }

	Object getLabel(int index) {
	    return labels[index];
	}

	int getIndexOf(Object label) {
	    for (int i = 0; i < labels.length; i++)
		if (labels[i].equals(label))
		    return i;
	    return -1;
	    //	    return Arrays.binarySearch(labels, label);
	}
  }

  class MusicLayerMeasurewise extends MusicLayer {
      ArrayList labels;
    MusicLayerMeasurewise(int measureWidth) {
      super(measureWidth * getDivision());
      labels = new ArrayList();
      for (int i = 0; i < getMeasureNum() * getDivision() / tiedLength; i++) {
        elements.add(new MusicElement(this, i * tiedLength));
      }
    }

    int getMeasureWidth() {
      return getTiedLength() / getDivision();
    }

      Object getLabel(int index) {
	  return labels.get(index);
      }

      int getIndexOf(Object label) {
	  for (int i = 0; i < labels.size(); i++)
	      if (labels.get(i).equals(label))
		  return i;
	  return -1;
	  //	  return Collections.binarySearch(labels, label);
      }


    /**
     * @param measure
     *          小節番号
     * @param index
     *          小節内index
     * @deprecated 小節単位の要素の並びを記述するレイヤでは小節内indexを用いて要素を参照する必要はありません
     */
    @Deprecated
    public MusicElement getElement(int measure, int index) {
      return getElementByMeasureNumber(measure);
    }

    /**
     * 小節番号から対応する要素を返します。小節番号の初めの要素は0番目として参照します。
     * 要素に2小節以上の幅(tiedLength>=2)がある時、返す要素は引数の小節番号を含みますが、
     * 要素の先頭がその小節番号の要素であるとは限りません。
     * 
     * @param measure
     *          小節番号
     */
    public MusicElement getElementByMeasureNumber(int measure) {
      return elements.get(measure * getDivision() / getTiedLength());
    }
  }

  public class MusicElement implements BackPointerSetter {
      private ArrayList<ElementProbPair> pairs;
      private int evidenceIndex = -1;
    private boolean setflag = false;
    // TODO 固定の要素の場合最初にペアを突っ込むことになりそうなので一応フラグ
      private MusicLayer parent;
    private int indexInMusRep;
      private double maxLL = Double.NEGATIVE_INFINITY;
      private int argmax = -1;
      //      private int bpmeasure = -1;
      //      private int bptick = -1;
      private MusicElement backpoint = null;

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


      public double[] getAllProbs() {
	  double[] allprobs = new double[pairs.size()];
	  for (int i = 0; i < pairs.size(); i++)
	      allprobs[i] = getProb(i);
	      //  System.err.println(allprobs[i] = pairs.get(i).prob);
	  return allprobs;
      }

      public double getLogLikelihood(int index) {
	  if (evidenceIndex != -1)
	      return evidenceIndex == index ? 0.0 : Double.NEGATIVE_INFINITY;
	  else
	      return pairs.get(index).loglik;
      }
	  
    public double getProb(int index) {
	if (evidenceIndex != -1) 
	    return evidenceIndex == index ? 1.0 : 0.0;
	else
	    return Math.exp(pairs.get(index).loglik);
      //      return pairs.get(index).prob;
    }

      //    public E getElement(int index) {
      //      return pairs.get(index).element;
      //    }

    public int getHighestProbIndex() {
	if (evidenceIndex != -1)
	    return evidenceIndex;
	else
	    return argmax;
	    //	    return getRankedProbIndex(0);
    }

    public int getRankedProbIndex(int rank) {
	List<ElementProbPair> rankedpairs = (ArrayList<ElementProbPair>) pairs.clone();
	Collections.sort(rankedpairs);
	//	for (ElementProbPair p : rankedpairs)
	//	    System.err.print(p.prob + " ");
	return rankedpairs.get(rank).index;
      //      return labels.indexOf(rankedprobs.get(rank));
    }

      public void getNBestIndices(int[] nbest) {
	  List<ElementProbPair> rankedpairs
	      = (ArrayList<ElementProbPair>)pairs.clone();
	  Collections.sort(rankedpairs);
	  for (int i = 0; i < nbest.length; i++)
	      nbest[i] = rankedpairs.get(i).index;
      }

      public int[] getNBestIndices(int n) {
	  List<ElementProbPair> rankedpairs 
	      = (ArrayList<ElementProbPair>) pairs.clone();
	  Collections.sort(rankedpairs);
	  int[] nbest = new int[n];
	  for (int i = 0; i < n; i++)
	      nbest[i] = rankedpairs.get(i).index;
	  return nbest;
      }

    public int getProbLength() {
      return pairs.size();
    }

    public boolean set() {
      return setflag;
    }

    public boolean hasEvidence() {
      return evidenceIndex != -1 ? true : false;
    }

    public void setEvidence(int i) {
      this.evidenceIndex = i;
      parent.update(this);
    }

    public void removeEvidence(int i) {
      evidenceIndex = -1;
    }

      public void setLogLikelihood(int i, double value) {
	  evidenceIndex = -1;
	  pairs.get(i).loglik = value;
	  if (value > maxLL) {
	      maxLL = value;
	      argmax = i;
	  }
	  setflag = true;
	  parent.update(this);
      }

      public void setProb(int i, double value) {
	  setLogLikelihood(i, Math.log(value));
	//	pairs.get(i).dist = -Math.log(value);
	//      setflag = true;
	//      parent.update(this);
      }

      int getBackPointer(int i) {
	  return pairs.get(i).backpointer;
      }

      public void setBackPointer(int i, int value) {
	  pairs.get(i).backpointer = value;
      }

      public void setBackPointerTo(BackPointerSetter e) {
	  backpoint = (MusicElement)e;
      }

      /*
      public void setBackPointerTo(int measure, int tick) {
	  bpmeasure = measure;
	  bptick = tick;
      }
      */
    // TODO test

    /*
    public void addEntry(E e) {
      ElementProbPair epp = new ElementProbPair();
      epp.element = e;
      epp.prob = 1.0;
      pairs.add(epp);
      parent.update(this);
    }
      */

      public int addNewLabel(Object o) {
	  if (parent instanceof MusicLayerMeasurewise) {
	      MusicLayerMeasurewise l 
		  = (MusicLayerMeasurewise)parent;
	      l.labels.add(o);
	      int newindex = l.labels.size() - 1;
	      pairs.add(new ElementProbPair(newindex, 
					    Double.POSITIVE_INFINITY));
	      return newindex;
	  }else {
	      throw new UnsupportedOperationException();
	  }
      }
      
      public Object getLabel(int index) {
        return parent.getLabel(index);
      }
      
      public int getIndexOf(Object label) {
        return parent.getIndexOf(label);
      }

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
        /*
         * たぶんこれ昇順 if(this.prob == arg0.prob) return 0; return (this.prob -
         * arg0.prob) > 0 ? 1 : -1 ;
         */   
      }
    }
  }




    /*
  public static void main(String[] args) {
    // 単純に隣のElementをsetするcalcを登録して先頭の要素をsetしたときに末尾の要素までsetされてるか
    MusicRepresentation mr = new MusicRepresentation(3, 4);
    final MusicLayerBasic<String> mlb = mr.new MusicLayerBasic<String>(1);
    final double prob = 0.5;
    mlb.addListener(new MusicLayerListener<String>() {
      public void update(MusicRepresentation musRep, MusicElement<String> me,
          int indexInMusRep) {
        int next = indexInMusRep + 1;
        if(next == musRep.getMeasureNum() * musRep.getDivision())
          return;
        MusicElement<String> el = mlb.getElement(next / musRep.getDivision(),
            next % musRep.getDivision());
        el.addEntry("hoge");
        el.setProb(0, prob);
      }
    });
    MusicElement<String> head = mlb.getElement(0, 0);
    head.addEntry("hoge");
    head.setProb(0, 0.1);
    assert(mlb.getElement(2, 3).getProb(0) == prob);

    // 同じことをsetChainLimitして挙動を確認する
    final MusicLayerBasic<String> mlb2 = mr.new MusicLayerBasic<String>(1);
    mlb2.setUpdateChainLimit(4);
    mlb2.addListener(new MusicLayerListener<String>() {
      public void update(MusicRepresentation musRep, MusicElement<String> me,
          int indexInMusRep) {
        int next = indexInMusRep + 1;
        if(next == musRep.getMeasureNum() * musRep.getDivision())
          return;
        MusicElement<String> el = mlb2.getElement(next / musRep.getDivision(),
            next % musRep.getDivision());
        el.addEntry("hoge");
        el.setProb(0, prob);
      }
    });
    head = mlb2.getElement(0, 0);
    head.addEntry("hoge");
    head.setProb(0, 1.0);
    System.out.println(mlb2.getElement(1, 1).getProb(0));
    assert(mlb2.getElement(2, 3).getProb(0) == prob);
//    assert(mlb2.getElement(1, 1).getProb(0) == prob);
  }

    */

  /*
   * 090907以前 public interface MusicElement { double getProb(int i);
   * 
   * int getHighestProbIndex();
   * 
   * int getRankedProbIndex(int rank);
   * 
   * int getProbLength();
   * 
   * boolean set();
   * 
   * void setEvidence(int i);
   * 
   * void setProb(int i, double value); }
   * 
   * private interface MusicLayer { MusicElement getElement(int index);
   * 
   * MusicElement getElement(int measure, int index);
   * 
   * void addCalculator(Calculator calc);
   * 
   * void update(int index);
   * 
   * void update(int measure, int index); }
   * 
   * public class MusicElementBasic implements MusicElement {
   * 
   * private double prob[]; private boolean isEvidence; private int evidence;
   * private boolean set = false; private String[] labels;
   * 
   * public MusicElementBasic(String[] labels) { prob = new
   * double[labels.length]; this.labels = labels; }
   * 
   * public double getProb(int i) { if (isEvidence) { if (evidence == i) return
   * 1.0; return 0.0; } return prob[i]; }
   * 
   * public int getHighestProbIndex() { if (isEvidence) return evidence; double
   * max = Double.MIN_VALUE; int index = 0; for (int i = 0; i < prob.length;
   * i++) { if (prob[i] > max) { max = prob[i]; index = i; } } return index; }
   * 
   * public int getRankedProbIndex(int rank) { double[] cloneArray =
   * prob.clone(); Arrays.sort(cloneArray); for (int i = 0; i < prob.length;
   * i++) { if (prob[i] == cloneArray[cloneArray.length - 1 - rank]) return i; }
   * return 0; }
   * 
   * public int getProbLength() { return prob.length; }
   * 
   * public boolean set() { return set; }
   * 
   * public void setEvidence(int i) { set = true; isEvidence = true; evidence =
   * i; }
   * 
   * public void setProb(int i, double value) { set = true; isEvidence = false;
   * prob[i] = value; }
   * 
   * public String getLabel(int i) { return labels[i]; }
   * 
   * public int indexOf(String s) { for (int i = 0; i < labels.length; i++) if
   * (labels[i].equals(s)) return i; return -1; }
   * 
   * private void copy(MusicElementBasic e) { prob = e.prob.clone(); isEvidence
   * = e.isEvidence; evidence = e.evidence; set = e.set; labels =
   * labels.clone(); }
   * 
   * }
   * 
   * public class MusicLayerBasic implements MusicLayer {
   * 
   * MusicElementBasic[] elements; List<Calculator> calculators; int tiedLength
   * = 1; String[] labels; MusicRepresentation musRep;
   * 
   * MusicLayerBasic(String[] labels) { elements = new
   * MusicElementBasic[division * measureNum]; calculators = new
   * LinkedList<Calculator>(); this.labels = labels; this.musRep =
   * MusicRepresentation.this; }
   * 
   * public MusicElement getElement(int index) { if (elements[index] == null) {
   * addElement(index); } return elements[index]; }
   * 
   * int getTiedLength() { return tiedLength; }
   * 
   * void setTiedLength(int length) { tiedLength = length; for (int i = 0; i <
   * division * measureNum; i += tiedLength) if (elements[i] == null)
   * addElement(i); else { MusicElementBasic me = elements[i]; addElement(i);
   * elements[i].copy(me); } }
   * 
   * public void addCalculator(Calculator calc) { calculators.add(calc); }
   * 
   * void addElement(int index) { MusicElementBasic me = new
   * MusicElementBasic(labels); int head = (index / tiedLength) * tiedLength;
   * for (int i = head; i < head + tiedLength && i < division * measureNum; i++)
   * elements[i] = me; }
   * 
   * public void update(int index) { for (Calculator c : calculators)
   * c.update(musRep, elements[index], index); }
   * 
   * public MusicElement getElement(int measure, int index) { return
   * getElement(getElementIndex(measure, index)); }
   * 
   * public void update(int measure, int index) {
   * update(getElementIndex(measure, index)); }
   * 
   * private int getElementIndex(int measure, int index) { return (measure *
   * division) + index; } }
   * 
   * public class MusicElementMeasurewise<E> implements MusicElement {
   * 
   * private List<Double> prob; private boolean isEvidence; private int
   * evidence; private boolean set = false; private List<E> entries;
   * 
   * public MusicElementMeasurewise() { prob = new LinkedList<Double>(); entries
   * = new LinkedList<E>(); }
   * 
   * public int getHighestProbIndex() { if (isEvidence) return evidence; double
   * max = Double.MIN_VALUE; int index = 0; for (int i = 0; i < prob.size();
   * i++) { if (prob.get(i) > max) { max = prob.get(i); index = i; } } return
   * index; }
   * 
   * public double getProb(int i) { if (isEvidence) { if (evidence == i) return
   * 1.0; return 0.0; } else return prob.get(i); }
   * 
   * public int getProbLength() { return prob.size(); }
   * 
   * public int getRankedProbIndex(int rank) { Double[] cloneArray = (Double[])
   * prob.toArray(); Arrays.sort(cloneArray); for (int i = 0; i < prob.size();
   * i++) { if (prob.get(i) == cloneArray[cloneArray.length - 1 - rank]) return
   * i; } return 0; }
   * 
   * public int indexOf(E s) { return entries.indexOf(s); }
   * 
   * public boolean set() { return set; }
   * 
   * public void setEvidence(int i) { set = true; isEvidence = true; evidence =
   * i; }
   * 
   * public void setProb(int i, double value) { set = true; isEvidence = false;
   * prob.set(i, (Double) value); }
   * 
   * public int newEntry(E e) { entries.add(e); return entries.lastIndexOf(e); }
   * 
   * public void setNewEvidence(E e) { int i = newEntry(e); setEvidence(i); }
   * 
   * List<E> getEntries(){ return entries; }
   * 
   * E getEntry(int index) { return entries.get(index); } }
   * 
   * public class MusicLayerMeasurewise<E> implements MusicLayer {
   * 
   * int measureWidth = -1; List<MusicElementMeasurewise<E>> elements;
   * List<Calculator> calculators;
   * 
   * public MusicLayerMeasurewise(int measureWidth) { this.measureWidth =
   * measureWidth; this.elements = new LinkedList<MusicElementMeasurewise<E>>();
   * for (int i = 0; i < measureNum / measureWidth; i++) this.elements.add(new
   * MusicElementMeasurewise<E>()); this.calculators = new
   * LinkedList<Calculator>(); }
   * 
   * public void addCalculator(Calculator calc) { calculators.add(calc); }
   * 
   * // public MusicElement getElement(int index) { // return elements.get(index
   * / (division * measureWidth)); // }
   * 
   * public MusicElementMeasurewise<E> getElement(int index) { return
   * elements.get(index / (division * measureWidth)); }
   * 
   * public MusicElement getElement(int measure, int index) { return
   * getElement(getElementIndex(measure, index)); }
   * 
   * public void update(int index) { for (Calculator c : calculators)
   * c.update(MusicRepresentation.this, elements.get(index), index); }
   * 
   * public void update(int measure, int index) {
   * update(getElementIndex(measure, index)); }
   * 
   * private int getElementIndex(int measure, int index) { return (measure *
   * division) + index; } }
   */
}
