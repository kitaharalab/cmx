package jp.crestmuse.cmx.amusaj.commands;

import jp.crestmuse.cmx.amusaj.filewrappers.AmusaDataSetCompatible;
import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;
import jp.crestmuse.cmx.amusaj.sp.AmusaParameterSet;
import jp.crestmuse.cmx.amusaj.sp.FeatureExtractionModule2;
import jp.crestmuse.cmx.amusaj.sp.HarmonicsExtractor2;
import jp.crestmuse.cmx.amusaj.sp.HarmonicsTimeSeriesGenerator;
import jp.crestmuse.cmx.amusaj.sp.KitaharaFeatureExtractor;
import jp.crestmuse.cmx.amusaj.sp.PeakExtractor;
import jp.crestmuse.cmx.amusaj.sp.ProducerConsumerCompatible;
import jp.crestmuse.cmx.amusaj.sp.SPExecutor;
import jp.crestmuse.cmx.amusaj.sp.SPSpreadModule;
import jp.crestmuse.cmx.amusaj.sp.STFT;
import jp.crestmuse.cmx.amusaj.sp.TimeSeriesCompatible;
import jp.crestmuse.cmx.math.DoubleArrayFactory;

import static jp.crestmuse.cmx.math.Operations.nn2Hz;


public class WAV2TBD extends AbstractWAVAnalyzer {
//  private double nnFrom = Double.NaN, nnThru = Double.NaN, step = Double.NaN;
//  private String filterName = null;
  private boolean paramSet = false;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private ProducerConsumerCompatible stft, peakext, spread;

  static {
    addOptionHelpMessage("-f <nn>", "lower bound note number for analysis");
    addOptionHelpMessage("-t <nn>", "uppper bound note number for analysis");
    addOptionHelpMessage("-step <nn>", "");
//    addOptionHelpMessage("-filter <filter_name>", "filter used in calculating F0PDF");
  }


  protected boolean setOptionsLocal(String option, String value) {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-from") || option.equals("-f")) {
      params.setParam("feature", "NOTENUMBER_FROM", String.valueOf(value));
      return true;
    } else if (option.equals("-thru") || option.equals("-t")) {
      params.setParam("feature", "NOTENUMBER_THRU", String.valueOf(value));
      return true;
    } else if (option.equals("-step")) {
      params.setParam("feature", "STEP", String.valueOf(value));
      return true;
//    } else if (option.equals("-filter")) {
//      params.put("FILTER_NAME", value);
//      return true;
    } else {
      return false;
    }
  }

  protected ProducerConsumerCompatible[] getUsedModules() {
    int nnFrom = getParamInt("feature", "NOTENUMBER_FROM");
    int nnThru = getParamInt("feature", "NOTENUMBER_THRU");
    double step = getParamDouble("feature", "STEP");
    int nF0s = (int)((nnThru - nnFrom) * step);
    System.err.println(nF0s);
    return new ProducerConsumerCompatible[] {
      stft = new STFT(usesStereo()), 
      peakext = new PeakExtractor(), 
      spread = new SPSpreadModule(PeakSet.class, nF0s)
    };
  }

  protected ModuleConnection[] getModuleConnections() {
    return new ModuleConnection[] {
      new ModuleConnection(getWindowSlider(), 0, stft, 0), 
      new ModuleConnection(stft, 0, peakext, 0), 
      new ModuleConnection(stft, 1, peakext, 1), 
      new ModuleConnection(stft, 2, peakext, 2), 
      new ModuleConnection(peakext, 0, spread, 0)
    };
  }

  protected void customSetting(SPExecutor ex, AmusaDataSetCompatible dataset){
    int nnFrom = getParamInt("feature", "NOTENUMBER_FROM");
    int nnThru = getParamInt("feature", "NOTENUMBER_THRU");
    double step = getParamDouble("feature", "STEP");
    int nF0s = (int)((nnThru - nnFrom) * step);
    System.err.println(nF0s);
    for (int i = 0; i < nF0s; i++) {
      double f0 = nn2Hz(nnFrom + step * i);
      HarmonicsExtractor2 he = new HarmonicsExtractor2(f0);
      ex.addSPModule(he);
      ex.connect(spread, i, he, 0);
      HarmonicsTimeSeriesGenerator htsg = new HarmonicsTimeSeriesGenerator();
      ex.addSPModule(htsg);
      ex.connect(he, 0, htsg, 0);
      FeatureExtractionModule2 fe = new FeatureExtractionModule2
        (new KitaharaFeatureExtractor(3, 10));
      ex.addSPModule(fe);
      ex.connect(htsg, 0, fe, 0);
      TimeSeriesCompatible data = ex.getResult(fe).get(0);
      data.setAttribute("notenum", nnFrom + step * i);
//      System.err.println(data);
      dataset.add(data);
    }
  }

  protected String getAmusaXMLFormat() {
    return "array";
  }

  protected OutputData[] getOutputData() {
    return new OutputData[] {};
  }

