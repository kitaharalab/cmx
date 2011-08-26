package jp.crestmuse.cmx.processing;

import processing.core.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.sound.*;

public class CMXApplet extends PApplet implements MusicListener {

  private static final CMXController ctrl = CMXController.getInstance();

  public static CMXFileWrapper createDocument(String toptag) {
    return ctrl.createDocument(toptag);
  }

  public static CMXFileWrapper readfile(String filename) {
    return ctrl.readfile(filename);
  }

  public void addSPModule(ProducerConsumerCompatible module) {
    ctrl.addSPModule(module);
  }

  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    ctrl.connect(output, ch1, input, ch2);
  }

  public void wavread(String filename) {
    ctrl.wavread(filename);
    ctrl.addMusicListener(this);
  }

  public void smfread(String filename) {
    ctrl.smfread(filename);
    ctrl.addMusicListener(this);
  }

  public void playMusic() {
    System.err.println("a");
    ctrl.playMusic();
  }

  public void stopMusic() {
    ctrl.stopMusic();
  }

  public boolean isNowPlaying() {
    return ctrl.isNowPlaying();
  }

  public long musicPosition() {
    return ctrl.musicPosition();
  }

  public long tickPosition() {
    return ctrl.tickPosition();
  }

  public int ticksPerBeat() {
    return ctrl.ticksPerBeat();
  }

  public void musicStarted(MusicPlaySynchronizer ms) {
    musicStarted();
  }

  public void musicStopped(MusicPlaySynchronizer ms) {
    musicStopped();
  }

  public void synchronize(double currentTime, long currentTick, 
                          MusicPlaySynchronizer ms) {
    synchronize();
  }

  protected void musicStarted() {
    // do nithing
  }

  protected void musicStopped() {
    // do nothing
  }

  protected void synchronize() {
    // do nothing
  }

  public void handleDraw() {
    if (frameCount == 1) {
      autostart();
      super.handleDraw();
    } else {
      super.handleDraw();
    }
  }

  private void autostart() {
    println("SPExector automatically started.");
    ctrl.startSP();
  }


}
