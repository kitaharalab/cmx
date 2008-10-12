package jp.crestmuse.cmx.sound;

import java.util.*;
import javax.sound.sampled.*;

public class MusicPlaySynchronizer implements Runnable, LineListener {

  private MusicPlayer player;
  private Thread thPlay = null;
  private Thread thDraw;
  private List<MusicPlaySynchronized> synclist = 
    new ArrayList<MusicPlaySynchronized>();
  private boolean thPlayStarted = false;
  private boolean stoppedByUser = false;
  private long sleeptime = 100;

  public MusicPlaySynchronizer(MusicPlayer player) {
    this.player = player;
    if (player instanceof LineSupportingMusicPlayer)
      ((LineSupportingMusicPlayer)player).addLineListener(this);
    thPlay = new Thread(player);
  }

  public void addSynchronizedComponent(MusicPlaySynchronized c) {
    synclist.add(c);
  }

  public void play() {
    player.play();
    if (!thPlayStarted) thPlay.start();
    thPlayStarted = true;
    stoppedByUser = false;
  } 

  public void stop() {
    stoppedByUser = true;
    if (player != null) player.stop();
  }

  public boolean isStoppedByUser() {
    return stoppedByUser;
  }

  public boolean isNowPlaying() {
    return  (player != null) && (player.isNowPlaying());
  }

  public void update(LineEvent e) {
    LineEvent.Type type = e.getType();
    if (type.equals(LineEvent.Type.START)) {
      thDraw = new Thread(this);
      thDraw.start();
      for (MusicPlaySynchronized sync : synclist)
        sync.start(this);
    } else if (type.equals(LineEvent.Type.STOP)) {
      //thDraw.stop();
      //thDraw = null;
      thDraw.interrupt();
      for (MusicPlaySynchronized sync : synclist)
        sync.stop(this);
    }
  }

  public void setSleepTime(long sleeptime) {
    this.sleeptime = sleeptime;
  }

  public void run() {
    while (!Thread.interrupted()) {
      if (isNowPlaying()) {
        long currentTick = -1;
        try {
          currentTick = player.getTickPosition();
        } catch (UnsupportedOperationException e) {}
        long currentPosition = player.getMicrosecondPosition();
        double t = (double)currentPosition / 1000000.0;
        for (MusicPlaySynchronized sync : synclist)
          sync.synchronize(t, currentTick, this);
      }
      try {
        Thread.sleep(sleeptime);
      } catch (InterruptedException e) {
        break;
      }
    }
  }
}