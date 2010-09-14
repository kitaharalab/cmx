package jp.crestmuse.cmx.misc;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.ControlChange;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Note;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.HeaderElement;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Annotation;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;

import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

public class PerformanceMatcher3 {

  private static int baseTempo = 120;
  private static final double BASE_DYNAMICS = 100.0;
  private static final double ATTACK_LIMIT = 2000.0;
  private static double rowRiscInc = 1; // originally int
  private static double colRiscInc = 0; // originally int
  private static double ioiWeight = 1.6;

  public static String DTW_PATH_FILENAME = null;

  public static double MISS_EXTRA_ONSET_DIFF = 0.4;

  public static void setRowRiscInc(double value) {
    rowRiscInc = value;
  }

  public static void setColRiscInc(double value) {
    colRiscInc = value;
  }

  public static void setIoiWeight(double value) {
    ioiWeight = value;
  }

  private MusicXMLWrapper musicxml;
  private String partid;
  private Measure[] measurelist;
  private Note[] scoreNotes, pfmNotes;
  private ControlChange[] controls;
  private List<NoteInSameTime> compressedScore;
  private Annotation[] barlines;
  // private DeviationDataSet dds;
  private int scoreTicksPerBeat;
  private int pfmTicksPerBeat;
  private int[] score2pfm;
  private Map<MusicXMLWrapper.Note, Integer> musicxmlwrappernote2index;
  private Map<SCCXMLWrapper.Note, Integer> extraNoteMap;

