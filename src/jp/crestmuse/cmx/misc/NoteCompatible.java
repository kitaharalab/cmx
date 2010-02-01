package jp.crestmuse.cmx.misc;

public interface NoteCompatible {
  int onset(int ticksPerBeat);
  int onsetInMilliSec();
  /**@deprecated*/
  int onsetInMSec();
  int offset(int ticksPerBeat);
  int offsetInMilliSec();
  /**@deprecated*/
  int offsetInMSec();
  int duration(int ticksPerBeat);
  int notenum();
  int velocity();
}
