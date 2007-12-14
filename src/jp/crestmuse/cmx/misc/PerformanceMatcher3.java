package jp.crestmuse.cmx.misc;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Note;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.HeaderElement;

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

  private MusicXMLWrapper musicxml;
  private MIDIXMLWrapper midixml;

  private SCCXMLWrapper scoreSCC;
  private SCCXMLWrapper pfmSCC;

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
    scoreSCC = score.makeDeadpanSCCXML(ticksPerBeat);
    pfmSCC = pfm.toSCCXML();
    scoreTicksPerBeat = scoreSCC.getDivision();
    pfmTicksPerBeat = pfmSCC.getDivision();
    HeaderElement[] h = pfmSCC.getHeaderElementList();
    if (h.length >= 1 && h[0].name().equals("TEMPO"))
      baseTempo = Integer.parseInt(h[0].content());
//    scoreSCC.writefile("scoreSCC.xml");
//    pfmSCC.writefile("pfmSCC.xml");
  }

  public DeviationInstanceWrapper extractDeviation() 
    throws ParserConfigurationException, SAXException, IOException, 
    TransformerException  {
    DeviationInstanceWrapper dev = 
      DeviationInstanceWrapper.createDeviationInstanceFor(musicxml);
    DeviationDataSet dds = dev.createDeviationDataSet();
    Note[] scoreNotes = scoreSCC.getPartList()[0].getSortedNoteOnlyList(1);
    Note[] pfmNotes = pfmSCC.getPartList()[0].getSortedNoteOnlyList(100);
    int[] path = getPath(dtw(scoreNotes, pfmNotes, 500));
    int[] indexlist = new int[path.length];
    List<Note> extraNotes = new ArrayList<Note>();
    alignNotes(scoreNotes, pfmNotes, path, indexlist, extraNotes);
    ArrayList<TempoAndTime> tempolist = alignBeats(scoreNotes, 
                                                   pfmNotes, indexlist);
    interpolateBeatTime(tempolist);
    double avgtempo = calcTempo(tempolist);
    double initSil = tempolist.get(1).timeInSec;
    dds.setInitialSilence(initSil);
    String partid = musicxml.getPartList()[0].id();
    Measure[] measures = musicxml.getPartList()[0].getMeasureList();
    setMeasureNumbers(measures, tempolist);
    int headMeasure = measures[0].number();
    dds.addNonPartwiseControl(headMeasure, 1, "tempo", avgtempo);
    addTempoDeviations(dds, tempolist, avgtempo);
    setNotewiseDeviations(dds, scoreNotes, pfmNotes, indexlist, 
                          extraNotes, partid, tempolist);
    dds.addElementsToWrapper();
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

/*
  public static DeviationInstanceWrapper extractDeviation
    (MusicXMLWrapper score, MIDIXMLWrapper pfm) 
    throws ParserConfigurationException, SAXException, IOException,
    TransformerException {
    DeviationInstanceWrapper dev = 
      DeviationInstanceWrapper.createDeviationInstanceFor(score);
    DeviationDataSet dds = dev.createDeviationDataSet();
    SCCXMLWrapper scoreSCC = score.makeDeadpanSCCXML(pfm.ticksPerBeat());
    SCCXMLWrapper pfmSCC = pfm.toSCCXML();
    SCCXMLWrapper sortedScore = scoreSCC.getSortedSCCXML(1);
    SCCXMLWrapper sortedPfm = pfmSCC.getSortedSCCXML(100);
    //sortedScore.write(new FileOutputStream(new File("scoreSCC.xml")));
    //sortedPfm.write(new FileOutputStream(new File("pfmSCC.xml")));
    int[] path = getPath(dtw(sortedScore, sortedPfm, 500));
    setExtraNote(dds, path, sortedScore, sortedPfm, score);
    setMissNote(dds, path, sortedScore, sortedPfm);
    ArrayList<Double> beatToTempo = getTempoList(sortedScore, sortedPfm, path);
    setTempoDev(dds, beatToTempo, score);
    setDeviation(dds, sortedScore, sortedPfm, path, beatToTempo);
    dds.addElementsToWrapper();
    return dev;
  }
*/

/*
  private static void setMissNote(DeviationDataSet dds, int[] path,
      SCCXMLWrapper score, SCCXMLWrapper pfm) throws TransformerException{
    for(int i=0; i<path.length; i++){
      if(score.getPartList()[0].getNoteList()[i].notenum() != pfm.getPartList()[0].getNoteList()[path[i]].notenum() && Math.abs(score.getPartList()[0].getNoteList()[i].notenum()-pfm.getPartList()[0].getNoteList()[path[i]].notenum())%12!=0){
        dds.addMissNote(score.getPartList()[0].getNoteList()[i].getMusicXMLWrapperNote());
        path[i] = -1;
//System.out.println(i);
      }
    }
  }
*/

/*
  private static void setExtraNote(DeviationDataSet dds, int[] path, SCCXMLWrapper score, SCCXMLWrapper pfm, MusicXMLWrapper musicxml) throws TransformerException{
    int partLength = Math.min(score.getPartList().length, pfm.getPartList().length);
    for (int i = 0; i < partLength; i++) {
      Note[] scoreNotes = score.getPartList()[i].getNoteList();
      Note[] pfmNotes = pfm.getPartList()[i].getNoteList();
      String id = musicxml.getPartList()[i].getAttribute("id");
      double vol = pfm.getPartList()[i].volume();
      
      int[] reversePath = new int[pfmNotes.length];
      for(int j=0; j<reversePath.length; j++) reversePath[j] = -1;
      for(int j=0; j<path.length; j++) reversePath[path[j]] = j;
      for(int j=0; j<reversePath.length; j++){
        try {
          int pfmIndex = j, scoreIndex = reversePath[j];
          if(scoreNotes[scoreIndex].notenum() != pfmNotes[pfmIndex].notenum()){
            MusicXMLWrapper.Note note = scoreNotes[scoreIndex].getMusicXMLWrapperNote();
            dds.addExtraNote(id, note.measure().number(), note.beat(),
                pfmNotes[pfmIndex].notenum(),
                (pfmNotes[pfmIndex].offset() - pfmNotes[pfmIndex].onset()) / (double) pfm.getDivision(),
                pfmNotes[pfmIndex].velocity() / vol, pfmNotes[pfmIndex].velocity() / vol);
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          MusicXMLWrapper.Note noteA;
          MusicXMLWrapper.Note noteC;
          int A = j-1, x;
          try {
            while(reversePath[A] == -1) A--;
            x = pfmNotes[j].onset() - pfmNotes[A].onset();
            noteA = scoreNotes[reversePath[A]].getMusicXMLWrapperNote();
          } catch (IndexOutOfBoundsException e1) {
            noteA = scoreNotes[0].getMusicXMLWrapperNote();
            x = 0;
          }
          int C = j+1, y;
          try {
            while(reversePath[C] == -1) C++;
            y = pfmNotes[C].onset() - pfmNotes[j].onset();
            noteC = scoreNotes[reversePath[C]].getMusicXMLWrapperNote();
          } catch (IndexOutOfBoundsException e1) {
            noteC = scoreNotes[scoreNotes.length-1].getMusicXMLWrapperNote();
            y = 0;
          }
          int measure = noteA.measure().number();
          double beat = noteA.beat() + (noteC.beat() - noteA.beat()) * x / (x + y);
          if(beat > noteA.measure().duration(1)){
            measure++;
            beat -= noteA.measure().duration(1);
          }
          dds.addExtraNote(id, measure, beat,
              pfmNotes[j].notenum(),
              (pfmNotes[j].offset() - pfmNotes[j].onset()) / (double) pfm.getDivision(),
              pfmNotes[j].velocity() / vol, pfmNotes[j].velocity() / vol);
        }
        }*/
      /*for (int j = 0; j < path.length; j++) {
        if (scoreNotes[j].notenum() != pfmNotes[path[j]].notenum()) {
          MusicXMLWrapper.Note note = scoreNotes[j].getMusicXMLWrapperNote();
          dds.addExtraNote(id, note.measure().number(), note.beat(),
              pfmNotes[path[j]].notenum(),
              (pfmNotes[path[j]].offset() - pfmNotes[path[j]].onset()) / (double) pfm.getDivision(),
              pfmNotes[path[j]].velocity() / vol, pfmNotes[path[j]].velocity() / vol);
        }
      }*/ /*
    }    
  }
          */

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
    /*try {
      PrintWriter write = new PrintWriter(new FileOutputStream(new File("match.txt")));
      for(i=0; i<path.length; i++) write.println(i+" "+path[i]);
      write.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }*/
//for(int a=0; a<path.length; a++)System.out.println(a+" "+path[a]);
    return path;
  }
  
  private static DTWMatrix dtw(Note[] scoreNotes, Note[] pfmNotes, int r) {
    int I = scoreNotes.length;
    int J = pfmNotes.length;
    int scoreTicks = scoreNotes[I-1].offset();
    int pfmTicks = pfmNotes[J-1].offset();
    //r = Math.max(0, Math.max(J-I, r));
    r = J;
    DTWMatrix matrix = new DTWMatrix(I, J);
    matrix.set(-1, -1, 0, -1, -1);
    int colRisc=1;
    for (int i = 0; i < I; i++) {
      //System.err.print(".");
      int rowRisc=1;
      for (int j = Math.max(0, i-r) ; j <= Math.min(i+r, J-1); j++) {
        Note e1 = scoreNotes[i];
        Note e2 = pfmNotes[j];
        double d = dist(e1, e2, scoreTicks, pfmTicks);
        double c1 = matrix.getValue(i-1, j) + d + colRisc;
        double c2 = matrix.getValue(i-1, j-1) + 2 * d;
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
  
  private static double dist(Note e1, Note e2, int scoreTicks, int pfmTicks){
    int notenum = Math.abs(e1.notenum() - e2.notenum());
    double position = Math.abs((e1.onset() / (double)scoreTicks - e2.onset() / (double)pfmTicks));
    if(notenum == 0) return position;
    else if(Math.abs(e1.notenum()-e2.notenum()) % 12 == 0) return 10 + position;
    return 100;
  }

  private static class DTWMatrix {
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

/*
  private double calcInitialSilence(Note[] scoreNotes, 
                                    Note[] pfmNotes) {
    int t1 = scoreNotes[0].onset();
    if (t1 == 0) {
      int t2 = pfmNotes[0].onset();
      double sil;
      if (scoreTicksPerBeat == pfmTicksPerBeat) 
        sil = (double)((t2 - t1) * 60) 
          / (double)(scoreTicksPerBeat * baseTempo);
      else
        sil = (double)((t2 * pfmTicksPerBeat - t1 * scoreTicksPerBeat) * 60)
          / (double)(scoreTicksPerBeat * pfmTicksPerBeat * baseTempo);
      return sil;
    }
    return 0;
  }
*/


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
  }

  private static void alignNotes(Note[] scoreNotes, 
                                 Note[] pfmNotes, 
                                 int[] path, 
                                 int[] indexlist, 
                                 List<Note> extraNotes) {
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



/*
  private static void alignNotes(Note[] scoreNotes, 
                                 Note[] pfmNotes, 
                                 int[] path, 
                                 int[] indexlist, 
                                 List<Note> extraNotes) {
    for (int m = 0; m < indexlist.length; m++)
      indexlist[m] = -1;
    boolean[] alreadyPut = new boolean[pfmNotes.length];
    for (int n = 0; n < alreadyPut.length; n++)
      alreadyPut[n] = false;
    int i;
    for (i = 0; i < path.length - 1; ) {
      System.err.print("(" + i + "/" + (path.length-1) + ")");
      int j;
      for (j = i + 1; j < path.length; j++)
        if (path[j] > path[i])
          break;
      int m = i, n = path[i];
      boolean done = false;
      System.err.println();
      System.err.println("grace: " + scoreNotes[i].getMusicXMLWrapperNote().grace());
      System.err.print("(j=" + j + ")");
      while (m < j ) {
//      while (m < j && n < path[j]) {
        System.err.print("(m=" + m +")");
        System.err.print("(n=" + n + ")");
        if (indexlist[m] == -1 && !alreadyPut[n] && 
            scoreNotes[m].notenum() == pfmNotes[n].notenum()) {
//        if (!done && scoreNotes[m].notenum() == pfmNotes[n].notenum()) {
//        if (!done && 
//            (Math.abs(scoreNotes[m].notenum() - pfmNotes[n].notenum())
//             % 12 == 0)) {
          indexlist[m] = n;
          System.err.print("[" + m + ":" + n + "]  "
          + scoreNotes[m].getMusicXMLWrapperNote().getXPathExpression());
          alreadyPut[n] = true;
          if (path[m+1] == n) {
            m++;
          } else if (path[m+1] - n > 1) {
            n++;
          } else {
            m++;
            n++;
          }
//          m++;
//          n = path[m];
//          n++;
//          done = true;
        } else if (path[m+1] == n) {
//          System.err.println("Note with m="+m+" was judged to be a miss note.");
//          System.err.println("notenum=" + pfmNotes[m].notenum());
//          indexlist[m] = -1;
          m++;
        } else if (path[m+1] - n > 1) {
          if (!alreadyPut[n]) {
            extraNotes.add(pfmNotes[n]);
            alreadyPut[n] = true;
//            done = true;
          }
          System.err.println("Added not with n="+n+" to the extra note list.");
          System.err.println("notenum=" + pfmNotes[n].notenum());
          n++;
        } else {
//          indexlist[m] = -1;
//          if (indexlist[m] == -1)
          if (!alreadyPut[n]) {
            extraNotes.add(pfmNotes[n]);
            alreadyPut[n] = true;
//            done = true;
          }
          System.err.println("Note with m="+m+" was judged to be a miss note.");
          System.err.println("notenum=" + pfmNotes[m].notenum());
          System.err.println("Added note with n="+n+" to the extra note list.");
          System.err.println("notenum=" + pfmNotes[n].notenum());
          m++;
          n++;
        }
      }
      i = j;
      System.err.print("@@");
    }
    if (i == path.length - 1) {
      if ((Math.abs(scoreNotes[i].notenum() - pfmNotes[path[i]].notenum())
           * 12 == 0)) {
        indexlist[i] = path[i];
      } else {
//        indexlist[i] = -1;
        extraNotes.add(pfmNotes[path[i]]);
      }
    }
    for (int n = 0; n < alreadyPut.length; n++)
      System.err.println(alreadyPut[n]);
  }
*/


  private void setNotewiseDeviations(DeviationDataSet dds, 
                                     Note[] scoreNotes, 
                                     Note[] pfmNotes, 
                                     int[] indexlist, 
                                     List<Note> extraNotes, 
                                     String partid, 
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

  private TempoAndTime searchTnT(double tick, int i, 
                                 ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    while (i >= size || tempolist.get(i).tickInPfm > tick && i > 0) 
      i--;
    while (i < 0 || tempolist.get(i+1).tickInPfm <= tick && i < size-2)
      i++;
    return tempolist.get(i);
  }

  private void addNoteDeviation(DeviationDataSet dds, Note noteS, 
                                Note noteP, 
                                ArrayList<TempoAndTime> tempolist){
    int initticks = (int)(tempolist.get(0).tickInPfm);
//    int offsetBeat = noteS.offset() / scoreTicksPerBeat;
//    onsetBeat = Math.min(onsetBeat, tempolist.size()-2);
//    offsetBeat = Math.min(offsetBeat, tempolist.size()-2);
//    TempoAndTime tnt = tempolist.get(onsetBeat+1);
    TempoAndTime tnt = searchTnT(noteP.onset(), 
                                 noteS.onset()/scoreTicksPerBeat, 
                                 tempolist);
    double onsetInPfm, onsetInScore;
    if (tnt.measure >= 0) {
	onsetInPfm = noteP.onset() - tnt.tickInPfm;
	onsetInScore = noteS.onset() - tnt.tickInScore;
    } else {
	onsetInPfm = noteP.onset() - tempolist.get(1).tickInPfm;
	onsetInScore = noteS.onset() - tempolist.get(1).tickInScore;
	//double onsetInScore = noteS.onset() - scoreTicksPerBeat * onsetBeat;
    }
    double attack = onsetInPfm * tnt.tempo / (pfmTicksPerBeat * baseTempo)
	- onsetInScore / scoreTicksPerBeat;
//    double attack = 
//      (noteP.onset() -  tnt.tickInPfm) * tnt.tempo / BASE_TEMPO
//      - (noteS.onset() - scoreTicksPerBeat * onsetBeat);
//    tnt = tempolist.get(offsetBeat+1);
    tnt = searchTnT(noteP.offset(), noteS.offset() / scoreTicksPerBeat,
                    tempolist);
    double offsetInPfm, offsetInScore;
    if (tnt.measure >= 0) {
	offsetInPfm  = noteP.offset() - tnt.tickInPfm;
	offsetInScore = noteS.offset() - tnt.tickInScore;
    } else {
	offsetInPfm = noteP.offset() - tempolist.get(1).tickInPfm;
	offsetInScore = noteS.offset() - tempolist.get(1).tickInScore;
    }
//    double offsetInScore = noteS.offset() - scoreTicksPerBeat * offsetBeat;
    double release = 
      offsetInPfm * tnt.tempo / (pfmTicksPerBeat * baseTempo)
      - offsetInScore / scoreTicksPerBeat;
//    double release = 
//      (noteP.offset() - tnt.tickInPfm) * tnt.tempo / baseTempo
//      - (noteS.offset() - scoreTicksPerBeat * offsetBeat);
//    attack /= (double)scoreTicksPerBeat;
//    release /= (double)scoreTicksPerBeat;
    double dynamics = noteP.velocity() / (double)BASE_DYNAMICS;
    dds.addNoteDeviation(noteS.getMusicXMLWrapperNote(), 
                         attack, release, dynamics, dynamics);
  }

  private static void addMissNote(DeviationDataSet dds, Note note) {
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
//    if (i <= 0) i = 1;
    TempoAndTime tnt0 = tempolist.get(i-1);
//    double beatLength;
//    if (i < size)
//      beatLength = tempolist.get(i).tickInPfm - tnt0.tickInPfm;
//    else
//      beatLength = (double)pfmTicksPerBeat * baseTempo / tnt0.tempo;
    double inbeat0 = (double)(note.onset() - tnt0.tickInPfm) 
      / tnt0.beatLength();
    //    double inbeat0 = (note.onset() - tnt0.tickInPfm)
    //      / (tempolist.get(i).tickInPfm - tnt0.tickInPfm);
    int j;
    for (j = i-1; j < size; j++) {
      TempoAndTime tnt = tempolist.get(j);
      if (tnt.measure == -1 && tnt.beat == -1 ||
          tnt.tickInPfm > note.offset())
        break;
    }
//    if (j <= 0) j = 1;
    TempoAndTime tnt1 = tempolist.get(j-1);
//    if (j < size)
//      beatLength = tempolist.get(j).tickInPfm - tnt1.tickInPfm;
//    else
//      beatLength = (double)pfmTicksPerBeat * baseTempo / tnt1.tempo;
    double inbeat1 = (double)(note.offset() - tnt1.tickInPfm) 
      / tnt1.beatLength();
    //    double inbeat1 = (note.offset() - tnt1.tickInPfm)
    //      / (tempolist.get(j).tickInPfm - tnt1.tickInPfm);
    dds.addExtraNote(partid, tnt0.measure, tnt0.beat + inbeat0, 
                     note.notenum(), (double)(j-i) + inbeat1 - inbeat0, 
                     note.velocity() / BASE_DYNAMICS, 
                     note.velocity() / BASE_DYNAMICS);
  }


                                  

/*

  private static void setNotewiseDeviations(DeviationDataSet dds, 
                                            Note[] scoreNotes, 
                                            Note[] pfmNotes, 
                                            int[] path, 
                                            ArrayList<TempoAndTime> tempolist,
                                            String partid,
                                            int scoreTicksPerBeat, 
                                            int pfmTicksPerBeat) 
    throws TransformerException {

//    Note[] scoreNotes = score.getPartList()[0].getNoteList();
//    Note[] pfmNotes = pfm.getPartList()[0].getNoteList();

//    double initsil = score.getPartList()[0].getNoteList();
//    dds.setInitialSilence(initsil);
//    init initticks = (int)(initsil * baseTempo * pfm.getDeviation() / 60);

//    double[] beatToTick = new double[beatToTempo.size()];
//    double timeStamp = beatToTick[0] = 0.0;
//    for (int i = 0; i < beatToTick.length; i++) 
//      beatToTick[i] = timeStamp += 
//        score.getDivision() * baseTempo / beatToTempo.get(i);
    for (int i = 0; i < path.length-1; ) {
      int j;
      for (j = i+1; j < path.length; j++) 
        if (path[j] > path[i])
          break;
      int m = i, n = path[i];
      boolean done = false;
      while (m < j && n < path[j]) {
        if (!done && (
              Math.abs(scoreNotes[m].notenum() - pfmNotes[n].notenum()) 
              % 12 == 0)) {
          addNoteDeviation(dds, scoreNotes[m], pfmNotes[n], 
                           tempolist, scoreTicksPerBeat, pfmTicksPerBeat);
          m++;
          n++;
          done = true;
        } else if (path[m+1] == n) {
          addMissNote(dds, scoreNotes[m]);
          m++;
        } else if (path[m+1] - n > 1) {
          addExtraNote(dds, pfmNotes[n], tempolist, scoreTicksPerBeat, 
                       pfmTickPerBeat);
          n++;
        } else {
          addMissNote(dds, scoreNotes[m]);
          addExtraNote(dds, pfmNotes[n], tempolist, scoreTicksPerBeat, 
                       pfmTicksPerBeat);
          m++;
          n++;
        }
      }
    }
  }      
*/

/*
  private static void setDeviation(DeviationDataSet dds,
      SCCXMLWrapper score, SCCXMLWrapper pfm,
      int[] path, ArrayList<Double> beatToTempo) throws TransformerException{

    Note[] scoreNotes = score.getPartList()[0].getNoteList();
    Note[] pfmNotes = pfm.getPartList()[0].getNoteList();

    double initsil = calcInitialSilence(score, pfm);
    dds.setInitialSilence(initsil);
    int initticks = (int)(initsil * baseTempo * pfm.getDivision() / 60);

    double timeStamp = 0;
    ArrayList<Double> beatToTick = new ArrayList<Double>();
    beatToTick.add(timeStamp);
    for(int i=0; i<beatToTempo.size(); i++){
      timeStamp += score.getDivision() * baseTempo / beatToTempo.get(i);
      beatToTick.add(timeStamp);
    }

    for(int i=0; i<scoreNotes.length; i++){
      if(path[i] == -1) continue;
      Note s = scoreNotes[i];
      Note p = pfmNotes[path[i]];
      int onsetBeat = s.onset() / score.getDivision();
      int offsetBeat = s.offset() / score.getDivision();

      try {
        double attack = (p.onset() - initticks - beatToTick.get(onsetBeat)) * beatToTempo.get(onsetBeat) / baseTempo - (s.onset() - score.getDivision() * onsetBeat);
        double release = (p.offset() - initticks - beatToTick.get(offsetBeat)) * beatToTempo.get(offsetBeat) / baseTempo - (s.offset() - score.getDivision() * offsetBeat);
        double dynamics = p.velocity() / (double)s.velocity();
        attack /= (double)score.getDivision();
        release /= (double)score.getDivision();
        dds.addNoteDeviation(s.getMusicXMLWrapperNote(), attack, release, dynamics, dynamics);
      } catch (IndexOutOfBoundsException e) {
        onsetBeat = Math.min(onsetBeat, beatToTempo.size()-1);
        offsetBeat = Math.min(offsetBeat, beatToTempo.size()-1);
        double attack = (p.onset() - initticks - beatToTick.get(onsetBeat)) * beatToTempo.get(onsetBeat) / baseTempo - (s.onset() - score.getDivision() * onsetBeat);
        double release = (p.offset() - initticks - beatToTick.get(offsetBeat)) * beatToTempo.get(offsetBeat) / baseTempo - (s.offset() - score.getDivision() * offsetBeat);
        double dynamics = p.velocity() / (double)s.velocity();
        attack /= (double)score.getDivision();
        release /= (double)score.getDivision();
        dds.addNoteDeviation(s.getMusicXMLWrapperNote(), attack, release, dynamics, dynamics);
      }
    }
  }
*/

    private static boolean tickcmp(int tick1, int tick2, int threshold) {
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

  private static double calcTempo(ArrayList<TempoAndTime> tempolist) {
    int size = tempolist.size();
    double prevTempo = 0.0;
    double sum = 0.0;
    int count = 0;
    TempoAndTime prevtnt = tempolist.get(1);
    for (int i = 2; i < size; i++) {
      TempoAndTime tnt = tempolist.get(i);
      if (Double.isNaN(tnt.timeInSec)) {
        prevtnt.tempo = prevTempo;
        tnt.setTimeInSec(prevtnt.timeInSec + 60.0 / prevTempo);
      } else {
        prevtnt.tempo = (60.0 / (tnt.timeInSec - prevtnt.timeInSec));
        prevTempo = prevtnt.tempo;
      }
      sum += prevtnt.tempo;
      count++;
      prevtnt = tnt;
    }
    tempolist.get(size-1).tempo = prevTempo;
//    for (int i = 1; i < size; i++) {
//      TempoAndTime tnt1 = tempolist.get(i);
////      System.err.println(i + ":" + tnt1.timeInSec);
//      TempoAndTime tnt2;
//      if (i == size-1) {
//        tnt1.tempo = prevTempo;
//        sum += prevTempo;
//        count++;
//      } else if (Double.isNaN((tnt2 = tempolist.get(i+1)).timeInSec)) {
//        tnt1.tempo = prevTempo;
//        tnt2.setTimeInSec(tnt1.timeInSec + 60.0 / prevTempo);
//        sum += prevTempo;
//        count++;
//      } else {
//        tnt1.tempo = prevTempo = (60.0 / (tnt2.timeInSec - tnt1.timeInSec));
//        sum += prevTempo;
//        count++;
//      }
//    }
    return sum / (double)count;
  }

  private void setMeasureNumbers(Measure[] measures, 
                                 List<TempoAndTime> tempolist) {
      int lastmeasure = -1;
      int lastbeat = -1;
    Iterator<TempoAndTime> it = tempolist.iterator();
    it.next();
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

  private static void addTempoDeviations(DeviationDataSet dds, 
                                         ArrayList<TempoAndTime> tempolist, 
                                         double avgtempo) {
    int size = tempolist.size();
    for (int i = 1; i < size - 1; i++) {
      TempoAndTime tnt = tempolist.get(i);
      dds.addNonPartwiseControl(tnt.measure, tnt.beat, "tempo-deviation", 
                                tnt.tempo / avgtempo);
    }
  }
      
/*
  
  private static ArrayList<Double> getTempoList(SCCXMLWrapper score,
      SCCXMLWrapper pfm, int[] path) throws TransformerException{
    
    ArrayList<Double> align = new ArrayList<Double>();
    Note[] scoreNotes = score.getPartList()[0].getNoteList();
    Note[] pfmNotes = pfm.getPartList()[0].getNoteList();
    
    int tick = 0;
    for(int i=0; i<scoreNotes.length; i++){
      if(path[i] == -1) continue;
      if(scoreNotes[i].onset() == tick){
        int j, count=0, total=0;
        for(j=i;;j++){
          try {
            if(path[j]==-1) { continue; }
          } catch (ArrayIndexOutOfBoundsException e1) {
            break;
          }
          total += pfmNotes[path[j]].onset();
          count++;
          try {
            if(scoreNotes[j+1].onset() != tick) break;
          } catch (ArrayIndexOutOfBoundsException e) {
            break;
          }
        }
        align.add(total / (double)count / pfm.getDivision() * 60. / baseTempo);
        i = j;
        tick += score.getDivision();
      }else if(scoreNotes[i].onset() > tick){
        try {
          int j=i-1;
          while(path[j] == -1)j--;
          double d = pfmNotes[path[j]].onset() + (pfmNotes[path[i]].onset() - pfmNotes[path[j]].onset()) * (tick - scoreNotes[j].onset()) / (double)(scoreNotes[i].onset() - scoreNotes[j].onset());
          d = d / pfm.getDivision() * 60. / baseTempo;
          align.add(d);
        } catch (ArrayIndexOutOfBoundsException e) {
          // 楽譜の最初が0で始まらなかったとき（突貫工事）
          align.add(0.);
        }
        tick += score.getDivision();
        i--;
      }
    }
//for(double o:align)System.out.println(o);
    ArrayList<Double> beatToTempo = new ArrayList<Double>();
    boolean isFirstInfinity = false;
    for(int i=0; i<align.size()-1; i++){
      double d = align.get(i+1)-align.get(i);
      beatToTempo.add(60 / d);
      if(d < 0.01){
        try{
          System.err.println("infinite at "+i);
          beatToTempo.set(i, beatToTempo.get(i-1));
        }catch(ArrayIndexOutOfBoundsException e){
          isFirstInfinity = true;
        }
      }
    }
    if(isFirstInfinity){
      //テンポリストの先頭がinfinityだったときの対応（突貫工事）
      System.err.println("first infinite");
      int i=0;
      while(beatToTempo.get(i).isInfinite()) i++;
      for(int j=0; j<i; j++) beatToTempo.set(j, beatToTempo.get(i));
    }

    return beatToTempo;
  }
*/  

/*
  private static void setTempoDev(DeviationDataSet dds,
      ArrayList<Double> beatToTempo, MusicXMLWrapper score){

    // 元のMusicXMLから開始小節の番号とビートタイプを得る
    Part[] parts = score.getPartList();
    Measure[] measures = parts[0].getMeasureList();

    int headMeasure = measures[0].number();
 
    // 反映する
    double total=0;
    for(double d : beatToTempo) total += d;
    double baseTempo = total / beatToTempo.size();
    dds.addNonPartwiseControl(headMeasure, 1, "tempo", baseTempo);
    Iterator<Double> it = beatToTempo.iterator();
    try {
      int index=0;
      for (Measure measure : measures) {
        int length = measure.duration(1);
        for (int i = 1; i <= length; i++){
          dds.addNonPartwiseControl(measure.number(), i, "tempo-deviation", 
                                    it.next() / baseTempo);
          index++;
        }
      }
    } catch (NoSuchElementException e) { }
  }

*/


  
  /*private static 
    TimeFreqRepresentation<SCCXMLWrapper.Note> 
    getTimeFreqRepresentation(SCCXMLWrapper scc) 
    throws TransformerException {
    final int division = scc.getDivision();
    final TimeFreqRepresentation<SCCXMLWrapper.Note> tfr 
      = new TimeFreqRepresentation<SCCXMLWrapper.Note>(N_BANDS);
    scc.processNotes(new SCCHandler() {
        public void beginHeader(SCCXMLWrapper w) { }
        public void endHeader(SCCXMLWrapper w) { }
        public void beginPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {}
        public void endPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {}
        public void processHeaderElement(int timestamp, String name, 
                                         String content, SCCXMLWrapper w) {}
        public void processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w) {
          int onset = note.onset();
          int offset = note.offset();
          int notenum = note.notenum();
          int length;
          while ((length = tfr.length()) < offset)
            tfr.addTime(length * division);
          tfr.set(onset, notenum, note);
          for (int n = onset; n < offset; n++)
            tfr.set(n, notenum, (byte)127);
        }
      });
    return tfr;
  }*/

/*
  public static void main(String[] args) {
    try {
      int range = Integer.parseInt(args[3]);
      MIDIXMLWrapper midixml = MIDIXMLWrapper.readSMF(args[1]);
      SCCXMLWrapper scc1 = midixml.toSCCXML();
      scc1 = sortSCC(scc1, range);
      //TimeFreqRepresentation tfr1 = getTimeFreqRepresentation(scc1);
      MusicXMLWrapper musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile(args[0]);
      SCCXMLWrapper scc0 = musicxml.makeDeadpanSCCXML(scc1.getDivision());
      scc0 = sortSCC(scc0, range);
      //TimeFreqRepresentation tfr0 = getTimeFreqRepresentation(scc0);
      //System.out.println(tfr0.length());
      //System.out.println(tfr1.length());
      DTWMatrix matrix = dtw(scc0, scc1, Integer.parseInt(args[2]));
      int[] path = getPath(matrix);
      for (int n : path)
        System.out.print(n + " ");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
*/

}
  
