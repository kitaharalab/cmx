package jp.crestmuse.cmx.amusaj.sp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.amusaj.filewrappers.StringElement;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.SequenceGeneratable;
import jp.crestmuse.cmx.sound.SequencerManager;

public class BaseGenerator extends SPModule<StringElement, SPDummyObject>
    implements SequenceGeneratable {

  private String nextChord;
  private boolean finished;
  private Map<String, Integer> diatonic2notenum;
  private double[] seqMap = { 1.0, 1.0, 1.0, 1.0, 1.0, 0.1, 1.0, 0.1, 0.5, 0.5,
      0.1, 0.1, 0.1, 0.1, 0.1, 0.5, 0.5, 0.1, 1.0, 0.1, 1.0, 1.0, 1.0, 1.0, 1.0 };
  private int BASE = 48;

  public BaseGenerator(String chord) {
    nextChord = chord;
    diatonic2notenum = new HashMap<String, Integer>();
    diatonic2notenum.put("C", 0);
    diatonic2notenum.put("Dm", 2);
    diatonic2notenum.put("Em", 4);
    diatonic2notenum.put("F", 5);
    diatonic2notenum.put("G", 7);
    diatonic2notenum.put("Am", 9);
    diatonic2notenum.put("Bm(b5)", 11);
  }

  public void execute(List<QueueReader<StringElement>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest)
      throws InterruptedException {
    nextChord = src.get(0).take().encode();
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 0;
  }

  public boolean changeMeasure(Track track, long measureTick) {
    if (finished)
      return false;
    int notenum = diatonic2notenum.get(nextChord) + BASE;
    addNote(track, notenum, (int) measureTick);
    int[] path = getNearestPath(AccompanimentGenerator.getChord(), nextChord);
    for (int i = 0; i < path.length; i++) {
      addNote(track, notenum + path[i], (int) measureTick
          + SequencerManager.TICKS_PER_BEAT * (i + 1));
    }
    return true;
  }

  @Override
  public void stop(List<QueueReader<StringElement>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest) {
    finished = true;
  }

  private void addNote(Track track, int notenum, int start) {
    try {
      ShortMessage sm = new ShortMessage();
      sm.setMessage(ShortMessage.NOTE_ON, 2, notenum, 100);
      track.add(new MidiEvent(sm, start));
      sm = new ShortMessage();
      sm.setMessage(ShortMessage.NOTE_OFF, 2, notenum, 0);
      track.add(new MidiEvent(sm, start + SequencerManager.TICKS_PER_BEAT));
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  private int[] getNearestPath(String from, String to) {
    int step = 3;
    int noteKinds = 25;
    int[] result = new int[step];
    DPElement[][] dpGraph = new DPElement[step][noteKinds];
    for (int i = 0; i < step; i++)
      for (int j = 0; j < noteKinds; j++)
        dpGraph[i][j] = new DPElement();

    for (int i = 0; i < noteKinds; i++) {
      int fromNum = diatonic2notenum.get(from) + BASE;
      dpGraph[0][i].dist = getNoteDist(from, 0, fromNum)
          + getSeqDist(fromNum, fromNum + i - 12);
    }

    for (int i = 0; i < step - 1; i++) {
      for (int j = 0; j < noteKinds; j++) {
        for (int k = 0; k < noteKinds; k++) {
          if (Math.abs(j - k) > 12)
            continue;
          double dist = dpGraph[i][j].dist
              + getNoteDist(from, i + 1, k - 12 + BASE)
              + getSeqDist(k - 12 + BASE, j - 12 + BASE);
          if (dpGraph[i + 1][k].dist > dist) {
            dpGraph[i + 1][k].dist = dist;
            dpGraph[i + 1][k].from = j;
          }
        }
      }
    }

    double minDist = Double.MAX_VALUE;
    int minFrom = 0;
    for (int i = 0; i < noteKinds; i++) {
      int toNum = diatonic2notenum.get(to) + BASE;
      if(Math.abs(toNum - i - 12 + BASE) > 12) continue;
      double dist = dpGraph[step - 1][i].dist
          + getSeqDist(i - 12 + BASE, toNum);
      if (dist < minDist) {
        minDist = dist;
        minFrom = i;
      }
    }

    while (step > 0) {
      step--;
      result[step] = minFrom - 12;
      minFrom = dpGraph[step][minFrom].from;
    }
    return result;
  }

  /**
   * コードがchordのとき、ベースのindex音目がnotenumである尤もらしさ(距離)．
   */
  private double getNoteDist(String chord, int index, int notenum) {
    return 0.0;
  }

  /**
   * ある音の次にある音が来る尤もらしさ(距離)．
   */
  private double getSeqDist(int from, int to) {
    return -Math.log(seqMap[to - from + 12]);
  }

  private class DPElement {
    double dist = Double.MAX_VALUE;
    int from = -1;
  }

}
