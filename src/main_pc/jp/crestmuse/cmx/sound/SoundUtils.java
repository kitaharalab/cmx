package jp.crestmuse.cmx.sound;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFormat;

import jp.crestmuse.cmx.math.DoubleArray;

public class SoundUtils {
  public static AudioDataCompatible excerpt(AudioDataCompatible audiodata,int from,int thru){
    return new MyAudioData(audiodata, from, thru);
  }

  private static class MyAudioData implements AudioDataCompatible {
    AudioDataCompatible org;
    int from;
    int thru;
    DoubleArray[] waveform = null;
    MyAudioData(AudioDataCompatible audiodata, int from, int thru) {
      if (!audiodata.supportsRandomAccess())
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
    public DoubleArray[] read(long microsecond, int sampleSize) {
      DoubleArray[] array = new DoubleArray[channels()];
      int from = (int)(sampleRate() * microsecond / 1000000);
      for (int i = 0; i < array.length; i++)
        array[i] = getDoubleArrayWaveform()[i].subarrayX(from, from+sampleSize);
      return array;
    }
    public boolean supportsRandomAccess() {
      return true;
    }
//    public boolean supportsWholeWaveformGetter() {
//      return true;
//      return org.supportsWholeWaveformGetter();
  }

  //    public static enum MidiDeviceType {INPUT, OUTPUT};
  
  @Deprecated
  public static List<MidiDevice.Info> getMidiDeviceInfo(MidiDeviceType t) 
    throws MidiUnavailableException {
    MidiDevice.Info[] infoarray = MidiSystem.getMidiDeviceInfo();
    List<MidiDevice.Info> l = new ArrayList<MidiDevice.Info>();
    for (MidiDevice.Info info : infoarray) {
//      System.out.println(info);
      MidiDevice device = MidiSystem.getMidiDevice(info);
      if (t.equals(MidiDeviceType.INPUT) 
          && device.getMaxTransmitters() != 0)
        l.add(info);
      else if (t.equals(MidiDeviceType.OUTPUT) 
               && device.getMaxReceivers() != 0)
        l.add(info);
    }
    return l;
  }

  public static List<MidiDevice.Info> getMidiInDeviceInfo()
    throws MidiUnavailableException {
    return getMidiDeviceInfo(MidiDeviceType.INPUT);
  }

  public static List<MidiDevice.Info> getMidiOutDeviceInfo() 
    throws MidiUnavailableException {
    return getMidiDeviceInfo(MidiDeviceType.OUTPUT);
  }

  public static MidiDevice getMidiOutDevice(int index) 
    throws MidiUnavailableException {
    return getMidiDevice(index, MidiDeviceType.OUTPUT);
  }

  public static MidiDevice getMidiInDevice(int index) 
    throws MidiUnavailableException {
    return getMidiDevice(index, MidiDeviceType.INPUT);
  }

  @Deprecated
  public static MidiDevice getMidiDevice(int index, MidiDeviceType t) 
    throws MidiUnavailableException {
    List<MidiDevice.Info> l = getMidiDeviceInfo(t);
	return MidiSystem.getMidiDevice(l.get(index));
  }

  public static MidiDevice getMidiOutDeviceByName(String name) 
    throws MidiUnavailableException {
    return getMidiDeviceByName(name, MidiDeviceType.OUTPUT);
  }

  public static MidiDevice getMidiInDeviceByName(String name) 
    throws MidiUnavailableException {
    return getMidiDeviceByName(name, MidiDeviceType.INPUT);
  }

  @Deprecated
  public static MidiDevice getMidiDeviceByName(String name, 
                                               MidiDeviceType t) 
    throws MidiUnavailableException {
    MidiDevice.Info[] infoarray = MidiSystem.getMidiDeviceInfo();
    for (MidiDevice.Info info : infoarray) {
      if (info.getName().contains(name)) {
        MidiDevice device = MidiSystem.getMidiDevice(info);
        if (t.equals(MidiDeviceType.INPUT) 
            && device.getMaxTransmitters() != 0)
          return device;
        else if (t.equals(MidiDeviceType.OUTPUT) 
                 && device.getMaxReceivers() != 0)
          return device;
        else
          continue;
      }
    }
    return null;
  }

