package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


public class WAV2FPD extends AbstractWAVAnalyzer {
  private double nnFrom = Double.NaN, nnThru = Double.NaN, step = Double.NaN;
  private String filterName = null;
  private boolean paramSet = false;

    private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

    static {
	addOptionHelpMessage("-f <nn>", "lower bound note number for analysis");
	addOptionHelpMessage("-t <nn>", "uppper bound note number for analysis");
	addOptionHelpMessage("-step <nn>", "");
	addOptionHelpMessage("-filter <filter_name>", "filter used in calculating F0PDF");
    }


  protected boolean setOptionsLocal(String option, String value) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-from") || option.equals("-f")) {
      params.setParam("f0pdf", "NOTENUMBER_FROM", value);
      return true;
    } else if (option.equals("-thru") || option.equals("-t")) {
      params.setParam("f0pdf", "NOTENUMBER_THRU", value);
      return true;
    } else if (option.equals("-step")) {
      params.setParam("f0pdf", "STEP", value);
      return true;
    } else if (option.equals("-filter")) {
      params.setParam("f0pdf", "FILTER_NAME", value);
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
    STFT stft = new STFT(usesStereo());
//    stft.setStereo(winslider.isStereo());
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    int ch = winslider.getOutputClasses().length;
    for (int i = 0; i < ch; i++) {
      exec.connect(winslider, i, stft, i);
      exec.connect(stft, i, peakext, i);
    }
    F0PDFCalculatorModule f0calc = new F0PDFCalculatorModule();
    exec.addSPModule(f0calc);
    exec.connect(peakext, 0, f0calc, 0);
    exec.start();
    TimeSeriesCompatible ts = 
	(TimeSeriesCompatible)exec.getResult(f0calc).get(0);
    AmusaDataSet dataset = new AmusaDataSet("array");
    dataset.add(ts);
    return dataset;
  }

  private static final int WINSIZE = 7;

  public static void main(String[] args) {
    WAV2FPD wav2fpd = new WAV2FPD();
    try {
      wav2fpd.start(args);
    } catch (Exception e) {
      wav2fpd.showErrorMessage(e);
      System.exit(1);
    }
  }
}
