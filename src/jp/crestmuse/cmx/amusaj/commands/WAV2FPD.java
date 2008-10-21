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


public class WAV2FPD extends AbstractWAVAnalyzer {
  private double nnFrom = Double.NaN, nnThru = Double.NaN, step = Double.NaN;
  private boolean paramSet = false;

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-from") || option.equals("-f")) {
      params.put("NOTENUMBER_FROM", Double.valueOf(value));
      return true;
    } else if (option.equals("-thru") || option.equals("-t")) {
      params.put("NOTENUMBER_THRU", Double.valueOf(value));
      return true;
    } else if (option.equals("-step")) {
      params.put("STEP", Double.valueOf(value));
      return true;
    } else {
      return false;
    }
  }

  protected void analyzeWaveform(AudioDataCompatible wav, 
                                 WindowSlider winslider, 
                                 SPExecutor exec)  throws IOException,
    ParserConfigurationException,SAXException,TransformerException {
    exec.addSPModule(winslider);
    STFT stft = new STFT();
    stft.setStereo(winslider.isStereo());
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    int ch = winslider.getOutputChannels();
    for (int i = 0; i < ch; i++) {
      exec.connect(winslider, i, stft, i);
      exec.connect(stft, i, peakext, i);
    }
    F0PDFCalculatorModule f0calc = 
      new F0PDFCalculatorModule();
    exec.addSPModule(f0calc);
    exec.connect(peakext, 0, f0calc, 0);
//    try {
      exec.start();
      prepareOutputData(FPDXMLWrapper.TOP_TAG);
      TimeSeriesCompatible ts = 
        (TimeSeriesCompatible)exec.getResult(f0calc).get(0);
//      TimeSeriesCompatible ts = (TimeSeriesCompatible)exec.getResult(3).get(0);
//      FPDXMLWrapper fpd = 
//        (FPDXMLWrapper)CMXFileWrapper.createDocument(FPDXMLWrapper.TOP_TAG);
//      AmusaDataSet<TimeSeriesCompatible> dataset = fpd.createDataSet();
      addOutputData(ts);
//      dataset.add(ts);
//      dataset.setHeaders(params);
//      dataset.addElementsToWrapper();
//      setOutputData(fpd);
//    } catch (InterruptedException e) {
//      showErrorMessage(e);
//    }
  }

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