  public static MidiDevice getMidiOutDeviceByDescription(String description) 
    throws MidiUnavailableException {
    return getMidiDeviceByDescription(description, MidiDeviceType.OUTPUT);
  }

  public static MidiDevice getMidiInDeviceByDescription(String description) 
    throws MidiUnavailableException {
    return getMidiDeviceByDescription(description, MidiDeviceType.INPUT);
  }

  @Deprecated
  public static MidiDevice getMidiDeviceByDescription(String description, 
						 MidiDeviceType t) 
	throws MidiUnavailableException {
	MidiDevice.Info[] infoarray = MidiSystem.getMidiDeviceInfo();
	for (MidiDevice.Info info : infoarray) {
	    if (info.getDescription().contains(description)) {
		MidiDevice device = MidiSystem.getMidiDevice(info);
		if (t.equals(MidiDeviceType.INPUT) 
		    && device.getMaxTransmitters() != 0)
		    return device;
		else if (t.equals(MidiDeviceType.OUTPUT) 
			 && device.getMaxReceivers() != 0)
		    return device;
		else
		    continue;
	    }
	}
	return null;
  }

  public static MidiDevice getMidiOutDeviceByVendor(String vendor) 
    throws MidiUnavailableException {
    return getMidiDeviceByVendor(vendor, MidiDeviceType.OUTPUT);
  }

  public static MidiDevice getMidiInDeviceByVendor(String vendor) 
    throws MidiUnavailableException {
    return getMidiDeviceByVendor(vendor, MidiDeviceType.INPUT);
  }

  @Deprecated
  public static MidiDevice getMidiDeviceByVendor(String vendor, 
						 MidiDeviceType t) 
    throws MidiUnavailableException {
    MidiDevice.Info[] infoarray = MidiSystem.getMidiDeviceInfo();
    for (MidiDevice.Info info : infoarray) {
      if (info.getVendor().contains(vendor)) {
        MidiDevice device = MidiSystem.getMidiDevice(info);
        if (t.equals(MidiDeviceType.INPUT) 
            && device.getMaxTransmitters() > 0)
          return device;
        else if (t.equals(MidiDeviceType.OUTPUT) 
                 && device.getMaxReceivers() > 0)
          return device;
        else
          continue;
      }
    }
    return null;
  }

  @Deprecated
  public static List<String> toStringList(List<MidiDevice.Info> infolist) {
    List<String> strlist = new ArrayList<String>();
    int i = 0;
    for (MidiDevice.Info info : infolist) 
      strlist.add(i++ + ": " + info.getName() + " (" + info.getVendor()
                  + " / " + info.getDescription()+ ")" );
    return strlist;
  }
  
	
	    
    /*
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
    */

  public static AudioDataCompatible makeMultiChannelWaveform(List<? extends AudioDataCompatible> wav) {
    return makeMultiChannelWaveform(
      wav.toArray(new AudioDataCompatible[wav.size()])
    );
  }

  public static AudioDataCompatible makeMultiChannelWaveform(AudioDataCompatible[] wav) {
    int nCh = wav.length;
    int sampleRate = wav[0].sampleRate();
    int wavlength = wav[0].getDoubleArrayWaveform()[0].length();
    DoubleArray[] newwaveform = new DoubleArray[nCh];
    for (int i = 0; i < nCh; i++) {
      if (wav[i].sampleRate() != sampleRate) 
        throw new IllegalArgumentException("inconsistent sampling rate");
      newwaveform[i] = wav[i].getDoubleArrayWaveform()[0];
      if (newwaveform[i].length() != wavlength)
        throw new IllegalArgumentException("inconsistant waveform length");
    }
    return  new MutableWaveform(newwaveform, sampleRate);
  }
    

}