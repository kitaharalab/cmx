package jp.crestmuse.cmx.filewrappers;

import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class DeviationDataSet {

  private DeviationInstanceWrapper devxml = null;
  private MusicXMLWrapper musicxml = null;
  private TreeView<Control> nonPartwise;
  private Map<String,TreeView<Control>> partwise;
  private List<NotewiseDeviation> notewise;
  private Map<String,TreeView<ExtraNote>> extraNotes;
  private Control last = null;
  private double initSil = 0.0;

  public DeviationDataSet(DeviationInstanceWrapper devxml) {
    this.devxml = devxml;
    nonPartwise = new TreeView<Control>();
    partwise = new HashMap<String,TreeView<Control>>();
    notewise = new ArrayList<NotewiseDeviation>();
    extraNotes = new HashMap<String,TreeView<ExtraNote>>();
  }
  
  /**
   * 
   * @param musicxml
   */
  public DeviationDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
    nonPartwise = new TreeView<Control>();
    partwise = new HashMap<String,TreeView<Control>>();
    notewise = new ArrayList<NotewiseDeviation>();
    extraNotes = new HashMap<String,TreeView<ExtraNote>>();
    //TODO
  }

  public DeviationInstanceWrapper getTargetDeviationInstanceWrapper() {
    return devxml;
  }
  
  public MusicXMLWrapper getTargetMusicXMLWrapper(){
    return musicxml;
  }

  public void setInitialSilence(double initSil) {
    this.initSil = initSil;
  }

  public void addNonPartwiseControl(int measure, double beat,
                                    String type, double value) {
    Control c = new Control();
    c.measure = measure;
    c.beat = beat;
    c.type = type;
    c.value = value;
    nonPartwise.add(c, "");
    last = c;
  }
  
  /**
   * 属性(Attribute)を持つNonPartwiseControl要素をTreeViewに追加します。
   * @param measure
   * @param beat
   * @param type
   * @param attrName 属性名
   * @param attrValue 属性値
   * @param value
   */
  public void addNonPartwiseControl(int measure, double beat, String type,
      String attrName, String attrValue, double value){
    Control c = new Control();
    c.measure = measure;
    c.beat = beat;
    c.type = type;
    c.attr.put(attrName, attrValue);
    c.value = value;
    nonPartwise.add(c, "");
    last = c;
  }

  public void addNonPartwiseControl(int measure, double beat, 
                                    String type) {
    addNonPartwiseControl(measure, beat, type, Double.NaN);
  }

  public void addPartwiseControl(String partid, int measure, double beat, 
                                 String type, double value) {
    Control c = new Control();
    c.measure = measure;
    c.beat = beat;
    c.type = type;
    c.value = value;
    if (partwise.containsKey(partid)) {
      partwise.get(partid).add(c, "");
    } else {
      TreeView<Control> treeview = new TreeView<Control>();
      treeview.add(c, "");
      partwise.put(partid, treeview);
    }
    last = c;
  }
  
  /**
   * 属性(Attribute)を持つPartwiseControlをTreeViewに追加します。
   * @param partid
   * @param measure
   * @param beat
   * @param type
   * @param attrName 属性名
   * @param attrValue 属性値
   * @param value
   */
  public void addPartwiseControl(String partid, int measure, double beat, String type,
      String attrName, String attrValue, double value){
    Control c = new Control();
    c.measure = measure;
    c.beat = beat;
    c.type = type;
    c.attr.put(attrName, attrValue);
    c.value = value;
    if(partwise.containsKey(partid)){
      partwise.get(partid).add(c, "");
    }else{
      TreeView<Control> treeview = new TreeView<Control>();
      treeview.add(c, "");
      partwise.put(partid, treeview);
    }
    last = c;
  }

  public void addPartwiseControl(String partid, int measure, double beat, 
                                 String type) {
    addPartwiseControl(partid, measure, beat, type, Double.NaN);
  }

  public void setAttribute(String key, String value) {
    if (last.attr == null)
      last.attr = new HashMap<String,String>();
    last.attr.put(key, value);
  }

  public void setAttribute(String key, int value) {
    setAttribute(key, String.valueOf(value));
  }

  public void setAttribute(String key, double value) {
    setAttribute(key, String.valueOf(value));
  }
                 
  public void addNoteDeviation(MusicXMLWrapper.Note note, 
                               double attack, double release, 
                               double dynamics, double endDynamics) {
    NoteDeviation nd = new NoteDeviation();
    nd.note = note;
    nd.attack = attack;
    nd.release = release;
    nd.dynamics = dynamics;
    nd.endDynamics = endDynamics;
    nd.name = "note-deviation";
    notewise.add(nd);
  }
  
  /**
   * 属性付きdynamicsのNoteDeviationをリストに挿入します。
   * @param note
   * @param attack
   * @param release
   * @param dynamics
   * @param dyAttrName
   * @param dyAttrValue
   * @param endDynamics
   */
  public void addNoteDeviation(MusicXMLWrapper.Note note, double attack, double release,
      double dynamics, String dyAttrName, String dyAttrValue, double endDynamics){
    NoteDeviation nd = new NoteDeviation();
    nd.note = note;
    nd.attack = attack;
    nd.release = release;
    nd.dynamics = dynamics;
    nd.dyAttr.put(dyAttrName, dyAttrValue);
    nd.endDynamics = endDynamics;
    nd.name = "note-deviation";
    notewise.add(nd);
  }

  public void addChordDeviation(MusicXMLWrapper.Note note, 
                                double attack, double release, 
                                double dynamics, double endDynamics) {
    NoteDeviation nd = new NoteDeviation();
    nd.note = note;
    nd.attack = attack;
    nd.release = release;
    nd.dynamics = dynamics;
    nd.endDynamics = endDynamics;
    nd.name = "chord-deviation";
    notewise.add(nd);
  }

  public void addMissNote(MusicXMLWrapper.Note note) {
    MissNote mn = new MissNote();
    mn.note = note;
    notewise.add(mn);
  }

  public void addExtraNote(String partid, int measure, double beat, 
                           String pitchStep, int pitchAlter, int pitchOctave, 
                           double duration, double dynamics, 
                           double endDynamics) {
    ExtraNote en = new ExtraNote();
    en.measure = measure;
    en.beat = beat;
    en.pitchStep = pitchStep;
    en.pitchAlter = pitchAlter;
    en.pitchOctave = pitchOctave;
    en.duration = duration;
    en.dynamics = dynamics;
    en.endDynamics = endDynamics;
    if (extraNotes.containsKey(partid)) {
      extraNotes.get(partid).add(en, "");
    } else {
      TreeView<ExtraNote> treeview = new TreeView<ExtraNote>();
      treeview.add(en, "");
      extraNotes.put(partid, treeview);
    }
  }
  
  /**
   * 属性つきDynamicsをTreeViewに追加します。
   * @param partid
   * @param measure
   * @param beat
   * @param pitchStep
   * @param pitchAlter
   * @param pitchOctave
   * @param duration
   * @param dynamics
   * @param dyAttrName
   * @param dyAttrValue
   * @param endDynamics
   */
  public void addExtraNote(String partid, int measure, double beat, 
      String pitchStep, int pitchAlter, int pitchOctave, 
      double duration, double dynamics, String dyAttrName, String dyAttrValue,
      double endDynamics) {
    ExtraNote en = new ExtraNote();
    en.measure = measure;
    en.beat = beat;
    en.pitchStep = pitchStep;
    en.pitchAlter = pitchAlter;
    en.pitchOctave = pitchOctave;
    en.duration = duration;
    en.dynamics = dynamics;
    en.dyAttr.put(dyAttrName, dyAttrValue);
    en.endDynamics = endDynamics;
    if (extraNotes.containsKey(partid)) {
      extraNotes.get(partid).add(en, "");
    } else {
      TreeView<ExtraNote> treeview = new TreeView<ExtraNote>();
      treeview.add(en, "");
      extraNotes.put(partid, treeview);
    }
  }


  private static final String[] pitchList = 
    new String[] {"C", "C", "D", "D", "E", "F", "F", "G", "G", "A", "A", "B"};
  private static final int[] pitchAlterList = 
    new int[] {0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0};

  public void addExtraNote(String partid, int measure, double beat, 
                           int notenum, double duration, double dynamics, 
                           double endDynamics) {
    addExtraNote(partid, measure, beat, 
                 pitchList[notenum%12], pitchAlterList[notenum%12], 
                 notenum/12-1, duration, dynamics, endDynamics);
  }
  
  private int currentMeasure = Integer.MIN_VALUE;

  private void controlToWrapper(Control c) {
    if (c != null) {
      if (c.measure > currentMeasure) {
        if (currentMeasure >= 0)
          devxml.returnToParent();
        devxml.addChild("measure");
        devxml.setAttribute("number", c.measure);
        currentMeasure = c.measure;
      }
      devxml.addChild("control");
      devxml.setAttribute("beat", c.beat);
      devxml.addChild(c.type);
      if (c.attr != null) {
        Set<Map.Entry<String,String>> entries = c.attr.entrySet();
        for (Map.Entry<String,String> e : entries)
          devxml.setAttribute(e.getKey(), e.getValue());
      }
      if (!Double.isNaN(c.value))
        devxml.addText(c.value);
      devxml.returnToParent();
      devxml.returnToParent();
    }
  }

  private void addControlViewToWrapper(TreeView<Control> treeview) {
    if (!treeview.isempty()) {
      controlToWrapper(treeview.getRoot());
      currentMeasure = Integer.MIN_VALUE;
      while (treeview.hasElementsAtNextTime()) {
        controlToWrapper(treeview.getFirstElementAtNextTime());
        while (treeview.hasMoreElementsAtSameTime())
          controlToWrapper(treeview.getNextElementAtSameTime());
      }
      devxml.returnToParent();
    }
  }

  private void extraNoteToWrapper(ExtraNote en) {
    if (en != null) {
      if (en.measure > currentMeasure) {
        if (currentMeasure >= 0)
          devxml.returnToParent();
        devxml.addChild("measure");
        devxml.setAttribute("number", en.measure);
        currentMeasure = en.measure;
      }
      devxml.addChild("extra-note");
      devxml.setAttribute("beat", en.beat);
      devxml.addChild("pitch");
      devxml.addChildAndText("step", en.pitchStep);
      devxml.addChildAndText("alter", en.pitchAlter);
      devxml.addChildAndText("octave", en.pitchOctave);
      devxml.returnToParent();
      devxml.addChildAndText("duration", en.duration);
      devxml.addChild("dynamics");
      if (en.dyAttr != null) {
        Set<Map.Entry<String,String>> entries = en.dyAttr.entrySet();
        for (Map.Entry<String,String> e : entries)
          devxml.setAttribute(e.getKey(), e.getValue());
      }
      devxml.addText(en.dynamics);
      devxml.addChildAndText("end-dynamics", en.endDynamics);
      devxml.returnToParent();
    }
  }

  private void addExtraNotesToWrapper(TreeView<ExtraNote> treeview) {
    if (!treeview.isempty()) {
      extraNoteToWrapper(treeview.getRoot());
      currentMeasure = Integer.MIN_VALUE;
      while (treeview.hasElementsAtNextTime()) {
        extraNoteToWrapper(treeview.getFirstElementAtNextTime());
        while (treeview.hasMoreElementsAtSameTime()) 
          extraNoteToWrapper(treeview.getNextElementAtSameTime());
      }
      devxml.returnToParent();
    }
  }

  /**
   * @deprecated
   * 互換性維持のためのメソッドです。
   * DeviationInstanceWrapperを生成するときはDeviationDataSet.toWrapper()を利用してください。
   */
  public void addElementsToWrapper(){
    toWrapper();
  }
  
  public DeviationInstanceWrapper toWrapper() {
    
    if(devxml == null){
      devxml = DeviationInstanceWrapper.createDeviationInstanceFor(musicxml);
    }

    devxml.setAttributeNS("http://www.w3.org/2000/xmlns/", 
                           "xmlns:xlink", 
                           "http://www.w3.org/1999/xlink");
    devxml.setAttribute("init-silence", initSil);
    
    devxml.addChild("non-partwise");
    addControlViewToWrapper(nonPartwise);
    devxml.returnToParent();
    
    devxml.addChild("partwise");
    Set<String> keys = partwise.keySet();
    for (String key : keys) {
      devxml.addChild("part");
      devxml.setAttribute("id", key);
      addControlViewToWrapper(partwise.get(key));
      devxml.returnToParent();
    }
    devxml.returnToParent();
    
    devxml.addChild("notewise");
    for (NotewiseDeviation nd : notewise ) 
      nd.addToWrapper(devxml);
    devxml.returnToParent();
    
    devxml.addChild("extra-notes");
    keys = extraNotes.keySet();
    for (String key : keys) {
      devxml.addChild("part");
      devxml.setAttribute("id", key);
      addExtraNotesToWrapper(extraNotes.get(key));
      devxml.returnToParent();
    }
    
    return devxml;
  }

    

