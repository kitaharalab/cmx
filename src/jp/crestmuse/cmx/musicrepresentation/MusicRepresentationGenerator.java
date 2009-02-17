package jp.crestmuse.cmx.musicrepresentation;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

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
    MusicElement currentElem = musicRepresentation.getMusicElement(1, Math.max(
        nextMeasureIndex - musicRepresentation.getDivision(), 0));
    MusicElement nextElem = musicRepresentation.getMusicElement(1,
        nextMeasureIndex);
    if (nextElem == null) {
      nextElem = currentElem;
    }
    if (nextMeasureIndex < (musicRepresentation.getMeasureNum() - 1)
        * musicRepresentation.getDivision()
        && musicRepresentation.getMusicElement(1, nextMeasureIndex
            + musicRepresentation.getDivision()) == null) {
      musicRepresentation.setPredict(1, nextMeasureIndex
          + musicRepresentation.getDivision(), nextElem);
    }

    // 伴奏データをシーケンサに追加
    for (int i = 0; i < 4; i++) {
      for (int num : nextElem.getNums()) {
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
    musicRepresentation.setEvidence(1, nextMeasureIndex, nextElem);

    // ベースデータをシーケンサに追加
    for (int i = 0; i < 4; i++) {
      int num = musicRepresentation
          .getMusicElement(2, nextMeasureIndex + i * 2).getNums()[0];
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
