package jp.crestmuse.cmx.amusaj.sp;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.TickTimer;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;

public class MidiInputModule 
  extends SPModule<SPDummyObject,MidiEventWithTicktime>
  implements Receiver
{

  private TickTimer tt;
  private Transmitter tm;
  private MidiDevice input_device;
  private BlockingQueue<MidiEventWithTicktime> src_queue = new LinkedBlockingQueue<MidiEventWithTicktime>();

  public MidiInputModule(MidiDevice device)
      throws MidiUnavailableException{
    this.tt = null;
    this.input_device = device;
  }

  public MidiInputModule(TickTimer tt, MidiDevice device)
      throws MidiUnavailableException {
    this.tt = tt;
    this.input_device = device;

    // initializing device
    input_device.open();
    tm = input_device.getTransmitter();
    tm.setReceiver(this);
  }
  
  public void setTickTimer(TickTimer tt){
    this.tt = tt;
  }
  
  /*
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
*/

  public void execute(List<QueueReader<SPDummyObject>> src,
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

  // Receiver
  public void close() {
    tm.close();
    input_device.close();
  }

  public void send(MidiMessage message, long timeStamp) {
    // MIDIメッセージが来るたびにsendが呼び出される。
    long position = -1;
    if(tt != null)
      position = tt.getTickPosition();
    MidiEventWithTicktime miwt = new MidiEventWithTicktime(message,
        timeStamp, position);
    src_queue.add(miwt);
  }

/*
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
*/
}
