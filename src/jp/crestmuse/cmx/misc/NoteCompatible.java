package jp.crestmuse.cmx.misc;

public interface NoteCompatible extends OnsetOffsetCompatible {
  /**@deprecated*/
  int onsetInMSec();
  /**@deprecated*/
  int offsetInMSec();

  int notenum();
  int velocity();
}
