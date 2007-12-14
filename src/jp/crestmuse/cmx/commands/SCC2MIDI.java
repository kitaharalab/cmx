package jp.crestmuse.cmx.commands;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

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