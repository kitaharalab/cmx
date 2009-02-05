package jp.crestmuse.cmx.amusaj.sp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.SMFPlayer;

public class MidiModuleSamples implements Runnable{
  
  SPExecutor sp = null;
  SMFPlayer player = null;
  MidiInputModule  mi = null;
  MidiOutputModule mo = null;
  OctaveUp ou = null;
  PrintModule pm = null;
  
  public static MidiDevice.Info[] getMidiDeviceInfos() {
    return MidiSystem.getMidiDeviceInfo();
  }

  public MidiDevice setMidiDevice() {
    MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
    MidiDevice device = null;

    for (int i = 0; i < info.length; i++) {
      try {
        System.err.println("*** " + i + " ***");
        System.err.println("  Description:" + info[i].getDescription());
        System.err.println("  Name:" + info[i].getName());
        System.err.println("  Vendor:" + info[i].getVendor());
        device = MidiSystem.getMidiDevice(info[i]);
        if (device instanceof Sequencer) {
          System.err.println("  *** This is Sequencer.");
        }
        if (device instanceof Synthesizer) {
          System.err.println("  *** This is Synthesizer.");
        }
        System.err.println();
      } catch (MidiUnavailableException e) {
        e.printStackTrace();
      }
    }

    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
      System.out.print("Using Device Number: ");
      String s = r.readLine();
      device = MidiSystem.getMidiDevice(info[Integer.parseInt(s)]);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return device;
  }
  
  public class OctaveUp extends SPModule<MidiEventWithTicktime,MidiEventWithTicktime> {
    //implements ProducerConsumerCompatible<MidiEventWithTicktime, 
    //MidiEventWithTicktime>{

//      public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
//          int frames, int timeunit) {
//        return new MidiEvents();
//      }

      public void execute(List<QueueReader<MidiEventWithTicktime>> src,
          List<TimeSeriesCompatible<MidiEventWithTicktime>> dest)
          throws InterruptedException {
        MidiEventWithTicktime  me = src.get(0).take();
        ShortMessage sm = (ShortMessage)me.getMessage();
        try {
          sm.setMessage(sm.getStatus(), Math.min(127, sm.getData1() + 12), sm.getData2());
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
        dest.get(0).add(new MidiEventWithTicktime(sm, me.getTick(), me.music_position));
      }

      public int getInputChannels() {
        return 1;
      }

      public int getOutputChannels() {
        return 1;
      }

//      public void setParams(Map<String, Object> params) {
//      }
      
      
    }
  
  public class PrintModule extends SPModule<MidiEventWithTicktime,MidiEventWithTicktime> {
    //implements ProducerConsumerCompatible<MidiEventWithTicktime, 
    //MidiEventWithTicktime>{

//      @Override
//      public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
//          int frames, int timeunit) {
//        return new MidiEvents();
//      }

      public void execute(List<QueueReader<MidiEventWithTicktime>> src,
          List<TimeSeriesCompatible<MidiEventWithTicktime>> dest)
          throws InterruptedException {
        MidiEventWithTicktime e =  src.get(0).take();
        System.out.println(e.getMessage().getMessage()[1]);
        dest.get(0).add(e);
      }

      public int getInputChannels() {
        return 1;
      }

      public int getOutputChannels() {
        return 1;
      }

//      @Override
//      public void setParams(Map<String, Object> params) {
//      }
      
    }
  
  public void run(){
    while(player.isNowPlaying() == true){
      try{
        Thread.sleep(1000);
      }catch(Exception e){
        e.printStackTrace();
      }
    }
    sp.stop();
    mi.close();
  }
  
  public MidiModuleSamples(String[] args){
    try {
      sp = new SPExecutor(null, 0);
      player = new SMFPlayer();
      player.readSMF(args[0]);
      //mi = new MidiInputModule(player, new VirtualKeyboard());
      mi = new MidiInputModule(player, MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[0]));
      mo = new MidiOutputModule(MidiSystem.getReceiver());
      //ou = new OctaveUp();
      pm = new PrintModule();
      sp.addSPModule(mi);
      sp.addSPModule(pm);
      //sp.addSPModule(ou);
      sp.addSPModule(mo);
      sp.connect(mi, 0, pm, 0);
      sp.connect(pm, 0, mo, 0);
      //sp.connect(mi, 0, mo, 0);
      sp.start();
      System.out.println("press enter to start >>>");
      System.in.read();
      player.play();
      Thread th = new Thread(this);
      th.start();
      //mi.play();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    new MidiModuleSamples(args);
  }
  
}
