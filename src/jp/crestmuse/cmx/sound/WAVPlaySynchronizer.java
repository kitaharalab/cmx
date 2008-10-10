package jp.crestmuse.cmx.sound;

import java.util.*;
import javax.sound.sampled.*;

public class WAVPlaySynchronizer implements Runnable, LineListener {

  private MusicPlayer player;
  private Thread thPlay = null;
  private Thread thDraw;
  private List<WAVPlaySynchronized> synclist = 
    new ArrayList<WAVPlaySynchronized>();
  private boolean thPlayStarted = false;
  private boolean stoppedByUser = false;
  private long sleeptime = 100;

  public WAVPlaySynchronizer(MusicPlayer player) {
    this.player = player;
    if (player instanceof LineSupportingMusicPlayer)
      ((LineSupportingMusicPlayer)player).addLineListener(this);
    thPlay = new Thread(player);
  }

  public void addSynchronizedComponent(WAVPlaySynchronized c) {
    synclist.add(c);
  }

  public void wavplay() {
    player.play();
    if (!thPlayStarted) thPlay.start();
    thPlayStarted = true;
    stoppedByUser = false;
  } 

  public void wavstop() {
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
      for (WAVPlaySynchronized sync : synclist)
        sync.start(this);
    } else if (type.equals(LineEvent.Type.STOP)) {
      thDraw.stop();
      thDraw = null;
      for (WAVPlaySynchronized sync : synclist)
        sync.stop(this);
    }
  }

  public void setSleepTime(long sleeptime) {
    this.sleeptime = sleeptime;
  }

  public void run() {
    while (true) {
      if (isNowPlaying()) {
        long currentTick = player.getTickPosition();
        long currentPosition = player.getMicrosecondPosition();
        double t = (double)currentPosition / 1000000.0;
        for (WAVPlaySynchronized sync : synclist)
          sync.synchronize(t, currentTick, this);
        try {
          thDraw.sleep(sleeptime);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}