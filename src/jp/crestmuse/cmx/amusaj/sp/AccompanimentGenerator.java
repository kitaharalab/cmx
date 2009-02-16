package jp.crestmuse.cmx.amusaj.sp;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.amusaj.filewrappers.StringElement;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.ChordOperator;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.SequenceGeneratable;

public class AccompanimentGenerator
    extends SPModule<StringElement, SPDummyObject>
    implements SequenceGeneratable {

  private static String currentChord;
  private String nextChord;
  private LinkedList<ShortMessageEvent> shortMessages;
  private boolean finished = false;

  public AccompanimentGenerator(String chord, String smf)
      throws MidiUnavailableException, InvalidMidiDataException, IOException{
    currentChord = nextChord = chord;
    shortMessages = new LinkedList<ShortMessageEvent>();
    Sequence seq = MidiSystem.getSequence(new File(smf));
    for(Track track : seq.getTracks()){
      for(int i=0; i<track.size(); i++){
        MidiEvent me = track.get(i);
        MidiMessage mm = me.getMessage();
        int status = mm.getStatus() & 0xff;
        // 2chでノートオンかオフのとき
        if(status == 129 || status == 145){
          ShortMessage sm = new ShortMessage();
          sm.setMessage(status, mm.getMessage()[1], mm.getMessage()[2]);
          shortMessages.add(new ShortMessageEvent(sm, me.getTick()));
        }
      }
    }
  }

  public void execute(List<QueueReader<StringElement>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest) throws InterruptedException {
    nextChord = src.get(0).take().encode();
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 0;
  }

  public boolean changeMeasure(Track track, long measureTick) {
    if(finished) return false;
    currentChord = nextChord;
    int[] map = ChordOperator.diatonic_map_inC(currentChord);
    Iterator<ShortMessageEvent> it = shortMessages.iterator();
    while(it.hasNext()){
      for(int i=0; i<3; i++){
        ShortMessageEvent sme = it.next();
        try {
          ShortMessage newSm = new ShortMessage();
          newSm.setMessage(sme.sm.getStatus(), sme.sm.getData1() + map[i], sme.sm.getData2());
          //newSm.setMessage(sme.sm.getStatus(), sme.sm.getData1() + map[i], 0);
          track.add(new MidiEvent(newSm, sme.tick + measureTick));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
      }
    }
    return true;
  }

  @Override
  public void stop(List<QueueReader<StringElement>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest) {
    finished = true;
  }

  public static String getChord(){ return currentChord; }

  private class ShortMessageEvent{
    ShortMessage sm;
    long tick;
    ShortMessageEvent(ShortMessage sm, long tick){
      this.sm = sm;
      this.tick = tick;
    }
  }

}
