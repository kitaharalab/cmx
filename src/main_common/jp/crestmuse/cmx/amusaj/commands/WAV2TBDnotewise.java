package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import static jp.crestmuse.cmx.math.Operations.*;

public class WAV2TBDnotewise extends NotewiseWAVAnalyzer {
  private boolean paramSet = false;

  private ProducerConsumerCompatible stft, peakext, he, htsg, fe;

  static {
    addOptionHelpMessage("-f <nn>", "lower bound note number for analysis");
    addOptionHelpMessage("-t <nn>", "uppper bound note number for analysis");
    addOptionHelpMessage("-step <nn>", "");
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
    } else {
      return false;
    }
  }

  protected ProducerConsumerCompatible[] getUsedModules(Note note) {
//    System.err.println(note.offsetInMilliSec() - note.onsetInMilliSec());
    return new ProducerConsumerCompatible[] {
      stft = new STFT(usesStereo()), 
      peakext = new PeakExtractor(), 
      he = new HarmonicsExtractor2(nn2Hz(note.notenum())), 
      htsg = new HarmonicsTimeSeriesGenerator(), 
      fe = new FeatureExtractionModule2(new KitaharaFeatureExtractor(3, 10))
    };
  }

  protected ModuleConnection[] getModuleConnections() {
    return new ModuleConnection[] {
      new ModuleConnection(getWindowSlider(), 0, stft, 0), 
      new ModuleConnection(stft, 0, peakext, 0), 
      new ModuleConnection(stft, 1, peakext, 1), 
      new ModuleConnection(stft, 2, peakext, 2), 
      new ModuleConnection(peakext, 0, he, 0), 
      new ModuleConnection(he, 0, htsg, 0), 
      new ModuleConnection(htsg, 0, fe, 0)
    };
  }

  protected String getAmusaXMLFormat() {
    return "array";
  }

  protected OutputData[] getOutputData() {
    return new OutputData[] { new OutputData(fe, 0) };
  }

  public static void main(String[] args) { 
    WAV2TBDnotewise wav2tbd = new WAV2TBDnotewise();
    try {
      wav2tbd.start(args);
    } catch (Exception e) {
      wav2tbd.showErrorMessage(e);
      System.exit(1);
    }
  }

}