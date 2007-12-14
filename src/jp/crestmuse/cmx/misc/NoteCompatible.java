package jp.crestmuse.cmx.misc;

public interface NoteCompatible {
  int onset(int ticksPerBeat);
  int offset(int ticksPerBeat);
  int duration(int ticksPerBeat);
  int notenum();
}
