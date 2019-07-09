package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;

public class SMF2MIDIXML extends CMXCommand<MIDIXMLWrapper,MIDIXMLWrapper> {

  protected final MIDIXMLWrapper readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException {
      return MIDIXMLWrapper.readSMF(filename);
  }

    protected MIDIXMLWrapper run(MIDIXMLWrapper indata) 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
	return indata;
    }

    public static void main(String[] args) {
      SMF2MIDIXML s2m = new SMF2MIDIXML();
      try {
        s2m.start(args);
      } catch (Exception e) {
        s2m.showErrorMessage(e);
        System.exit(1);
      }
    }
}