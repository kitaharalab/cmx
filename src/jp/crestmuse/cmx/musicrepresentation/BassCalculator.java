package jp.crestmuse.cmx.musicrepresentation;

import java.util.HashMap;
import java.util.Map;

import jp.crestmuse.cmx.amusaj.commands.ChordConverter;
import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.MusicElement;
import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.Type;

public class BassCalculator implements Calculator {

  private MusicRepresentation musicRepresentation;
  private Map<String, Integer> diatonic2notenum;
  private double[] seqMap = { 1.0, 1.0, 1.0, 1.0, 1.0, 0.1, 1.0, 0.1, 0.5, 0.5,
      0.1, 0.1, 0.1, 0.1, 0.1, 0.5, 0.5, 0.1, 1.0, 0.1, 1.0, 1.0, 1.0, 1.0, 1.0 };
  private int BASE = 36;
  private double[][] noteTable = new double[12][3];
  private ChordConverter cc = new ChordConverter();

  public BassCalculator(MusicRepresentation mr) {
    musicRepresentation = mr;
    diatonic2notenum = new HashMap<String, Integer>();
    diatonic2notenum.put("C", 0);
    diatonic2notenum.put("Dm", 2);
    diatonic2notenum.put("Em", 4);
    diatonic2notenum.put("F", 5);
    diatonic2notenum.put("G", 7);
    diatonic2notenum.put("Am", 9);
    diatonic2notenum.put("Bm(b5)", 11); // Bdim

    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 3; j++) {
        noteTable[i][j] = 0.5;
      }
    }
    noteTable[0][1] = 0.01; // I 3音目
    noteTable[4][1] = 0.01; // III 3音目
    noteTable[7][1] = 0.01; // V 3音目
  }

  public Type[] drivenBy() {
    return new Type[]{Type.Chord};
  }

  public void update(MusicElement me, int index) {
    //String currentChord = me.getName();
    String currentChord = me.getLabel(me.getHighestProdIndex());
    String nextChord;
    try {
      //nextChord = musicRepresentation.getMusicElement(track, index + musicRepresentation.getDivision()).getName();
      MusicElement chord = musicRepresentation.getChordElement(index + musicRepresentation.getDivision());
      nextChord = chord.getLabel(chord.getHighestProdIndex());
    } catch (IndexOutOfBoundsException e) {
      nextChord = currentChord;
    }
    int notenum = diatonic2notenum.get(currentChord) + BASE;
    /*
    NoteElement ne = new NoteElement();
    ne.note = new int[] { notenum };
    musicRepresentation.setPredict(2, index, ne);
    */
    MusicElement bass = musicRepresentation.addBassElements(index);
    bass.setProd(notenum, 1.0);
    int[] path = getNearestPath(currentChord, nextChord);
    for (int i = 0; i < path.length; i++) {
      /*
      ne = new NoteElement();
      ne.note = new int[] { notenum + path[i] };
      musicRepresentation.setPredict(2, index + (i + 1) * 2, ne);
      */
      bass = musicRepresentation.addBassElements(index + (i + 1)*2);
      bass.setProd(notenum + path[i], 0.5);
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

    // 2音目
    for (int i = 0; i < noteKinds; i++) {
      int fromNum = diatonic2notenum.get(from) + BASE;
      dpGraph[0][i].dist = getNoteDist(from, 0, fromNum)
          + getSeqDist(fromNum, fromNum + i - 12);
    }

    // 3, 4音目
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

    // 4音目 -> ゴール
    double minDist = Double.MAX_VALUE;
    int minFrom = 0;
    for (int i = 0; i < noteKinds; i++) {
      int toNum = diatonic2notenum.get(to) + BASE;
      if (Math.abs(toNum - i - 12 + BASE) > 12)
        continue;
      double dist = dpGraph[step - 1][i].dist
          + getSeqDist(i - 12 + BASE, toNum);
      if (dist < minDist) {
        minDist = dist;
        minFrom = i;
      }
    }

    // ゴール -> 2音目まで辿る
    while (step > 0) {
      step--;
      result[step] = minFrom - 12;
      minFrom = dpGraph[step][minFrom].from;
    }
    return result;
  }

  /**
   * 調がkeyのとき、ベースのindex音目がnotenumである尤もらしさ(距離)．
   */
  private double getNoteDist(String key, int index, int notenum) {
    // TODO 調変化に対応
    notenum = cc.noteTransfer("C", notenum % 12);
    // return -Math.log(noteTable[notenum][index]);
    return noteTable[notenum][index];
  }

  /**
   * ある音の次にある音が来る尤もらしさ(距離)．
   */
  private double getSeqDist(int from, int to) {
    // return -Math.log(seqMap[to - from + 12]);
    return seqMap[to - from + 12];
  }

  private class DPElement {
    double dist = Double.MAX_VALUE;
    int from = -1;
  }

}
