package jp.crestmuse.cmx.inference;

import java.util.*;
import org.apache.commons.math3.distribution.*;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentationImpl implements MusicRepresentation {

  private int measureNum;
  private int division;
  private HashMap<String, AbstractMusicLayer> name2layer;
  private HashMap<String, MusicLayerType> name2type;

  MusicRepresentationImpl(int measureNum, int division) {
    this.measureNum = measureNum;
    this.division = division;
    name2layer = new HashMap<String, AbstractMusicLayer>();
    name2type = new HashMap<String, MusicLayerType>();
  }

  public int getMeasureNum() {
    return measureNum;
  }

  public int getDivision() {
    return division;
  }

/*
  public void addMusicLayer(String name, int notenum) {
    addMusicLayer(name, notenum, 1);
  }

  public void addMusicLayer(String name, int notenum, int tiedLength) {
    String[] arr = new String[notenum];
    for(int i=0; i<notenum; i++) arr[i] = i + "";
    addMusicLayer(name, arr, tiedLength);
  }
*/

  public void addMusicLayer(String name, Object[] labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, Object[] labels, int tiedLength) {
    name2layer.put(name, new MusicLayer(name, labels));
    name2layer.get(name).setTiedLength(tiedLength);
    name2type.put(name, MusicLayerType.STANDARD);
  }

  public void addMusicLayer(String name, List<Object> labels) {
    addMusicLayer(name, labels, 1);
  }

  public void addMusicLayer(String name, List<Object> labels, 
                            int tiedLength) {
    addMusicLayer(name, labels.toArray(), tiedLength);
  }

  public void addMusicLayerCont(String name) {
    addMusicLayerCont(name, 1);
  }

  public void addMusicLayerCont(String name, int tiedLength) {
    name2layer.put(name, new MusicLayerCont(name));
    name2layer.get(name).setTiedLength(tiedLength);
    name2type.put(name, MusicLayerType.CONT);
  }

  public boolean containsMusicLayer(String name) {
    return name2layer.containsKey(name);
  }
  
  public MusicLayerType getMusicLayerType(String name) {
    return name2type.get(name);
  }
  
  public Object[] getLabels(String layer) {
    AbstractMusicLayer l = name2layer.get(layer);
    if (l instanceof MusicLayer)
      return ((MusicLayer)l).labels;
    else
      throw new IllegalArgumentException();
    //    return name2layer.get(layer).labels;
  }

  private MusicElement getMusicElement(String layer, int index) {
    return name2layer.get(layer).getElement(index);
  }

  public MusicElement getMusicElement(String layer, int measure, int tick) {
    return getMusicElement(layer, measure * division + tick);
  }


  public List<MusicElement> getMusicElementList(String layer) {
    List<MusicElement> l = new ArrayList<MusicElement>();
    int tied = getTiedLength(layer);
    for (int i = 0; i < measureNum * division; i += tied) {
      MusicElement e = getMusicElement(layer, i);
      if (!e.tiedFromPrevious() && !e.rest())
        l.add(e);
    }
    return l;
  }

  public List<MusicElement>
    getMusicElementList(String layer, int measureFrom, int tickFrom,
                        int measureThru, int tickThru) {
    List<MusicElement> l = new ArrayList<MusicElement>();
    int tied = getTiedLength(layer);
    int from = measureFrom * division + tickFrom;
    int thru = measureThru * division + tickThru;
    for (int i = from; i < thru; i += tied) {
      MusicElement e = getMusicElement(layer, i);
      if (!e.tiedFromPrevious())
        l.add(e);
    }
    return l;
  }

  public void reflectTies(String layerFrom, String layerThru) {
    int tied = getTiedLength(layerFrom);
    if (tied != getTiedLength(layerThru))
      throw new IllegalArgumentException("tiedLength of two layers must be same");
    for (int i = 0; i < measureNum * division; i += tied) {
      MusicElement e1 = getMusicElement(layerFrom, i);
      MusicElement e2 = getMusicElement(layerThru, i);
      e2.setTiedFromPrevious(e1.tiedFromPrevious());
    }
  }

  public void reflectRests(String layerFrom, String layerThru) {
    int tied = getTiedLength(layerFrom);
    if (tied != getTiedLength(layerThru))
      throw new IllegalArgumentException("tiedLength of two layers must be same");
    for (int i = 0; i < measureNum * division; i += tied) {
      MusicElement e1 = getMusicElement(layerFrom, i);
      MusicElement e2 = getMusicElement(layerThru, i);
      e2.setRest(e1.rest());
    }
  }
  
  public int getTiedLength(String layer) {
    return name2layer.get(layer).getTiedLength();
  }

//  public void setTiedLength(String layer, int tiedLength) {
//    name2layer.get(layer).setTiedLength(tiedLength);
//  }

  public void addMusicCalculator(String layer, MusicCalculator calc) {
    name2layer.get(layer).addMusicCalculator(calc);
  }

  abstract class AbstractMusicElementImpl implements MusicElement {
    boolean isEvidence;
    Object evidence;
    boolean set = false;
    AbstractMusicLayer parent;
    int index;
    boolean suspended = false;
    boolean tiedFromPrevious = false;
    boolean rest = false;

    AbstractMusicElementImpl(AbstractMusicLayer parent, int index) {
      this.parent = parent;
      this.index = index;
    }

    abstract boolean check(Object label);
    abstract double getProbLocal(Object label);
    abstract Object getMostLikelyLocal();
    abstract Object generateLocal();
    abstract void setEvidenceLocal(Object label);
    
    public synchronized void suspendUpdate() {
      suspended = true;
    }

    public synchronized void resumeUpdate() {
      suspended = false;
      parent.update(index);
    }

    public synchronized void setEvidence(Object label) {
      if (tiedFromPrevious()) {
        prev().setEvidence(label);
      } else {
        if (check(label)) {
          set = true;
          isEvidence = true;
          setEvidenceLocal(label);
          //evidence = Double.valueOf(((Number)label).doubleValue());
          //          evidence = label;
          //        System.err.println("UPDATE: " + parent.name + " " + label);
          if (!suspended)
            parent.update(index);
        } else {
          throw new IllegalArgumentException(label + ": not supported.");
        }
      }
    }

    public synchronized double getProb(Object label) {
      if (tiedFromPrevious()) {
        return prev().getProb(label);
      } else {
        if (isEvidence) {
          if (evidence.equals(label))
            return 1.0;
        else
          return 0.0;
        } else {
          return getProbLocal(label);
        }
      }
    }

    public synchronized Object getMostLikely() {
      if (tiedFromPrevious()) {
        return prev().getMostLikely();
      } else {
        if (isEvidence) 
          return evidence;
        else
        return getMostLikelyLocal();
      }
    }
    
    public synchronized Object generate() {
      if (tiedFromPrevious()) {
        return prev().generate();
      } else {
        if (isEvidence)
          return evidence;
        else
        return generateLocal();
      }
    }

    public synchronized boolean rest() {
      return rest;
    }

    public synchronized void setRest(boolean b) {
      rest = b;
      if (rest) setTiedFromPrevious(false);
    }
    
    public synchronized boolean tiedFromPrevious() {
      return tiedFromPrevious;
    }

    public synchronized void setTiedFromPrevious(boolean b) {
      if (index == 0 && b == true)
        throw new IllegalArgumentException("tiedFromPrevious must not be true at the first element");
      tiedFromPrevious = b;
      if (tiedFromPrevious) setRest(false);
    }

    public synchronized MusicElement next() {
      try {
        return parent.getElement(index + parent.getTiedLength());
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }

    public synchronized MusicElement prev() {
      try {
        return parent.getElement(index - parent.getTiedLength());
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }

    public synchronized int measure() {
      return index / division;
    }

    public synchronized int tick() {
      return index % division;
    }

    private int untilNext() {
      AbstractMusicElementImpl next = (AbstractMusicElementImpl)next();
      if (next == null)
        return 1;
      else if (next.tiedFromPrevious())
        return 1 + next.untilNext();
      return 1;
    }
      
    
    public synchronized int duration() {
      if (tiedFromPrevious()) {
        return prev().duration();
      } else {
        return untilNext();
      }
    }

  }
  
  class MusicElementImpl extends AbstractMusicElementImpl {

    private double prob[];

    MusicElementImpl(MusicLayer parent, int index) {
      super(parent, index);
      prob = new double[parent.labels.length];
    }

    boolean check(Object label) {
      for (int i = 0; i < ((MusicLayer)parent).labels.length; i++) {
        if (label.equals(((MusicLayer)parent).labels[i]))
          return true;
      }
      return false;
    }

    double getProbLocal(Object label) {
      for (int i = 0; i < ((MusicLayer)parent).labels.length; i++) {
        if (((MusicLayer)parent).labels[i].equals(label))
          return prob[i];
      }
      return 0.0;
    }

    Object getMostLikelyLocal() {
      double max = prob[0];
      int index = 0;
      for (int i = 1; i < prob.length; i++) {
        if (prob[i] > max) {
          max = prob[i];
          index = i;
        }
      }
      return ((MusicLayer)parent).labels[index];
    }

    Object generateLocal() {
      double sum = 0;
      for (int i = 0; i < prob.length; i++) {
        sum += prob[i];
      }
      double rand = Math.random();
      double cum = 0;
      for (int i = 0; i < prob.length; i++) {
        if (cum / sum <= rand && rand <= (cum + prob[i]) / sum)
          return ((MusicLayer)parent).labels[i];
        cum += prob[i];
      }
      return null;
    }

    void setEvidenceLocal(Object label) {
      evidence = label;
    }
    
    public void setProb(Object label, double value) {
      set = true;
      isEvidence = false;
      for (int i = 0; i < ((MusicLayer)parent).labels.length; i++)
        if (((MusicLayer)parent).labels[i].equals(label))
          prob[i] = value;
      if (!suspended)
        parent.update(index);
    }

    public void setProb(Map<Object,Double> map) {
      suspendUpdate();
      for (Map.Entry<Object,Double> e : map.entrySet()) {
        setProb(e.getKey(), e.getValue());
      }
      resumeUpdate();
    }

    public void setDistribution(RealDistribution d) {
      throw new UnsupportedOperationException();
    }

    private void copy(MusicElement e) {
      MusicElementImpl ee = (MusicElementImpl)e;
      prob = ee.prob.clone();
      isEvidence = ee.isEvidence;
      evidence = ee.evidence;
      //isNote = e.isNote;
      // type = e.type;
      set = ee.set;
      //chordLabel = e.chordLabel.clone();
      parent = ee.parent;
      index = ee.index;
//      labels = ee.labels.clone();
    }

  }

  class MusicElementContImpl extends AbstractMusicElementImpl {
    double mean, var;
    RealDistribution dist = null;

    MusicElementContImpl(MusicLayerCont parent, int index) {
      super(parent, index);
    }

    boolean check(Object label) {
      return label instanceof Number;
      //      return label instanceof Double;
    }

    public void setDistribution(RealDistribution d) {
      dist = d;
      parent.update(index);
    }

    public void setProb(Object label, double value) {
      throw new UnsupportedOperationException();
    }

    public void setProb(Map<Object,Double> map) {
      throw new UnsupportedOperationException();
    }

    double getProbLocal(Object label) {
      if (dist == null)
        return 0.0;
      else
        return dist.probability((Double)label);
    }

    void setEvidenceLocal(Object label) {
      evidence = Double.valueOf(((Number)label).doubleValue());
    }
    
    Double getMostLikelyLocal() {
      if (dist == null)
        return Double.NaN;
      else
        return dist.getNumericalMean();
    }

    Double generateLocal() {
      if (dist == null)
        return Double.NaN;
      else
        return dist.sample();
    }
  }

  private abstract class AbstractMusicLayer {
    String name;
    MusicElement[] elements;
    List<MusicCalculator> calculators;
    int tiedLength = 1;

    AbstractMusicLayer(String name) {
      this.name = name;
      elements = new MusicElement[division * measureNum];
      calculators = new LinkedList<MusicCalculator>();
    }

    MusicElement getElement(int index) {
      if (elements[index] == null) {
        addElement(index);
      }
      return elements[index];
    }

    int getTiedLength() {
      return tiedLength;
    }

    void setTiedLength(int length) {
      tiedLength = length;
      for (int i = 0; i < division * measureNum; i += tiedLength)
        if (elements[i] == null)
          addElement(i);
        else {
          MusicElement me = elements[i];
          addElement(i);
          ((MusicElementImpl)elements[i]).copy(me);
        }
    }

    void addMusicCalculator(MusicCalculator calc) {
      calculators.add(calc);
    }

    void addElement(int index) {
      MusicElement me = createMusicElement(index);
      int head = (index / tiedLength) * tiedLength;
      for (int i = head; i < head+tiedLength && i < division*measureNum; i++)
        elements[i] = me;
    }

    abstract MusicElement createMusicElement(int index);

    void update(int index) {
      for (MusicCalculator c : calculators)
        c.updated(index/division, index%division, name, MusicRepresentationImpl.this);
    }

  }

  class MusicLayer extends AbstractMusicLayer {
    Object[] labels;

    MusicLayer(String name, Object[] labels) {
      super(name);
      this.labels = labels;
    }

    MusicElement createMusicElement(int index) {
      return new MusicElementImpl(this, index);
    }
  }

  private class MusicLayerCont extends AbstractMusicLayer {
    MusicLayerCont(String name) {
      super(name);
    }

    MusicElement createMusicElement(int index) {
      return new MusicElementContImpl(this, index);
    }
  }

}
