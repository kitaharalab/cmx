package jp.crestmuse.cmx.amusaj.sp;

import java.util.List;

import javax.sound.midi.Receiver;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;
import jp.crestmuse.cmx.misc.QueueReader;

public class MidiOutputModule extends SPModule<MidiEventWithTicktime,Object> {
  //implements
  //      ProducerConsumerCompatible<MidiEventWithTicktime, Object> {

    Receiver receiver = null;

    public MidiOutputModule(Receiver receiver) {
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

//    public void setParams(Map<String, Object> params) {
//    }

  }