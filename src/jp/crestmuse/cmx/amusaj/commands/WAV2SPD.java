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
//    private static int winsize = 0;
//    private static String wintype = null;
//    private static double[] window;
//    private static double shift = Double.NaN;
//    private static double powerthrs = 0;
//    private static double rpowerthrs = 0;
//    private static boolean paramSet = false;
//    private static int ch = 0;
//    private static PeakExtractor peakext;
//  private Map<String,Object> params = new HashMap<String,Object>();
//  private static final STFTFactory factory = STFTFactory.getFactory();
//  private static STFT stft;

//    protected boolean setOptionsLocal(String option, String value) {
//	return setOptionsStatic(option, value);
//    }

/*
    protected boolean setOptionsLocal(String option, String value) {
	if (option.equals("-winsize")) {
//	    winsize = Integer.parseInt(value);
          params.put("WINDOW_SIZE", Integer.valueOf(value));
	    return true;
	} else if (option.equals("-wintype")) {
//	    wintype = value.toLowerCase();
          params.put("WINDOW_TYPE", Integer.valueOf(value));
	    return true;
	} else if (option.equals("-shift")) {
//	    shift = Double.parseDouble(value);
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
*/

/*
  private static void setParams() {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
    if (winsize == 0)
      winsize = config.getParamInt("param", "fft", "WINDOW_SIZE");
    if (wintype == null)
      wintype = config.getParam("param", "fft", "WINDOW_TYPE").toLowerCase();
    if (wintype.startsWith("ham"))
      window = hamming(winsize);
    else if (wintype.startsWith("hum"))
      window = hanning(winsize);
    else if (wintype.startsWith("gaus"))
      window = gaussian(winsize);
    else
      throw new ConfigXMLException
        ("WINDOW_TYPE is not found or wrong.");
    if (Double.isNaN(shift))
      shift = config.getParamDouble("param", "fft", "SHIFT");
    powerthrs = config.getParamDouble("param", "fft", "POWER_THRESHOLD");
    rpowerthrs = config.getParamDouble("param", "fft", 
				       "RELATIVE_POWER_THRESHOLD");
    peakext = new PeakExtractor();
    stft = factory.createSTFT();
    paramSet = true;
  }
*/

/*
  static PeakExtractor getPeakExtractor(WAVXMLWrapper wav) {
    int fs = (int)wav.getFmtChunk().sampleRate();
	double[][][] fftresults = executeSTFT(wav);
	//	PeakExtractor pe = new PeakExtractor();
	peakext.setFFTResult(fftresults[0], fftresults[1], fftresults[2], 
			fs, winsize);
	return peakext;
    }
*/

/*
  protected CMXFileWrapper readInputData(String filename) throws IOException,
    ParserConfigurationException,SAXException,TransformerException {
    return WAVXMLWrapper.readWAV(filename);
  }
*/

/*
  protected void run() throws IOException,ParserConfigurationException,
    TransformerException,SAXException{
    WAVXMLWrapper wav = (WAVXMLWrapper)indata();
//    int fs = wav.getFmtChunk().sampleRate();
//    params.put("SAMPLE_RATE", fs);
    STFT stft = new STFT();
    stft.setParams(params);
    stft.setInputData(wav);
    int nFrames = stft.getAvailableFrames();
    int timeunit = stft.getTimeUnit();
    SPExecutor ex = new SPExecutor(params, nFrames, timeunit);
    ex.addSPModule(stft);
//    MutableTimeSeries ts = new MutableTimeSeries(nFrames, timeunit);
//    QueueReader<DoubleArray> q = ts.getQueueReader();
    PeakExtractor peakext = new PeakExtractor();
    ex.addSPModule(peakext);
    ex.connect(stft, 0, peakext, 0);
    if (stft.getOutputChannels() > 1) {
      ex.connect(stft, 1, peakext, 1);
      ex.connect(stft, 2, peakext, 2);
    }
    try {
      ex.start();
    } catch (InterruptedException e) {}
    PeaksCompatible peaks = (PeaksCompatible)ex.getResult(1).get(0);
//    peakext.setParams(params);
//    MutablePeaks peaks = new MutablePeaks(nFrames, timeunit);
//    List<TimeSeriesCompatible> l1 = new ArrayList<TimeSeriesCompatible>();
//    l1.add(ts);
//    List<PeaksCompatible> l2 = new ArrayList<PeaksCompatible>();
//    l2.add(peaks);
//    List<QueueReader<DoubleArray>> l3 = new ArrayList<QueueReader<DoubleArray>>();
//    l3.add(q);
//    try {
//      for (int t = 0; t < nFrames; t++) {
//        stft.execute(null, l1);
//        peakext.execute(l3, l2);
//      }
//    } catch (InterruptedException e) {}
    
    SPDXMLWrapper spd = 
      (SPDXMLWrapper)CMXFileWrapper.createDocument(SPDXMLWrapper.TOP_TAG);
    AmusaDataSet<PeaksCompatible> dataset = spd.createDataSet();
    dataset.add(peaks);
    dataset.setHeaders(params);
    dataset.addElementsToWrapper();
    setOutputData(spd);
  }
*/

  protected void analyzeWaveform(AudioDataCompatible wav, 
                                 WindowSlider winslider, 
                            SPExecutor exec)
    throws IOException,
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
    try {
      exec.start();
      prepareOutputData(SPDXMLWrapper.TOP_TAG);
      TimeSeriesCompatible<PeakSet> peaks = 
        (TimeSeriesCompatible<PeakSet>)exec.getResult(peakext).get(0);
//      PeaksCompatible peaks = (PeaksCompatible)exec.getResult(1).get(0);
//      SPDXMLWrapper spd = 
//        (SPDXMLWrapper)CMXFileWrapper.createDocument(SPDXMLWrapper.TOP_TAG);
//      AmusaDataSet<PeaksCompatible> dataset = spd.createDataSet();
      addOutputData(peaks);
//      dataset.add(peaks);
//      dataset.setHeaders(params);
//      dataset.addElementsToWrapper();
//      setOutputData(spd);
    } catch (InterruptedException e) {
      showErrorMessage(e);
    }
  }


