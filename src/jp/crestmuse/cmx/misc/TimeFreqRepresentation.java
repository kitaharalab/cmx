package jp.crestmuse.cmx.misc;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.elements.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;

public class TimeFreqRepresentation {
  private List<TimeFreqElement> tflist;
  private int nbands;

  // private int current = 0;

  private TimeFreqRepresentation(int nbands) {
    this.nbands = nbands;
    tflist = new ArrayList<TimeFreqElement>();
  }

  private void add(double time, byte[] values, NoteCompatible[] data) {
    if (data.length != nbands)
      throw new IllegalStateException();
    tflist.add(new TimeFreqElement(time, values, data));
  }

  private void addTime(double time) {
    add(time, new byte[nbands], new NoteCompatible[nbands]);
  }

  public TimeFreqElement get(int timeindex) {
    return tflist.get(timeindex);
  }

  private void set(int timeindex, int freqindex, byte value) {
    tflist.get(timeindex).values[freqindex] = value;
  }

  private void set(int timeindex, int freqindex, boolean value) {
    set(timeindex, freqindex, value ? 127 : 0);
  }

  private void set(int timeindex, int freqindex, double value) {
    set(timeindex, freqindex, (byte) (value * 127.0));
  }

  private void set(int timeindex, int freqindex, NoteCompatible data) {
    tflist.get(timeindex).data[freqindex] = data;
  }

  private void changeTime(int timeindex, double newtime) {
    tflist.get(timeindex).t = newtime;
  }

  private void changeIsMeasureHead(int timeindex, boolean isMeasureHead) {
    tflist.get(timeindex).isMeasureHead = isMeasureHead;
  }

  // void resetCounter() {
  // current = 0;
  // }

  // TimeFreqElement next() {
  // return tflist.get(current++);
  // }

  public int length() {
    return tflist.size();
  }

  /*
   * TimeFreqElement search(double t) { int size = tflist.size();
   * TimeFreqElement lastElem = tflist.get(current); for (int i = current + 1; i <
   * size; i++) { TimeFreqElement currElem = tflist.get(i); if (currElem.t > t) {
   * current = i - 1; return lastElem; } else { lastElem = currElem; } } return
   * null; }
   */

  static int dist(TimeFreqElement e1, TimeFreqElement e2) {
    return e1.dist(e2);
  }

  public void println() {
    for (TimeFreqElement e : tflist)
      System.out.println(e);
  }

  public class TimeFreqElement {
    private byte[] values;
    private NoteCompatible[] data;
    private double t;
    private boolean isMeasureHead;

    private TimeFreqElement(double t, byte[] values, NoteCompatible[] data) {
      this.values = values;
      this.data = data;
      this.t = t;
      this.isMeasureHead = false;
    }

    public byte[] values() {
      return values;
    }

    int dist(TimeFreqElement another) {
      if (values.length != another.values.length)
        throw new IllegalStateException();
      int dist = 0;
      for (int i = 0; i < values.length; i++) {
        int d = values[i] - another.values[i];
        dist += d >= 0 ? d : -d;
      }
      return dist;
    }

    int distFromZero() {
      int dist = 0;
      for (int i = 0; i < values.length; i++)
        dist += values[i] > 0 ? values[i] : -values[i];
      return dist;
    }

    public double time() {
      return t;
    }

    public NoteCompatible[] data() {
      return data;
    }

    public boolean isMeasureHead() {
      return isMeasureHead;
    }

    public String toString() {
      String s = "";
      for (int i = 0; i < values.length; i++)
        s += " " + values[i];
      return s;
    }
    /*
     * public String toString2() { String s = ""; for (int i = 0; i <
     * values.length; i++) s += values[i] > 0 ? "o" : "."; return s; }
     */
  }

  private static final int N_BANDS = 128;

  public static TimeFreqRepresentation getTimeFreqRepresentation(
      PianoRollCompatible filewrapper, final int ticksPerBeat)
      throws TransformerException, IOException, ParserConfigurationException,
      SAXException {
    return getTimeFreqRepresentation(filewrapper, ticksPerBeat, 4, null);
  }

