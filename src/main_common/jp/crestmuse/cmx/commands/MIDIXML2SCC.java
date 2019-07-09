package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;

public class MIDIXML2SCC extends CMXCommand<MIDIXMLWrapper,SCCXMLWrapper> {
  protected SCCXMLWrapper run(MIDIXMLWrapper indata) 
    throws IOException, ParserConfigurationException, 
    TransformerException, SAXException {
    return indata.toSCCXML();
  }
  public static void main(String[] args) {
    MIDIXML2SCC m2s = new MIDIXML2SCC();
    try {
      m2s.start(args);
    } catch (Exception e) {
      m2s.showErrorMessage(e);
      System.exit(1);
    }
  }
}
