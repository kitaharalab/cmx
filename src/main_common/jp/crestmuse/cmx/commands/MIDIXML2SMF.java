package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;

public class MIDIXML2SMF extends CMXCommand {

    private String smffilename = null;

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
      ((MIDIXMLWrapper)indata()).writefileAsSMF(smffilename);
    }

    public static void main(String[] args) {
        MIDIXML2SMF m2m = new MIDIXML2SMF();
	try {
	    m2m.start(args);
	} catch (Exception e) {
	    m2m.showErrorMessage(e);
	    System.exit(1);
	}
    }
}