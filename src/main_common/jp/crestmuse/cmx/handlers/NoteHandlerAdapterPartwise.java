package jp.crestmuse.cmx.handlers;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;

public class NoteHandlerAdapterPartwise implements NoteHandlerPartwise {
  public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper) {
    // do nothing
  }

  public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper) {
    // do nothing
  }

  public void beginMeasure(MusicXMLWrapper.Measure measure, 
                           MusicXMLWrapper wrapper) {
    // do nothing
  }

  public void endMeasure(MusicXMLWrapper.Measure measure, 
                         MusicXMLWrapper wrapper) {
    // do nothing
  }

  public void processMusicData(MusicXMLWrapper.MusicData md, 
                               MusicXMLWrapper wrapper) {
    // do nothing
  }
}
