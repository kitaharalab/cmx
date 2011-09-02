package jp.crestmuse.cmx.amusaj.sp;

import java.io.*;
import java.util.concurrent.*;
import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;

public class SynchronizedWindowSlider extends WindowSlider 
                                      implements MusicListener {

  public SynchronizedWindowSlider(boolean isStereo) {
    super(isStereo);
  }

  private BlockingQueue<DoubleArray[]> queue = 
    new LinkedBlockingQueue<DoubleArray[]>();


  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    long ticktime = ticktimer != null ? ticktimer.getTickPosition() : -1;
    DoubleArray[] wav = queue.take();
    for (int i = 0; i < chTarget.length; i++) {
      DoubleArrayWithTicktime w;
      if (chTarget[i] == -2)
        w = new DoubleArrayWithTicktime(jp.crestmuse.cmx.math.Operations.mean(wav), ticktime);
      else        w = new DoubleArrayWithTicktime(wav[chTarget[i]], ticktime);
      dest[i].add(w);
    }
  }
    
  public void musicStarted(MusicPlaySynchronizer musicSync) {

  }

  public void musicStopped(MusicPlaySynchronizer musicSync) {

  }

  public void synchronize(double currentTime, long currentTick, 
                          MusicPlaySynchronizer musicSync) {
    long t = (long)(currentTime * 1000000);
    try {
      queue.add(audiodata.read(t, winsize));
    } catch (IOException e) {
      throw new SPException(e);
    } catch (ArrayIndexOutOfBoundsException e) {
      // do nothing 
    }
  }
  

}
