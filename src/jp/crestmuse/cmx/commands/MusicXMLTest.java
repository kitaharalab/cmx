package jp.crestmuse.cmx.commands;

import java.io.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class MusicXMLTest extends CMXCommand {

    DeviationDataSet dds;

  /*protected void run() throws TransformerException {
    List<TreeView<MusicXMLWrapper.Note>> noteviews = 
      ((MusicXMLWrapper)indata()).getPartwiseNoteView();
    for (TreeView nv : noteviews)
      System.out.println(nv);
  }*/


    protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, 
	       InvalidFileTypeException {
    /*SCCXMLWrapper sccxml = (SCCXMLWrapper) CMXFileWrapper
		.createDocument(SCCXMLWrapper.TOP_TAG);
    MIDIXMLWrapper midifile = MIDIXMLWrapper.readSMF("samples\\prelude-20\\paderewski-p045-045.MID");
    midifile.toSCCXML(sccxml);
    DataOutputStream dataout = new DataOutputStream 
    (new BufferedOutputStream(new FileOutputStream("samples\\prelude-20\\performance.xml")));
    sccxml.write(dataout);
    
    SCCXMLWrapper sccxml2 = (SCCXMLWrapper) CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    DeviationInstanceWrapper outputDeviationData = DeviationInstanceWrapper.createDeviationInstanceFor((MusicXMLWrapper)indata());
	outputDeviationData.finalizeDocument();
	outputDeviationData.toSCCXML(sccxml2, 1024);
	DataOutputStream dataout2 = new DataOutputStream 
    (new BufferedOutputStream(new FileOutputStream("samples\\prelude-20\\score.xml")));
	sccxml2.write(dataout2);*/
	
	DeviationInstanceWrapper diw = (DeviationInstanceWrapper) CMXFileWrapper.readfile("samples\\prelude-20\\deviation.xml");
	diw.setTargetMusicXMLFileName("samples\\prelude-20\\paderewski-p045-045.xml");
	SCCXMLWrapper sccxml3 = (SCCXMLWrapper) CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
	diw.toSCCXML(sccxml3, 1024);
	sccxml3.write(System.out);
	
    }


    public static void main(String[] args) {
	MusicXMLTest m = new MusicXMLTest();
	try {
	    m.start(args);
	} catch (Exception e) {
	    m.showErrorMessage(e);
	    System.exit(1);
	}
    }
/*
    private class TestHandler implements NoteHandlerPartwise {
	public void beginPart(MusicXMLWrapper.Part p,
			      MusicXMLWrapper w) {
	    System.out.println(p.getXPathExpression());
	}
	
	public void endPart(MusicXMLWrapper.Part p, 
			    MusicXMLWrapper w) {
	}

	public void beginMeasure(MusicXMLWrapper.Measure m,
				 MusicXMLWrapper w) {
	    System.out.println(m.getXPathExpression());
	}

	public void endMeasure(MusicXMLWrapper.Measure m,
			       MusicXMLWrapper w) {
	}

	private int i = 1;

	public void processMusicData(MusicXMLWrapper.MusicData md, 
				     MusicXMLWrapper w) {
	    /*if (md instanceof MusicXMLWrapper.Note) {
		MusicXMLWrapper.Note n = (MusicXMLWrapper.Note)md;
		System.out.println("Note No." + i);
		System.out.println("notenum="+n.notenum());
		i++;
		System.out.println(n.getXPathExpression());
		dds.addNoteDeviation(n, 0, 0, 0, 0);
	    }*/
/*	}
    }
*/
}
	