package jp.crestmuse.cmx.sound;
import javax.sound.sampled.*;

public interface LineSupportingMusicPlayer extends MusicPlayer {
  void addLineListener(LineListener listener);
  void removeLineListener(LineListener listener);
}