package jp.crestmuse.cmx.sound;

import javax.sound.midi.Track;

public interface SequenceGeneratable {

  /**
   * 続く場合trueを返す
   */
  public boolean changeMeasure(Track track, long measureTick);

}
