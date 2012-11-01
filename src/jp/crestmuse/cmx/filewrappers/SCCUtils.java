package jp.crestmuse.cmx.filewrappers;
import jp.crestmuse.cmx.elements.*;
import groovy.lang.*;
import javax.xml.transform.*;
import java.io.*;

class SCCUtils {

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

}
