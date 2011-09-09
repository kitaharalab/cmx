package jp.crestmuse.cmx.processing;

import processing.core.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.sound.*;
import java.awt.*;
import java.io.*;

public class CMXApplet extends PApplet implements MusicListener,TickTimer {

  private static final CMXController ctrl = CMXController.getInstance();

  public static CMXFileWrapper createDocument(String toptag) {
    return ctrl.createDocument(toptag);
  }

  public CMXFileWrapper readfile(String filename) {
    return ctrl.read(createInput(filename));
  }

  public CMXFileWrapper read(InputStream input) {
    return ctrl.read(input);
  }

  public void addSPModule(ProducerConsumerCompatible module) {
    ctrl.addSPModule(module);
  }

  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    ctrl.connect(output, ch1, input, ch2);
  }

  public void wavread(String filename) {
    ctrl.wavread(createInput(filename));
    ctrl.addMusicListener(this);
  }

  public void mp3read(String filename) {
    ctrl.mp3read(createInput(filename));
    ctrl.addMusicListener(this);
  }

  public void smfread(String filename) {
    ctrl.smfread(createInput(filename));
    ctrl.addMusicListener(this);
  }

  public void playMusic() {
    ctrl.playMusic();
  }

  public void stopMusic() {
    ctrl.stopMusic();
  }

  public boolean isNowPlaying() {
    return ctrl.isNowPlaying();
  }

  public long getMicrosecondPosition() {
    return ctrl.getMicrosecondPosition();
  }

  public void setMicrosecondPosition(long t) {
    ctrl.setMicrosecondPosition(t);
  }

  public long getTickPosition() {
    return ctrl.getTickPosition();
  }

  public int getTicksPerBeat() {
    return ctrl.getTicksPerBeat();
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

  public MidiInputModule createVirtualKeyboard() {
    if (this instanceof Component)
      return ctrl.createVirtualKeyboard(this);
    else
      return ctrl.createVirtualKeyboard();
  }

  public MidiOutputModule createMidiOut() {
    return ctrl.createMidiOut();
  }

  public WindowSlider createMic() {
    return ctrl.createMic();
  }

  public WindowSlider createMic(int fs) {
    return ctrl.createMic(fs);
  }

  public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
    return ctrl.createWaveCapture(isStereo);
  }

  public void readConfig(String filename) {
    ctrl.readConfig(createInput(filename));
  }

  public void readConfig(InputStream input) {
    ctrl.readConfig(input);
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

  public static void main(String className) {
    main(new String[]{className});
  }
}