/*
  protected void run() throws IOException,ParserConfigurationException,
    TransformerException,SAXException{
    WAVXMLWrapper wav = (WAVXMLWrapper)indata();
    int fs = wav.getFmtChunk().sampleRate();
    SPDXMLWrapper spd = 
      (SPDXMLWrapper)CMXFileWrapper.createDocument(SPDXMLWrapper.TOP_TAG);
    AmusaDataSet<PeaksCompatible> dataset = spd.createDataSet();
    double[][][] fftresults = executeSTFT(wav);
    peakext.setFFTResult(fftresults[0], fftresults[1], fftresults[2], 
                         fs, winsize);
    MutablePeaks peaks = new MutablePeaks(peakext.nFrames(), 
                                          (int)(1000 * winsize / shift));
    try {
      int nFrames = peakext.nFrames();
      for (int i = 0; i < nFrames; i++)
        peakext.execute(peaks);
    } catch (InterruptedException e) {}
    dataset.add(peaks);
    dataset.setHeader("NUM_OF_FRAMES", peakext.nFrames());
    dataset.setHeader("SAMPLE_RATE", peakext.sampleRate());
    dataset.setHeader("PITCH_UNIT", "Hz");
    dataset.setHeader("SHIFT", (int)shift);
    dataset.setHeader("WINDOW_SIZE", winsize);
    dataset.setHeader("WINDOW_TYPE", wintype);
    dataset.addElementsToWrapper();
    setOutputData(spd);
  }
*/

/*
    public static double[][][] executeSTFT(WAVXMLWrapper wavxml) {
	if (!paramSet)
	    setParams();
	int channels = wavxml.getFmtChunk().channels();
	int fs = (int)wavxml.getFmtChunk().sampleRate();
	if (shift < 1)
	    shift = shift * fs;
	int shift_ = (int)shift;
        DoubleArray[] wav = wavxml.getDataChunkList()[0].getAudioData().
          getDoubleArrayWaveform();
	double[][][] fftresults = new double[3][][];
	if (ch == 0 && channels == 2) {
          DoubleArray wavM = add(wav[0], wav[1]);
          divX(wavM, 2);
          fftresults[1] = stft.executeR2C(wav[0].toArray(), window, shift_);
          fftresults[2] = stft.executeR2C(wav[1].toArray(), window, shift_);
          fftresults[0] = stft.executeR2C(wavM.toArray(), window, shift_);
	} else if (ch == -1 && channels == 2) {
          DoubleArray wavM = add(wav[0], wav[1]);
          divX(wavM, 2);
          fftresults[0] = stft.executeR2C(wavM.toArray(), window, shift_);
          fftresults[1] = null;
          fftresults[2] = null;
	} else {
          fftresults[0] = stft.executeR2C(wav[ch].toArray(),
                                          window, shift_);
          fftresults[1] = null;
          fftresults[2] = null;
	}
	return fftresults;
    }
*/

/*
    public static class Parameters {
	public static final int winsize() { return winsize; }
	public static final String wintype()  { return wintype; }
    }
*/


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
