package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;

public class SMF2SCC extends CMXCommand {

  protected final CMXFileWrapper readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException {
      return MIDIXMLWrapper.readSMF(filename);
  }

    protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
//      setOutputData(((MIDIXMLWrapper)indata()).toSCCXML());
      setOutputData(((MIDIXMLWrapper)indata()).toSCC().toWrapper());
    }

    public static void main(String[] args) {
      SMF2SCC s2s = new SMF2SCC();
      try {
        s2s.start(args);
      } catch (Exception e) {
        s2s.showErrorMessage(e);
        System.exit(1);
      }
    }
}