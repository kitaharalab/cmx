package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;


public class MakeDeadpanSCC extends CMXCommand<MusicXMLWrapper,SCCXMLWrapper> {
  public SCCXMLWrapper run(MusicXMLWrapper musicxml) throws IOException, ParserConfigurationException, TransformerException, SAXException {
    return musicxml.makeDeadpanSCCXML(480);
//    newOutputData("scc");
//    ((MusicXMLWrapper)indata()).makeDeadpanSCCXML((SCCXMLWrapper)outdata(), 480);   
  }
  public static void main(String[] args) {
    MakeDeadpanSCC m = new MakeDeadpanSCC();
    try {
      m.start(args);
    } catch (Exception e) {
      m.showErrorMessage(e);
      System.exit(1);
    }
  }
}