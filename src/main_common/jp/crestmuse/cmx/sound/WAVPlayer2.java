package jp.crestmuse.cmx.sound;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WAVPlayer2 implements MusicPlayer {

//  private AudioDataCompatible wav;
  private Clip clip = null;
  private boolean loop = false;

  public WAVPlayer2(AudioDataCompatible wav) throws LineUnavailableException,IOException,UnsupportedAudioFileException {
    init(wav);
  }

  public void changeWaveform(AudioDataCompatible wav) throws LineUnavailableException,IOException,UnsupportedAudioFileException {
    if (clip != null)
      clip.close();
    init(wav);
  }

  private void init(AudioDataCompatible wav) throws LineUnavailableException,IOException,UnsupportedAudioFileException {
//    this.wav = wav;
    byte[] waveform = wav.getByteArrayWaveform();
    AudioFormat fmt = wav.getAudioFormat();
    AudioInputStream audiostream;
//    if (loop) {
//      ByteArrayInputStream bytestream = 
//        new LoopingByteArrayInputStream(waveform);
//      audiostream = 
//        new AudioInputStream(bytestream, fmt, 2 * waveform.length / fmt.getFrameSize());
//        new AudioInputStream(bytestream, fmt, AudioSystem.NOT_SPECIFIED);
//    } else {
      ByteArrayInputStream bytestream = new ByteArrayInputStream(waveform);
      audiostream = new AudioInputStream(bytestream, fmt, 
                                         waveform.length / fmt.getFrameSize());
//    }
    clip = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, fmt));
    clip.open(audiostream);
  }

  public void setMicrosecondPosition(long t) {
    clip.setMicrosecondPosition(t);
  }

  public void setLoopEnabled(boolean b) {
    loop = b;
/*
    try {
      init(wav);
    } catch (LineUnavailableException e) {
      throw new IllegalStateException(e.toString());
    } catch (IOException e) {
      throw new IllegalStateException(e.toString());
    } catch (UnsupportedAudioFileException e) {
      throw new IllegalStateException(e.toString());
    }
*/
  }

  public synchronized void run() {

  }

  public long getMicrosecondLength() {
    return clip.getMicrosecondLength();
  }

  public long getMicrosecondPosition() {
    return clip.getMicrosecondPosition();
  }

  public boolean isNowPlaying() {
    return clip.isRunning();
  }


  public void play() {
    if (loop)
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    else
      clip.start();
  }

  public void stop() {
    clip.stop();
  }

  protected void finalize() {
    clip.close();
  }

  public int getTicksPerBeat() {
    throw new UnsupportedOperationException();
  }

  public long getTickPosition() {
    throw new UnsupportedOperationException();
  }

  public Control getControl(Control.Type type) {
    return clip.getControl(type);
  }

  public boolean isControlSupported(Control.Type type) {
    return clip.isControlSupported(type);
  }

/*
  public FloatControl getMasterGainControl() {
    return (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
  }

  public FloatControl getVolumeControl() {
    return (FloatControl)clip.getControl(FloatControl.Type.VOLUME);
  }
*/
}