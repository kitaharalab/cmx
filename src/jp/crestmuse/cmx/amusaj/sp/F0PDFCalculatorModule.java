package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

//public class F0PDFCalculatorModule extends SPModule<PeakSet,SPDoubleArray> {
public class F0PDFCalculatorModule extends SPModule {

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

/*
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
*/

  private void setParams() {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    nnFrom = params.getParamDouble("f0pdf", "NOTENUMBER_FROM");
    nnThru = params.getParamDouble("f0pdf", "NOTENUMBER_THRU");
    step = params.getParamDouble("f0pdf", "STEP");
    f0calc = factory.createCalculator(nnFrom, nnThru, step);
    String filterName = params.getParam("f0pdf", "FILTER_NAME");
    if (filterName != null) {
      System.err.println(filterName);
      System.err.println(params.getFilterParam(filterName, "LOW_CUT_FILTER"));
      filter = PeakSet.getFilter
        (params.getFilterParam(filterName, "LOW_CUT_FILTER").equals("on"), 
         params.getFilterParamDouble(filterName, "LOW_CUT_BUTTOM"), 
         params.getFilterParamDouble(filterName, "LOW_CUT_TOP"), 
         params.getFilterParam(filterName, "HIGH_CUT_FILTER").equals("on"), 
         params.getFilterParamDouble(filterName, "HIGH_CUT_TOP"), 
         params.getFilterParamDouble(filterName, "HIGH_CUT_BUTTOM"));
    }
    paramSet = true;
  }

  public F0PDFCalculatorModule() {

  }
/*
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
*/
  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    if (!paramSet) setParams();
    PeakSet peaks = (PeakSet)src[0];
    if (filter != null) peaks.filter(filter);
    dest[0].add(new SPDoubleArray(f0calc.calcWeights(peaks)));
  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ PeakSet.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    return new Class[]{ SPDoubleArray.class };
  }

}
