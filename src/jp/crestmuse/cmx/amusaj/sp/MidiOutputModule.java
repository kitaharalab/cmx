package jp.crestmuse.cmx.amusaj.sp;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;
import jp.crestmuse.cmx.misc.QueueReader;

public class MidiOutputModule extends SPModule<MidiEventWithTicktime,SPDummyObject> {

    private Receiver receiver;
    private Track recoder;
    
    public MidiOutputModule(Receiver rec) {
      this(rec, null);
    }
    
    public MidiOutputModule(Receiver rec, Track track){
      receiver = rec;
      recoder = track;
    }

    public void execute(List<QueueReader<MidiEventWithTicktime>> src,
        List<TimeSeriesCompatible<SPDummyObject>> dest) throws InterruptedException {
      MidiEventWithTicktime e = src.get(0).take();
      receiver.send(e.getMessage(), 0);
      if(recoder != null){
        recoder.add(new MidiEvent(e.getMessage(), e.music_position));
      }
    }

    public int getInputChannels() {
      return 1;
    }

    public int getOutputChannels() {
      return 0;
    }

  }
