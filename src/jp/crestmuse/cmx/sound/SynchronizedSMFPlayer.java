package jp.crestmuse.cmx.sound;
import javax.sound.midi.*;

public class SynchronizedSMFPlayer extends SMFPlayer 
  implements WAVPlaySynchronized {

  public SynchronizedSMFPlayer() throws MidiUnavailableException {
    super();
  }

  public void synchronize(double currentTime, WAVPlaySynchronizer wavsync) {
    sequencer.setMicrosecondPosition((long)(1000 * 1000 * currentTime));
  }

  public void start(WAVPlaySynchronizer wavsync) {
    play();
  }

  public void stop(WAVPlaySynchronizer wavsync) {
    stop();
  }
}