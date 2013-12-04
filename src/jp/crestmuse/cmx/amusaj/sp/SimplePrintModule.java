package jp.crestmuse.cmx.amusaj.sp;

public class SimplePrintModule extends SPModule {

  public void execute(Object[] src, TimeSeriesCompatible[] dest) {
    System.out.println(src[0]);
  }

  public Class[] getInputClasses() {
    return new Class[] { Object.class };
  }

  public Class[] getOutputClasses() {
    return new Class[0];
  }
}