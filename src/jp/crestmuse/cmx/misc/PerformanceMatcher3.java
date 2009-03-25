package jp.crestmuse.cmx.misc;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Note;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.HeaderElement;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Annotation;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
  
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PerformanceMatcher3 {

  private static int baseTempo = 120;
  private static final double BASE_DYNAMICS = 100.0;
  private static final double ATTACK_LIMIT = 2000.0;

  private MusicXMLWrapper musicxml;
  private MIDIXMLWrapper midixml;

  private String partid;
  private Measure[] measurelist;
  private Note[] scoreNotes, pfmNotes;
  private List<NoteInSameTime> compressedScore;
  private Annotation[] barlines;

  //  private SCCXMLWrapper scoreSCC;
  //  private SCCXMLWrapper pfmSCC;

  private DeviationDataSet dds;

  private int scoreTicksPerBeat;
  private int pfmTicksPerBeat;

  public PerformanceMatcher3(MusicXMLWrapper score, 
                             MIDIXMLWrapper pfm) 
    throws ParserConfigurationException, SAXException, IOException,
    TransformerException {
    this(score, pfm, pfm.ticksPerBeat());
  }

  public PerformanceMatcher3(MusicXMLWrapper score, 
                             MIDIXMLWrapper pfm, 
                             int ticksPerBeat) 
    throws ParserConfigurationException, SAXException, IOException, 
    TransformerException {
    this.musicxml = score;
    this.midixml = pfm;
    Part part0 = musicxml.getPartList()[0];
    measurelist = part0.getMeasureList();
    partid = part0.id();
    SCCXMLWrapper scoreSCC = score.makeDeadpanSCCXML(ticksPerBeat);
    SCCXMLWrapper pfmSCC = pfm.toSCCXML();
    barlines = scoreSCC.getBarlineList();
    scoreNotes = scoreSCC.getPartList()[0].getSortedNoteOnlyList(1);
    pfmNotes = pfmSCC.getPartList()[0].getSortedNoteOnlyList(1);
    initCompressedScore();
    scoreTicksPerBeat = scoreSCC.getDivision();
    pfmTicksPerBeat = pfmSCC.getDivision();
    HeaderElement[] h = pfmSCC.getHeaderElementList();
    if (h.length >= 1 && h[0].name().equals("TEMPO"))
      baseTempo = Integer.parseInt(h[0].content());
  }

  public DeviationInstanceWrapper extractDeviation() 
    throws ParserConfigurationException, SAXException, IOException, 
    TransformerException  {
    DeviationInstanceWrapper dev = 
        DeviationInstanceWrapper.createDeviationInstanceFor(musicxml);
    DeviationDataSet dds = dev.createDeviationDataSet();
    List<Note> extraNotes = new ArrayList<Note>();
    int[] indexlist = getPath(dtw(500), extraNotes);
    sortIndexList(indexlist);
    ArrayList<TempoAndTime> tempolist = alignBeats(indexlist);
    interpolateBeatTime(tempolist);
    double avgtempo = calcTempo(tempolist);
    double initSil = tempolist.get(1).timeInSec;
    dds.setInitialSilence(initSil);
    int headMeasure = measurelist[0].number();
    dds.addNonPartwiseControl(headMeasure, 1, "tempo", avgtempo);
    addTempoDeviations(dds, tempolist, avgtempo);
    setNotewiseDeviations(dds, indexlist, extraNotes, tempolist);
    dds.toWrapper();
    return dev;
  }

  public static DeviationInstanceWrapper extractDeviation
  (MusicXMLWrapper score, MIDIXMLWrapper pfm)
    throws ParserConfigurationException, SAXException, IOException,
    TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm);
    return pm.extractDeviation();
  }

  public static DeviationInstanceWrapper extractDeviation
  (MusicXMLWrapper score, MIDIXMLWrapper pfm, int ticksPerBeat)
    throws ParserConfigurationException, SAXException, IOException,
    TransformerException {
    PerformanceMatcher3 pm = new PerformanceMatcher3(score, pfm, 
                                                     ticksPerBeat);
    return pm.extractDeviation();
  }

  private int[] getPath(DTWMatrix matrix, List<Note> extraNotes) {
    int I = matrix.nrows;
    int J = matrix.ncols;
    NoteInSameTime[] com2pfm = new NoteInSameTime[compressedScore.size()];
    for(int i=0; i<com2pfm.length; i++)
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
    for(i=0; i<com2pfm.length; i++) {
      NoteInSameTime snotes = compressedScore.get(i);
      NoteInSameTime pnotes = com2pfm[i];
      boolean[] matched = new boolean[pnotes.notes.size()];
      for(j=0; j<snotes.notes.size(); j++) {
        for(int k=0; k<pnotes.notes.size(); k++) {
          if(snotes.notes.get(j).notenum() == pnotes.notes.get(k).notenum() && !matched[k]) {
            indexlist[snotes.indexAtScore + j] = pnotes.indexAtScore + k;
            matched[k] = true;
            break;
          }
        }
      }
      for(j=0; j<matched.length; j++)
        if(!matched[j])
          extraNotes.add(pnotes.notes.get(j));
    }
    return indexlist;
  }

