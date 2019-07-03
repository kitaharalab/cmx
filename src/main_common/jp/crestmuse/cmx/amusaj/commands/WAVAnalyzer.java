package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.filewrappers.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/*

public class WAVAnalyzer extends AbstractWAVAnalyzer {
  private String scriptName;
  private AmusaScriptWrapper script;

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-script")) {
      scriptName = value;
      return true;
    } else {
      return false;
    }
  }

  protected void preproc() throws IOException {
    script = (AmusaScriptWrapper)CMXFileWrapper.readfile(scriptName);
    String[] paramNameList = script.getParamNameList();
    for (String name : paramNameList)
      params.put(name, script.getParam(name));
  }

  protected AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
                                            WindowSlider winslider, 
                                            SPExecutor exec) 
    throws IOException, ParserConfigurationException, 
    SAXException, TransformerException {
    Map<String,String> moduleNames = script.getModules();
    Map<String,ProducerConsumerCompatible> modules
      = new LinkedHashMap<String,ProducerConsumerCompatible>();
    for (Map.Entry<String,String> e : moduleNames.entrySet()) {
      ProducerConsumerCompatible m;
      try {
        if (e.getValue().equals("jp.crestmuse.cmx.amusaj.sp.WindowSlider"))
          m = winslider;
        else
          m = (ProducerConsumerCompatible)Class.forName(e.getValue()).newInstance();
      } catch (ClassNotFoundException ce) {
        throw new IllegalStateException("unknown object: " + e.getValue());
      } catch (InstantiationException ie) {
        throw new IllegalStateException("unknown object: " + e.getValue());
      } catch (IllegalAccessException ie) {
        throw new IllegalStateException("unknown object: " + e.getValue());
      }
      modules.put(e.getKey(), m);
      System.err.println("Added " + m);
      exec.addSPModule(m);
    }
    AmusaScriptWrapper.Connection[] connections = script.getConnections();
    for (AmusaScriptWrapper.Connection c : connections) {
      exec.connect(modules.get(c.objFrom), c.chFrom, 
                   modules.get(c.objTo), c.chTo);
      System.err.println("Connected " + modules.get(c.objFrom) + " ch." + 
                         c.chFrom + " to " + modules.get(c.objTo) + " ch." + 
                         c.chTo);
    }
    exec.start();
//    exec.startSingleThread();
    AmusaDataSet dataset = new AmusaDataSet(script.getOutputFormat());
    AmusaScriptWrapper.Output[] outputs = script.getOutputs();
    for (AmusaScriptWrapper.Output o : outputs)
      dataset.add(exec.getResult(modules.get(o.object)).get(o.ch));
    return dataset;
//    return dataset.toWrapper();
  }

  public static void main(String[] args) {
    WAVAnalyzer w = new WAVAnalyzer();
    try {
      w.start(args);
    } catch (Exception e) {
      w.showErrorMessage(e);
      System.exit(1);
    }
  }
}


*/