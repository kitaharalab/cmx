package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public class SimplePrintModule extends SPModule {

  private boolean thru;

  public SimplePrintModule(boolean thru) {
    this.thru = thru;
  }

  public SimplePrintModule() {
    this(false);
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest) 
  throws InterruptedException {
    if (src[0] instanceof Array)
      MathUtils.println((Array)src[0]);
    else
      System.out.println(src[0]);
    if (thru) 
      dest[0].add(src[0]);
  }

  public Class[] getInputClasses() {
    return new Class[] { Object.class };
  }

  public Class[] getOutputClasses() {
    return thru ? new Class[] { Object.class } : new Class[0];
  }
}
