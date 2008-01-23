package jp.crestmuse.cmx.handlers;
import jp.crestmuse.cmx.misc.*;

public interface CommonNoteHandler {
  void beginPart(String id, PianoRollCompatible filewrapper);
  void endPart(String id, PianoRollCompatible filewrapper);
  void processNote(NoteCompatible note, PianoRollCompatible filewrapper);
}
