package jp.crestmuse.cmx.sound;

import jp.crestmuse.cmx.amusaj.filewrappers.*;
import java.nio.*;
import java.util.*;
import javax.sound.sampled.*;

public class WAVPlayer implements MusicPlayer {

  private byte[] waveform;
  private AudioFormat fmt = null;
  private SourceDataLine line = null;
  private List<LineListener> listeners = new ArrayList<LineListener>();

  private int sampleRate;
  private int channels;
  private int samplesPerSec;
  private int framesize;
  private int framerate;
  private int lengthInSec;

  private long startposition;
  private long lengthProcessed;

  private boolean nowPlaying;

  private static final int BUFFER_SIZE = 256 * 1024;
  private static final long SLEEP_TIME = 1000;

//  public WAVPlayer(WAVXMLWrapper wav) throws LineUnavailableException {
//    nowPlaying = false;
//    init(wav.getDataChunkList()[0].getAudioData());
//  }

  public WAVPlayer(AudioDataCompatible wav) throws LineUnavailableException {
    nowPlaying = false;
    init(wav);
  }
  
//  public void changeWaveform(WAVXMLWrapper wav) 
//    throws LineUnavailableException {
//    init(wav.getDataChunkList()[0].getAudioData());
//  }

  public void changeWaveform(AudioDataCompatible wav)
    throws LineUnavailableException {
    init(wav);
  }

  private void init(AudioDataCompatible wav) throws LineUnavailableException {
    if (isNowPlaying()) stop();
    waveform = wav.getByteArrayWaveform();
//    waveform = wav.getDataChunkList()[0].getAudioData().getByteArray();
    AudioFormat fmt = wav.getAudioFormat();
//    AudioFormat fmt = wav.getFmtChunk().getAudioFormat();
    System.out.println(fmt);
    if (!fmt.equals(this.fmt)) {
      this.fmt = fmt;
      sampleRate = (int)fmt.getSampleRate();
      channels = (int)fmt.getChannels();
      samplesPerSec = sampleRate * channels;
      framesize = fmt.getFrameSize();
      if (line != null) line.close();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
      line = (SourceDataLine)AudioSystem.getLine(info);
      line.open(fmt, BUFFER_SIZE * framesize);
//      line.open(fmt, BUFFER_SIZE);
//      line.open(fmt);
      System.out.println("Audio line buffer size: " + line.getBufferSize());
      for (LineListener listener : listeners)
        line.addLineListener(listener);
    }
    lengthInSec = waveform.length / framesize;
    changeStartPosition(0.0);
  }

  public void changeStartPositionInMicrosecond(long t) {
    if (isNowPlaying()) {
      line.stop();
      line.flush();
      startposition = t;
      line.start();
    } else {
      startposition = t;
    }
  }

  public void changeStartPosition(double t) {
    changeStartPositionInMicrosecond((long)(t * 1000000));
  }

  public void addLineListener(LineListener listener) {
    listeners.add(listener);
    line.addLineListener(listener);
  }

  public void removeLineListener(LineListener listener) {
    listeners.remove(listener);
    line.removeLineListener(listener);
  }

  public double getLengthInSec() {
    return lengthInSec;
  }

  public synchronized void run() {
   while (true) {
     try {
       if (isNowPlaying()) {
         int sleepTime = (BUFFER_SIZE / sampleRate / 2) * 1000;
//         System.out.println(sleepTime);
         byte[] buff;
         int index = (int)(startposition * framesize / 1000000);
//         int index = (int)(startposition * samplesPerSec / 1000000);
//         int index = (int)(startposition * framesize * framerate / 1000000);
         do {
//             System.out.println(line.available());
           buff = toByteArray(waveform, index, BUFFER_SIZE * framesize);
           if (buff != null) {
             index += BUFFER_SIZE * framesize;
             line.write(buff, 0, buff.length);
             Thread.currentThread().sleep(sleepTime);
           }
//             System.out.println(line.available());
         } while (buff != null && isNowPlaying());
//           line.drain();
         while (line.isActive()) {
           Thread.currentThread().sleep(SLEEP_TIME);
         }
         line.stop();
         line.flush();
         nowPlaying = false;
         startposition = 0;
       }
       Thread.currentThread().sleep(SLEEP_TIME);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
  }

  public long getMicrosecondPosition() {
    return line.getMicrosecondPosition() - lengthProcessed + startposition;
  }

  public boolean isNowPlaying() {
    return nowPlaying;
//    return line.isRunning();
  }

  public void play() {
    line.start();
    lengthProcessed = line.getMicrosecondPosition();
    nowPlaying = true;
  }

  public void stop() {
    line.stop();
    line.flush();
    nowPlaying = false;
//    startposition = 0;
  }

/*
  public void pause() {
    long position = getMicrosecondPosition();
    line.stop();
    line.flush();
    nowPlaying = false;
    changeStartPositionInMicrosecond(position);
    System.out.println(position);
  }
*/
  // Kari
  protected void finalize() {
    line.close();
  }

  private byte[] toByteArray(byte[] x, int from, int length) {
    if (from >= x.length) return null;
    else if (length > x.length - from) length = x.length - from;
    ByteBuffer buff = ByteBuffer.allocate(length);
    buff.order(fmt.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < length; i++) 
      buff.put(x[i + from]);
    return buff.array();
  }

  public int getTicksPerBeat() {
    throw new UnsupportedOperationException();
  }

  public long getTickPosition() {
    throw new UnsupportedOperationException();
  }

} 