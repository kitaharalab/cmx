package jp.crestmuse.cmx.amusaj.sp;

import java.util.Arrays;

public class SPSpreadModule extends SPModule {

  private int spreadNum;
  private Class c;
  private Class[] classes;

  public SPSpreadModule(Class c, int spreadNum) {
    this.spreadNum = spreadNum;
    this.c = c;
    classes = new Class[spreadNum];
    Arrays.fill(classes, c);
  }

  public SPSpreadModule(ProducerConsumerCompatible module, int ch, 
                        int spreadNum) {
    this(module.getOutputClasses()[ch], spreadNum);
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
    return classes;
  }

}
