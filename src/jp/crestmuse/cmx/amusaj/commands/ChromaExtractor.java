package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


public class ChromaExtractor extends AbstractWAVAnalyzer {

    static {
	addOptionHelpMessage("-l <freq>", "lower bound frequency for analysis");
	addOptionHelpMessage("-h <freq>", "upper bound frequency for analysis");
    }
    
  protected boolean setOptionsLocal(String option, String value) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.startsWith("-l")) {
      params.setParam("chroma", "CHROMA_LOW_LIMIT_FREQ", value);
      return true;
    } else if (option.startsWith("-h")) {
      params.setParam("chroma", "CHROMA_HIGH_LIMIT_FREQ", value);
      return true;
    } else {
      return false;
    }
  }


  protected AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
                                            WindowSlider winslider, 
                                            SPExecutor exec)  
    throws IOException,
    ParserConfigurationException,SAXException,TransformerException {
    exec.addSPModule(winslider);
    STFT stft = new STFT();
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    int ch = winslider.getOutputClasses().length;
    for (int i = 0; i < ch; i++) {
      exec.connect(winslider, i, stft, i);
      exec.connect(stft, i, peakext, i);
    }
    jp.crestmuse.cmx.amusaj.sp.ChromaExtractor chroma = 
      new jp.crestmuse.cmx.amusaj.sp.ChromaExtractor();
    exec.addSPModule(chroma);
    exec.connect(peakext, 0, chroma, 0);
//    TimeCoarsener tc = new TimeCoarsener();
//    exec.addSPModule(tc);
//    exec.connect(chroma, 0, tc, 0);
    exec.start();
    TimeSeriesCompatible ts = 
      (TimeSeriesCompatible)exec.getResult(chroma).get(0);
    AmusaDataSet dataset = new AmusaDataSet("array");
    dataset.add(ts);
    return dataset;
//    return dataset.toWrapper();
  }

  public static void main(String[] args) {
    ChromaExtractor ce = new ChromaExtractor();
    try {
      ce.start(args);
    } catch (Exception e) {
      ce.showErrorMessage(e);
      System.exit(1);
    }
  }
}
