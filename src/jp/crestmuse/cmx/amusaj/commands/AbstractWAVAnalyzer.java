package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import javax.sound.sampled.*;
import org.xml.sax.*;

public abstract class AbstractWAVAnalyzer 
  extends CMXCommand<WAVWrapper,AmusaDataSetCompatible> {
//  protected Map<String,String> params = new HashMap<String,String>();
  private AmusaDataSet dataset = null;
  WindowSlider winslider = null;
  private boolean fromMic = false;

  static {
      addOptionHelpMessage("-winsize <winsize>", "window size in STFT");
      addOptionHelpMessage("-wintype {hamming|hanning|gaussian}", "window type in STFT");
      addOptionHelpMessage("-shift <value>", "shift size in STFT");
//      addOptionHelpMessage("-ch <nn>", "target channel");
  }

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (OptionUtils.setFFTOptions(option, value)) {
      return true;
    } else {
      return false;
    }
  }

/*
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
*/

  protected boolean setBoolOptionsLocal(String option) {
    if (super.setBoolOptionsLocal(option)) {
      return true;
    } else if (option.equals("-mic")) {
      fromMic = true;
      return true;
    } else {
      return false;
    }
  }

  protected String getParam(String category, String key) {
    return AmusaParameterSet.getInstance().getParam(category, key);
  }

  protected int getParamInt(String category, String key) {
    return AmusaParameterSet.getInstance().getParamInt(category, key);
  }

  protected double getParamDouble(String category, String key) {
    return AmusaParameterSet.getInstance().getParamDouble(category, key);
  }

  protected void setParam(String category, String key, String value) {
    AmusaParameterSet.getInstance().setParam(category, key, value);
  }

  protected void setParam(String category, String key, int value) {
    AmusaParameterSet.getInstance().setParam(category, key, value);
  }

  protected void setParam(String category, String key, double value) {
    AmusaParameterSet.getInstance().setParam(category, key, value);
  }

  protected int requiredFiles() {
    return fromMic ? 0 : 1;
  }

  protected boolean usesStereo() {
    return false;
  }

//  protected int winsize() {
//    return AmusaParameterSet.getInstance().getParamInt("fft", "WINDOW_SIZE");
////    return Integer.valueOf(params.get("WINDOW_SIZE"));
//  }

  protected FileWrapperCompatible 
  readInputData(String filename) throws IOException{
    return WAVWrapper.readfile(filename);
//    return WAVXMLWrapper.readWAV(filename);
  }

  protected void preproc() throws IOException, ParserConfigurationException, TransformerException, SAXException  {
    AmusaParameterSet.getInstance().setAnotherParameterSet(CMXCommand.getConfigXMLWrapper());
  }

  protected AmusaDataSetCompatible run(WAVWrapper wav) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException {
    SPExecutor ex = new SPExecutor();
    winslider = new WindowSlider(usesStereo());
    if (wav != null) {
      winslider.setInputData(wav);
    } else {
      try {
        AudioInputStreamWrapper audioin = AudioInputStreamWrapper.createWrapper8(16000);
        winslider.setInputData(audioin);
        audioin.getLine().start();
      } catch (LineUnavailableException e) {
        throw new IOException();
      }
    }
    ex.addSPModule(winslider);
    for (ProducerConsumerCompatible module : getUsedModules()) 
      ex.addSPModule(module);
    for (ModuleConnection mc : getModuleConnections())
      ex.connect(mc.inModule, mc.inCh, mc.outModule, mc.outCh);
    AmusaDataSet dataset = new AmusaDataSet(getAmusaXMLFormat());
    for (OutputData outdata : getOutputData()) {
      TimeSeriesCompatible ts = 
        ex.getResult(outdata.module).get(outdata.ch);
//      for (Map.Entry<String,String> entry : outdata.attrs.entrySet())
//        ts.setAttribute(entry.getKey(), entry.getValue());
      dataset.add(ts);
    }
    customSetting(ex, dataset);
    ex.start();
    return dataset;
//    return analyzeWaveform(wav, winslider, ex);
  }

  protected abstract ProducerConsumerCompatible[] getUsedModules();

  protected abstract ModuleConnection[] getModuleConnections();

  /** "array" or "peaks" */
  protected abstract String getAmusaXMLFormat();

  protected abstract OutputData[] getOutputData();

  protected void customSetting(SPExecutor ex, AmusaDataSetCompatible dataset) {
    // do nothing 
  }

  protected WindowSlider getWindowSlider() {
    return winslider;
  }

//  protected abstract AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
//                                          WindowSlider winslider, 
//                                          SPExecutor exec) 
//    throws IOException,ParserConfigurationException,
//    TransformerException,SAXException;

  protected class OutputData {
    ProducerConsumerCompatible module;
    int ch;
//    Map<String,String> attrs;
    protected OutputData(ProducerConsumerCompatible module, int ch) {
      this.module = module;
      this.ch = ch;
//      attrs = new HashMap<String,String>();
    }
//    protected void setAttribute(String key, String value) {
//      attrs.put(key, value);
//    }
//    protected void setAttribute(String key, int value) {
//      attrs.put(key, String.valueOf(value));
//    }
//    protected void setAttribute(String key, double value) {
//      attrs.put(key, String.valueOf(value));
//    }
  }


}