/*
  public void println() {
    //	System.out.println("Non-partwise deviation:");
    //	System.out.println("Partwise deviation:");
    System.out.println("Notewise deviation:");
    for (NoteDeviation nd : notewise)
      System.out.println("attack=" + nd.attack + 
                         ", release=" + nd.release + 
                         ", dynamics=" + nd.dynamics);
  }
*/

  private class Control implements Ordered {
    private int measure;
    private double beat;
    private String type;
    private double value = Double.NaN;
    private HashMap<String,String> attr = null;
    public final int ordinal() {
      return measure;
    }
    public final int subordinal() {
      return (int)(1920.0 * beat);
    }
  }

  private class ExtraNote implements Ordered {
    private int measure;
    private double beat;
    private String pitchStep;
    private int pitchAlter = 0;
    private int pitchOctave;
    private double duration;
    private double dynamics = 1.0;
    private HashMap<String, String> dyAttr = null;
    private double endDynamics = 1.0;
    public final int ordinal() {
      return measure;
    }
    public final int subordinal() {
      return (int)(1920.0 * beat);
    }
  }

  private abstract class NotewiseDeviation {
    MusicXMLWrapper.Note note;
    abstract void addToWrapper(DeviationInstanceWrapper wrapper);
  }

  private class NoteDeviation extends NotewiseDeviation {
    private String name;
    private double attack;
    private double release;
    private double dynamics;
    private HashMap<String, String> dyAttr = null;
    private double endDynamics;
    void addToWrapper(DeviationInstanceWrapper wrapper) {
      wrapper.addChild(name);
      wrapper.setAttributeNS("http://www.w3.org/1999/xlink", 
                             "xlink:href", 
                             "#xpointer(" + 
                             note.getXPathExpression() + ")");
      wrapper.addChildAndText("attack", attack);
      wrapper.addChildAndText("release", release);
      devxml.addChild("dynamics");
      if (dyAttr != null) {
        Set<Map.Entry<String,String>> entries = dyAttr.entrySet();
        for (Map.Entry<String,String> e : entries)
          devxml.setAttribute(e.getKey(), e.getValue());
      }
      devxml.addText(dynamics);
      wrapper.addChildAndText("end-dynamics", endDynamics);
      wrapper.returnToParent(); 
    }
  }

  private class MissNote extends NotewiseDeviation {
    void addToWrapper(DeviationInstanceWrapper wrapper) {
      wrapper.addChild("miss-note");
      wrapper.setAttributeNS("http://www.w3.org/1999/xlink", 
                             "xlink:href", 
                             "#xpointer(" + 
                             note.getXPathExpression() + ")");
      wrapper.returnToParent();
    }
  }

}
