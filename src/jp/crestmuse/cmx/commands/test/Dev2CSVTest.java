package jp.crestmuse.cmx.commands.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.commands.CMXCommand;
import jp.crestmuse.cmx.filewrappers.CSVWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;

public class Dev2CSVTest extends
    CMXCommand<DeviationInstanceWrapper, CSVWrapper> {
  
  private int devisionPerBeat = 1;
  private int devisionPerWindow = 1;
  
  protected boolean setOptionsLocal(String option, String value) {
    if(option.equals("-dpb")){
      devisionPerBeat = Integer.parseInt(value);
      return true;
    }else if(option.equals("-dpw")){
      devisionPerWindow = Integer.parseInt(value);
      return true;
    }
    return false;
  }

  protected CSVWrapper run(DeviationInstanceWrapper f) throws IOException,
      ParserConfigurationException, SAXException, TransformerException,
      InvalidFileTypeException {
    return f.toCSV(devisionPerBeat, devisionPerWindow);
  }

  public static void main(String[] args) {
    Dev2CSVTest d2c = new Dev2CSVTest();
    try {
      d2c.start(args);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
