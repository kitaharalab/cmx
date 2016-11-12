package jp.crestmuse.cmx.sound;

public interface MusicPlayer extends Runnable, TickTimer{
  void play();
  void stop();
  boolean isNowPlaying();
  void setLoopEnabled(boolean b);
  void setMicrosecondPosition(long position);
  long getMicrosecondPosition();
  long getMicrosecondLength();
}
