package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public abstract class AbstractWAVAnalyzer 
  extends CMXCommand<WAVWrapper,AmusaXMLWrapper> {
  protected Map<String,String> params = new HashMap<String,String>();
  private AmusaDataSet dataset = null;

  protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-winsize")) {
      params.put("WINDOW_SIZE", String.valueOf(value));
      return true;
    } else if (option.equals("-wintype")) {
      params.put("WINDOW_TYPE", String.valueOf(value));
      return true;
    } else if (option.equals("-shift")) {
      params.put("SHIFT", String.valueOf(value));
      return true;
    } else if (option.equals("-ch")) {
      if (value.equals("mix"))
        params.put("TARGET_CHANNEL", "-1");
      else
        params.put("TARGET_CHANNEL", String.valueOf(value));
      return true;
    } else {
      return false;
    }
  }

  protected FileWrapperCompatible 
  readInputData(String filename) throws IOException{
    return WAVWrapper.readfile(filename);
//    return WAVXMLWrapper.readWAV(filename);
  }

  protected AmusaXMLWrapper run(WAVWrapper wav) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException {
    WindowSlider winslider = new WindowSlider();
    winslider.setParams(params);
    winslider.setInputData(wav);
    int nFrames = winslider.getAvailableFrames();
    int timeunit = winslider.getTimeUnit();
    SPExecutor ex = new SPExecutor(params, nFrames, timeunit);
    return analyzeWaveform(wav, winslider, ex);
  }

  protected abstract AmusaXMLWrapper analyzeWaveform(AudioDataCompatible wav, 
                                          WindowSlider winslider, 
                                          SPExecutor exec) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException;

}
