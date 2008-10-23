package jp.crestmuse.cmx.amusaj.sp;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.misc.QueueWrapper;
import jp.crestmuse.cmx.sound.MusicPlayer;
import jp.crestmuse.cmx.sound.SMFPlayer;

public class MidiInputOutputModule {

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

  public class MidiInput implements
      ProducerConsumerCompatible<Object, MidiEventWithTicktime>, Receiver,
      Runnable {

    MusicPlayer mp;
    SPExecutor sp;
    Transmitter tm;
    MidiDevice input_device;
    BlockingQueue<MidiEventWithTicktime> src_queue = new LinkedBlockingQueue<MidiEventWithTicktime>();

    public MidiInput(MusicPlayer mp, SPExecutor sp, MidiDevice device)
        throws MidiUnavailableException {
      this.input_device = device;
      this.mp = mp;
      this.sp = sp;

      // initializing device
      input_device.open();
      tm = input_device.getTransmitter();
      tm.setReceiver(this);
    }

    public void play() {
      mp.play();
      Thread th = new Thread(this);
      th.start();
    }

    // ProducerConsumerCompatible
    public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
        int frames, int timeunit) {
      return new MidiEvents();
    }

    public void execute(List<QueueReader<Object>> src,
        List<TimeSeriesCompatible<MidiEventWithTicktime>> dest)
        throws InterruptedException {
      dest.get(0).add(src_queue.take());
    }

    public int getInputChannels() {
      return 0;
    }

    public int getOutputChannels() {
      return 1;
    }

    public void setParams(Map<String, Object> params) {
    }

    // Receiver
    public void close() {
    }

    public void send(MidiMessage message, long timeStamp) {
      // MIDIメッセージが来るたびにsendが呼び出される。
      long position = -1;
      if (mp.isNowPlaying()) {
        position = mp.getTickPosition();
      }
      MidiEventWithTicktime miwt = new MidiEventWithTicktime(message,
          timeStamp, position);
      src_queue.add(miwt);
    }

    // Runnable
    // MusicPlayerを監視し、曲が停止したら終了処理
    public void run() {
      while (mp.isNowPlaying()) {
        try {
          Thread.sleep(1000);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      sp.stop();
      tm.close();
      input_device.close();
    }
  }

  public class OctaveUp implements ProducerConsumerCompatible<MidiEventWithTicktime, MidiEventWithTicktime>{

    public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
        int frames, int timeunit) {
      return new MidiEvents();
    }

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

    public void setParams(Map<String, Object> params) {
    }
    
    
  }
  
  public class MidiOutput implements
      ProducerConsumerCompatible<MidiEventWithTicktime, Object> {

    Receiver receiver = null;

    public MidiOutput(Receiver receiver) {
      this.receiver = receiver;
    }

    public TimeSeriesCompatible<Object> createOutputInstance(int frames,
        int timeunit) {
      return null;
    }

    public void execute(List<QueueReader<MidiEventWithTicktime>> src,
        List<TimeSeriesCompatible<Object>> dest) throws InterruptedException {
      MidiEventWithTicktime e = src.get(0).take();
      receiver.send(e.getMessage(), e.getTick());
    }

    public int getInputChannels() {
      return 1;
    }

    public int getOutputChannels() {
      return 0;
    }

    public void setParams(Map<String, Object> params) {
    }

  }

  public class PrintModule implements ProducerConsumerCompatible<MidiEventWithTicktime, MidiEventWithTicktime>{

    @Override
    public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
        int frames, int timeunit) {
      return new MidiEvents();
    }

    @Override
    public void execute(List<QueueReader<MidiEventWithTicktime>> src,
        List<TimeSeriesCompatible<MidiEventWithTicktime>> dest)
        throws InterruptedException {
      MidiEventWithTicktime e =  src.get(0).take();
      System.out.println(e.getMessage().getMessage()[1]);
      dest.get(0).add(e);
    }

    @Override
    public int getInputChannels() {
      return 1;
    }

    @Override
    public int getOutputChannels() {
      return 1;
    }

    @Override
    public void setParams(Map<String, Object> params) {
    }
    
  }
  
  public class MidiEventWithTicktime extends MidiEvent {
    long music_position;

    public MidiEventWithTicktime(MidiMessage message, long tick, long position) {
      super(message, tick);
      music_position = position;
    }
  }

  public class MidiEvents implements
      TimeSeriesCompatible<MidiEventWithTicktime> {

    private LinkedBlockingQueue<MidiEventWithTicktime> queue = new LinkedBlockingQueue<MidiEventWithTicktime>();
    private QueueWrapper<MidiEventWithTicktime> qWrapper;
    private int frames = 1024;

    MidiEvents() {
      queue = new LinkedBlockingQueue<MidiEventWithTicktime>();
      qWrapper = new QueueWrapper<MidiEventWithTicktime>(queue, frames);
    }

    public void add(MidiEventWithTicktime d) throws InterruptedException {
      queue.add(d);
    }

    public int bytesize() {
      return 0;
    }

    public int dim() {
      return 1;
    }

    public int frames() {
      return frames;
    }

    public QueueReader<MidiEventWithTicktime> getQueueReader() {
      return qWrapper.createReader();
    }

    public int timeunit() {
      return 0;
    }

    public String getAttribute(String key) {
      return null;
    }

    public double getAttributeDouble(String key) {
      return 0;
    }

    public int getAttributeInt(String key) {
      return 0;
    }

    public Iterator<Entry<String, String>> getAttributeIterator() {
      return null;
    }

    public void setAttribute(String key, String value) {
    }

    public void setAttribute(String key, int value) {
    }

    public void setAttribute(String key, double value) {
    }

  }

  public MidiInputOutputModule(String[] args) {
    try {
      SPExecutor sp = new SPExecutor(null, 0, 0);
      SMFPlayer player = new SMFPlayer();
      player.readSMF(args[0]);
      MidiInput mi = new MidiInput(player, sp, setMidiDevice());
      MidiOutput mo = new MidiOutput(MidiSystem.getReceiver());
      OctaveUp ou = new OctaveUp();
      PrintModule pm = new PrintModule();
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
      mi.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new MidiInputOutputModule(args);
  }

}
