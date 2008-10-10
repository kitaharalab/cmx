package jp.crestmuse.cmx.sound;

public interface MusicPlayer extends Runnable {
  void play();
  void stop();
  boolean isNowPlaying();
  long getMicrosecondPosition();
  long getTickPosition();
}