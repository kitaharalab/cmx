package jp.crestmuse.cmx.musicrepresentation;

import java.util.List;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime;
import jp.crestmuse.cmx.amusaj.sp.SPDummyObject;
import jp.crestmuse.cmx.amusaj.sp.SPModule;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.MusicElement;

public class MusicRepresentationModule extends
    SPModule<MidiEventWithTicktime, SPDummyObject> {

  private MusicRepresentation musicRepresentation;

  public MusicRepresentationModule(MusicRepresentation mr){
    musicRepresentation = mr;
  }

  public void execute(List<QueueReader<MidiEventWithTicktime>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest)
      throws InterruptedException {
    MidiEventWithTicktime w = src.get(0).take();
    /*
    NoteElement ne = new NoteElement();
    ne.note = new int[]{w.getMessage().getMessage()[1]};
    musicRepresentation.setEvidence(0, w.music_position, ne);
    */
    int index = musicRepresentation.getIndex(w.music_position);
    MusicElement me = musicRepresentation.addMelodyElement(index);
    me.setEvidence(w.getMessage().getMessage()[1]);
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 0;
  }

}
