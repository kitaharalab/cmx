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

public abstract class AbstractWAVAnalyzer extends CMXCommand {
  protected Map<String,Object> params = new HashMap<String,Object>();
  private AmusaDataSet dataset = null;

  protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-winsize")) {
      params.put("WINDOW_SIZE", Integer.valueOf(value));
      return true;
    } else if (option.equals("-wintype")) {
      params.put("WINDOW_TYPE", Integer.valueOf(value));
      return true;
    } else if (option.equals("-shift")) {
      params.put("SHIFT", Double.valueOf(value));
      return true;
    } else if (option.equals("-ch")) {
      if (value.equals("mix"))
        params.put("TARGET_CHANNEL", -1);
      else
        params.put("TARGET_CHANNEL", Integer.valueOf(value));
      return true;
    } else {
      return false;
    }
  }

  protected FileWrapperCompatible 
  readInputData(String filename) throws IOException{
    return WAVWrapper.readfile(filename);
//    return WAVXMLWrapper.readWAV(filename);
  }

  protected void run() throws IOException,ParserConfigurationException,
    TransformerException,SAXException {
    WAVWrapper wav = (WAVWrapper)indata();
//    WAVXMLWrapper wav = (WAVXMLWrapper)indata();  
    WindowSlider winslider = new WindowSlider();
    winslider.setParams(params);
//    AudioDataCompatible audiodata = wav.getDataChunkList()[0].getAudioData();
//    winslider.setInputData(audiodata);           // originally wav
    winslider.setInputData(wav);
//    STFT stft = new STFT();
//    stft.setParams(params);
//    stft.setInputData(wav);
//    stft.setInputData(wav.getDataChunkList()[0].getAudioData());
    int nFrames = winslider.getAvailableFrames();
    int timeunit = winslider.getTimeUnit();
//    int nFrames = stft.getAvailableFrames();
//    int timeunit = stft.getTimeUnit();
    SPExecutor ex = new SPExecutor(params, nFrames, timeunit);
//    analyzeWaveform(audiodata, winslider, ex);
    analyzeWaveform(wav, winslider, ex);
//    analyzeWaveform(wav, stft, ex);
    if (dataset != null) {
      dataset.setHeaders(params);
      dataset.addElementsToWrapper();
    }
  }

  protected void prepareOutputData(String toptag) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException {
    newOutputData(toptag);
    dataset = ((AmusaXMLWrapper)outdata()).createDataSet();
  }

  protected void addOutputData(AmusaDataCompatible data) {
    dataset.add(data);
  }

  protected abstract void analyzeWaveform(AudioDataCompatible wav, 
                                          WindowSlider winslider, 
                                          SPExecutor exec) 
    throws IOException,ParserConfigurationException,
    TransformerException,SAXException;

}
