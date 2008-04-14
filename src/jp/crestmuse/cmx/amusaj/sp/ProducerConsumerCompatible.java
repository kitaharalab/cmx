package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public interface ProducerConsumerCompatible<D, E> {
  public void setParams(Map<String,Object> params);
//  public boolean setOptionsLocal(String option, String value);
  public void execute(List<QueueReader<D>> src, 
                      List<TimeSeriesCompatible<E>> dest) 
    throws InterruptedException;
//  public void execute(List<QueueReader<D>> src, 
//                      List<E> dest) throws InterruptedException;
  public int getInputChannels();
  public int getOutputChannels();
  public TimeSeriesCompatible<E> 
    createOutputInstance(int nFrames, int timeunit);
}
