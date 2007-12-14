package jp.crestmuse.cmx.commands.test;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class MusicXMLSlurTest extends CMXCommand {

  protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
      //      readInputData();
      final MusicXMLWrapper musicxml = (MusicXMLWrapper)indata();
//      final MusicXMLWrapper.SlurredNoteViewList slurview = 
//	  musicxml.getSlurredNoteView();
      System.out.println(musicxml.getSlurredNoteViews((byte)1));
      musicxml.processNotePartwise(new NoteHandlerPartwise() {
        public void beginPart(MusicXMLWrapper.Part p, MusicXMLWrapper w){}
	public void endPart(MusicXMLWrapper.Part p, MusicXMLWrapper w){}
	public void beginMeasure(MusicXMLWrapper.Measure m,MusicXMLWrapper w){}
	public void endMeasure(MusicXMLWrapper.Measure m,MusicXMLWrapper w){}
	public void processMusicData(MusicXMLWrapper.MusicData md,
	                             MusicXMLWrapper w) {
	  if (md instanceof MusicXMLWrapper.Note) {
          MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
          List<? extends TreeView<MusicXMLWrapper.Note>> l = 
            musicxml.getNoteViewsStartingWith(note);
          if (l != null && l.size() >= 1) {
            TreeView<MusicXMLWrapper.Note> t = l.get(0);
          }
	  System.out.println("Note [" + note + "]");
	  System.out.println("Slur(s) starting with this note: "
	                     + musicxml.getNoteViewsStartingWith(note));
          System.out.println("Slur(s) including this note: "
	                     + musicxml.getNoteViewsIncluding(note));
          System.out.println("Slur(s) ending with this note: "
                             + musicxml.getNoteViewsEndingWith(note));
          }
	  }
      });
  }


  public static void main(String[] args) {
      MusicXMLSlurTest t = new MusicXMLSlurTest();
    try {
      t.start(args);
    } catch (Exception e) {
      t.showErrorMessage(e);
      System.exit(1);
    }
  }
}