/*
  protected ProducerConsumerCompatible[] getUsedModules() {
    stft = new STFT(usesStereo());
    peakext = new PeakExtractor();
    spread = new SPSpreadModule(PeakSet.class, nF0s);
    he = new HarmonicsExtractor2[nF0s];
    htsg = new HarmonicsTimeSeriesGenerator[nF0s];
    fe = new FeatureExtractionModule2[nF0s];
    for (int i = 0; i < nF0s; i++) {
      double f0 = nn2Hz(nnFrom + step * i);
      he[i] = new HarmonicsExtractor2(f0);
      htsg[i] = new HarmonicsTimeSeriesGenerator();
      fe[i] = new FeatureExtractionModule2(new KitaharaFeatureExtractor(3,10));
    }
    return (ProducerConsumerCompatible[])
      Misc.to1dimArray(new Object[] {stft, peakext, spread, 
                                     he, htsg, fe});
  }
*/

    
/*

  protected AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
                                                   WindowSlider winslider, 
                                                   SPExecutor exec)  
    throws IOException,
    ParserConfigurationException,SAXException,TransformerException {
    AmusaDataSet dataset = new AmusaDataSet("array");
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    int nnFrom = params.getParamInt("feature", "NOTENUMBER_FROM");
    int nnThru = params.getParamInt("feature", "NOTENUMBER_THRU");
    double step = params.getParamDouble("feature", "STEP");
    int n = (int)((nnThru - nnFrom) * step);
    exec.addSPModule(winslider);
    STFT stft = new STFT(usesStereo());
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    SPSpreadModule spread = new SPSpreadModule(PeakSet.class, n);
    exec.addSPModule(spread);
    exec.connect(winslider, 0, stft, 0);
    exec.connect(stft, 0, peakext, 0);
    exec.connect(stft, 1, peakext, 1);
    exec.connect(stft, 2, peakext, 2);
    exec.connect(peakext, 0, spread, 0);
    for (int i = 0; i < n; i++) {
      double f0 = nn2Hz(nnFrom + step * i);
      HarmonicsExtractor2 he = new HarmonicsExtractor2(f0);
      exec.addSPModule(he);
      exec.connect(spread, i, he, 0);
      HarmonicsTimeSeriesGenerator htsg = new HarmonicsTimeSeriesGenerator();
      exec.addSPModule(htsg);
      exec.connect(he, 0, htsg, 0);
      FeatureExtractionModule2 fe = new FeatureExtractionModule2
        (new KitaharaFeatureExtractor(3, 10));
      exec.addSPModule(fe);
      exec.connect(htsg, 0, fe, 0);
      TimeSeriesCompatible<SPElement> data = exec.getResult(fe).get(0);
      data.setAttribute("notenum", nnFrom + step * i);
      dataset.add(data);
    }
    exec.start();
    return dataset;
  }
*/


//  private static final int WINSIZE = 7;

  public static void main(String[] args) {
    WAV2TBD wav2tbd = new WAV2TBD();
    try {
      wav2tbd.start(args);
    } catch (Exception e) {
      wav2tbd.showErrorMessage(e);
      System.exit(1);
    }
  }
}