  public static TimeFreqRepresentation getTimeFreqRepresentation(
      PianoRollCompatible filewrapper, final int ticksPerBeat,
      final int divisionPerMeasure, final MusicXMLWrapper musicxml)
      throws TransformerException, IOException, ParserConfigurationException,
      SAXException {
    final TimeFreqRepresentation tfr = new TimeFreqRepresentation(N_BANDS);
    filewrapper.processNotes(new CommonNoteHandler() {
      public void beginPart(String id, PianoRollCompatible filewrapper) {
      }

      public void endPart(String id, PianoRollCompatible filewrapper) {
      }

      public void processNote(NoteCompatible note, PianoRollCompatible w) {
        int onsetIndex = note.onset(ticksPerBeat) * divisionPerMeasure / 4
            / ticksPerBeat;
        int offsetIndex = note.offset(ticksPerBeat) * divisionPerMeasure / 4
            / ticksPerBeat;
        int notenum = note.notenum();
        ///// onsetがoffsetより長いなぞのパターン ///////
        ///// とりあえず入れ替えておく            ///////
        if(onsetIndex > offsetIndex){
          onsetIndex += offsetIndex;
          offsetIndex = onsetIndex - offsetIndex;
          onsetIndex = onsetIndex - offsetIndex;
        }
        //////////////////////////////////////////////
        int length;
        while ((length = tfr.length()) <= offsetIndex)
          tfr.addTime(length * ticksPerBeat * 4 / divisionPerMeasure);
        tfr.set(onsetIndex, notenum, note);
        byte vel;
        try {
          vel = (byte) note.velocity();
        } catch (UnsupportedOperationException e) {
          vel = (byte) 127;
        }
        for (int n = onsetIndex; n <= offsetIndex; n++)
          tfr.set(n, notenum, vel);
      }
    });
    if (musicxml != null) {
      int i = 1;
      while (true) {
        try {
          tfr.changeIsMeasureHead(musicxml
              .getCumulativeTicks(i++, ticksPerBeat)
              * divisionPerMeasure / 4 / ticksPerBeat, true);
        } catch (IndexOutOfBoundsException e) {
          break;
        }
      }
    }
    return tfr;
  }

  /*
   * public static TimeFreqRepresentation getTimeFreqRepresentation(
   * PianoRollCompatible filewrapper, final int ticksPerBeat) throws
   * TransformerException, IOException, ParserConfigurationException,
   * SAXException { final TimeFreqRepresentation tfr = new
   * TimeFreqRepresentation(N_BANDS); filewrapper.processNotes(new
   * CommonNoteHandler() { public void beginPart(String id, PianoRollCompatible
   * filewrapper) { }
   * 
   * public void endPart(String id, PianoRollCompatible filewrapper) { }
   * 
   * public void processNote(NoteCompatible note, PianoRollCompatible w) { int
   * onset = note.onset(ticksPerBeat); int offset = note.offset(ticksPerBeat);
   * int notenum = note.notenum(); int length; while ((length = tfr.length()) <=
   * offset) tfr.addTime(length * ticksPerBeat); tfr.set(onset, notenum, note);
   * byte vel; try { vel = (byte) note.velocity(); } catch
   * (UnsupportedOperationException e) { vel = (byte) 127; } for (int n = onset;
   * n < offset; n++) tfr.set(n, notenum, vel); } }); return tfr; }
   */
  /*
   * public static TimeFreqRepresentation
   * getTimeFreqRepresentation(SCCXMLWrapper scc) throws TransformerException {
   * final int division = scc.getDivision(); final TimeFreqRepresentation tfr =
   * new TimeFreqRepresentation(N_BANDS); scc.processNotes(new SCCHandler() {
   * public void beginHeader(SCCXMLWrapper w) { } public void
   * endHeader(SCCXMLWrapper w) { } public void beginPart(SCCXMLWrapper.Part
   * part, SCCXMLWrapper w) {} public void endPart(SCCXMLWrapper.Part part,
   * SCCXMLWrapper w) {} public void processHeaderElement(int timestamp, String
   * name, String content, SCCXMLWrapper w) {} public void
   * processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w) { int onset =
   * note.onset(); int offset = note.offset(); int notenum = note.notenum(); //
   * System.err.println(onset + " " + offset + " " + notenum); int length; while
   * ((length = tfr.length()) <= offset) tfr.addTime(length * division);
   * tfr.set(onset, notenum, note); for (int n = onset; n < offset; n++)
   * tfr.set(n, notenum, (byte)note.velocity()); // tfr.set(n, notenum,
   * (byte)(127 * (offset - n) / (offset - onset))); } }); return tfr; }
   */

}