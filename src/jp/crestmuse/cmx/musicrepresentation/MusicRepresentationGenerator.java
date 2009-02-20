package jp.crestmuse.cmx.musicrepresentation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.misc.Chord;
import jp.crestmuse.cmx.misc.ChordOperator;
import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.MusicElement;
import jp.crestmuse.cmx.sound.SequenceGeneratable;
import jp.crestmuse.cmx.sound.SequencerManager;

public class MusicRepresentationGenerator implements SequenceGeneratable {

  public int VELOCITY = 100;
  private MusicRepresentation musicRepresentation;
  private List<ShortMessageEvent> messages;

  public MusicRepresentationGenerator(MusicRepresentation mr, String smf) throws InvalidMidiDataException, IOException {
    this.musicRepresentation = mr;
    this.messages = new LinkedList<ShortMessageEvent>();
    Sequence seq = MidiSystem.getSequence(new File(smf));
    for(Track track : seq.getTracks()){
      for(int i=0; i<track.size(); i++){
        MidiEvent me = track.get(i);
        MidiMessage mm = me.getMessage();
        int status = mm.getStatus() & 0xff;
        // 2chでノートオンかオフのとき
        if(status == 129 || status == 145){
          ShortMessage sm = new ShortMessage();
          sm.setMessage(status, mm.getMessage()[1], mm.getMessage()[2]);
          messages.add(new ShortMessageEvent(sm, me.getTick()));
        }
      }
    }
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
    /*
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
    */
    int[] map = diatonicMapC(nextElem.getLabel(nextElemHighestProdIndex));
    Iterator<ShortMessageEvent> it = messages.iterator();
    while(it.hasNext()){
      for(int i=0; i<3; i++){
        ShortMessageEvent sme = it.next();
        try {
          ShortMessage newSm = new ShortMessage();
          newSm.setMessage(sme.sm.getStatus(), sme.sm.getData1() + map[i], sme.sm.getData2());
          track.add(new MidiEvent(newSm, sme.tick + measureTick));
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
    return true;
  }

  private int[] diatonicMapC(String chord){
    int[] diatonic = new int[3];
    if(chord.equals("C")) diatonic = new int[] {0, 0, 0};
    else if(chord.equals("Dm")) diatonic = new int[] {2, 1, 2};
    else if(chord.equals("Em")) diatonic = new int[] {4, 3, 4};
    else if(chord.equals("F")) diatonic = new int[] {5, 5, 5};
    else if(chord.equals("G")) diatonic = new int[] {7, 7, 7};
    else if(chord.equals("Am")) diatonic = new int[] {9, 8, 9};
    else diatonic =  new int[] {11, 10, 10}; //B(b5)
    return diatonic;
  }

  private class ShortMessageEvent{
    ShortMessage sm;
    long tick;
    ShortMessageEvent(ShortMessage sm, long tick){
      this.sm = sm;
      this.tick = tick;
    }
  }

}
