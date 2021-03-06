package jp.crestmuse.cmx.filewrappers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.TransformerException;

import groovy.lang.Closure;
import jp.crestmuse.cmx.elements.MutableAnnotation;
import jp.crestmuse.cmx.elements.MutableMusicEvent;
import jp.crestmuse.cmx.elements.MutableNote;
import jp.crestmuse.cmx.math.DoubleMatrix;
import jp.crestmuse.cmx.math.MathUtils;
import jp.crestmuse.cmx.misc.ChordSymbol2;
import jp.crestmuse.cmx.misc.KeySymbol;

public class SCCUtils {

  static SCC.HeaderElement getFirstHeader(SCC scc, String name) {
    SCC.HeaderElement[] headers = scc.getHeaderElementList();
    for (SCC.HeaderElement h : headers) {
      if (h.name().equals(name))
        return h;
    }
    return null;
  }

  static SCC.Annotation[] getAnnotationListOf(String tagname, SCC scc) {
    SCC.Annotation[] ann = scc.getAnnotationList();
    if (ann == null) return null;
    int n = 0;
    for (int i = 0; i < ann.length; i++) {
      if (ann[i].type().equals(tagname))
        n++;
    }
    SCC.Annotation[] ann2 = new SCC.Annotation[n];
    int k = 0;
    for (int i = 0; i < ann.length; i++)
      if (ann[i].type().equals(tagname)) {
        ann2[k] = ann[i];
        k++;
      }
    return ann2;
  }

  public static class KEY {
    long time;
    KeySymbol key;
    public String toString() {
      return "[time:" + time + "  key:" + key.encode() + "]";
    }
  }

  public static List<KEY> getKeyList(SCC scc) {
    SCC.HeaderElement[] headers = scc.getHeaderElementList();
    List<KEY> l = new ArrayList<KEY>();
    for (SCC.HeaderElement h : headers) {
      if (h.name().equals("KEY")) {
        KEY key = new KEY();
        key.time = h.time();
        key.key = KeySymbol.parse(h.content());
        l.add(key);
      }
    }
    return l;
  }

/*
	static SCC.Annotation[] getChordList(SCC scc) {
		SCC.Annotation[] ann = scc.getAnnotationList();
		if (ann == null) return null;
		int n = 0;
		for (int i = 0; i < ann.length; i++) {
			if (ann[i].type().equals("chord"))
				n++;
		}
		SCC.Annotation[] chords = new SCC.Annotation[n];
		int k = 0;
		for (int i = 0; i < ann.length; i++)
			if (ann[i].type().equals("chord")) {
				chords[k] = ann[i];
				k++;
			}
		return chords;
	}


	static SCC.Annotation[] getBarlineList(SCC scc) {
		SCC.Annotation[] ann = scc.getAnnotationList();
		if (ann == null) return null;
		int n = 0;
		for (int i = 0; i < ann.length; i++)
			if (ann[i].type().equals("barline"))
				n++;
		SCC.Annotation[] barlines = new SCC.Annotation[n];
		int k = 0;
		for (int i = 0; i < ann.length; i++)
			if (ann[i].type().equals("barline")) {
				barlines[k] = ann[i];
				k++;
			}
		return barlines;
	}

	// Added: 2014/11/18, Author: tama
	static SCC.Annotation[] getLyricList(SCC scc) {
		SCC.Annotation[] ann = scc.getAnnotationList();
		if (ann == null) return null;
		int n = 0;
		for (int i = 0; i < ann.length; i++)
			if (ann[i].type().equals("lyric"))
				n++;
		SCC.Annotation[] lyrics = new SCC.Annotation[n];
		int k = 0;
		for (int i = 0; i < ann.length; i++)
			if (ann[i].type().equals("lyric")) {
				lyrics[k] = ann[i];
				k++;
			}
		return lyrics;
	}
*/

