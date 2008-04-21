package jp.crestmuse.cmx.misc;

public interface NoteCompatible {
  int onset(int ticksPerBeat);
  int onsetInMSec();
  int offset(int ticksPerBeat);
  int offsetInMSec();
  int duration(int ticksPerBeat);
  int notenum();
  int velocity();
}
