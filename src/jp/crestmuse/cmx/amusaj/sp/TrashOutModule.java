package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;

public class TrashOutModule extends SPModule {
  public Class[] getInputClasses() {
    return new Class[]{Object.class};
  }
  public Class[] getOutputClasses() {
    return new Class[0];
  }
  public void execute(Object[]src, TimeSeriesCompatible[] dst) {
    // do nonthing
  }
}