  public static SCC transposeByFifth(SCC scc, int diff_fifth, boolean sharp)
  throws TransformerException {
    if (diff_fifth == 0) {
      return scc;
    } else if (diff_fifth > 0) {
      int diff = 0;
      while (diff_fifth * 7 % 12 != diff % 12) {
        diff++;
      }
      return transpose(scc, diff, sharp);
    } else {
      int diff = 0;
      while (diff_fifth * 7 % 12 != diff % 12) {
        diff--;
      }
      return transpose(scc, diff, sharp);
    }
  }
  
  public static SCC transpose(SCC scc, int diff, boolean sharp)
    throws TransformerException {
    SCCDataSet scc2 = scc.toDataSet().clone();
    //    int fifth = 0;
    //    while (fifth * 7 % 12 != diff)
    //      if (diff > 0) fifth++;
    //      else fifth--;
    SCCDataSet.HeaderElement[] headers = scc2.getHeaderElementList();
    for (SCCDataSet.HeaderElement h : headers) {
      if (h.name().equals("KEY")) {
        KeySymbol key = KeySymbol.parse(h.content);
        h.content = key.transpose(diff).encode();
        //        String[] data = h.content.split(" ");
        //        int newfifth = Integer.parseInt(
        //                                        data[0].startsWith("+") ? data[0].substring(1) : data[0]
        //                                        ) + fifth;
        //        h.content =
        //          (newfifth > 0 ? ("+" + newfifth) : newfifth)
        //          + " " + data[1];
      }
    }
    SCCDataSet.Part[] parts = scc2.getPartList();
    for (SCCDataSet.Part part : parts) {
      if (part.channel() != 10) {
        MutableMusicEvent[] events = part.getNoteList();
        for (MutableMusicEvent e : events) {
          if (e instanceof MutableNote) {
            MutableNote note = (MutableNote)e;
            note.setNoteNum(note.notenum() + diff);
          }
        }
      }
    }
    SCC.Annotation[] chords = scc2.getChordList();
    for (SCC.Annotation chord : chords) {
      MutableAnnotation c = (MutableAnnotation)chord;
      c.setContent(ChordSymbol2.parse(c.content()).transpose(diff, sharp).encode());
    }
    return scc2;
  }

  public static SCCDataSet createVoicewiseSCC(SCC scc) throws TransformerException {
    int serial = 0;
    Map<String,SCCDataSet.Part> newparts =
      new TreeMap<String,SCCDataSet.Part>();
    int div = scc.getDivision();
    SCCDataSet newscc = new SCCDataSet(div);
    SCC.HeaderElement[] headers = scc.getHeaderElementList();
    for (SCC.HeaderElement h : headers)
      newscc.addHeaderElement(h.time(), h.name(), h.content());
    SCC.Part[] parts = scc.getPartList();
    for (SCC.Part part : parts) {
      SCC.Note[] notes = part.getNoteList();
      for (SCC.Note note : notes) {
        String name = part.name() + "; oldserial=" + part.serial() +
          (note.hasAttribute("voice") ?
           " voice=" + note.getAttribute("voice") : "");
        SCCDataSet.Part newpart = newparts.get(name);
        if (newpart == null) {
          newpart = newscc.addPart(++serial, part.channel(),
                                   part.prognum(), part.volume(), name);
          newparts.put(name, newpart);
        }
        newpart.addNoteElement(note.onset(div), note.offset(div),
                               note.notenum(), note.velocity(),
                               note.offVelocity(), note.getAttributes());
      }
    }
    SCC.Annotation[] anns = scc.getAnnotationList();
    for (SCC.Annotation ann : anns) {
      newscc.addAnnotation(ann.type(), ann.onset(div), ann.offset(div),
                           ann.content());
    }
    return newscc;
  }

