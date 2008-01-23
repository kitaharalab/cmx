package jp.crestmuse.cmx.filewrappers;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class DeviationDataSet {

  private DeviationInstanceWrapper wrapper;
  private TreeView<Control> nonPartwise;
  private Map<String,TreeView<Control>> partwise;
  private List<NotewiseDeviation> notewise;
  private Map<String,TreeView<ExtraNote>> extraNotes;
  private Control last = null;
  private double initSil = 0.0;

  DeviationDataSet(DeviationInstanceWrapper wrapper) {
    this.wrapper = wrapper;
    nonPartwise = new TreeView<Control>();
    partwise = new HashMap<String,TreeView<Control>>();
    notewise = new ArrayList<NotewiseDeviation>();
    extraNotes = new HashMap<String,TreeView<ExtraNote>>();
  }

  public DeviationInstanceWrapper getTargetWrapper() {
    return wrapper;
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
          wrapper.returnToParent();
        wrapper.addChild("measure");
        wrapper.setAttribute("number", c.measure);
        currentMeasure = c.measure;
      }
      wrapper.addChild("control");
      wrapper.setAttribute("beat", c.beat);
      wrapper.addChild(c.type);
      if (c.attr != null) {
        Set<Map.Entry<String,String>> entries = c.attr.entrySet();
        for (Map.Entry<String,String> e : entries)
          wrapper.setAttribute(e.getKey(), e.getValue());
      }
      if (!Double.isNaN(c.value))
        wrapper.addText(c.value);
      wrapper.returnToParent();
      wrapper.returnToParent();
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
      wrapper.returnToParent();
    }
  }

  private void extraNoteToWrapper(ExtraNote en) {
    if (en != null) {
      if (en.measure > currentMeasure) {
        if (currentMeasure >= 0)
          wrapper.returnToParent();
        wrapper.addChild("measure");
        wrapper.setAttribute("number", en.measure);
        currentMeasure = en.measure;
      }
      wrapper.addChild("extra-note");
      wrapper.setAttribute("beat", en.beat);
      wrapper.addChild("pitch");
      wrapper.addChildAndText("step", en.pitchStep);
      wrapper.addChildAndText("alter", en.pitchAlter);
      wrapper.addChildAndText("octave", en.pitchOctave);
      wrapper.returnToParent();
      wrapper.addChildAndText("duration", en.duration);
      wrapper.addChildAndText("dynamics", en.dynamics);
      wrapper.addChildAndText("end-dynamics", en.endDynamics);
      wrapper.returnToParent();
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
      wrapper.returnToParent();
    }
  }

  public void addElementsToWrapper() {
    wrapper.setAttributeNS("http://www.w3.org/2000/xmlns/", 
                           "xmlns:xlink", 
                           "http://www.w3.org/1999/xlink");
    wrapper.setAttribute("init-silence", initSil);
    wrapper.addChild("non-partwise");
    addControlViewToWrapper(nonPartwise);
    wrapper.returnToParent();
    wrapper.addChild("partwise");
    Set<String> keys = partwise.keySet();
    for (String key : keys) {
      wrapper.addChild("part");
      wrapper.setAttribute("id", key);
      addControlViewToWrapper(partwise.get(key));
      wrapper.returnToParent();
    }
    wrapper.returnToParent();
    wrapper.addChild("notewise");
    for (NotewiseDeviation nd : notewise ) 
      nd.addToWrapper(wrapper);
    wrapper.returnToParent();
    wrapper.addChild("extra-notes");
    keys = extraNotes.keySet();
    for (String key : keys) {
      wrapper.addChild("part");
      wrapper.setAttribute("id", key);
      addExtraNotesToWrapper(extraNotes.get(key));
      wrapper.returnToParent();
    }
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
    private double endDynamics;
    void addToWrapper(DeviationInstanceWrapper wrapper) {
      wrapper.addChild(name);
      wrapper.setAttributeNS("http://www.w3.org/1999/xlink", 
                             "xlink:href", 
                             "#xpointer(" + 
                             note.getXPathExpression() + ")");
      wrapper.addChildAndText("attack", attack);
      wrapper.addChildAndText("release", release);
      wrapper.addChildAndText("dynamics", dynamics);
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
