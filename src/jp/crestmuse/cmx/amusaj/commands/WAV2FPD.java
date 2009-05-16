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
//    int ch = winslider.getOutputChannels();
    int ch = winslider.getOutputClasses().length;
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
//
//    MedianFilter mf = new MedianFilter();
//    exec.addSPModule(mf);
//    exec.connect(f0calc, 0, mf, 0);
//    exec.newThread();
//
    F0Tracker f0track = new F0Tracker();
    exec.addSPModule(f0track);
    exec.connect(f0calc, 0, f0track, 0);
////    exec.connect(mf, 0, f0track, 0);

    MedianFilter mf = new MedianFilter();
    exec.addSPModule(mf);
    exec.connect(f0track, 0, mf, 0);
//
//    SPProgressDisplayModule disp = new SPProgressDisplayModule();
//    exec.addSPModule(disp);
//    exec.connect(f0track, 0, disp, 0);
//
//    HarmonicsExtractor harm = new HarmonicsExtractor();
//    exec.addSPModule(harm);
//    exec.connect(peakext, 0, harm, 0);
//    exec.connect(f0track, 0, harm, 1);
    exec.start();
    TimeSeriesCompatible ts = 
//      (TimeSeriesCompatible)exec.getResult(f0calc).get(0);
      (TimeSeriesCompatible)exec.getResult(mf).get(0);
//      (TimeSeriesCompatible)exec.getResult(f0track).get(0);
//      (TimeSeriesCompatible)exec.getResult(harm).get(0);
    AmusaDataSet dataset = new AmusaDataSet("array", exec.getParams());
//    AmusaDataSet dataset = new AmusaDataSet("peaks", exec.getParams());
    dataset.add(ts);
    return dataset;
//    return dataset.toWrapper();
  }

  private static final int WINSIZE = 7;

  //private class MedianFilter extends SPModule<SPDoubleArray,SPDoubleArray> {
  private class MedianFilter extends SPModule {
    private DoubleArray[] buff = null;
    private int t = 0;
/*
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
*/
//    protected int getNumOfOutputFrames(int nFrames) {
//      return nFrames - WINSIZE;
//    }
    public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
        throws InterruptedException {
      SPDoubleArray a = (SPDoubleArray)src[0];
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
        dest[0].add(new SPDoubleArray(b));
      }
      t++;
    }
    public Class<SPElement>[] getInputClasses() {
      return new Class[]{ SPDoubleArray.class };
    }
    public Class<SPElement>[] getOutputClasses() {
      return new Class[]{ SPDoubleArray.class };
    }
  }

  //private class F0Tracker extends SPModule<SPDoubleArray,SPDoubleArray> {
  private class F0Tracker extends SPModule {

    private static final double THRS = 0;  // originally 0.1;
    private int t = 0;
    private double nnFrom, nnThru, step;
    private boolean paramSet = false;
    protected String getParamCategory() {
      return "f0pdf";
    }
    protected String[] getUsedParamNames(){
      return new String[]{"NOTENUMBER_FROM", "NOTENUMBER_THRU", "STEP"};
    }
    private void setParams() {
      nnFrom = getParamDouble("NOTENUMBER_FROM");
      nnThru = getParamDouble("NOTENUMBER_THRU");
      step = getParamDouble("STEP");
      paramSet = true;
    }

    private double f0prev = 0;
/*
    public void execute(List<QueueReader<SPDoubleArray>> src,
                        List<TimeSeriesCompatible<SPDoubleArray>> dest)
      throws InterruptedException {
      if (!paramSet) setParams();
      SPDoubleArray a = src.get(0).take();
      MaxResult maxresult = max(a);
      DoubleArray b = factory.createArray(1);  // originally 1;
      if (maxresult.max > THRS) {
        double f1 = nn2Hz(nnFrom + step * maxresult.argmax);
        double f2 = nn2Hz(nnFrom + step * maxresult.argmax2nd);
        double f3 = nn2Hz(nnFrom + step * maxresult.argmax3rd);
        b.set(0, Math.min(f1, f2));
//        if (f3 == f0prev)
//          f0prev = f3;
//        else if (f2 == f0prev)
//          f0prev = f2;
//        else
//          f0prev = f1;
//
//        if (Math.abs(f3 - f0prev) < Math.abs(f1 - f0prev) &&
//           Math.abs(f3 - f0prev) < Math.abs(f2 - f0prev))
//          f0prev = f3;
//        else if (Math.abs(f2 - f0prev) < Math.abs(f1 - f0prev))
//          f0prev = f2;
//        else
//          f0prev = f1;
//        b.set(0, f0prev);
//
//        b.set(0, nn2Hz(nnFrom + step * maxresult.argmax));
//        b.set(1, nn2Hz(nnFrom + step * maxresult.argmax2nd));
//        System.err.println(nn2Hz(nnFrom + step * maxresult.argmax));
      }
      dest.get(0).add(new SPDoubleArray(b, a.hasNext()));
    }

    public int getInputChannels() {
      return 1;
    }
    public int getOutputChannels() {
      return 1;
    }
*/
//    protected int getNumOfOutputFrames(int nFrames) {
//      System.err.println(nFrames - WINSIZE);
//      return nFrames - WINSIZE-1;
//    }
    public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
        throws InterruptedException {
      if (!paramSet) setParams();
      SPDoubleArray a = (SPDoubleArray)src[0];
      MaxResult maxresult = max(a);
      DoubleArray b = factory.createArray(1);  // originally 1;
      if (maxresult.max > THRS) {
        double f1 = nn2Hz(nnFrom + step * maxresult.argmax);
        double f2 = nn2Hz(nnFrom + step * maxresult.argmax2nd);
        double f3 = nn2Hz(nnFrom + step * maxresult.argmax3rd);
        b.set(0, Math.min(f1, f2));
      }
      dest[0].add(new SPDoubleArray(b));
    }
    public Class<SPElement>[] getInputClasses() {
      return new Class[]{ SPDoubleArray.class };
    }
    public Class<SPElement>[] getOutputClasses() {
      return new Class[]{ SPDoubleArray.class };
    }
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
