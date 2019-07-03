package jp.crestmuse.cmx.filewrappers;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class TimespanXMLWrapper extends CMXFileWrapper {
  
  public static final String TOP_TAG = "tstree";
  private MusicXMLWrapper targetMusicXML = null;
  private Timespan topts = null;

  private void setTargetMusicXML(MusicXMLWrapper musicxml) {
    targetMusicXML = musicxml;
  }

  protected void analyze() {
    topts = new Timespan(selectSingleNode("/tstree/ts"), null, true);
  }
    
  public Timespan getTopTimespan() {
    if (topts == null)
      analyze();
    return topts;
  }

/*
  public void parsetree(Timespan ts, int depth) {
    System.out.println(depth+"\t");
    System.out.println(ts.head().getMusicXMLWrapperNotes()[0]);
    if (ts.hasPrimary())
      parsetree(ts.primary(), depth+1);
    if (ts.hasSecondary())
      parsetree(ts.secondary(), depth+1);
  }
*/




  public class Timespan extends NodeInterface {
    private Timespan parent, primary = null, secondary = null;
    private boolean isPrimary = false;
    private Chord head;

    private Timespan(Node node, Timespan parent, boolean isPrimary) {
      super(node);
      this.parent = parent;
//      System.err.println(node);
//      System.err.println(node.getClass());
//      Node n1 = node;
//      while (n1 != null) {
//        System.out.print(n1);
//        n1 = n1.getParentNode();
//      }
//      System.out.println();
      this.isPrimary = isPrimary;
      head = new Chord(getChildOfChildByTagName("head", "chord"));
    }

    @Override
    protected String getSupportedNodeName() {
      return "ts";
    }

    public Chord head() {
      return head;
    }

    public boolean hasPrimary() {
      return hasChild("primary", node());
    }

    public Timespan primary() {
      if (primary == null)
        primary = new Timespan(getChildOfChildByTagName("primary", "ts"), 
                               this, true);
      return primary;
    }

    public boolean hasSecondary() {
      return hasChild("secondary", node());
    }

    public Timespan secondary() {
      if (secondary == null)
        secondary = new Timespan(getChildOfChildByTagName("secondary", "ts"), 
                                 this, false);
      return secondary;
    }

    public boolean isPrimary() {
      return isPrimary;
    }

    public Timespan parent() {
      return parent;
    }
  }

  public class Chord extends NodeInterface {
    private Chord(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "chord";
    }
    public MusicXMLWrapper.Note[] getMusicXMLWrapperNotes() {
      NodeList nl = getChildNodes();
      int length = nl.getLength();
      MusicXMLWrapper.Note[] notes = new MusicXMLWrapper.Note[length];
      for (int i = 0; i < length; i++) {
        NamedNodeMap attr = nl.item(i).getAttributes();
        String id = attr.getNamedItem("id").getNodeValue();
        String[] pmi = id.split("-");
        MusicXMLWrapper.Part part = targetMusicXML.getPartOf(pmi[0]);
        MusicXMLWrapper.Measure measure = 
          part.getMeasureList()[Integer.parseInt(pmi[1])-1];
        notes[i] = 
          measure.getNoteOnlyList()[Integer.parseInt(pmi[2])-1];
      }
      return notes;
    }
  }
}