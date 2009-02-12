package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class F0PDFCalculatorModule extends SPModule<PeakSet,SPDoubleArray> {

  private F0PDFCalculatorFactory factory = 
    F0PDFCalculatorFactory.getFactory();
  private F0PDFCalculator f0calc;
  private boolean paramSet = false;
  private double nnFrom, nnThru, step;

  private String filterName = null;
  private PeakSet.Filter filter = null;

  protected String getParamCategory() {
    return "f0pdf";
  }

  protected String[] getUsedParamNames(){
    return new String[]{"NOTENUMBER_FROM", "NOTENUMBER_THRU", "STEP"};
  }

  public void setParams(Map<String,String> params) {
    super.setParams(params);
    copyParamsFromConfigXML("param", "f0pdf", 
                            "NOTENUMBER_FROM", "NOTENUMBER_THRU", 
                            "STEP");
    if (params.containsKey("FILTER_NAME"))
      copyParamsFromConfigXML("filters", 
                              filterName = params.get("FILTER_NAME"), 
                             "LOW_CUT_FILTER", "LOW_CUT_BUTTOM", 
                             "LOW_CUT_TOP", "HIGH_CUT_FILTER", 
                             "HIGH_CUT_TOP", "HIGH_CUT_BUTTOM");
    paramSet = false;
  }

  private void setParams() {
    nnFrom = getParamDouble("NOTENUMBER_FROM");
    nnThru = getParamDouble("NOTENUMBER_THRU");
    step = getParamDouble("STEP");
    f0calc = factory.createCalculator(nnFrom, nnThru, step);
    if (filterName != null) 
      filter = PeakSet.getFilter(getParam("LOW_CUT_FILTER").equals("on"), 
                                 getParamDouble("LOW_CUT_BUTTOM"), 
                                 getParamDouble("LOW_CUT_TOP"), 
                                 getParam("HIGH_CUT_FILTER").equals("on"), 
                                 getParamDouble("HIGH_CUT_TOP"), 
                                 getParamDouble("HIGH_CUT_BUTTOM"));
    paramSet = true;
  }

  public F0PDFCalculatorModule() {

  }

  public void execute(List<QueueReader<PeakSet>> src, 
                      List<TimeSeriesCompatible<SPDoubleArray>> dest) 
    throws InterruptedException {
    if (!paramSet) setParams();
    PeakSet peaks = src.get(0).take();
    if (filter != null) peaks.filter(filter);
    dest.get(0).add(new SPDoubleArray(f0calc.calcWeights(peaks), 
                                      peaks.hasNext()));
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 1;
  }

}