	/*
  static SCCXMLWrapper toWrapper(SCC scc) throws TransformerException {
    try {
    SCCXMLWrapper newscc =
      (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    int div = scc.getDivision();
    newscc.setDivision(div);
    SCC.HeaderElement[] headerlist = scc.getHeaderElementList();
    newscc.beginHeader();
    for (SCC.HeaderElement h : headerlist) {
      newscc.addHeaderElement(h.time(), h.name(), h.content());
    }
    newscc.endHeader();
    SCC.Part[] partlist = scc.getPartList();
    for (SCC.Part p : partlist) {
      newscc.newPart(p.serial(), p.channel(), p.prognum(),
                     p.volume(), p.name());
      SCC.Note[] notelist = p.getNoteList();
      for (SCC.Note n : notelist) {
        if (n.word() == null)
          newscc.addNoteElement(n.onset(div), n.offset(div), n.notenum(),
                                n.velocity(), n.offVelocity());
        else
          newscc.addNoteElementWithWord(n.word(), n.onset(div), n.offset(div),
                                        n.notenum(), n.velocity(),
                                        n.offVelocity());
      }
      newscc.endPart();
    }
    newscc.beginAnnotations();
    SCC.Annotation[] annlist = scc.getAnnotationList();
    for (SCC.Annotation a : annlist) {
      newscc.addAnnotation(a.type(), a.onset(div), a.offset(div),
                           a.content());
    }
    newscc.endAnnotations();
    newscc.finalizeDocument();
    return newscc;
    } catch (InvalidFileTypeException e) {
      throw new IllegalStateException();
    } catch (IOException e) {
      e.printStackTrace();
      throw new TransformerException(e.toString());
    }
  }
	 */


	/*
  static SCCDataSet toDataSet(SCC scc) throws TransformerException {
    int div = scc.getDivision();
    SCCDataSet newscc = new SCCDataSet(div);
    SCC.HeaderElement[] headerlist = scc.getHeaderElementList();
    for (SCC.HeaderElement h : headerlist) {
      newscc.addHeaderElement(h.time(), h.name(), h.content());
    }
    SCC.Part[] partlist = scc.getPartList();
    for (SCC.Part p : partlist) {
      SCCDataSet.Part newpart =
        newscc.addPart(p.serial(), p.channel(), p.prognum(),
                       p.volume(), p.name());
      SCC.Note[] notelist = p.getNoteList();
      for (SCC.Note n : notelist) {
        if (n instanceof SCCXMLWrapper.ControlChange) {
          SCCXMLWrapper.ControlChange cc = (SCCXMLWrapper.ControlChange)n;
          newpart.addControlChange(cc.onset(div), cc.ctrlnum(), cc.value());
        } else if (n instanceof SCCXMLWrapper.PitchBend) {
          SCCXMLWrapper.PitchBend pb = (SCCXMLWrapper.PitchBend)n;
          newpart.addPitchBend(pb.onset(), pb.value());
        } else if (n.word() == null) {
          newpart.addNoteElement(n.onset(div), n.offset(div), n.notenum(),
                                 n.velocity(), n.offVelocity());
        } else {
          newpart.addNoteElementWithWord(n.word(), n.onset(div), n.offset(div),
                                         n.notenum(), n.velocity(),
                                         n.offVelocity());
        }
      }
    }
    SCC.Annotation[] annlist = scc.getAnnotationList();
    if (annlist != null)
      for (SCC.Annotation a : annlist ) {
        newscc.addAnnotation(a.type(), a.onset(div), a.offset(div),
                             a.content());
      }
    return newscc;
  }
	 */


  static void eachnote(SCC scc, Closure closure) throws TransformerException {
    SCC.Part[] partlist = scc.getPartList();
    for (SCC.Part part : partlist) {
      SCC.Note[] notelist = part.getNoteList();
      for (SCC.Note note : notelist) {
        closure.call(new Object[]{note});
      }
    }
  }


  static void eachpart(SCC scc, Closure closure) throws TransformerException {
    SCC.Part[] partlist = scc.getPartList();
    for (SCC.Part part : partlist) {
      closure.call(new Object[]{part});
    }
  }
  
  static void eachnote(SCC.Part part, Closure closure) throws TransformerException {
    SCC.Note[] notelist = part.getNoteList();
    for (SCC.Note note : notelist) {
      closure.call(new Object[]{note});
    }
  }

