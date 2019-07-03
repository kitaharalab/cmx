package jp.crestmuse.cmx.sound;

@Deprecated
public interface MusicPlaySynchronized {
  void synchronize(double currentTime, long currentTick, MusicPlaySynchronizer wavsync);
  void start(MusicPlaySynchronizer wavsnyc);
  void stop(MusicPlaySynchronizer wavsync);
}