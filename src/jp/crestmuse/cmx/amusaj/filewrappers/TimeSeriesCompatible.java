package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

public interface TimeSeriesCompatible 
  extends AmusaDataCompatible<DoubleArray> {
//  public QueueReader<DoubleArray> getQueueReader();
  public int dim();
//  public int frames();
  public int timeunit();
  public void add(DoubleArray array) throws InterruptedException;
}
