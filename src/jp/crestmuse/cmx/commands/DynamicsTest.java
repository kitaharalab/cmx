package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;

public class DynamicsTest extends CMXCommand {
  private String dirname = null;

  protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-targetdir")) {
      dirname = value;
      DeviationInstanceWrapper.changeDefaultMusicXMLDirName(dirname);
      return true;
    } else {
      return false;
    }
  }

//  public void init(CMXFileWrapper f) {
//    if (f instanceof DeviationInstanceWrapper)
//    if (dirname != null)
//      ((DeviationInstanceWrapper)f).setTargetMusicXMLDirName(dirname);
//  }

  protected void run() throws TransformerException, SAXException, ParserConfigurationException, IOException{
    DeviationInstanceWrapper dev = (DeviationInstanceWrapper)indata();
    SCCXMLWrapper scc = dev.toSCCXML(480);
    TimeFreqRepresentation tfr = 
      TimeFreqRepresentation.getTimeFreqRepresentation(scc);
    tfr.println();
  }

  public static void main(String[] args) {
    try {
      DynamicsTest t = new DynamicsTest();
      t.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}