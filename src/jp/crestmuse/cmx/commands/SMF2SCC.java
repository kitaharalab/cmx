package jp.crestmuse.cmx.commands;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.filewrappers.*;

public class SMF2SCC extends CMXCommand {

  protected final CMXFileWrapper readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException {
      return MIDIXMLWrapper.readSMF(filename);
  }

    protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
      setOutputData(((MIDIXMLWrapper)indata()).toSCCXML());
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