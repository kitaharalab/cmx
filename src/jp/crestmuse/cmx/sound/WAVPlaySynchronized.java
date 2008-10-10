package jp.crestmuse.cmx.sound;

public interface WAVPlaySynchronized {
  void synchronize(double currentTime, long currentTick, WAVPlaySynchronizer wavsync);
  void start(WAVPlaySynchronizer wavsnyc);
  void stop(WAVPlaySynchronizer wavsync);
}