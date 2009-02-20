package jp.crestmuse.cmx.musicrepresentation;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.misc.Chord;
import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.MusicElement;
import jp.crestmuse.cmx.sound.SequenceGeneratable;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentationGenerator implements SequenceGeneratable {

  public int VELOCITY = 63;
  private MusicRepresentation musicRepresentation;

  public MusicRepresentationGenerator(MusicRepresentation mr) {
    this.musicRepresentation = mr;
  }

  public boolean changeMeasure(Track track, long measureTick) {
    // 次と次の次のコードがまだ予測されていなければ現在のコードと同じ値を設定しておく
    int nextMeasureIndex = (int) measureTick
        / (SequencerManager.TICKS_PER_BEAT * 4)
        * musicRepresentation.getDivision();
    if (nextMeasureIndex >= musicRepresentation.getMeasureNum()
        * musicRepresentation.getDivision())
      return false;
    /*
    MusicElement currentElem = musicRepresentation.getMusicElement(1, Math.max(
        nextMeasureIndex - musicRepresentation.getDivision(), 0));
    MusicElement nextElem = musicRepresentation.getMusicElement(1,
        nextMeasureIndex);
    */
    MusicElement currentElem = musicRepresentation.getChordElement(Math.max(nextMeasureIndex - musicRepresentation.getDivision(), 0));
    MusicElement nextElem = musicRepresentation.getChordElement(nextMeasureIndex);
    if (nextElem == null) {
      nextElem = musicRepresentation.addChordElement(nextMeasureIndex);
      nextElem.setProd(currentElem.getHighestProdIndex(), 0.1);
    }
    int nextElemHighestProdIndex = nextElem.getHighestProdIndex();
    if (nextMeasureIndex < (musicRepresentation.getMeasureNum() - 1)
        * musicRepresentation.getDivision()
        && musicRepresentation.getChordElement(nextMeasureIndex
            + musicRepresentation.getDivision()) == null) {
      /*
      musicRepresentation.setPredict(1, nextMeasureIndex
          + musicRepresentation.getDivision(), nextElem);
      */
      MusicElement chord = musicRepresentation.addChordElement(nextMeasureIndex + musicRepresentation.getDivision());
      chord.setProd(chord.indexOf(nextElem.getLabel(nextElemHighestProdIndex)), 0.01);
    }

    // 伴奏データをシーケンサに追加
    for (int i = 0; i < 4; i++) {
      Chord c = new Chord(nextElem.getLabel(nextElemHighestProdIndex));
      for (int num : c.getNotesList()) {
        try {
          ShortMessage sm = new ShortMessage();
          sm.setMessage(ShortMessage.NOTE_ON, 2, num, VELOCITY);
          track.add(new MidiEvent(sm, measureTick + i
              * SequencerManager.TICKS_PER_BEAT));
          sm = new ShortMessage();
          sm.setMessage(ShortMessage.NOTE_ON, 2, num, VELOCITY);
          track.add(new MidiEvent(sm, measureTick + (i + 1)
              * SequencerManager.TICKS_PER_BEAT));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
      }
    }

    // MusicRepresentationを更新する（ベースを予測する）
    //musicRepresentation.setEvidence(1, nextMeasureIndex, nextElem);
    nextElem.setEvidence(nextElemHighestProdIndex);

    // ベースデータをシーケンサに追加
    for (int i = 0; i < 4; i++) {
      /*
      int num = musicRepresentation
          .getMusicElement(2, nextMeasureIndex + i * 2).getNums()[0];
      */
      int num = musicRepresentation.getBassElement(nextMeasureIndex + i*2).getHighestProdIndex();
      try {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 3, num, VELOCITY);
        track.add(new MidiEvent(sm, measureTick + i
            * SequencerManager.TICKS_PER_BEAT));
        sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 3, num, VELOCITY);
        track.add(new MidiEvent(sm, measureTick + (i + 1)
            * SequencerManager.TICKS_PER_BEAT));
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

}
