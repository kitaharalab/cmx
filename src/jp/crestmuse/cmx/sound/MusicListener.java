package jp.crestmuse.cmx.sound;

public interface MusicListener {

  void musicStarted(MusicPlaySynchronizer musicSync);

  void musicStopped(MusicPlaySynchronizer musicSync);

  void synchronize(double currentTime, long currentTick, 
                   MusicPlaySynchronizer wavsync);
}
