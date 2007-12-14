package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.*;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class OverlapNoteChecker extends CMXCommand {
  protected void run() throws IOException, TransformerException, 
    ParserConfigurationException, SAXException {
    int prevOnset = -1;
    List<Note> notelist = new LinkedList<Note>();
    MusicXMLWrapper musicxml = (MusicXMLWrapper)indata();
    SCCXMLWrapper sccxml = musicxml.makeDeadpanSCCXML(480);
    Note[] sorted = sccxml.getPartList()[0].getSortedNoteList();
//    SortedSet<Note> s = sccxml.getPartList()[0].getSortedNoteSet();
    for (SCCXMLWrapper.Note note : sorted) {
      if (prevOnset != note.onset()) {
        notelist.clear();
        prevOnset = note.onset();
      } else {
        MusicXMLWrapper.Note mnote = note.getMusicXMLWrapperNote();
        for (Note note2 : notelist) {
          if (note2.notenum() == note.notenum()) {
            MusicXMLWrapper.Note mnote2 = note2.getMusicXMLWrapperNote();
            System.out.println("Overlapping notes: ");
            System.out.println(mnote.getXPathExpression());
            System.out.println("pitch: " + mnote.noteName() + 
                               ", duration: " + mnote.duration() + 
                               ", grace: " + mnote.grace() + 
                               ", notehead: " + mnote.notehead());
            System.out.println(mnote2.getXPathExpression());
            System.out.println("pitch: " + mnote2.noteName() + 
                               ", duration: " + mnote2.duration() + 
                               ", grace: " + mnote2.grace() + 
                               ", notehead: " + mnote2.notehead());
          }
            
        }
        notelist.add(note);
      }
    }
  }

  public static void main(String[] args) {
    try {
      OverlapNoteChecker onc = new OverlapNoteChecker();
      onc.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}