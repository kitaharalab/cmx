package jp.crestmuse.cmx.amusaj.sp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import jp.crestmuse.cmx.amusaj.filewrappers.BayesNetWrapper;
import jp.crestmuse.cmx.amusaj.filewrappers.StringElement;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.TickTimer;
import jp.crestmuse.cmx.sound.VirtualKeyboard;

public class AccompanimentGenerator
    extends SPModule<StringElement, SPDummyObject>
    implements TickTimer, Runnable {

  private int currentSpan = 1;
  private Sequencer sequencer;
  private HashMap<String, Sequence> chord2sequence;
  private String nextChord;
  private Thread player;
  
  public AccompanimentGenerator(Map<String, String> chord2smf)
      throws MidiUnavailableException, InvalidMidiDataException, IOException{
    sequencer = MidiSystem.getSequencer();
    sequencer.open();
    chord2sequence = new HashMap<String, Sequence>();
    for(String chord : chord2smf.keySet()){
      Sequence seq = MidiSystem.getSequence(new File(chord2smf.get(chord)));
      chord2sequence.put(chord, seq);
      nextChord = chord;
    }
    sequencer.setSequence(chord2sequence.get(nextChord));
    player = new Thread(this);
    player.start();
  }

  public long getTickPosition() {
    return currentSpan;
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
    sequencer.start();
    while(true){
      if(!sequencer.isRunning()){
        currentSpan++;
        try {
          sequencer.setSequence(chord2sequence.get(nextChord));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
        sequencer.setTickPosition(0);
        sequencer.start();
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
    sequencer.close();
  }
  
  public static void main(String[] args){
    try {
      MidiInputModule mi = new MidiInputModule(new VirtualKeyboard());
      MidiOutputModule mo = new MidiOutputModule(MidiSystem.getReceiver());
      ChordPredictorModule cp = new ChordPredictorModule(new ChordPredictor(new BayesNetWrapper("MaxDemo/080811model3.bif")));
      Map<String, String> c2s = new HashMap<String, String>();
      // init maps
      c2s.put("C", "midis/C.mid");
      c2s.put("Dm", "midis/Dm.mid");
      c2s.put("Em", "midis/Em.mid");
      c2s.put("F", "midis/F.mid");
      c2s.put("G", "midis/G.mid");
      c2s.put("Am", "midis/Am.mid");
      c2s.put("Bm(b5)", "midis/Bm(b5).mid");
      AccompanimentGenerator ag = new AccompanimentGenerator(c2s);
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
