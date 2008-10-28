package jp.crestmuse.cmx.sound;

import java.util.*;

/**
 * <p>このクラスは複数の{@link MusicPlaySynchronized}オブジェクトをひとつの
 * {@link MusicPlayer}の再生と同期させます．</p>
 * 
 * <p>一定時間ごとに所持する{@link MusicPlaySynchronized}オブジェクトの
 * {@link MusicPlaySynchronized#synchronize(double, long, MusicPlaySynchronizer)}
 * メソッドを呼び出します．これはこのクラスの{@link #play()}メソッドから演奏を
 * 開始したときのみ動作し、外部から{@link MusicPlayer#play()}メソッドを呼び
 * 出すと同期処理は行われません．</p>
 * 
 * <p>最初にこのクラスの{@link #play()}を呼び出したときのみ、
 * MusicPlayerのスレッドを生成、開始します．</p>
 * @author Naoyuki Totani
 * @see MusicPlaySynchronized
 * @see MusicPlayer
 *
 */
public class MusicPlaySynchronizer implements Runnable {

  private MusicPlayer player;
  private List<MusicPlaySynchronized> synclist = 
    new ArrayList<MusicPlaySynchronized>();
  private boolean playerThreadStarted = false;
  private boolean syncThreadStarted = false;
  private boolean stoppedByUser = false;
  private long sleeptime = 100;

  public MusicPlaySynchronizer(MusicPlayer player) {
    this.player = player;
  }

  public void addSynchronizedComponent(MusicPlaySynchronized c) {
    synclist.add(c);
  }

  public void play() {
    player.play();
    if (!playerThreadStarted)
      new Thread(player).start();
    if(!syncThreadStarted)
      new Thread(this).start();
    playerThreadStarted = true;
    syncThreadStarted = true;
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

  public void setSleepTime(long sleeptime) {
    this.sleeptime = sleeptime;
  }

  public void run() {
    for (MusicPlaySynchronized sync : synclist)
      sync.start(this);
    while (isNowPlaying() || isStoppedByUser()) {
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
    for (MusicPlaySynchronized sync : synclist)
      sync.stop(this);
    syncThreadStarted = false;
  }
}