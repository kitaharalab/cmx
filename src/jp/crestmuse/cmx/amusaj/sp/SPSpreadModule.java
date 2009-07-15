package jp.crestmuse.cmx.amusaj.sp;

import java.util.Arrays;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;

public class SPSpreadModule extends SPModule {

  private int spreadNum;
  private Class c;

  public SPSpreadModule(Class c, int spreadNum) {
    this.spreadNum = spreadNum;
    this.c = c;
  }

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    for(int i=0; i<spreadNum; i++)
      dest[i].add(src[0]);
  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ c };
  }

  public Class<SPElement>[] getOutputClasses() {
    Class[] classes = new Class[spreadNum];
    Arrays.fill(classes, c);
    return classes;
  }

}