/*
  private static int[] getPath(DTWMatrix matrix) {
    int I = matrix.nrows;
    int J = matrix.ncols;
    int[] path = new int[I];
    int i = I - 1, j = J - 1;
    do {
      path[i] = j;
      DTWMatrix.DTWMatrixElement e = matrix.get(i, j);
      i = e.targetX;
      j = e.targetY;
    } while (i >= 0 || j >= 0);
    return path;
  }
*/
  private DTWMatrix dtw(int r) {
    int I = compressedScore.size();
    int J = pfmNotes.length;
    int scoreTicks = compressedScore.get(I - 1).notes.get(0).offset();
    int pfmTicks = pfmNotes[J-1].offset();
    //r = Math.max(0, Math.max(J-I, r));
    r = J;
    DTWMatrix matrix = new DTWMatrix(I, J);
    matrix.set(-1, -1, 0, -1, -1);
    int colRisc=1;
    for (int i = 0; i < I; i++) {
      int rowRisc=1;
      for (int j = Math.max(0, i-r) ; j <= Math.min(i+r, J-1); j++) {
        NoteInSameTime e1 = compressedScore.get(i);
        Note e2 = pfmNotes[j];
        double ioi = 0;
        if(j > 0) {
          int diff = e2.onset() - pfmNotes[j - 1].onset();
          if(diff == 0) ioi = Double.POSITIVE_INFINITY;
          else ioi = 1.0 / diff;
        }
        double d = dist(e1, e2, scoreTicks, pfmTicks);
        double c1 = matrix.getValue(i-1, j) + d + colRisc;
        double c2 = matrix.getValue(i-1, j-1) + 2 * d + ioi;
        double c3 = matrix.getValue(i, j-1) + d + rowRisc;
        double c_min = Math.min(c2, Math.min(c1, c3));
        if (c_min == c2){
          matrix.set(i, j, c_min, i-1, j-1);
          colRisc = 1;
          rowRisc = 1;
        }else if (c_min == c3){
          matrix.set(i, j, c_min, i, j-1);
          rowRisc += 1;
        }else{
          matrix.set(i, j, c_min, i-1, j);
          colRisc += 1;
        }
      }
    }
    return matrix;
  }

  private double dist(NoteInSameTime e1, Note e2, int scoreTicks, int pfmTicks){
    double position = Math.abs((e1.notes.get(0).onset() / (double)scoreTicks - e2.onset() / (double)pfmTicks));
    for(Note n : e1.notes)
      if(n.notenum() == e2.notenum())
        return position;
    //else if(Math.abs(e1.notenum()-e2.notenum()) % 12 == 0) return 10 + position;
    return 100;
  }

  /*
  private static double dist(Note e1, Note e2, int scoreTicks, int pfmTicks){
    int notenum = Math.abs(e1.notenum() - e2.notenum());
    double position = Math.abs((e1.onset() / (double)scoreTicks - e2.onset() / (double)pfmTicks));
    if(notenum == 0) return position;
    else if(Math.abs(e1.notenum()-e2.notenum()) % 12 == 0) return 10 + position;
    return 100;
  }
*/
  private void initCompressedScore() {
    compressedScore = new ArrayList<NoteInSameTime>();
    Note prev = scoreNotes[0];
    NoteInSameTime nist = new NoteInSameTime();
    nist.addNote(prev, 0);
    compressedScore.add(nist);
    for(int i=1; i<scoreNotes.length; i++) {
      if(scoreNotes[i].onset() != prev.onset()) {
        nist = new NoteInSameTime();
        compressedScore.add(nist);
      }
      nist.addNote(scoreNotes[i], i);
      prev = scoreNotes[i];
    }
  }

  private void sortIndexList(int[] indexlist) {
    for(int i=0; i<indexlist.length - 1; i++) {
      if(indexlist[i] > indexlist[i + 1] && indexlist[i + 1] != -1) {
        Note tmp = pfmNotes[indexlist[i]];
        pfmNotes[indexlist[i]] = pfmNotes[indexlist[i + 1]];
        pfmNotes[indexlist[i + 1]] = tmp;
        int tmpi = indexlist[i];
        indexlist[i] = indexlist[i + 1];
        indexlist[i + 1] = tmpi;
      }
    }
  }

  private class DTWMatrix {
    private HashMap<IntPair,DTWMatrixElement> values;
    private int nrows, ncols;
    private final DTWMatrixElement DEFAULT_MATRIX_ELEMENT = 
      new DTWMatrixElement(Integer.MAX_VALUE / 2, -1, -1);
    private DTWMatrix(int nrows, int ncols) {
      this.nrows = nrows;
      this.ncols = ncols;
      values = new HashMap<IntPair,DTWMatrixElement>(Math.max(nrows,ncols));
    }
    private void set(int i, int j, double value, int targetX, int targetY) {
      values.put(new IntPair(i, j), 
                 new DTWMatrixElement(value, targetX, targetY));
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
        IntPair another = (IntPair)o;
        return (i == another.i) && (j == another.j);
      }
      public int hashCode() {
        return i * nrows + j;
      }
    }
    private class DTWMatrixElement {
      private int targetX, targetY;
      private double value;
      private DTWMatrixElement(double value, int targetX, int targetY){
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
      return tempo + ", " + tickInScore + ", " + tickInPfm + ", " + timeInSec + ", " + measure + ", " + beat;
    }
  }

  private class NoteInSameTime {
    List<Note> notes;
    int indexAtScore = Integer.MAX_VALUE;
    NoteInSameTime() { notes = new LinkedList<Note>(); }
    void addNote(Note n, int index) {
      notes.add(n);
      indexAtScore = Math.min(index, indexAtScore);
    }
    @Override
    public String toString() {
      String s = "";
      for(Note n : notes)
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
    for ( ; m < indexlist.length - 1; ) {
      if (indexlist[m] == -1 && !alreadyPut[n] && 
          scoreNotes[m].notenum() == pfmNotes[n].notenum()) {
        indexlist[m] = n;
        alreadyPut[n] = true;
        if (path[m+1] == n) {
          m++;
        } else if (path[m+1] - n > 1) {
          n++;
        } else {
          m++;
          n++;
        }
      } else if (path[m+1] == n) {
        m++;
      } else if (path[m+1] - n > 1) {
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
    if (indexlist[m] == -1 && !alreadyPut[n] && 
        scoreNotes[m].notenum() == pfmNotes[n].notenum()) 
      indexlist[m] = n;
    else
      extraNotes.add(pfmNotes[n]);
  }

  private void setNotewiseDeviations(DeviationDataSet dds, 
                                     int[] indexlist, 
                                     List<Note> extraNotes, 
                                     ArrayList<TempoAndTime> tempolist) {
    for (int i = 0; i < indexlist.length; i++) {
      if (indexlist[i] >= 0)
        addNoteDeviation(dds, scoreNotes[i], pfmNotes[indexlist[i]], 
                         tempolist);
      else
        addMissNote(dds, scoreNotes[i]);
    }
    for (Note note : extraNotes)
      addExtraNote(dds, note, partid, tempolist);
  }

  private int i = 0;

  private TempoAndTime searchTnT(double tick, 
                                 ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    while (i >= size-1 || tempolist.get(i).tickInPfm > tick && i > 0) 
	i--;
    while (i < 0 || tempolist.get(i+1).tickInPfm <= tick && i < size-2)
	i++;
    return tempolist.get(i);
  }

  private void addNoteDeviation(DeviationDataSet dds, Note noteS, 
                                Note noteP, 
                                ArrayList<TempoAndTime> tempolist){
    int initticks = (int)(tempolist.get(0).tickInPfm);
    TempoAndTime tnt = searchTnT(noteP.onset(), 
				 //noteS.onset()/scoreTicksPerBeat, 
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
    tnt = searchTnT(noteP.offset(), //noteS.offset() / scoreTicksPerBeat,
                    tempolist);
    double offsetInPfm, offsetInScore;
    if (tnt.measure >= 0) {
	offsetInPfm  = noteP.offset() - tnt.tickInPfm;
	offsetInScore = noteS.offset() - tnt.tickInScore;
    } else {
	offsetInPfm = noteP.offset() - tempolist.get(1).tickInPfm;
	offsetInScore = noteS.offset() - tempolist.get(1).tickInScore;
    }
    double release = 
      offsetInPfm * tnt.tempo / (pfmTicksPerBeat * baseTempo)
      - offsetInScore / scoreTicksPerBeat;
    double dynamics = noteP.velocity() / (double)BASE_DYNAMICS;
    if(Math.abs(attack) > ATTACK_LIMIT){
      addMissNote(dds, noteS);
      addExtraNote(dds, noteP, partid, tempolist);
    }else
      dds.addNoteDeviation(noteS.getMusicXMLWrapperNote(), 
                         attack, release, dynamics, dynamics);
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
      if (tnt.measure == -1 && tnt.beat == -1 || 
        tnt.tickInPfm > note.onset())
        break;
    }
    TempoAndTime tnt0 = tempolist.get(i-1);
    double inbeat0 = (double)(note.onset() - tnt0.tickInPfm) 
      / tnt0.beatLength();
    int j;
    for (j = i-1; j < size; j++) {
      TempoAndTime tnt = tempolist.get(j);
      if (tnt.measure == -1 && tnt.beat == -1 ||
          tnt.tickInPfm > note.offset())
        break;
    }
    TempoAndTime tnt1 = tempolist.get(j-1);
    double inbeat1 = (double)(note.offset() - tnt1.tickInPfm) 
      / tnt1.beatLength();
    dds.addExtraNote(partid, tnt0.measure, tnt0.beat + inbeat0, 
                     note.notenum(), (double)(j-i) + inbeat1 - inbeat0, 
                     note.velocity() / BASE_DYNAMICS, 
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

  private ArrayList<TempoAndTime> alignBeats(int[] indexlist) {
    ArrayList<TempoAndTime> tempolist = new ArrayList<TempoAndTime>();
    tempolist.add(getZerothTempoAndTime());
    int i = 0;
    for (int k = 0; k < barlines.length - 1; k++) {
      int measure = getMeasureNumber(barlines[k].onset());
      int beat = 1;
      for (int currentTick = barlines[k].onset(); 
           currentTick < barlines[k+1].onset(); 
           currentTick += scoreTicksPerBeat) {
        i = addTempoAndTime(currentTick, measure, beat, i, 
                            indexlist, tempolist);
        beat++;
      }
    }
    int currentTick = barlines[barlines.length-1].onset();
    int measure = getMeasureNumber(currentTick);
    int beat = 1;
    i = addTempoAndTime(currentTick, measure, beat, i, 
                        indexlist, tempolist);
    int lastOffset = currentTick;
    for ( ; i < scoreNotes.length; i++)
      if (scoreNotes[i].offset() > lastOffset)
        lastOffset = scoreNotes[i].offset();
    for (currentTick += scoreTicksPerBeat; 
         currentTick <= lastOffset; 
         currentTick += scoreTicksPerBeat) {
      TempoAndTime tnt = new TempoAndTime(currentTick);
      tnt.measure = measure;
      tnt.beat = ++beat;
      tempolist.add(tnt);
      //	    tempolist.add(new TempoAndTime(currentTick));
    }
    return tempolist;
  }
  
  private int addTempoAndTime(int currentTick, int measure, int beat, int i, 
                              int[] indexlist, 
                              ArrayList<TempoAndTime> tempolist) {
    while (i < scoreNotes.length) {
      if (indexlist[i] == -1) {
        i++;
        continue;
      }
      if (tickcmp(scoreNotes[i].onset(), currentTick, 5)) {
        int j, count = 0, total = 0;
        for (j = i; ; j++) {
          try {
            if (!tickcmp(scoreNotes[j].onset(),currentTick,5))
              break;
            if (indexlist[j] == -1)
              continue;
            total += pfmNotes[indexlist[j]].onset();
            count++;
          } catch (ArrayIndexOutOfBoundsException e) {
            break;
          }
        }
        TempoAndTime tnt = new TempoAndTime(currentTick);
        tnt.measure = measure;
        tnt.beat = beat;
        if (count > 0)
          tnt.setTickInPfm((double)total / (double)count);
        tempolist.add(tnt);
        return j;
      }
      if (scoreNotes[i].onset() > currentTick) {
        TempoAndTime tnt = new TempoAndTime(currentTick);
        tnt.measure = measure;
        tnt.beat = beat;
        tempolist.add(tnt);
        //		tempolist.add(new TempoAndTime(currentTick));
        return i;
      }
      i++;
    }
    return i;
  }

  private int lastMeasureNumber = 0;

  private int getMeasureNumber(int tick) {
    int tick0 = 
      measurelist[lastMeasureNumber].cumulativeTicks(scoreTicksPerBeat);
    if (tick0 == tick) {
      return measurelist[lastMeasureNumber].number();
//      return lastMeasureNumber;
    } else if (tick0 > tick) {
      lastMeasureNumber--;
      return getMeasureNumber(tick);
    } else {
      lastMeasureNumber++;
      return getMeasureNumber(tick);
    }
  }
  




    /*
  private ArrayList<TempoAndTime> alignBeats(Note[] scoreNotes, 
                                             Note[] pfmNotes, 
                                             int[] indexlist) {
    ArrayList<TempoAndTime> tempolist = new ArrayList<TempoAndTime>();
    tempolist.add(getZerothTempoAndTime());
    int tick = 0;
    double prevTickInPfm = Double.NEGATIVE_INFINITY;
    int lastOffset = 0;
    for (int i = 0; i < scoreNotes.length; ) {
      if (scoreNotes[i].offset() > lastOffset)
        lastOffset = scoreNotes[i].offset();
      if (indexlist[i] == -1) {
        i++;
        continue;
      }
      if (tickcmp(scoreNotes[i].onset(), tick, 5)) {
        int j, count = 0, total = 0;
        for (j = i; ; j++) {
          try {
            if (!tickcmp(scoreNotes[j].onset(), tick, 5)) break;
            if (indexlist[j] == -1) continue;
            total += pfmNotes[indexlist[j]].onset();
            count++;
          } catch (ArrayIndexOutOfBoundsException e) {
            break;
          }
        }
        TempoAndTime tnt = new TempoAndTime(tick);
	double newTickInPfm = ((total) / (double)count);
	if (newTickInPfm > prevTickInPfm) {
	    tnt.setTickInPfm(newTickInPfm);
	    prevTickInPfm = newTickInPfm;
	}
        tempolist.add(tnt);
        i = j;
        tick += scoreTicksPerBeat;
      } else if (scoreNotes[i].onset() > tick) {
        tempolist.add(new TempoAndTime(tick));
        i++;
        tick += scoreTicksPerBeat;
      } else {
        i++;
      }
    }
    while (tick <= lastOffset) {
      tempolist.add(new TempoAndTime(tick));
      tick += scoreTicksPerBeat;
    }
    return tempolist;
  }
    */

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
      for (i = 2; i < size-1; i++) {
        TempoAndTime tnt = tempolist.get(i);
        if (Double.isNaN(tnt.tickInPfm)) {
          if (i >= nextIndex) {
            for (nextIndex = i; ; nextIndex++) {
              nextTick = tempolist.get(nextIndex).tickInPfm;
              if (!Double.isNaN(nextTick))
                break;
            }
          }
          tnt.setTickInPfm
            ((prevTick * (nextIndex - i) + nextTick * (i - prevIndex)) 
             / (nextIndex - prevIndex));
        } else {
        prevIndex = i;
        prevTick = tnt.tickInPfm;
        }
      }
    } catch (IndexOutOfBoundsException e) {}
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
	      double pfmInterval = 
	          (double)(interval * 60 / scoreTicksPerBeat) / prevTempo;
	      tnt.setTimeInSec(prevtnt.timeInSec + pfmInterval);
	      //  tnt.setTimeInSec(prevtnt.timeInSec + 60.0 / prevTempo);
      } else {
	      int interval = tnt.tickInScore - prevtnt.tickInScore;
	      double pfmInterval = tnt.timeInSec - prevtnt.timeInSec;
	      prevtnt.tempo = 
	          (double)(interval * 60 / scoreTicksPerBeat) / pfmInterval;
	      // prevtnt.tempo = (60.0 / (tnt.timeInSec - prevtnt.timeInSec));
        prevTempo = prevtnt.tempo;
      }
      sum += prevtnt.tempo;
      count++;
      prevtnt = tnt;
    }
    tempolist.get(size-1).tempo = prevTempo;
    return sum / (double)count;
  }

    /*
  private void setMeasureNumbers(Measure[] measures, 
                                 List<TempoAndTime> tempolist) {
      int lastmeasure = -1;
      int lastbeat = -1;
    Iterator<TempoAndTime> it = tempolist.iterator();
    it.next();
    System.err.println(scoreTicksPerBeat);
    try {
      for (Measure measure : measures) {
        int tick = measure.cumulativeTicks(scoreTicksPerBeat);
        int length = measure.duration(1);
        for (int i = 1; i <= length; i++) {
          TempoAndTime tnt = it.next();
          System.err.println(i + ": " + tnt.tickInScore + " " + 
                             measure.duration(480) + " " + 
                             measure.number() + " " + measure.cumulativeTicks(scoreTicksPerBeat));
//          if (i == 1 && tnt.tickInScore != tick)
          if (i == 1 && !tickcmp(tnt.tickInScore, tick, 2))
            System.err.println("*** tick-in-score mismatch***");
          lastmeasure = tnt.measure = measure.number();
          lastbeat = tnt.beat = i;
        }
      }
    } catch (NoSuchElementException e) {
	while (it.hasNext()) {
	    TempoAndTime tnt = it.next();
	    tnt.measure = lastmeasure;
	    tnt.beat = ++lastbeat;
	}
    }

  }
    */

  private void addTempoDeviations(DeviationDataSet dds, 
                                         ArrayList<TempoAndTime> tempolist, 
                                         double avgtempo) {
    int size = tempolist.size();
    for (int i = 1; i < size - 1; i++) {
      TempoAndTime tnt = tempolist.get(i);
      dds.addNonPartwiseControl(tnt.measure, tnt.beat, "tempo-deviation", 
                                tnt.tempo / avgtempo);
    }
  }

  public static void main(String[] args){
    //
  }
}
  