  public PerformanceMatcher3(MusicXMLWrapper score, MIDIXMLWrapper pfm)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    this(score, pfm, pfm.ticksPerBeat());
  }

  public PerformanceMatcher3(MusicXMLWrapper score, MIDIXMLWrapper pfm,
      int ticksPerBeat) throws ParserConfigurationException, SAXException,
      IOException, TransformerException {
    this.musicxml = score;
    Part part0 = musicxml.getPartList()[0];
    measurelist = part0.getMeasureList();
    partid = part0.id();
    SCCXMLWrapper scoreSCC = score.makeDeadpanSCCXML(ticksPerBeat);
    SCCXMLWrapper pfmSCC = pfm.toSCCXML();
    barlines = scoreSCC.getBarlineList();
    scoreNotes = scoreSCC.getPartList()[0].getSortedNoteOnlyList(1);
    calcMusicXMLNote2Index();
    pfmNotes = pfmSCC.getPartList()[0].getSortedNoteOnlyList(1);
    controls = pfmSCC.getPartList()[0].getControlChangeList();
    initCompressedScore();
    scoreTicksPerBeat = scoreSCC.getDivision();
    pfmTicksPerBeat = pfmSCC.getDivision();
    HeaderElement[] h = pfmSCC.getHeaderElementList();
    if (h.length >= 1 && h[0].name().equals("TEMPO"))
      baseTempo = Integer.parseInt(h[0].content());
    extraNoteMap = new HashMap<Note, Integer>();
  }

  private void calcMusicXMLNote2Index() {
    musicxmlwrappernote2index = new HashMap<MusicXMLWrapper.Note, Integer>();
    for (int i = 0; i < scoreNotes.length; i++)
      musicxmlwrappernote2index.put(scoreNotes[i].getMusicXMLWrapperNote(), i);
  }

  public DeviationInstanceWrapper extractDeviation()
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    List<Note> extraNotes = new ArrayList<Note>();
    score2pfm = getPath(dtw(500), extraNotes);
    if (DTW_PATH_FILENAME != null)
      writePathToFile(DTW_PATH_FILENAME);
    return path2dev(extraNotes);
  }

  public DeviationInstanceWrapper extractDeviation(File file)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    List<Note> extraNotes = new ArrayList<Note>();
    score2pfm = getPath(file, extraNotes);
    return path2dev(extraNotes);
  }

  public DeviationInstanceWrapper extractDeviation(int[] indexlist) {
    List<Note> extraNotes = new ArrayList<Note>();
    score2pfm = getPath(indexlist, extraNotes);
    int[] score2pfm_2 = (int[]) score2pfm.clone();
    DeviationInstanceWrapper d = path2dev(extraNotes);
    for (int i = 0; i < score2pfm.length; i++)
      if (score2pfm[i] != score2pfm_2[i])
        System.err.println("NOT SAME: " + i + "  " + score2pfm[i] + " "
            + score2pfm_2[i]);
    return d;
    // return path2dev(extraNotes);
  }

  private int[] getPath(DTWMatrix matrix, List<Note> extraNotes) {
    int I = matrix.nrows;
    int J = matrix.ncols;
    NoteInSameTime[] com2pfm = new NoteInSameTime[compressedScore.size()];
    for (int i = 0; i < com2pfm.length; i++)
      com2pfm[i] = new NoteInSameTime();
    int i = I - 1, j = J - 1;
    do {
      com2pfm[i].addNote(pfmNotes[j], j);
      DTWMatrix.DTWMatrixElement e = matrix.get(i, j);
      i = e.targetX;
      j = e.targetY;
    } while (i >= 0 || j >= 0);
    int[] indexlist = new int[scoreNotes.length];
    Arrays.fill(indexlist, -1);
    boolean[] matched = new boolean[pfmNotes.length];
    for (i = 0; i < com2pfm.length; i++) {
      NoteInSameTime snotes = compressedScore.get(i);
      System.err.println("S: " + snotes.toString());
      System.err.println(snotes.notes.get(0).grace());
      NoteInSameTime pnotes = com2pfm[i];
      System.err.println("P: " + pnotes.toString());
      for (j = 0; j < snotes.notes.size(); j++) {
        for (int k = 0; k < pnotes.notes.size(); k++) {
          int pfmIndex = pnotes.indexes.get(k);
          if (snotes.notes.get(j).notenum() == pnotes.notes.get(k).notenum()
              && !matched[pfmIndex]) {
            indexlist[snotes.indexes.get(j)] = pfmIndex;
            matched[pfmIndex] = true;
            break;
          }
        }
      }
    }
    for (j = 0; j < matched.length; j++)
      if (!matched[j]) {
        extraNotes.add(pfmNotes[j]);
        extraNoteMap.put(pfmNotes[j], j);
      }
    return indexlist;
  }

  private int[] getPath(File file, List<Note> extraNotes) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    int length = Integer.parseInt(reader.readLine());
    int[] indexlist = new int[length];
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] data = line.split("\t");
      indexlist[Integer.parseInt(data[0])] = Integer.parseInt(data[1]);
    }
    reader.close();
    boolean[] matched = new boolean[pfmNotes.length];
    for (int i = 0; i < indexlist.length; i++)
      if (indexlist[i] >= 0)
        matched[indexlist[i]] = true;
    for (int j = 0; j < matched.length; j++)
      if (!matched[j]) {
        extraNotes.add(pfmNotes[j]);
        extraNoteMap.put(pfmNotes[j], j);
      }
    return indexlist;
  }

  private int[] getPath(int[] indexlist, List<Note> extraNotes) {
    boolean[] matched = new boolean[pfmNotes.length];
    for (int i = 0; i < indexlist.length; i++)
      if (indexlist[i] >= 0)
        matched[indexlist[i]] = true;
    for (int j = 0; j < matched.length; j++)
      if (!matched[j]) {
        extraNotes.add(pfmNotes[j]);
        extraNoteMap.put(pfmNotes[j], j);
      }
    return indexlist;
  }

  private void writePathToFile(String filename) throws IOException {
    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
        filename)));
    writer.println(score2pfm.length);
    for (int i = 0; i < score2pfm.length; i++)
      writer.println(i + "\t" + score2pfm[i] + "\t# " + scoreNotes[i] + " | "
          + (score2pfm[i] >= 0 ? pfmNotes[score2pfm[i]] : ""));
    writer.close();
  }

  private DeviationInstanceWrapper path2dev(List<Note> extraNotes) {
    DeviationDataSet dds = new DeviationDataSet(musicxml);
    // sortIndexList();
    ArrayList<TempoAndTime> tempolist = alignBeats();
    interpolateBeatTime(tempolist);
    double avgtempo = calcTempo(tempolist);
    double initSil = tempolist.get(1).timeInSec;
    dds.setInitialSilence(initSil);
    int headMeasure = measurelist[0].number();
    dds.addNonPartwiseControl(headMeasure, 1, "tempo", avgtempo);
    addTempoDeviations(dds, tempolist, avgtempo);
    checkMissAndExtraNotes(extraNotes, tempolist);
    setNotewiseDeviations(dds, extraNotes, tempolist);
    setPedalControls(dds, tempolist);
    return dds.toWrapper();
  }

  public static DeviationInstanceWrapper extractDeviation(
      MusicXMLWrapper score, MIDIXMLWrapper pfm)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm);
    return pm.extractDeviation();
  }

  public static DeviationInstanceWrapper extractDeviation(
      MusicXMLWrapper score, MIDIXMLWrapper pfm, File pathfile)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm);
    return pm.extractDeviation(pathfile);
  }

  public static DeviationInstanceWrapper extractDeviation(
      MusicXMLWrapper score, MIDIXMLWrapper pfm, int ticksPerBeat)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm, ticksPerBeat);
    return pm.extractDeviation();
  }

  public static DeviationInstanceWrapper extractDeviation(
      MusicXMLWrapper score, MIDIXMLWrapper pfm, int ticksPerBeat, File pathfile)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm, ticksPerBeat);
    return pm.extractDeviation(pathfile);
  }

  public int[] getScore2Pfm() {
    return score2pfm;
  }

  public Map<MusicXMLWrapper.Note, Integer> getMusicxmlwrappernote2Index() {
    return musicxmlwrappernote2index;
  }

  public Map<SCCXMLWrapper.Note, Integer> getExtraNoteMap() {
    return extraNoteMap;
  }

  // private static int[] getPath(DTWMatrix matrix) {
  // int I = matrix.nrows;
  // int J = matrix.ncols;
  // int[] path = new int[I];
  // int i = I - 1, j = J - 1;
  // do {
  // path[i] = j;
  // DTWMatrix.DTWMatrixElement e = matrix.get(i, j);
  // i = e.targetX;
  // j = e.targetY;
  // } while (i >= 0 || j >= 0);
  // return path;
  // }

  private DTWMatrix dtw(int r) {
    // for (Note n : pfmNotes)
    // System.err.println(n.notenum());
    // for (NoteInSameTime nn : compressedScore) {
    // System.err.print(nn.notes.get(0).onset(48) + "\t");
    // for (Note n : nn.notes)
    // System.err.print(n.notenum() + " " );
    // System.err.println();
    // }
    int I = compressedScore.size();
    int J = pfmNotes.length;
    int scoreTicks = compressedScore.get(I - 1).notes.get(0).offset();
    int pfmTicks = pfmNotes[J - 1].offset();
    // r = Math.max(0, Math.max(J-I, r));
    r = J;
    DTWMatrix matrix = new DTWMatrix(I, J);
    matrix.set(-1, -1, 0, -1, -1);
    // double colRisc=COL_RISC_INC; // originally int
    double[] rowRiscs = new double[I];
    Arrays.fill(rowRiscs, colRiscInc);
    for (int i = 0; i < I; i++) {
      double colRisc = rowRiscInc; // originally int
      for (int j = Math.max(0, i - r); j <= Math.min(i + r, J - 1); j++) {
        NoteInSameTime e1 = compressedScore.get(i);
        Note e2 = pfmNotes[j];
        double ioi = 0;
        // if(j > 0) {
        // int diff = e2.onset() - pfmNotes[j - 1].onset();
        // if(diff == 0) ioi = Double.POSITIVE_INFINITY;
        // else ioi = 1.0 / diff;
        // }
        if (i > 0 && j > 0) {
          int scoreDiff = e1.notes.get(0).onset()
              - compressedScore.get(i - 1).notes.get(0).onset();
          int pfmDiff = e2.onset() - pfmNotes[j - 1].onset();
          if (pfmDiff == 0)
            ioi = Double.POSITIVE_INFINITY;
          else
            ioi = scoreDiff / (double) pfmDiff;
        }
        double d = dist(e1, e2, scoreTicks, pfmTicks);
        double c1 = matrix.getValue(i - 1, j) + d + rowRiscs[i];
        double c2 = matrix.getValue(i - 1, j - 1) + 2 * d + ioi * ioiWeight;
        double c3 = matrix.getValue(i, j - 1) + d + colRisc;
        double c_min = Math.min(c2, Math.min(c1, c3));
        if (c_min == c2) {
          matrix.set(i, j, c_min, i - 1, j - 1);
          rowRiscs[i] = colRiscInc;
          colRisc = rowRiscInc;
        } else if (c_min == c3) {
          matrix.set(i, j, c_min, i, j - 1);
          rowRiscs[i] = colRiscInc;
          colRisc += rowRiscInc;
        } else {
          matrix.set(i, j, c_min, i - 1, j);
          rowRiscs[i] += colRiscInc;
          colRisc = rowRiscInc;
        }
      }
    }
    return matrix;
  }

  private double dist(NoteInSameTime e1, Note e2, int scoreTicks, int pfmTicks) {
    double position = Math.abs((e1.notes.get(0).onset() / (double) scoreTicks - e2.onset()
        / (double) pfmTicks));
    for (Note n : e1.notes) {
      if (n.notenum() == e2.notenum())
        return position;
    }
    // else if(Math.abs(e1.notenum()-e2.notenum()) % 12 == 0) return 10 +
    // position;
    return 100;
  }

  // private static double dist(Note e1, Note e2, int scoreTicks, int pfmTicks){
  // int notenum = Math.abs(e1.notenum() - e2.notenum());
  // double position = Math.abs((e1.onset() / (double)scoreTicks - e2.onset() /
  // (double)pfmTicks));
  // if(notenum == 0) return position;
  // else if(Math.abs(e1.notenum()-e2.notenum()) % 12 == 0) return 10 +
  // position;
  // return 100;
  // }

  private void initCompressedScore() {
    compressedScore = new ArrayList<NoteInSameTime>();
    NoteBuffer nb = new NoteBuffer();
    for (int i = 0; i < scoreNotes.length; i++) {
      if (i >= 1 && scoreNotes[i].onset() != scoreNotes[i - 1].onset()) {
        addNotesToCompressedScore(nb, compressedScore);
        nb = new NoteBuffer();
      }
      nb.addNote(scoreNotes[i], i);
    }
    addNotesToCompressedScore(nb, compressedScore);
  }

  private class NoteBuffer {
    Note[][] notes = new Note[64][128];
    int[][] indices = new int[64][128];
    int[] next = new int[64];

    void addNote(Note note, int index) {
      int grace = note.grace();
      notes[grace][next[grace]] = note;
      indices[grace][next[grace]] = index;
      next[grace]++;
    }
  }

  private void addNotesToCompressedScore(NoteBuffer nb,
      List<NoteInSameTime> compressedScore) {
    for (int n = 1; nb.next[n] > 0; n++) {
      NoteInSameTime nist = new NoteInSameTime();
      for (int i = 0; i < nb.next[n]; i++)
        nist.addNote(nb.notes[n][i], nb.indices[n][i]);
      compressedScore.add(nist);
    }
    NoteInSameTime nist = new NoteInSameTime();
    for (int i = 0; i < nb.next[0]; i++)
      nist.addNote(nb.notes[0][i], nb.indices[0][i]);
    compressedScore.add(nist);
  }

  /*
   * private void initCompressedScore() { compressedScore = new
   * ArrayList<NoteInSameTime>(); Note prev = scoreNotes[0]; NoteInSameTime nist
   * = new NoteInSameTime(); nist.addNote(prev, 0); compressedScore.add(nist);
   * for (int i = 1; i < scoreNotes.length; i++) { if (scoreNotes[i].onset() !=
   * prev.onset() || scoreNotes[i].getGrace() == 0) { nist = new
   * NoteInSameTime(); compressedScore.add(nist); } nist.addNote(scoreNotes[i],
   * i); prev = scoreNotes[i]; } }
   */

  private void sortIndexList() {
    for (int i = 0; i < score2pfm.length - 1; i++) {
      if (score2pfm[i] > score2pfm[i + 1] && score2pfm[i + 1] != -1) {
        Note tmp = pfmNotes[score2pfm[i]];
        pfmNotes[score2pfm[i]] = pfmNotes[score2pfm[i + 1]];
        pfmNotes[score2pfm[i + 1]] = tmp;
        int tmpi = score2pfm[i];
        score2pfm[i] = score2pfm[i + 1];
        score2pfm[i + 1] = tmpi;
      }
    }
  }

  private class DTWMatrix {
    private HashMap<IntPair, DTWMatrixElement> values;
    private int nrows, ncols;
    private final DTWMatrixElement DEFAULT_MATRIX_ELEMENT = new DTWMatrixElement(
        Integer.MAX_VALUE / 2, -1, -1);

    private DTWMatrix(int nrows, int ncols) {
      this.nrows = nrows;
      this.ncols = ncols;
      values = new HashMap<IntPair, DTWMatrixElement>(Math.max(nrows, ncols));
    }

    private void set(int i, int j, double value, int targetX, int targetY) {
      values.put(new IntPair(i, j), new DTWMatrixElement(value, targetX,
          targetY));
    }

    private DTWMatrixElement get(int i, int j) {
      if (i >= nrows || j >= ncols || i < -1 || j < -1)
        throw new ArrayIndexOutOfBoundsException();
      if (values.containsKey(new IntPair(i, j)))
        return values.get(new IntPair(i, j));
      else
        return DEFAULT_MATRIX_ELEMENT;
    }

    private double getValue(int i, int j) {
      return get(i, j).value;
    }

    private class IntPair {
      private int i, j;

      private IntPair(int i, int j) {
        this.i = i;
        this.j = j;
      }

      public boolean equals(Object o) {
        IntPair another = (IntPair) o;
        return (i == another.i) && (j == another.j);
      }

      public int hashCode() {
        return i * nrows + j;
      }
    }

    private class DTWMatrixElement {
      private int targetX, targetY;
      private double value;

      private DTWMatrixElement(double value, int targetX, int targetY) {
        this.value = value;
        this.targetX = targetX;
        this.targetY = targetY;
      }
    }
  }

  private class TempoAndTime {
    private double tempo = Double.NaN;
    private int tickInScore;
    private double tickInPfm = Double.NaN;
    private double timeInSec = Double.NaN;
    private int measure = -1;
    private int beat = -1;

    private TempoAndTime(int tickInScore) {
      this.tickInScore = tickInScore;
    }

    private void setTickInPfm(double tickInPfm) {
      this.tickInPfm = tickInPfm;
      timeInSec = tickInPfm * 60.0 / (pfmTicksPerBeat * baseTempo);
    }

    private void setTimeInSec(double timeInSec) {
      this.timeInSec = timeInSec;
      tickInPfm = timeInSec * pfmTicksPerBeat * baseTempo / 60.0;
    }

    private double beatLength() {
      return pfmTicksPerBeat * baseTempo / tempo;
    }

    @Override
    public String toString() {
      return "\n" + tempo + ", " + tickInScore + ", " + tickInPfm + ", "
          + timeInSec + ", " + measure + ", " + beat;
    }
  }

  private class NoteInSameTime {
    List<Note> notes;
    List<Integer> indexes;

    NoteInSameTime() {
      notes = new LinkedList<Note>();
      indexes = new LinkedList<Integer>();
    }

    void addNote(Note n, int index) {
      notes.add(n);
      indexes.add(index);
    }

    @Override
    public String toString() {
      String s = "";
      for (Note n : notes)
        s += n.notenum() + ", ";
      return s;
    }
  }

  private void alignNotes(int[] path, int[] indexlist, List<Note> extraNotes) {
    for (int m = 0; m < indexlist.length; m++)
      indexlist[m] = -1;
    boolean[] alreadyPut = new boolean[pfmNotes.length];
    for (int n = 0; n < alreadyPut.length; n++)
      alreadyPut[n] = false;
    int m = 0, n = 0;
    for (; m < indexlist.length - 1;) {
      if (indexlist[m] == -1 && !alreadyPut[n]
          && scoreNotes[m].notenum() == pfmNotes[n].notenum()) {
        indexlist[m] = n;
        alreadyPut[n] = true;
        if (path[m + 1] == n) {
          m++;
        } else if (path[m + 1] - n > 1) {
          n++;
        } else {
          m++;
          n++;
        }
      } else if (path[m + 1] == n) {
        m++;
      } else if (path[m + 1] - n > 1) {
        if (!alreadyPut[n]) {
          extraNotes.add(pfmNotes[n]);
          alreadyPut[n] = true;
        }
        n++;
      } else {
        if (!alreadyPut[n]) {
          extraNotes.add(pfmNotes[n]);
          alreadyPut[n] = true;
        }
        m++;
        n++;
      }
    }
    if (indexlist[m] == -1 && !alreadyPut[n]
        && scoreNotes[m].notenum() == pfmNotes[n].notenum())
      indexlist[m] = n;
    else
      extraNotes.add(pfmNotes[n]);
  }

  private void setNotewiseDeviations(DeviationDataSet dds,
      List<Note> extraNotes, ArrayList<TempoAndTime> tempolist) {
    for (int i = 0; i < score2pfm.length; i++) {
      int j = score2pfm[i];
      if (j >= 0) {
        addNoteDeviation(dds, scoreNotes[i], pfmNotes[j], tempolist);
      } else if (score2pfm[i] < -1) {
        // if (extraNotes.get(-j - 2) != null) {
        // addNoteDeviation(dds, scoreNotes[i], extraNotes.get(-j - 2),
        // tempolist);
        // extraNotes.set(-j - 2, null);
        // } else {
        // addMissNote(dds, scoreNotes[i]);
        // }
      } else {
        addMissNote(dds, scoreNotes[i]);
      }
    }
    for (Note note : extraNotes)
      if (note != null)
        addExtraNote(dds, note, partid, tempolist);
  }

  private void checkMissAndExtraNotes(List<Note> extraNotes,
      ArrayList<TempoAndTime> tempolist) {
    for (int i = 0; i < score2pfm.length; i++) {
      if (score2pfm[i] >= 0)
        continue;
      Note scoreNote = scoreNotes[i];
      double scoreOnset = getSecFromScoreTick(scoreNote.onset(), tempolist);
      int scoreNN = scoreNote.notenum();
      extraNoteLoop: for (int j = 0; j < extraNotes.size(); j++) {
        Note pfmNote = extraNotes.get(j);
        if (pfmNote == null)
          continue;
        int pfmNN = pfmNote.notenum();
        if (scoreNN != pfmNN)
          continue;
        double pfmOnset = getSecFromPfmTick(pfmNote.onset(), tempolist);
        if (scoreOnset - pfmOnset < MISS_EXTRA_ONSET_DIFF
            && pfmOnset - scoreOnset < MISS_EXTRA_ONSET_DIFF) {
          // TODO ?
          // score2pfm[i] = -j - 2;
          // System.err.print(i + ":" + j + " ");
          for (int k = 0; k < pfmNotes.length; k++)
            if (pfmNotes[k] == pfmNote) {
              score2pfm[i] = k;
              extraNotes.set(j, null);
              // System.err.print(i + ":" + j + " ");
              break extraNoteLoop;
            }
        }
      }
    }
  }

  private void setPedalControls(DeviationDataSet dds,
      ArrayList<TempoAndTime> tempolist) {
    for (ControlChange cc : controls)
      if (cc.ctrlnum() == 64) {
        TempoAndTime tnt = searchTnT(cc.onset(), tempolist);
        double diff = cc.onset() - tnt.tickInPfm;
        diff = diff * tnt.tempo / (pfmTicksPerBeat * baseTempo);
        dds.addPartwiseControl(musicxml.getPartList()[0].id(), tnt.measure,
            tnt.beat + diff, "pedal");
        if (cc.value() == 0)
          dds.setAttribute("action", "off");
        else {
          dds.setAttribute("action", "on");
          dds.setAttribute("value", cc.value() / 127.0);
        }
      }
  }

  // kari; redundant calculation
  private double getSecFromScoreTick(int tick, List<TempoAndTime> tempolist) {
    TempoAndTime last = tempolist.get(0);
    for (TempoAndTime tnt : tempolist) {
      if (tick < tnt.tickInScore)
        break;
      last = tnt;
    }
    return last.timeInSec + (tick - last.tickInScore) * 60.0
        / (scoreTicksPerBeat * last.tempo);
  }

  // kari; redundant calculation
  private double getSecFromPfmTick(int tick, List<TempoAndTime> tempolist) {
    TempoAndTime last = tempolist.get(0);
    for (TempoAndTime tnt : tempolist) {
      if (tick < tnt.tickInPfm)
        break;
      last = tnt;
    }
    return last.timeInSec + (tick - last.tickInPfm) * 60.0
        / (pfmTicksPerBeat * baseTempo);
  }

  private int i = 0;

  private TempoAndTime searchTnT(double tick, ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    while (i >= size - 1 || tempolist.get(i).tickInPfm > tick && i > 0)
      i--;
    while (i < 0 || tempolist.get(i + 1).tickInPfm <= tick && i < size - 2)
      i++;
    return tempolist.get(i);
  }

  private void addNoteDeviation(DeviationDataSet dds, Note noteS, Note noteP,
      ArrayList<TempoAndTime> tempolist) {
    int initticks = (int) (tempolist.get(0).tickInPfm);
    TempoAndTime tnt = searchTnT(noteP.onset(),
    // noteS.onset()/scoreTicksPerBeat,
        tempolist);
    double onsetInPfm, onsetInScore;
    if (tnt.measure >= 0) {
      onsetInPfm = noteP.onset() - tnt.tickInPfm;
      onsetInScore = noteS.onset() - tnt.tickInScore;
    } else {
      onsetInPfm = noteP.onset() - tempolist.get(1).tickInPfm;
      onsetInScore = noteS.onset() - tempolist.get(1).tickInScore;
    }
    double attack = onsetInPfm * tnt.tempo / (pfmTicksPerBeat * baseTempo)
        - onsetInScore / scoreTicksPerBeat;
    tnt = searchTnT(noteP.offset(), // noteS.offset() / scoreTicksPerBeat,
        tempolist);
    double offsetInPfm, offsetInScore;
    if (tnt.measure >= 0) {
      offsetInPfm = noteP.offset() - tnt.tickInPfm;
      offsetInScore = noteS.offset() - tnt.tickInScore;
    } else {
      offsetInPfm = noteP.offset() - tempolist.get(1).tickInPfm;
      offsetInScore = noteS.offset() - tempolist.get(1).tickInScore;
    }
    double release = offsetInPfm * tnt.tempo / (pfmTicksPerBeat * baseTempo)
        - offsetInScore / scoreTicksPerBeat;
    double dynamics = noteP.velocity() / (double) BASE_DYNAMICS;
    if (Math.abs(attack) > ATTACK_LIMIT) {
      addMissNote(dds, noteS);
      addExtraNote(dds, noteP, partid, tempolist);
    } else
      dds.addNoteDeviation(noteS.getMusicXMLWrapperNote(), attack, release,
          dynamics, dynamics);
  }

  private void addMissNote(DeviationDataSet dds, Note note) {
    dds.addMissNote(note.getMusicXMLWrapperNote());
  }

  private void addExtraNote(DeviationDataSet dds, Note note, String partid,
      ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    int i;
    for (i = 0; i < size; i++) {
      TempoAndTime tnt = tempolist.get(i);
      if (tnt.measure == -1 && tnt.beat == -1 || tnt.tickInPfm > note.onset())
        break;
    }
    TempoAndTime tnt0 = tempolist.get(i - 1);
    double inbeat0 = (double) (note.onset() - tnt0.tickInPfm)
        / tnt0.beatLength();
    int j;
    for (j = i - 1; j < size; j++) {
      TempoAndTime tnt = tempolist.get(j);
      if (tnt.measure == -1 && tnt.beat == -1 || tnt.tickInPfm > note.offset())
        break;
    }
    TempoAndTime tnt1 = tempolist.get(j - 1);
    double inbeat1 = (double) (note.offset() - tnt1.tickInPfm)
        / tnt1.beatLength();
    dds.addExtraNote(partid, tnt0.measure, tnt0.beat + inbeat0, note.notenum(),
        (double) (j - i) + inbeat1 - inbeat0, note.velocity() / BASE_DYNAMICS,
        note.velocity() / BASE_DYNAMICS);
  }

  private boolean tickcmp(int tick1, int tick2, int threshold) {
    return (tick1 >= tick2 - threshold && tick1 <= tick2 + threshold);
  }

  private TempoAndTime getZerothTempoAndTime() {
    TempoAndTime tnt = new TempoAndTime(0);
    tnt.tempo = 120.0;
    tnt.tickInPfm = 0.0;
    tnt.timeInSec = 0.0;
    tnt.measure = -1;
    tnt.beat = 1;
    return tnt;
  }

  private ArrayList<TempoAndTime> alignBeats() {
    ArrayList<TempoAndTime> tempolist = new ArrayList<TempoAndTime>();
    tempolist.add(getZerothTempoAndTime());
    int i = 0;
    int measure = 0, beat = 1, currentTick = 0;
    for (int k = 0; k < barlines.length - 2; k++) {
      Measure m = getMeasure(barlines[k].onset());
      measure = m.number();
      beat = (int) m.initialBeat();
      // measure = getMeasureNumber(barlines[k].onset());
      // beat = 1;
      for (currentTick = barlines[k].onset(); currentTick < barlines[k + 1].onset(); currentTick += scoreTicksPerBeat) {
        i = addTempoAndTime(currentTick, measure, beat, i, tempolist);
        beat++;
      }
    }
    currentTick = barlines[barlines.length - 2].onset();
    Measure m = getMeasure(currentTick);
    measure = m.number();
    beat = (int) m.initialBeat();
    // measure = getMeasureNumber(currentTick);
    // beat = 1;
    i = addTempoAndTime(currentTick, measure, beat, i, tempolist);

    int lastOffset = currentTick;
    for (int ii = 0; ii < scoreNotes.length; ii++)
      if (scoreNotes[ii].offset() > lastOffset)
        lastOffset = scoreNotes[ii].offset();

    for (currentTick += scoreTicksPerBeat; currentTick < barlines[barlines.length - 1].onset(); currentTick += scoreTicksPerBeat) {
      i = addTempoAndTime(currentTick, measure, ++beat, i, tempolist);
    }
    // kari
    // int lasttick = max(lastOffset+4*scoreTicksPerBeat,
    // barlines[barlines.length-1].onset());
    for (; currentTick <= lastOffset + 4 * scoreTicksPerBeat; currentTick += scoreTicksPerBeat) {
      // System.err.println("lastOffset: " + lastOffset);
      // System.err.println("currentTick: " + currentTick);
      TempoAndTime tnt = new TempoAndTime(currentTick);
      tnt.measure = measure;
      tnt.beat = ++beat;
      tempolist.add(tnt);
      // tempolist.add(new TempoAndTime(currentTick));
    }
    return tempolist;
  }

  private static int max(int a, int b) {
    return a >= b ? a : b;
  }

  private int addTempoAndTime(int currentTick, int measure, int beat, int i,
      ArrayList<TempoAndTime> tempolist) {
    while (i < scoreNotes.length) {
      if (score2pfm[i] == -1) {
        i++;
        continue;
      }
      if (tickcmp(scoreNotes[i].onset(), currentTick, 5)) {
        int j, count = 0, total = 0;
        for (j = i;; j++) {
          try {
            if (!tickcmp(scoreNotes[j].onset(), currentTick, 5))
              break;
            if (score2pfm[j] == -1)
              continue;
            total += pfmNotes[score2pfm[j]].onset();
            count++;
          } catch (ArrayIndexOutOfBoundsException e) {
            break;
          }
        }
        TempoAndTime tnt = new TempoAndTime(currentTick);
        tnt.measure = measure;
        tnt.beat = beat;
        if (count > 0)
          tnt.setTickInPfm((double) total / (double) count);
        tempolist.add(tnt);
        return j;
      }
      if (scoreNotes[i].onset() > currentTick) {
        TempoAndTime tnt = new TempoAndTime(currentTick);
        tnt.measure = measure;
        tnt.beat = beat;
        tempolist.add(tnt);
        // tempolist.add(new TempoAndTime(currentTick));
        return i;
      }
      i++;
    }
    // kari
    TempoAndTime tnt = new TempoAndTime(currentTick);
    tnt.measure = measure;
    tnt.beat = beat;
    tempolist.add(tnt);
    return i;
  }

  private int lastMeasureIndex = 0;

  private Measure getMeasure(int tick) {
    int tick0 = measurelist[lastMeasureIndex].cumulativeTicks(scoreTicksPerBeat);
    if (tick0 == tick) {
      return measurelist[lastMeasureIndex];
    } else if (tick0 > tick) {
      lastMeasureIndex--;
      return getMeasure(tick);
    } else {
      lastMeasureIndex++;
      return getMeasure(tick);
    }
  }

  // private int lastMeasureNumber = 0;
  //
  // private int getMeasureNumber(int tick) {
  // int tick0 =
  // measurelist[lastMeasureNumber].cumulativeTicks(scoreTicksPerBeat);
  // if (tick0 == tick) {
  // return measurelist[lastMeasureNumber].number();
  // // return lastMeasureNumber;
  // } else if (tick0 > tick) {
  // lastMeasureNumber--;
  // return getMeasureNumber(tick);
  // } else {
  // lastMeasureNumber++;
  // return getMeasureNumber(tick);
  // }
  // }

  // private ArrayList<TempoAndTime> alignBeats(Note[] scoreNotes,
  // Note[] pfmNotes,
  // int[] indexlist) {
  // ArrayList<TempoAndTime> tempolist = new ArrayList<TempoAndTime>();
  // tempolist.add(getZerothTempoAndTime());
  // int tick = 0;
  // double prevTickInPfm = Double.NEGATIVE_INFINITY;
  // int lastOffset = 0;
  // for (int i = 0; i < scoreNotes.length; ) {
  // if (scoreNotes[i].offset() > lastOffset)
  // lastOffset = scoreNotes[i].offset();
  // if (indexlist[i] == -1) {
  // i++;
  // continue;
  // }
  // if (tickcmp(scoreNotes[i].onset(), tick, 5)) {
  // int j, count = 0, total = 0;
  // for (j = i; ; j++) {
  // try {
  // if (!tickcmp(scoreNotes[j].onset(), tick, 5)) break;
  // if (indexlist[j] == -1) continue;
  // total += pfmNotes[indexlist[j]].onset();
  // count++;
  // } catch (ArrayIndexOutOfBoundsException e) {
  // break;
  // }
  // }
  // TempoAndTime tnt = new TempoAndTime(tick);
  // double newTickInPfm = ((total) / (double)count);
  // if (newTickInPfm > prevTickInPfm) {
  // tnt.setTickInPfm(newTickInPfm);
  // prevTickInPfm = newTickInPfm;
  // }
  // tempolist.add(tnt);
  // i = j;
  // tick += scoreTicksPerBeat;
  // } else if (scoreNotes[i].onset() > tick) {
  // tempolist.add(new TempoAndTime(tick));
  // i++;
  // tick += scoreTicksPerBeat;
  // } else {
  // i++;
  // }
  // }
  // while (tick <= lastOffset) {
  // tempolist.add(new TempoAndTime(tick));
  // tick += scoreTicksPerBeat;
  // }
  // return tempolist;
  // }

  private void interpolateBeatTime(ArrayList<TempoAndTime> tempolist) {
    TempoAndTime tnt0 = tempolist.get(1);
    if (Double.isNaN(tnt0.tickInPfm)) {
      System.err.println("first note in MusicXML is not at t=0");
      tnt0.setTickInPfm(0.0);
    }
    int size = tempolist.size();
    int prevIndex = 1;
    double prevTick = tnt0.tickInPfm;
    int nextIndex = -1;
    double nextTick = 0.0;
    int i;
    try {
      for (i = 2; i < size - 1; i++) {
        TempoAndTime tnt = tempolist.get(i);
        if (Double.isNaN(tnt.tickInPfm)) {
          if (i >= nextIndex) {
            for (nextIndex = i;; nextIndex++) {
              nextTick = tempolist.get(nextIndex).tickInPfm;
              if (!Double.isNaN(nextTick))
                break;
            }
          }
          tnt.setTickInPfm((prevTick * (nextIndex - i) + nextTick
              * (i - prevIndex))
              / (nextIndex - prevIndex));
        } else {
          prevIndex = i;
          prevTick = tnt.tickInPfm;
        }
      }
    } catch (IndexOutOfBoundsException e) {
    }
  }

  private double calcTempo(ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    double prevTempo = 0.0;
    double sum = 0.0;
    int count = 0;
    TempoAndTime prevtnt = tempolist.get(1);
    for (int i = 2; i < size; i++) {
      TempoAndTime tnt = tempolist.get(i);
      if (Double.isNaN(tnt.timeInSec)) {
        prevtnt.tempo = prevTempo;
        int interval = tnt.tickInScore - prevtnt.tickInScore;
        double pfmInterval = (double) (interval * 60 / scoreTicksPerBeat)
            / prevTempo;
        tnt.setTimeInSec(prevtnt.timeInSec + pfmInterval);
        // tnt.setTimeInSec(prevtnt.timeInSec + 60.0 / prevTempo);
      } else {
        int interval = tnt.tickInScore - prevtnt.tickInScore;
        double pfmInterval = tnt.timeInSec - prevtnt.timeInSec;
        // zantei
        if (pfmInterval < 0.0000000001) {
          tnt.setTimeInSec(tnt.timeInSec + 0.01);
          pfmInterval = tnt.timeInSec - prevtnt.timeInSec;
        }
        prevtnt.tempo = (double) (interval * 60 / scoreTicksPerBeat)
            / pfmInterval;
        // prevtnt.tempo = (60.0 / (tnt.timeInSec - prevtnt.timeInSec));
        prevTempo = prevtnt.tempo;
      }
      sum += prevtnt.tempo;
      count++;
      prevtnt = tnt;
    }
    tempolist.get(size - 1).tempo = prevTempo;
    return sum / (double) count;
  }

  // private void setMeasureNumbers(Measure[] measures,
  // List<TempoAndTime> tempolist) {
  // int lastmeasure = -1;
  // int lastbeat = -1;
  // Iterator<TempoAndTime> it = tempolist.iterator();
  // it.next();
  // System.err.println(scoreTicksPerBeat);
  // try {
  // for (Measure measure : measures) {
  // int tick = measure.cumulativeTicks(scoreTicksPerBeat);
  // int length = measure.duration(1);
  // for (int i = 1; i <= length; i++) {
  // TempoAndTime tnt = it.next();
  // System.err.println(i + ": " + tnt.tickInScore + " " +
  // measure.duration(480) + " " +
  // measure.number() + " " + measure.cumulativeTicks(scoreTicksPerBeat));
  // // if (i == 1 && tnt.tickInScore != tick)
  // if (i == 1 && !tickcmp(tnt.tickInScore, tick, 2))
  // System.err.println("*** tick-in-score mismatch***");
  // lastmeasure = tnt.measure = measure.number();
  // lastbeat = tnt.beat = i;
  // }
  // }
  // } catch (NoSuchElementException e) {
  // while (it.hasNext()) {
  // TempoAndTime tnt = it.next();
  // tnt.measure = lastmeasure;
  // tnt.beat = ++lastbeat;
  // }
  // }
  //
  // }

  private void addTempoDeviations(DeviationDataSet dds,
      ArrayList<TempoAndTime> tempolist, double avgtempo) {
    int size = tempolist.size();
    for (int i = 1; i < size - 1; i++) {
      TempoAndTime tnt = tempolist.get(i);
      dds.addNonPartwiseControl(tnt.measure, tnt.beat, "tempo-deviation",
          tnt.tempo / avgtempo);
    }
  }

  public static void main(String[] args) {
    try {
      MusicXMLWrapper score = (MusicXMLWrapper) CMXFileWrapper.readfile(args[0]);
      MIDIXMLWrapper pfm = MIDIXMLWrapper.readSMF(args[1]);
      // PerformanceMatcher3.DTW_PATH_FILENAME = args[2];
      // PerformanceMatcher3.extractDeviation(score, pfm, new
      // File(args[2])).writefile(
      // args[3]);
      PerformanceMatcher3.extractDeviation(score, pfm).write(System.out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
