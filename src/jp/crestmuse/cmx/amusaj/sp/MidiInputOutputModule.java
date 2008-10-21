package jp.crestmuse.cmx.amusaj.sp;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.misc.QueueWrapper;
import jp.crestmuse.cmx.sound.MusicPlayer;
import jp.crestmuse.cmx.sound.SMFPlayer;

public class MidiInputOutputModule {

  public class MidiInput implements
      ProducerConsumerCompatible<Object, MidiEventWithTicktime>,
      Receiver, Runnable {

    MusicPlayer mp;
    SPExecutor sp;
    Transmitter tm;

    BlockingQueue<MidiEventWithTicktime> src_queue = new LinkedBlockingQueue<MidiEventWithTicktime>();

    public MidiInput(MusicPlayer mp, SPExecutor sp)
        throws MidiUnavailableException {
      this.mp = mp;
      this.sp = sp;

      // initializing device
      tm = MidiSystem.getTransmitter();
      tm.setReceiver(this);

      // initializing
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
      MidiEventWithTicktime miwt = new MidiEventWithTicktime(message,
          timeStamp, mp.getTickPosition());
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
    }

  }

  public class MidiOutput implements
      ProducerConsumerCompatible<MidiEventWithTicktime, Object> {

    Receiver receiver = null;

    public MidiOutput() throws MidiUnavailableException {
      receiver = MidiSystem.getReceiver();
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
    private int frames = 256;

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

  public MidiInputOutputModule() {
    try {
      SPExecutor sp = new SPExecutor(null, 0, 0);
      SMFPlayer player = new SMFPlayer();
      player.readSMF("kaeru01.mid");
      MidiInput mi = new MidiInput(player, sp);
      MidiOutput mo = new MidiOutput();
      sp.addSPModule(mi);
      sp.addSPModule(mo);
      sp.connect(mi, 0, mo, 0);
      sp.start();
      // 擬似的にイベントを発生させる
      final Receiver r = mi;
      JFrame f = new JFrame();
      f.addKeyListener(new KeyListener() {
        public void keyPressed(KeyEvent e) {
          try {
            ShortMessage myMsg = new ShortMessage();
            myMsg
                .setMessage(ShortMessage.NOTE_ON, 0, e.getKeyCode() % 128, 100);
            r.send(myMsg, -1);
          } catch (InvalidMidiDataException e1) {
            e1.printStackTrace();
          }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
      });
      f.setSize(256, 256);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new MidiInputOutputModule();
  }

}
