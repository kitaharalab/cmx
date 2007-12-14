package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import java.io.*;

public class ExtraNoteTest extends CMXCommand {
  protected void run() throws TransformerException, ParserConfigurationException, IOException, SAXException {
    MusicXMLWrapper musicxml = (MusicXMLWrapper)indata();
    DeviationInstanceWrapper dev = 
      DeviationInstanceWrapper.createDeviationInstanceFor(musicxml);
    DeviationDataSet dds = dev.createDeviationDataSet();
    dds.addExtraNote("P1", 1, 1, "C", 0, 2, 4, 1, 1);
    dds.addExtraNote("P1", 2, 1, 84, 0.5, 1, 1);
    dds.addExtraNote("P1", 2, 1.5, 84, 0.5, 1, 1);
    dds.addExtraNote("P1", 2, 2, 84, 0.5, 1, 1);
    dds.addExtraNote("P1", 2, 2.5, 84, 0.5, 1, 1);

    dds.addElementsToWrapper();
    dev.finalizeDocument();
//    setOutputData(dev);
    setOutputData(dev.toSCCXML(480));
  }

  public static void main(String[] args) {
    try {
      ExtraNoteTest t = new ExtraNoteTest();
      t.start(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}