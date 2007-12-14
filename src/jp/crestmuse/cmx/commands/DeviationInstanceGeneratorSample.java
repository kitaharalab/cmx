package jp.crestmuse.cmx.commands;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;
import static jp.crestmuse.cmx.misc.Misc.*;

public class DeviationInstanceGeneratorSample extends CMXCommand {

    private DeviationDataSet dds;

    protected void run()
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
	//	readInputData();
        //      indata().removeBlankTextNodes();
	newOutputData("deviation");
	((DeviationInstanceWrapper)outdata()).setTargetMusicXMLFileName
	    (indata().getFileName());
	dds = ((DeviationInstanceWrapper)outdata()).createDeviationDataSet();
	((MusicXMLWrapper)indata()).processNotePartwise(new SampleHandler());
	dds.addElementsToWrapper();
	//	writeOutputData();
    }

    public static void main(String[] args) {
	DeviationInstanceGeneratorSample s = 
	    new DeviationInstanceGeneratorSample();
	try {
	    s.start(args);
	} catch (Exception e) {
	    s.showErrorMessage(e);
	    System.exit(1);
	}
    }

    private class SampleHandler implements NoteHandlerPartwise {
	public void beginPart(MusicXMLWrapper.Part p,
			      MusicXMLWrapper w) {
	    System.out.println("Part " + p.id());
	}
	
	public void endPart(MusicXMLWrapper.Part p, 
			    MusicXMLWrapper w) {
	}

	public void beginMeasure(MusicXMLWrapper.Measure m,
				 MusicXMLWrapper w) {
	    System.out.println("Measure " + m.number());
	}

	public void endMeasure(MusicXMLWrapper.Measure m,
			       MusicXMLWrapper w) {
	}

	public void processMusicData(MusicXMLWrapper.MusicData md, 
				     MusicXMLWrapper w) {
	    if (md instanceof MusicXMLWrapper.Note) {
		MusicXMLWrapper.Note n = (MusicXMLWrapper.Note)md;
		String s = "Note[" + n.pitchStep();
		if (n.pitchAlter() > 0) s += "#";
		else if (n.pitchAlter() < 0) s += "b";
		s += n.pitchOctave() + ", " + n.duration() + "]";
		System.out.println(s);
		if (inputYesNo("require deviation? ")) {
		    double attack = inputDouble("attack? ");
		    double release = inputDouble("release? ");
		    double dynamics = inputDouble("dynamics? ");
		    double endDynamics = inputDouble("end-dynamics? ");
		    dds.addNoteDeviation(n, attack, release, dynamics, 
					 endDynamics);
		}
		while (inputYesNo("input non-partwise control here? ")) {
		    System.out.println("measure is " + n.measure().number());
		    double beat = inputDouble("beat? ");
		    String type = inputString("type? ");
		    double value = inputDouble("value? ");
		    dds.addNonPartwiseControl(n.measure().number(), 
					      beat, type, value);
		}
	    }
	}
    }

}
	