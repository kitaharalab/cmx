package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.*;
import jp.crestmuse.cmx.misc.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import java.io.*;

public class SMFOverlapRemover extends 
                               CMXCommand<MIDIXMLWrapper,MIDIXMLWrapper> {

  NoteCompatible[] noteary = new NoteCompatible[128];
  int div;

  protected MIDIXMLWrapper readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
    return MIDIXMLWrapper.readSMF(filename);
  }

  protected MIDIXMLWrapper run(MIDIXMLWrapper indata) 
    throws ParserConfigurationException,SAXException,TransformerException,
    IOException {
    SCCXMLWrapper scc = indata.toSCCXML();
    SCCXMLWrapper newscc 
      = (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    newscc.setDivision(div = scc.getDivision());

    HeaderElement[] heads = scc.getHeaderElementList();
    newscc.beginHeader();
    for (HeaderElement h : heads)
      newscc.addHeaderElement(h.time(), h.name(), h.content());
    newscc.endHeader();

    Part[] parts = scc.getPartList();
    for (Part p : parts) {
      newscc.newPart(p.serial(), p.channel(), p.prognum(), p.volume());
      Note[] notes = p.getNoteList();
      for (Note n : notes) {
        int nn = n.notenum();
        if (noteary[nn] != null && noteary[nn].onset(div) == n.onset(div))
          System.out.println(n.notenum() + " " + n.onset() + " " + n.offset() + " : " + noteary[nn].notenum() + " " + noteary[nn].onset(div) + " " + noteary[nn].offset(div));
        noteary[n.notenum()] = n;
      }
      newscc.endPart();
    }
    newscc.finalizeDocument();
    return newscc.toMIDIXML();
  }

  public static void main(String[] args) {
    SMFOverlapRemover sor = new SMFOverlapRemover();
    try {
      sor.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
