package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class F0PDFCalculatorModule extends SPModule<PeakSet,DoubleArray> {

  private F0PDFCalculatorFactory factory = 
    F0PDFCalculatorFactory.getFactory();
  private F0PDFCalculator f0calc;
  private boolean paramSet = false;
  private double nnFrom, nnThru, step;

  public void setParams(Map<String,String> params) {
    super.setParams(params);
    copyParamsFromConfigXML("param", "f0pdf", 
                            "NOTENUMBER_FROM", "NOTENUMBER_THRU", 
                            "STEP");
    paramSet = false;
  }

  private void setParams() {
    nnFrom = getParamDouble("NOTENUMBER_FROM");
    nnThru = getParamDouble("NOTENUMBER_THRU");
    step = getParamDouble("STEP");
    f0calc = factory.createCalculator(nnFrom, nnThru, step);
    paramSet = true;
  }

  public F0PDFCalculatorModule() {

  }

  public void execute(List<QueueReader<PeakSet>> src, 
                      List<TimeSeriesCompatible<DoubleArray>> dest) 
    throws InterruptedException {
    if (!paramSet) setParams();
    PeakSet peaks = src.get(0).take();
//    try {
//      dest.get(0).add((DoubleArray)f0calc.calcWeights(peaks).clone());
//    } catch (CloneNotSupportedException e) {
//      e.printStackTrace();
//      System.exit(1);
//    }
    dest.get(0).add(f0calc.calcWeights(peaks));
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 1;
  }

}
