package jp.crestmuse.cmx.elements;

public interface NoteCompatible {
  int onset(int ticksPerBeat);
  int onsetInMilliSec();
  int offset(int ticksPerBeat);
  int offsetInMilliSec();
  int duration(int ticksPerBeat);
  int notenum();
  int velocity();
}
