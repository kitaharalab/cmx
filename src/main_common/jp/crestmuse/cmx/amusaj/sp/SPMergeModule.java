/*

package jp.crestmuse.cmx.amusaj.sp;

import java.util.Arrays;

public class SPMergeModule extends SPModule {

  private int num;
  private Class c;

  public SPSpreadModule(Class c, int num) {
    this.num = num;
    this.c = c;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
      throws InterruptedException {
    for(int i=0; i<spreadNum; i++)
      dest[i].add(src[0]);
  }

  public Class[] getInputClasses() {
    return new Class[]{ c };
  }

  public Class[] getOutputClasses() {
    Class[] classes = new Class[spreadNum];
    Arrays.fill(classes, c);
    return classes;
  }

}


*/