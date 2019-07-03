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