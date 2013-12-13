package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public class SimplePrintModule extends SPModule {

  public void execute(Object[] src, TimeSeriesCompatible[] dest) {
    if (src[0] instanceof Array)
      MathUtils.println((Array)src[0]);
    else
      System.out.println(src[0]);
  }

  public Class[] getInputClasses() {
    return new Class[] { Object.class };
  }

  public Class[] getOutputClasses() {
    return new Class[0];
  }
}