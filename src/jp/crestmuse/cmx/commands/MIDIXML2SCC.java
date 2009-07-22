package jp.crestmuse.cmx.commands;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.filewrappers.*;

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
