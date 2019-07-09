package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;

public class SCC2MIDI extends CMXCommand {

    private String smffilename = null;

    private int ticksPerBeat;

    protected boolean setOptionsLocal(String option, String value) {
	if (option.equals("-smf")) {
	    smffilename = value;
	    return true;
	} else {
	    return false;
	}
    }

    protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
	//	readInputData();
	newOutputData("MIDIFile");
	((SCCXMLWrapper)indata()).toMIDIXML((MIDIXMLWrapper)outdata());
	//	writeOutputData();
	if (smffilename != null)
	    ((MIDIXMLWrapper)outdata()).writefileAsSMF(smffilename);
    }

    public static void main(String[] args) {
	SCC2MIDI s2m = new SCC2MIDI();
	try {
	    s2m.start(args);
	} catch (Exception e) {
	    s2m.showErrorMessage(e);
	    System.exit(1);
	}
    }
}