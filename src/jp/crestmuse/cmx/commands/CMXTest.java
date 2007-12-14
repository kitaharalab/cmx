package jp.crestmuse.cmx.commands;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;

public class CMXTest extends CMXCommand {

  private int ticksPerBeat = 480;
  private int currentPart = -1;
  private int currentTick = 0;
  private int divisions = 1;

  protected void run() 
    throws IOException, ParserConfigurationException, 
           TransformerException, SAXException, InvalidFileTypeException {
      //    readInputData();
    newOutputData("scc");
    //    MusicXMLWrapper musicxml = 
    //        ((DeviationInstanceWrapper)indata()).getTargetMusicXML();
    //    indata().addLinks("//note-deviation", musicxml);
    //    DeviationInstanceWrapper.TimewiseControlView ctrlview = 
    //	((DeviationInstanceWrapper)indata()).getTimewiseControlView();
    //    ((SCCXMLWrapper)outdata()).setDivision(ticksPerBeat);
    //    ((SCCXMLWrapper)outdata()).beginHeader();
    //    controlToSCCHeader(ctrlview.getFirstControl(), (SCCXMLWrapper)outdata());
    //    while (ctrlview.hasControlsAtNextTime()) {
    //	controlToSCCHeader(ctrlview.getFirstControlAtNextTime(), 
    //			   (SCCXMLWrapper)outdata());
    //	while (ctrlview.hasMoreControlsAtSameTime()) 
    //	    controlToSCCHeader(ctrlview.getNextControlAtSameTime(), 
    //			       (SCCXMLWrapper)outdata());
    //    }
    //    //    ((SCCXMLWrapper)outdata()).addHeaderElement("DELTA", 
    //    //						String.valueOf(ticksPerBeat));
    //    ((SCCXMLWrapper)outdata()).endHeader();
    //    NoteHandlerPartwise handler = new TestHandler();
    //    musicxml.processNotePartwise(handler);
    ((DeviationInstanceWrapper)indata()).toSCCXML((SCCXMLWrapper)outdata(), 
						  ticksPerBeat);
    //    writeOutputData();
  }

	void controlToSCCHeader(DeviationInstanceWrapper.Control c, 
				SCCXMLWrapper dest) {
	    if (c != null) {
		int timestamp = (int)(ticksPerBeat * (4 * c.measure() + 
						      c.beat()));
		if (c.type().equals("tempo"))
		    dest.addHeaderElement(timestamp, "TEMPO", 
					  String.valueOf(c.value()));
	    }
	}

  public static void main(String[] args) {
    CMXTest c = new CMXTest();
    try {
//      CMXFileWrapper.addClassTable(
//        "deviation", 
//        CMXFileWrapper.getDefaultPackageName()+"."+"DeviationInstanceWrapper"
//      );
//      CMXFileWrapper.addClassTable(
//        "scc", 
//        CMXFileWrapper.getDefaultPackageName() + "." + "SCCXMLWrapper"
//      );
      c.start(args);
    } catch (Exception e) {
      c.showErrorMessage(e);
      System.exit(1);
    }
  }

    /*
  private class TestHandler implements NoteHandlerPartwise {
    public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper) {
      currentPart++;
      currentTick = 0;
      ((SCCXMLWrapper)outdata()).newPart(currentPart, currentPart, 0, 100);
    }

    public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper) {
	((SCCXMLWrapper)outdata()).endPart();
    }

      public void beginMeasure(MusicXMLWrapper.Measure measure, 
			       MusicXMLWrapper.Part part, 
                               MusicXMLWrapper wrapper) {
	  MusicXMLWrapper.Attributes attributes = 
	      measure.getAttributesNodeInterface();
	  if (attributes != null) {
	      divisions = attributes.divisions();
	  }
      }

      public void endMeasure(MusicXMLWrapper.Measure measure, 
			     MusicXMLWrapper.Part part, 
                             MusicXMLWrapper wrapper) {
      //  do nothing
    }

      public void processNote(MusicXMLWrapper.Note note, 
			      MusicXMLWrapper.Measure measure, 
			      MusicXMLWrapper.Part part, 
                              MusicXMLWrapper wrapper) {
      Node linkednode = 
	  wrapper.linkmanager.getNodeLinkedTo(note.node(), "note-deviation");
      int attack = 0;
      int release = 0;
      int dynamics = 100;
      if (linkednode != null) {
        DeviationInstanceWrapper.NoteDeviation nd = 
	    ((DeviationInstanceWrapper)indata()).
	    getNoteDeviationNodeInterface(linkednode);
        attack = (int)(ticksPerBeat * nd.attack());
        release = (int)(ticksPerBeat * nd.release());
        dynamics = (int)(100 * nd.dynamics());
      }
      int onset = currentTick + attack;
      currentTick += note.duration() * ticksPerBeat / divisions;
      int offset = currentTick + release;
      int notenum = note.notenum();
      ((SCCXMLWrapper)outdata()).addNoteElement(onset, offset, 
                                                    notenum, dynamics);
    }
  }
    */
}