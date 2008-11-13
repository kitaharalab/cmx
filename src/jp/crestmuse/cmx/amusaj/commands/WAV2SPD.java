package jp.crestmuse.cmx.amusaj.commands;

import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import java.io.*;
import java.util.*;
import static java.lang.Math.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class WAV2SPD extends AbstractWAVAnalyzer {

  protected AmusaDataSetCompatible analyzeWaveform(AudioDataCompatible wav, 
                                            WindowSlider winslider, 
                                            SPExecutor exec)
    throws IOException,
    ParserConfigurationException,SAXException,TransformerException {
    exec.addSPModule(winslider);
    STFT stft = new STFT();
//    stft.setStereo(winslider.isStereo());
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    int ch = winslider.getOutputChannels();
    for (int i = 0; i < ch; i++) {
      exec.connect(winslider, i, stft, i);
      exec.connect(stft, i, peakext, i);
    }
      exec.start();
//      prepareOutputData(SPDXMLWrapper.TOP_TAG);
      TimeSeriesCompatible<PeakSet> peaks = 
        (TimeSeriesCompatible<PeakSet>)exec.getResult(peakext).get(0);
      AmusaDataSet dataset = new AmusaDataSet("peaks", exec.getParams());
      dataset.add(peaks);
      return dataset;
//      return dataset.toWrapper();
//      addOutputData(peaks);
  }


  public static void main(String[] args) {
    WAV2SPD wav2spd = new WAV2SPD();
    try {
      wav2spd.start(args);
    } catch (Exception e) {
      wav2spd.showErrorMessage(e);
      System.exit(1);
    }
  }

}
