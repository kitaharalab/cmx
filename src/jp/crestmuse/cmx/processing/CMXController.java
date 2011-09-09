package jp.crestmuse.cmx.processing;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javazoom.jl.decoder.*;

public class CMXController implements TickTimer {

  private static final CMXController me = new CMXController();

  private SPExecutor spexec = null;
  private MusicPlayer musicPlayer = null;
  private MusicPlaySynchronizer musicSync = null;
  private AudioInputStreamWrapper mic = null;
  private AudioDataCompatible wav = null;

  private MidiDevice.Info midiin = null;
  private MidiDevice.Info midiout = null;


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

  public static CMXFileWrapper read(InputStream input) {
    try {
      return CMXFileWrapper.read(input);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
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
    if (mic != null) mic.getLine().start();
    if (spexec != null) spexec.start();
  }

  public void wavread(String filename) {
    try {
      musicPlayer = new WAVPlayer(wav = WAVWrapper.readfile(filename));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void wavread(InputStream input) {
    try {
      musicPlayer = new WAVPlayer(wav = WAVWrapper.read(input));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void mp3read(String filename) {
    try {
      musicPlayer = new WAVPlayer(wav = MP3Wrapper.readfile(filename));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read file: " + filename);
    } catch (DecoderException e) {
      throw new IllegalStateException("Cannot decode MP3 file: " + filename);
    } catch (BitstreamException e) {
      throw new IllegalStateException("Cannot decode MP3 file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void mp3read(InputStream input) {
    try {
      musicPlayer = new WAVPlayer(wav = MP3Wrapper.read(input));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read file");
    } catch (DecoderException e) {
      throw new IllegalStateException("Cannot decode MP3 file");
    } catch (BitstreamException e) {
      throw new IllegalStateException("Cannot decode MP3 file");
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
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
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data: " + filename);
    }
  }

  public void smfread(InputStream input) {
    try {
      musicPlayer =new SMFPlayer();
      ((SMFPlayer)musicPlayer).readSMF(input);
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data");
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

  public void setMicrosecondPosition(long t) {
    musicPlayer.setMicrosecondPosition(0);
  }

  public long getMicrosecondPosition() {
    return musicPlayer.getMicrosecondPosition();
  }

  public long getTickPosition() {
    return musicPlayer.getTickPosition();
  }

  public int getTicksPerBeat() {
    return musicPlayer.getTicksPerBeat();
  }

  public void addMusicListener(MusicListener l) {
    musicSync.addMusicListener(l);
  }
      
  public void waitForMusicStopped() {
    try {
      while (isNowPlaying()) {
        Thread.currentThread().sleep(100);
      }
    } catch (InterruptedException e) {
      return;
    }
  }

  public MidiInputModule createVirtualKeyboard() {
    try {
      VirtualKeyboard vkb = new VirtualKeyboard();
      MidiInputModule midiin = new MidiInputModule(vkb);
      midiin.setTickTimer(this);
      return midiin;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }
  
  public MidiInputModule createVirtualKeyboard(Component c) {
    try {
      VirtualKeyboard vkb = new VirtualKeyboard(c);
      MidiInputModule midiin = new MidiInputModule(vkb);
      return midiin;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  public MidiInputModule createMidiIn() {
    try {
      if (midiin == null) {
        throw new IllegalStateException
          ("MIDI IN device has not been selected yet");
      } else {
        return new MidiInputModule(
          SoundUtils.getMidiInDeviceByName(midiin.getName()));
      }
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  public MidiOutputModule createMidiOut() {
    try {
      if (midiout == null) {
        return new MidiOutputModule();
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
        return new MidiOutputModule(dev);
      }
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  public void showMidiInChooser(Component parent) {
    try {
      Object selected = 
        JOptionPane.showInputDialog(
          parent, "Select MIDI IN Device.", "Select MIDI IN Device...", 
          JOptionPane.PLAIN_MESSAGE, null, 
          SoundUtils.getMidiInDeviceInfo().toArray(), null);
      if (selected != null)
        midiin = (MidiDevice.Info)selected;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  public void showMidiOutChooser(Component parent) {
    try {
      Object selected = 
        JOptionPane.showInputDialog(
          parent, "Select MIDI OUT Device.", "Select MIDI OUT Device...", 
          JOptionPane.PLAIN_MESSAGE, null, 
          SoundUtils.getMidiOutDeviceInfo().toArray(), null);
      if (selected != null)
        midiout = (MidiDevice.Info)selected;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }
    

  public WindowSlider createMic() {
    return createMic(16000);
  }

  public WindowSlider createMic(int fs) {
    try {
      AudioInputStreamWrapper mic = 
        AudioInputStreamWrapper.createWrapper8(fs);
      WindowSlider winslider = new WindowSlider(false);
      winslider.setInputData(mic);
      winslider.setTickTimer(this);
      return winslider;
    } catch (LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
    SynchronizedWindowSlider winslider = 
      new SynchronizedWindowSlider(isStereo);
    winslider.setInputData(wav);
    addMusicListener(winslider);
    return winslider;
  }

  public void readConfig(String filename) {
    try {
      AmusaParameterSet.getInstance().setAnotherParameterSet(
        (ConfigXMLWrapper)CMXFileWrapper.readfile(filename));
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read config file: " + filename);
    }
  }

  public void readConfig(InputStream input) {
    try {
      AmusaParameterSet.getInstance().setAnotherParameterSet(
        (ConfigXMLWrapper)CMXFileWrapper.read(input));
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read config file");
    }
  }


}
