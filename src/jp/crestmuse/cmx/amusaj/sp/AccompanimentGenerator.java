package jp.crestmuse.cmx.amusaj.sp;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.amusaj.filewrappers.BayesNetWrapper;
import jp.crestmuse.cmx.amusaj.filewrappers.StringElement;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.ChordOperator;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.TickTimer;

public class AccompanimentGenerator
    extends SPModule<StringElement, SPDummyObject>
    implements TickTimer, Runnable {

  private int currentMeasure = 1;
  private Sequencer sequencer;
  private String nextChord;
  private Thread player;
  private LinkedList<ShortMessageEvent> shortMessages;
  
  public AccompanimentGenerator(String chord, String smf)
      throws MidiUnavailableException, InvalidMidiDataException, IOException{
    sequencer = MidiSystem.getSequencer(false);
    sequencer.getTransmitter().setReceiver(MidiSystem.getReceiver());
    sequencer.open();
    nextChord = chord;
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
    sequencer.setSequence(seq);
    player = new Thread(this);
    player.start();
  }

  public long getTickPosition() {
    return currentMeasure;
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
  
  @Override
  public void stop(List<QueueReader<StringElement>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest) {
    player.interrupt();
  }

  public void run() {
    long ticksPerBeat = sequencer.getSequence().getResolution();
    Track track = sequencer.getSequence().getTracks()[0];
    sequencer.start();
    while(true){
      if(sequencer.getTickPosition() >= ticksPerBeat*3*currentMeasure){
        currentMeasure++;
        int[] map = ChordOperator.diatonic_map_inC(nextChord);
        long tickLength = sequencer.getTickLength();
        Iterator<ShortMessageEvent> it = shortMessages.iterator();
        while(it.hasNext()){
          for(int i=0; i<3; i++){
            ShortMessageEvent sme = it.next();
            try {
              ShortMessage newSm = new ShortMessage();
              newSm.setMessage(sme.sm.getStatus(), sme.sm.getData1() + map[i], sme.sm.getData2());
              track.add(new MidiEvent(newSm, sme.tick + tickLength));
            } catch (InvalidMidiDataException e) {
              e.printStackTrace();
            }
          }
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
    sequencer.close();
  }
  
  private class ShortMessageEvent{
    ShortMessage sm;
    long tick;
    ShortMessageEvent(ShortMessage sm, long tick){
      this.sm = sm;
      this.tick = tick;
    }
  }
  
  public static void main(String[] args){
    try {
      MidiDevice dev = MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[0]);
      MidiInputModule mi = new MidiInputModule(dev);
      MidiOutputModule mo = new MidiOutputModule(MidiSystem.getReceiver());
      ChordPredictorModule cp = new ChordPredictorModule(new ChordPredictor(new BayesNetWrapper("MaxDemo/080811model3.bif")));
      AccompanimentGenerator ag = new AccompanimentGenerator("C", "midis/C.mid");
      mi.setTickTimer(ag);

      SPExecutor sp = new SPExecutor(null, 1);
      sp.addSPModule(mi);
      sp.addSPModule(cp);
      sp.addSPModule(ag);
      sp.addSPModule(mo);
      sp.connect(mi, 0, cp, 0);
      sp.connect(mi, 0, mo, 0);
      sp.connect(cp, 0, ag, 0);
      sp.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
