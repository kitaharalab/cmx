package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;

import static jp.crestmuse.cmx.misc.Misc.inputDouble;
import static jp.crestmuse.cmx.misc.Misc.inputString;
import static jp.crestmuse.cmx.misc.Misc.inputYesNo;

public class DeviationInstanceGeneratorSample 
  extends CMXCommand<MusicXMLWrapper,DeviationInstanceWrapper> {

    private DeviationDataSet dds;

    protected DeviationInstanceWrapper run(MusicXMLWrapper musicxml)
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
      dds = new DeviationDataSet(musicxml);
      musicxml.processNotePartwise(new SampleHandler());
      return dds.toWrapper();
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
	