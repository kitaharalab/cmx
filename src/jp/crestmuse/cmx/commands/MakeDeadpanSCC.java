package jp.crestmuse.cmx.commands;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.filewrappers.*;


public class MakeDeadpanSCC extends CMXCommand {
  public void run() throws IOException, ParserConfigurationException,
    TransformerException, SAXException {
    newOutputData("scc");
    ((MusicXMLWrapper)indata()).makeDeadpanSCCXML((SCCXMLWrapper)outdata(), 480);   
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