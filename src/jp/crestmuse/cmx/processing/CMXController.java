package jp.crestmuse.cmx.processing;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import java.io.*;
import java.util.*;

public class CMXController {

  private static final CMXController me = new CMXController();

  private SPExecutor spexec = null;
  private MusicPlayer musicPlayer = null;
  private MusicPlaySynchronizer musicSync = null;

  private CMXController() {

  }

  public static CMXController getInstance() {
    return me;
  }

  public static CMXFileWrapper createDocument(String toptag) {
    try {
      return CMXFileWrapper.createDocument(toptag);
    } catch (InvalidFileTypeException e) {
      throw new IllegalArgumentException("Invalid file type: " + toptag);
    }
  }

  public static CMXFileWrapper readfile(String filename) {
    try {
      return CMXFileWrapper.readfile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    }
  }

  public void addSPModule(ProducerConsumerCompatible module) {
    if (spexec == null)
      spexec = new SPExecutor();
    spexec.addSPModule(module);
  }

  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    spexec.connect(output, ch1, input, ch2);
  }

  public void startSP() {
    if (spexec != null) spexec.start();
  }

  public void wavread(String filename) {
    try {
      musicPlayer = new WAVPlayer(WAVWrapper.readfile(filename));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new RuntimeException("Audio device not available");
    }
  }

  public void smfread(String filename) {
    try {
      musicPlayer =new SMFPlayer();
      ((SMFPlayer)musicPlayer).readSMF(filename);
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new RuntimeException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data: " + filename);
    }
  }

  public void playMusic() {
    musicSync.play();
  }

  public void stopMusic() {
    musicSync.stop();
  }

  public boolean isNowPlaying() {
    return musicPlayer.isNowPlaying();
  }

  public long musicPosition() {
    return musicPlayer.getMicrosecondPosition();
  }

  public long tickPosition() {
    return musicPlayer.getTickPosition();
  }

  public int ticksPerBeat() {
    return musicPlayer.getTicksPerBeat();
  }

  public void addMusicListener(MusicListener l) {
    musicSync.addMusicListener(l);
  }
      

}
