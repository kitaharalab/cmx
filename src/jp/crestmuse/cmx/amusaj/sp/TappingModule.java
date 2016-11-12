package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.event.*;


public class TappingModule extends SPModule implements KeyListener {
  private TickTimer tt = null;
  private BlockingQueue src_queue = new LinkedBlockingQueue();
//  private BlockingQueue src_queue = new SynchronousQueue();

  public TappingModule() {

  }

  public void setTickTimer(TickTimer tt) {
    this.tt = tt;
  }

  public  void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
      src_queue.add(new Tap(tt != null ? tt.getTickPosition() : -1));
    } else if (e.getKeyCode() == KeyEvent.VK_Q) {
      src_queue.add(SPTerminator.getInstance());
    }
  }

  public void keyReleased(KeyEvent e) {
    // do nothing
  }

  public void keyTyped(KeyEvent e) {
    // do nothing
  }


  public void execute(Object[] src, TimeSeriesCompatible[] dest)
      throws InterruptedException {
    dest[0].add(src_queue.take());
  }

  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    return new Class[]{Tap.class};
  }

/*
  // Runnable
  // MusicPlayerを監視し、曲が停止したら終了処理
  public void run() {
    while (mp.isNowPlaying()) {
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    sp.stop();
    tm.close();
    input_device.close();
  }
*/
}
