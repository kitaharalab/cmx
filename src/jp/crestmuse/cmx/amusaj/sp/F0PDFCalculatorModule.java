package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class F0PDFCalculatorModule 
  implements ProducerConsumerCompatible<PeakSet,TimeSeriesCompatible> {

  private F0PDFCalculatorFactory factory = 
    F0PDFCalculatorFactory.getFactory();
  private F0PDFCalculator f0calc;
  private Map<String,Object> params;
  private boolean paramSet = false;
  private double nnFrom, nnThru, step;

  public void setParams(Map<String,Object> params) {
    this.params = params;
    paramSet = false;
  }

  private void setParams() {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
    if (params.containsKey("NOTENUMBER_FROM")) {
      nnFrom = (Double)params.get("NOTENUMBER_FROM");
    } else {
      nnFrom = config.getParamDouble("param", "f0pdf", "NOTENUMBER_FROM");
      params.put("NOTENUMBER_FROM", nnFrom);
    }
    if (params.containsKey("NOTENUMBER_THRU")) {
      nnThru = (Double)params.get("NOTENUMBER_THRU");
    } else {
      nnThru = config.getParamDouble("param", "f0pdf", "NOTENUMBER_THRU");
      params.put("NOTENUMBER_THRU", nnThru);
    }
    if (params.containsKey("STEP")) {
      step = (Double)params.get("STEP");
    } else {
      step = config.getParamDouble("param", "f0pdf", "STEP");
      params.put("STEP", step);
    }
    f0calc = factory.createCalculator(nnFrom, nnThru, step);

  }

  public F0PDFCalculatorModule() {
//    f0calc = factory.createCalculator(nnFrom, nnThru, step);
  }

  public void execute(List<QueueReader<PeakSet>> src, 
                      List<TimeSeriesCompatible> dest) 
    throws InterruptedException {
    if (!paramSet) setParams();
    PeakSet peaks = src.get(0).take();
    dest.get(0).add(f0calc.calcWeights(peaks));
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 1;
  }

  public TimeSeriesCompatible createOutputInstance(int nFrames, int timeunit) {
    return new MutableTimeSeries(nFrames, timeunit);
  }

}
