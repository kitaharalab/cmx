package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import java.util.*;

public class SPProgressDisplayModule<D extends SPElement>
  extends SPModule<D, D> {
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
}