  static void eachchord(SCC scc, Closure closure) throws TransformerException {
    SCC.Annotation[] chordlist = scc.getChordList();
    for (SCC.Annotation c : chordlist) {
      closure.call(new Object[]{c});
    }
  }

  static void eachbarline(SCC scc, Closure closure) throws TransformerException {
    SCC.Annotation[] barlinelist = scc.getBarlineList();
    for (SCC.Annotation b : barlinelist) {
      closure.call(new Object[]{b});
    }
  }

  public static int[][] countNoteTransition(SCC.Part p, boolean octaveIgnored) {
    if (octaveIgnored)
      return countNoteTransition(p, new int[12][12], octaveIgnored);
    else
      return countNoteTransition(p, new int[128][128], octaveIgnored);
  }
  
  public static int[][] countNoteTransition(SCC.Part p, int[][] counts,
                                            boolean octaveIgnored) {
    //    int[][] counts = new int[12][12];
    int d = octaveIgnored ? 12 : 128;
    SCC.Note[] notelist = p.getNoteOnlyList();
    for (int i = 0; i < notelist.length-1; i++) {
      int n1 = notelist[i+1].notenum();
      int n0 = notelist[i].notenum();
      counts[n0 % d][n1 % d]++;
    }
    return counts;
  }

  public static int[][] countChordTransition(SCC scc, int[][] counts,
                                             ChordSymbol2[] cc,
                                             boolean root, boolean mode,
                                             boolean bass, boolean seventh,
                                             boolean ninth, boolean eleventh,
                                             boolean thirteenth,
                                             boolean ignoresSharpFlat) {
    SCC.Annotation[] chords = scc.getChordList();
    for (int i = 0; i < chords.length-1; i++) {
      ChordSymbol2 c1 = ChordSymbol2.parse(chords[i+1].content());
      System.out.println(c1);
      ChordSymbol2 c0 = ChordSymbol2.parse(chords[i].content());
      System.out.println(c0);
      int m, n;
      for (m = 0; m < cc.length; m++)
        if (c0.match(cc[m], root, mode, bass, seventh, ninth,
                     eleventh, thirteenth, ignoresSharpFlat))
          break;
      for (n = 0; n < cc.length; n++)
        if (c1.match(cc[n], root, mode, bass, seventh, ninth,
                     eleventh, thirteenth, ignoresSharpFlat))
          break;
      if (m < cc.length && n < cc.length)
        counts[m][n]++;
    }
    return counts;
  }

  public static DoubleMatrix toMatrix(List<SCC.Note> notes, int channel) {
    DoubleMatrix matrix = MathUtils.createDoubleMatrix(notes.size(), 7);
    for (int i = 0; i < notes.size(); i++) {
      matrix.set(i, 0, (double)notes.get(i).onset(10080) / 10080.0);
      matrix.set(i, 1, (double)notes.get(i).duration(10080) / 10080.0);
      matrix.set(i, 2, channel);
      matrix.set(i, 3, notes.get(i).notenum());
      matrix.set(i, 4, notes.get(i).velocity());
      matrix.set(i, 5, (double)notes.get(i).onsetInMilliSec() / 1000.0);
      matrix.set(i, 6, (double)notes.get(i).durationInMilliSec() / 1000.0);
    }
    return matrix;
  }
      

  public static List<SCC.Note> getNotesBetween
    (SCC.Part part, int from, int thru, int ticksPerBeat,
     boolean onsetBased, boolean noteonly) {
    synchronized (part) {
      SCC.Note[] notes =
        noteonly ? part.getSortedNoteOnlyList() : part.getSortedNoteList();
      List<SCC.Note> notes2 = new ArrayList<SCC.Note>();
      for (SCC.Note e : notes) {
        if (e.onset(ticksPerBeat) >= from &&
            (onsetBased ? e.onset(ticksPerBeat) : e.offset(ticksPerBeat))
            <= thru)
          notes2.add(e);
      }
      return notes2;
    }
    
  }
}
