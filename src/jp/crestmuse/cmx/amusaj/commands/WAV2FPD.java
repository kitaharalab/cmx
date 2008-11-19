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


  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-from") || option.equals("-f")) {
      params.put("NOTENUMBER_FROM", String.valueOf(value));
      return true;
    } else if (option.equals("-thru") || option.equals("-t")) {
      params.put("NOTENUMBER_THRU", String.valueOf(value));
      return true;
    } else if (option.equals("-step")) {
      params.put("STEP", String.valueOf(value));
      return true;
    } else if (option.equals("-filter")) {
      params.put("FILTER_NAME", value);
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
//    stft.setStereo(winslider.isStereo());
    exec.addSPModule(stft);
    PeakExtractor peakext = new PeakExtractor();
    exec.addSPModule(peakext);
    int ch = winslider.getOutputChannels();
    for (int i = 0; i < ch; i++) {
      exec.connect(winslider, i, stft, i);
      exec.connect(stft, i, peakext, i);
    }
    F0PDFCalculatorModule f0calc = new F0PDFCalculatorModule();
    exec.addSPModule(f0calc);
    exec.connect(peakext, 0, f0calc, 0);
//    NoiseEraser ne = new NoiseEraser();
//    exec.addSPModule(ne);
//    exec.connect(f0calc, 0, ne, 0);
    MedianFilter mf = new MedianFilter();
    exec.addSPModule(mf);
    exec.connect(f0calc, 0, mf, 0);
    exec.newThread();
    F0Tracker f0track = new F0Tracker();
    exec.addSPModule(f0track);
    exec.connect(mf, 0, f0track, 0);
    SPProgressDisplayModule disp = new SPProgressDisplayModule();
    exec.addSPModule(disp);
    exec.connect(f0track, 0, disp, 0);
    exec.start();
    TimeSeriesCompatible ts = 
      (TimeSeriesCompatible)exec.getResult(disp).get(0);
    AmusaDataSet dataset = new AmusaDataSet("array", exec.getParams());
    dataset.add(ts);
    return dataset;
//    return dataset.toWrapper();
  }

    private static final int WINSIZE = 50;

  private class MedianFilter extends SPModule<SPDoubleArray,SPDoubleArray> {
    private DoubleArray[] buff = null;
    private int t = 0;
    public void execute(List<QueueReader<SPDoubleArray>> src,
                        List<TimeSeriesCompatible<SPDoubleArray>> dest)
      throws InterruptedException {
      SPDoubleArray a = src.get(0).take();
      int dim = a.length();
      if (buff == null) {
        buff = new DoubleArray[dim];
        for (int i = 0; i < dim; i++)
          buff[i] = factory.createArray(WINSIZE);
      }
      for (int i = 0; i < dim; i++)
        buff[i].set(t % WINSIZE, a.get(i));
      if (t >= WINSIZE) {
        DoubleArray b = factory.createArray(dim);
        for (int i = 0; i < dim; i++)
          b.set(i, jp.crestmuse.cmx.math.Operations.median(buff[i]));
        dest.get(0).add(new SPDoubleArray(b, a.hasNext()));
      }
      t++;
    }
    public int getInputChannels() {
      return 1;
    }
    public int getOutputChannels() {
      return 1;
    }
//    protected int getNumOfOutputFrames(int nFrames) {
//      return nFrames - WINSIZE;
//    }
  }

  private class F0Tracker extends SPModule<SPDoubleArray,SPDoubleArray> {
    private static final double THRS = 0.1;
    private int t = 0;
    public void execute(List<QueueReader<SPDoubleArray>> src,
                        List<TimeSeriesCompatible<SPDoubleArray>> dest)
      throws InterruptedException {
      SPDoubleArray a = src.get(0).take();
      MaxResult max = max(a);
      DoubleArray b = factory.createArray(1);
      if (max.max > THRS)
        b.set(0, max.argmax);
      dest.get(0).add(new SPDoubleArray(b, a.hasNext()));
    }
    public int getInputChannels() {
      return 1;
    }
    public int getOutputChannels() {
      return 1;
    }
//    protected int getNumOfOutputFrames(int nFrames) {
//      System.err.println(nFrames - WINSIZE);
//      return nFrames - WINSIZE-1;
//    }
  }

/*
  private class NoiseEraser extends SPModule<DoubleArray,DoubleArray> {
    private DoubleArray a0, a1 = null;
    private static final double F0PDF_THRESHOLD = 0.04;
    public void execute(List<QueueReader<DoubleArray>> src, 
                        List<TimeSeriesCompatible<DoubleArray>> dest) 
      throws InterruptedException {
      try {
        DoubleArray a = (DoubleArray)src.get(0).take().clone();
        if (a1 == null) {
          for (int i = 0; i < a.length(); i++)
            if (a.get(i) < F0PDF_THRESHOLD)
              a.set(i, 0);
          a1 = a;
        } else if (a0 == null) {
          a0 = a1;
          a1 = a;
        } else {
          for (int i = 0; i < a.length(); i++) {
            if (a1.get(i) < F0PDF_THRESHOLD 
                || (a0.get(i) < F0PDF_THRESHOLD && a.get(i) < F0PDF_THRESHOLD))
              a1.set(i, 0);
          }
          dest.get(0).add(a0);
          a0 = a1;
          a1 = a;
        }
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException();
      }
    }
    public int getInputChannels() {
      return 1;
    }
    public int getOutputChannels() {
      return 1;
    }
    protected int getNumOfOutputFrames(int nFrames) {
      return nFrames-2;
    }
  }
*/

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
