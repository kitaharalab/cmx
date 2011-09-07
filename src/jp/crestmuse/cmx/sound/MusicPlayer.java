package jp.crestmuse.cmx.sound;

public interface MusicPlayer extends Runnable, TickTimer{
  void play();
  void stop();
  boolean isNowPlaying();
  void setMicrosecondPosition(long potision);
  long getMicrosecondPosition();
}