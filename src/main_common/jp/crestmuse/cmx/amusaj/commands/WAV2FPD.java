package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.sp.F0PDFCalculatorModule;
import jp.crestmuse.cmx.amusaj.sp.PeakExtractor;
import jp.crestmuse.cmx.amusaj.sp.ProducerConsumerCompatible;
import jp.crestmuse.cmx.amusaj.sp.STFT;
import jp.crestmuse.cmx.math.DoubleArrayFactory;

/** WAV2FPD, given a WAV file, calculates the F0 predominance function 
    at every F0 in a given pitch range. 
    To execute this class, the implementation of F0PDFCalculator is 
    required.
    @author Tetsuro Kitahara
*/

public class WAV2FPD extends AbstractWAVAnalyzer {
//  private double nnFrom = Double.NaN, nnThru = Double.NaN, step = Double.NaN;
//  private String filterName = null;
  private boolean paramSet = false;

  private ProducerConsumerCompatible stft, peakext, f0calc;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  static {
    addOptionHelpMessage("-f <nn>", "lower bound note number for analysis");
    addOptionHelpMessage("-t <nn>", "uppper bound note number for analysis");
    addOptionHelpMessage("-step <nn>", "");
    addOptionHelpMessage("-filter <filter_name>", "filter used in calculating F0PDF");
  }

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (OptionUtils.setF0PDFOptions(option, value)) {
      return true;
    } else {
      return false;
    }
  }

/*
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
*/

  protected ProducerConsumerCompatible[] getUsedModules() {
    return new ProducerConsumerCompatible[] {
      stft = new STFT(usesStereo()), 
      peakext = new PeakExtractor(), 
      f0calc = new F0PDFCalculatorModule()
    }; 
  }

  protected ModuleConnection[] getModuleConnections() {
    return new ModuleConnection[] {
      new ModuleConnection(getWindowSlider(), 0, stft, 0), 
      new ModuleConnection(stft, 0, peakext, 0), 
      new ModuleConnection(stft, 1, peakext, 1), 
      new ModuleConnection(stft, 2, peakext, 2), 
      new ModuleConnection(peakext, 0, f0calc, 0)
    };
  }
  
  protected String getAmusaXMLFormat() {
    return "array";
  }

  protected OutputData[] getOutputData() {
    return new OutputData[] {
      new OutputData(f0calc, 0)
    };
  }

/*
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
*/


//  private static final int WINSIZE = 7;

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
