package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.math.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;
import java.io.*;

public class Utils {
  public static AudioDataCompatible excerpt(AudioDataCompatible audiodata,int from,int thru){
    return new MyAudioData(audiodata, from, thru);
  }

  private static class MyAudioData implements AudioDataCompatible {
    AudioDataCompatible org;
    int from;
    int thru;
    DoubleArray[] waveform = null;
    MyAudioData(AudioDataCompatible audiodata, int from, int thru) {
      if (!audiodata.supportsWholeWaveformGetter())
        throw new IllegalStateException();
      org = audiodata;
      this.from = from;
      this.thru = thru;
      DoubleArray[] w = org.getDoubleArrayWaveform();
      waveform = new DoubleArray[w.length];
      for (int i = 0; i < w.length; i++)
        waveform[i] = w[i].subarrayX(from, thru);
    }
    public int channels() {
      return org.channels();
    }
    public int sampleRate() {
      return org.sampleRate();
    }
    public AudioFormat getAudioFormat() {
      return org.getAudioFormat();
    }
    public DoubleArray[] getDoubleArrayWaveform() {
      return waveform;
    }
    public byte[] getByteArrayWaveform() {
      throw new UnsupportedOperationException();
    }
    private int next = 0;
    public DoubleArray[] readNext(int sampleSize, int nOverlap) {
      DoubleArray[] array = new DoubleArray[channels()];
      for (int i = 0; i < array.length; i++)
        array[i]=waveform[i].subarrayX(next, next+sampleSize);
      next += sampleSize - nOverlap;
      return array;
    }
    public boolean hasNext(int sampleSize) {
      return next + sampleSize <= waveform[0].length();
    }
    public boolean supportsWholeWaveformGetter() {
      return true;
//      return org.supportsWholeWaveformGetter();
    }
  }


  public static MidiDevice getMidiDevice(boolean trans) {
    MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
    MidiDevice device = null;

    for (int i = 0; i < info.length; i++) {
      try {
        device = MidiSystem.getMidiDevice(info[i]);
        if(trans && device.getMaxTransmitters() == 0) continue;
        if(!trans && device.getMaxReceivers() == 0) continue;
        System.err.println("*** " + i + " ***");
        System.err.println("  Description:" + info[i].getDescription());
        System.err.println("  Name:" + info[i].getName());
        System.err.println("  Vendor:" + info[i].getVendor());
        System.err.println();
      } catch (MidiUnavailableException e) {
        e.printStackTrace();
      }
    }

    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1)
;
      if(trans) System.err.print("Using Input Device Number: ");
      else System.err.print("Using Output Device Number: ");
      String s = r.readLine();
      device = MidiSystem.getMidiDevice(info[Integer.parseInt(s)]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return device;
  }
  
}