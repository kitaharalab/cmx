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
import jp.crestmuse.cmx.sound.MusicPlayer;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;

public class MidiInputModule 
  extends SPModule<Object,MidiEventWithTicktime>
  implements Receiver, Runnable 
//    implements
//      ProducerConsumerCompatible<Object, MidiEventWithTicktime>, Receiver,
//      Runnable 
{

  MusicPlayer mp;
  SPExecutor sp;
  Transmitter tm;
  MidiDevice input_device;
  BlockingQueue<MidiEventWithTicktime> src_queue = new LinkedBlockingQueue<MidiEventWithTicktime>();

  public MidiInputModule(SPExecutor sp, MidiDevice device)
      throws MidiUnavailableException{
    this.input_device = device;
    this.sp = sp;
    
    
    
  }
  public MidiInputModule(MusicPlayer mp, SPExecutor sp, MidiDevice device)
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

/*
  // ProducerConsumerCompatible
  public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
      int frames, int timeunit) {
    return new MidiEvents();
  }
*/

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

//  public void setParams(Map<String, Object> params) {
//  }

  // Receiver
  public void close() {}

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
