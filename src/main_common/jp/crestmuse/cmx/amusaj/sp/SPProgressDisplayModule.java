package jp.crestmuse.cmx.amusaj.sp;

//public class SPProgressDisplayModule<D extends SPElement> extends SPModule<D, D> {
public class SPProgressDisplayModule extends SPModule {
/*
  public void execute(List<QueueReader<D>> src, 
                      List<TimeSeriesCompatible<D>> dest)
    throws InterruptedException {
    System.err.print(".");
    dest.get(0).add(src.get(0).take());
  }
  public int getInputChannels() {
    return 1;
  }
  public int getOutputChannels() {
    return 1;
  }
*/
  public void execute(Object[] src, TimeSeriesCompatible[] dest)
      throws InterruptedException {
    System.err.print(".");
    dest[0].add(src[0]);
  }

  public Class[] getInputClasses() {
    return new Class[]{ Object.class };
  }

  public Class[] getOutputClasses() {
    return new Class[]{ Object.class };
  }

}


