package jp.crestmuse.cmx.sound;

import javax.sound.midi.Track;

public interface SequenceGeneratable {

  public boolean changeMeasure(Track track, long measureTick);

}
