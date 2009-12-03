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
  extends CMXCommand<WAVWrapper,AmusaDataSetCompatible> {
//  protected Map<String,String> params = new HashMap<String,String>();
  private AmusaDataSet dataset = null;

  static {
      addOptionHelpMessage("-winsize <winsize>", "window size in STFT");
      addOptionHelpMessage("-wintype {hamming|hanning|gaussian}", "window type in STFT");
      addOptionHelpMessage("-shift <value>", "shift size in STFT");
//      addOptionHelpMessage("-ch <nn>", "target channel");
  }

  protected boolean setOptionsLocal(String option, String value) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    if (option.equals("-winsize")) {
      params.setParam("fft", "WINDOW_SIZE", value);
//      params.put("WINDOW_SIZE", String.valueOf(value));
      return true;
    } else if (option.equals("-wintype")) {
      params.setParam("fft", "WINDOW_TYPE", value);
//      params.put("WINDOW_TYPE", String.valueOf(value));
      return true;
    } else if (option.equals("-shift")) {
      params.setParam("fft", "SHIFT", value);
//      params.put("SHIFT", String.valueOf(value));
      return true;
    } else if (option.equals("-ch")) {
      params.setParam("fft", "TARGET_CHANNEL", value);
//      params.put("TARGET_CHANNEL", value);
      return true;
    } else {
      return false;
    }
  }

  protected boolean usesStereo() {
    return false;
  }

  protected int winsize() {
    return AmusaParameterSet.getInstance().getParamInt("fft", "WINDOW_SIZE");
//    return Integer.valueOf(params.get("WINDOW_SIZE"));
  }

  protected FileWrapperCompatible 
  readInputData(String filename) throws IOException{
    return WAVWrapper.readfile(filename);
//    return WAVXMLWrapper.readWAV(filename);
  }

  protected void preproc() {
    AmusaParameterSet.getInstance().setAnotherParameterSet(CMXCommand.getConfigXMLWrapper());
  }

  protected AmusaDataSetCompatible run(WAVWrapper wav) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException {
    WindowSlider winslider = new WindowSlider(usesStereo());
//    winslider.setParams(params);
    winslider.setInputData(wav);
//    int nFrames = winslider.getAvailableFrames();
//    int timeunit = winslider.getTimeUnit();
//    SPExecutor ex = new SPExecutor(params, timeunit);
    SPExecutor ex = new SPExecutor();
    return analyzeWaveform(wav, winslider, ex);
  }

  protected abstract AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
                                          WindowSlider winslider, 
                                          SPExecutor exec) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException;

}
