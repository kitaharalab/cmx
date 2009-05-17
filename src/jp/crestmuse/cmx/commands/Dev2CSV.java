package jp.crestmuse.cmx.commands;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CSVWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;

public class Dev2CSV extends
    CMXCommand<DeviationInstanceWrapper, CSVWrapper> {
  
  private int devisionPerMeasure = 4;
  private int windowPerMeasure = 4;

  static {
      addOptionHelpMessage("-dpm <value>", "devisions per measure");
      addOptionHelpMessage("-wpm <value>", "window per measure");
      addOptionHelpMessage("-targetdir <dir>", "directory including target MusicXML file");
  }
  
  protected boolean setOptionsLocal(String option, String value) {
    if(option.equals("-dpm")){
      devisionPerMeasure = Integer.parseInt(value);
      return true;
    }else if(option.equals("-wpm")){
      windowPerMeasure = Integer.parseInt(value);
      return true;
    }else if(option.equals("-targetdir")){
      DeviationInstanceWrapper.changeDefaultMusicXMLDirName(value);
      return true;
    }
    return false;
  }

  protected CSVWrapper run(DeviationInstanceWrapper f) throws IOException,
      ParserConfigurationException, SAXException, TransformerException,
      InvalidFileTypeException {
    return f.toCSV(devisionPerMeasure, windowPerMeasure);
  }

  public static void main(String[] args) {
    Dev2CSV d2c = new Dev2CSV();
    try {
      d2c.start(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
