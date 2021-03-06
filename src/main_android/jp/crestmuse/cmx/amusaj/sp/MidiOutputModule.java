package jp.crestmuse.cmx.amusaj.sp;

import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Track;

public class MidiOutputModule extends SPModule {

    private Receiver receiver;
    private Track recoder;
    
    public MidiOutputModule(MidiDevice dev) throws MidiUnavailableException {
      this(dev.getReceiver());
    }

    public MidiOutputModule() throws MidiUnavailableException {
      this(MidiSystem.getReceiver());
    }

    public MidiOutputModule(Receiver rec) {
      this(rec, null);
    }
    
    public MidiOutputModule(Receiver rec, Track track){
      receiver = rec;
      recoder = track;
    }

    public void execute(Object[] src, TimeSeriesCompatible[] dest) throws InterruptedException {
      MidiEventWithTicktime e = (MidiEventWithTicktime)src[0];
      receiver.send(e.getMessage(), 0);
      if(recoder != null){
        recoder.add(new MidiEvent(e.getMessage(), e.music_position));
      }
    }
/*
    public int getInputChannels() {
      return 1;
    }

    public int getOutputChannels() {
      return 0;
    }
*/

    public Class[] getInputClasses() {
      return new Class[]{MidiEventWithTicktime.class};
    }

    public Class[] getOutputClasses() {
      return new Class[0];
    }
  }
