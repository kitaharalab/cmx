package jp.crestmuse.cmx.sound;

import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Track;

public interface SequenceGeneratable {

  /**
   * 続く場合trueを返す
   */
  public boolean changeMeasure(Track track, long measureTick);

    public void sendInitializingMessages(Receiver r);

}
