package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;

public class TempoTest extends CMXCommand {
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

  protected void run() throws TransformerException, ParserConfigurationException, IOException, SAXException {
    DeviationInstanceWrapper dev = (DeviationInstanceWrapper)indata();
    TreeView<DeviationInstanceWrapper.Control> ctrlview = 
      dev.getNonPartwiseControlView();
    DeviationInstanceWrapper.Control c = ctrlview.getRoot();
    while (ctrlview.hasElementsAtNextTime()) {
      printTempo(ctrlview.getFirstElementAtNextTime());
      while (ctrlview.hasMoreElementsAtSameTime()) {
        printTempo(ctrlview.getNextElementAtSameTime());
      }
    }
  }

  private void printTempo(DeviationInstanceWrapper.Control c) {
    if (c.type().equals("tempo-deviation"))
      System.out.println(c.value());
  }

  public static void main(String[] args) {
    try {
      TempoTest t = new TempoTest();
      t.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}