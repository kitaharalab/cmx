package jp.crestmuse.cmx.gui.deveditor.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.filewrappers.CSVWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.ChordDeviation;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.Control;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.ExtraNote;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.NoteDeviation;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.MusicData;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteUpdateListener;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;
import jp.crestmuse.cmx.misc.MutableMusicEvent;
import jp.crestmuse.cmx.misc.MutableNote;
import jp.crestmuse.cmx.misc.TreeView;

/**
 * このクラスは一つのSequenceと複数のDeviatedNoteを保持し、DeviationEditorで扱う曲の一曲を表します．
 * 
 * @author ntotani
 */
public class DeviatedPerformance {

  public static int TICKS_PER_BEAT = 480;
  private int BASE_VELOCITY = 100;
  private int TEMPO = 72;
  private Sequence sequence;
  private MusicXMLWrapper musicxml;
  private ArrayList<DeviatedNote> deviatedNotes;
  private TreeMap<Integer, Integer> ticks2tempo;
  private TreeMap<Integer, Integer> ticks2msec;
  private final int linearDivision = 8;
  private List<DeviatedNoteUpdateListener> listeners;
  private Track tempoTrack;
  private TreeMap<Integer, MidiEvent> ticks2midievent;

  public DeviatedPerformance(final DeviationInstanceWrapper deviation)
      throws IOException, InvalidMidiDataException {
    // TODO MusicXMLにTempoが指定してあった場合それを反映
    // TODO initSilenceに対応
    // TODO deviation読み込みを完全再現
    sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
    deviatedNotes = new ArrayList<DeviatedNote>();
    ticks2tempo = new TreeMap<Integer, Integer>();
    ticks2msec = new TreeMap<Integer, Integer>();
    ticks2midievent = new TreeMap<Integer, MidiEvent>();
    listeners = new LinkedList<DeviatedNoteUpdateListener>();

    processNonPartwiseControls(deviation);
    calcMsecs();

    final HashMap<String, Track> part2track = new HashMap<String, Track>();
    final HashMap<String, TreeMap<Integer, Double>> part2tbd = new HashMap<String, TreeMap<Integer, Double>>();
    musicxml = deviation.getTargetMusicXML();
    musicxml.processNotePartwise(new NoteHandlerPartwise() {
      private Track track;
      private TreeMap<Integer, Double> tick2basedynamics;

      public void beginPart(Part part, MusicXMLWrapper wrapper) {
        track = sequence.createTrack();
        part2track.put(part.id(), track);
        tick2basedynamics = getTick2BaseDynamics(deviation, part.id());
        part2tbd.put(part.id(), tick2basedynamics);
      }

      public void beginMeasure(Measure measure, MusicXMLWrapper wrapper) {
      }

      public void endMeasure(Measure measure, MusicXMLWrapper wrapper) {
      }

      public void endPart(Part part, MusicXMLWrapper wrapper) {
      }

      public void processMusicData(MusicData md, MusicXMLWrapper wrapper) {
        if (!(md instanceof MusicXMLWrapper.Note))
          return;
        MusicXMLWrapper.Note note = (MusicXMLWrapper.Note) md;
        if (note.rest() || "none".equals(note.notehead())
            || note.containsTieType("stop"))
          return;
        double baseDynamics = 1.0;
        for (Entry<Integer, Double> e : tick2basedynamics.entrySet()) {
          if (e.getKey() > note.onset(TICKS_PER_BEAT))
            break;
          baseDynamics = e.getValue();
        }
        try {
          if (deviation.getMissNote(note) != null) {
            deviatedNotes.add(new DeviatedNote(note, true, track, baseDynamics));
            return;
          }
          double attack = 0.0;
          double release = 0.0;
          double dynamics = 1.0;
          double endDynamics = 1.0;
          NoteDeviation nd = deviation.getNoteDeviation(note);
          if (nd != null) {
            attack += nd.attack();
            release += nd.release();
            dynamics *= nd.dynamics();
            endDynamics *= nd.endDynamics();
          }
          ChordDeviation cd = deviation.getChordDeviation(note);
          if (cd != null) {
            attack += cd.attack();
            release += cd.release();
            dynamics *= cd.dynamics();
            endDynamics *= cd.endDynamics();
          }
          String dynamicsType = "rate";
          if (nd != null)
            dynamicsType = nd.dynamicsType();
          deviatedNotes.add(new DeviatedNote(note, false, track, attack,
              release, dynamics, endDynamics, baseDynamics, dynamicsType));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
      }
    });

    for (Entry<String, Track> e : part2track.entrySet())
      processExtraNotes(deviation, e.getKey(), e.getValue(),
          part2tbd.get(e.getKey()));

    for(int i=0; i<sequence.getTickLength(); i+=TICKS_PER_BEAT)
      if(i > ticks2tempo.lastKey())
        setTempo(i, ticks2tempo.get(ticks2tempo.lastKey()));
    Collections.sort(deviatedNotes);
  }

  private void processNonPartwiseControls(DeviationInstanceWrapper deviation) {
    tempoTrack = sequence.createTrack();
    setTempo(0, TEMPO);
    int[] prevTempo = { TEMPO };
    TreeView<Control> tv = deviation.getNonPartwiseControlView();
    int currentTempo = addTempo(tv.getRoot(), TEMPO, prevTempo);
    while (tv.hasElementsAtNextTime()) {
      currentTempo = addTempo(tv.getFirstElementAtNextTime(), currentTempo,
          prevTempo);
      while (tv.hasMoreElementsAtSameTime())
        currentTempo = addTempo(tv.getNextElementAtSameTime(), currentTempo,
            prevTempo);
    }
  }

  private int addTempo(Control c, int currentTempo, int[] prevTempo) {
    if (c == null)
      return currentTempo;
    int tempo;
    if (c.type().equals("tempo")) {
      currentTempo = (int) c.value();
      tempo = currentTempo;
    } else if (c.type().equals("tempo-deviation")) {
      tempo = (int) (currentTempo * c.value());
      String curve = null;
      if (c.containsAttributeInChild("curve"))
        curve = c.getChildAttribute("curve");
      if (curve != null && curve.equals("linear")) {
        double timeDiv = TICKS_PER_BEAT / (double) linearDivision;
        double tempoDiv = (tempo - prevTempo[0]) / (double) linearDivision;
        for (int i = linearDivision - 1; i >= 1; i--) {
          try {
            setTempo(c.timestamp(TICKS_PER_BEAT) - (int) (timeDiv * i), tempo
                - (int) (tempoDiv * i));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } else
      return currentTempo;
    try {
      setTempo(c.timestamp(TICKS_PER_BEAT), tempo);
    } catch (IOException e) {
      e.printStackTrace();
    }
    prevTempo[0] = tempo;
    return currentTempo;
  }

  public void setTempo(int timeStamp, int tempo) {
    MetaMessage mmessage = new MetaMessage();
    int l = 60 * 1000000 / tempo;
    try {
      mmessage.setMessage(0x51, new byte[] { (byte) (l / 65536),
          (byte) (l % 65536 / 256), (byte) (l % 256) }, 3);
      MidiEvent me = ticks2midievent.get(timeStamp);
      if (me != null)
        tempoTrack.remove(me);
      me = new MidiEvent(mmessage, timeStamp);
      tempoTrack.add(me);
      ticks2tempo.put(timeStamp, tempo);
      ticks2midievent.put(timeStamp, me);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  private void calcMsecs() {
    int msec = 0, prevTicks = 0, prevTempo = 0;
    for (Entry<Integer, Integer> e : ticks2tempo.entrySet()) {
      msec += (e.getKey() - prevTicks) / (double) TICKS_PER_BEAT / prevTempo
          * 60 * 1000;
      ticks2msec.put(e.getKey(), msec);
      prevTicks = e.getKey();
      prevTempo = e.getValue();
    }
  }

  private TreeMap<Integer, Double> getTick2BaseDynamics(
      DeviationInstanceWrapper deviation, String partid) {
    TreeMap<Integer, Double> map = new TreeMap<Integer, Double>();
    TreeView<Control> cv = deviation.getPartwiseControlView(partid);
    cv.getRoot();
    while (cv.hasElementsAtNextTime()) {
      addBaseDynamics(cv.getFirstElementAtNextTime(), map);
      while (cv.hasMoreElementsAtSameTime())
        addBaseDynamics(cv.getNextElementAtSameTime(), map);
    }
    return map;
  }

  private void addBaseDynamics(Control c, TreeMap<Integer, Double> map) {
    if (c == null || !c.type().equals("base-dynamics"))
      return;
    try {
      map.put(c.timestamp(TICKS_PER_BEAT), c.value());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void processExtraNotes(DeviationInstanceWrapper dev, String partid,
      Track track, TreeMap<Integer, Double> tick2basedynamics) {
    TreeView<ExtraNote> tv = dev.getExtraNoteView(partid);
    addExtraNote(tv.getRoot(), partid, track, tick2basedynamics);
    while (tv.hasElementsAtNextTime()) {
      addExtraNote(tv.getFirstElementAtNextTime(), partid, track,
          tick2basedynamics);
      while (tv.hasMoreElementsAtSameTime())
        addExtraNote(tv.getNextElementAtSameTime(), partid, track,
            tick2basedynamics);
    }
  }

  private void addExtraNote(ExtraNote en, String partid, Track track,
      TreeMap<Integer, Double> tick2basedynamics) {
    if (en == null)
      return;
    try {
      int onset = en.timestamp(TICKS_PER_BEAT);
      int offset = onset + (int) (en.duration() * TICKS_PER_BEAT);
      double baseDynamics = 1.0;
      for (Entry<Integer, Double> e : tick2basedynamics.entrySet()) {
        if (e.getKey() > onset)
          break;
        baseDynamics = e.getValue();
      }
      deviatedNotes.add(new DeviatedNote(null, false, track, onset, offset,
          en.notenum(), 0.0, 0.0, en.dynamics(), en.endDynamics(), partid,
          baseDynamics, "rate"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Sequence getSequence() {
    return sequence;
  }

  public MusicXMLWrapper getMusicXML() {
    return musicxml;
  }

  public ArrayList<DeviatedNote> getDeviatedNotes() {
    return deviatedNotes;
  }

  public Map<Integer, Integer> getTicks2Tempo() {
    return ticks2tempo;
  }

  public void addListener(DeviatedNoteUpdateListener listener) {
    listeners.add(listener);
  }

  private void notifyUpdate(DeviatedNote dn) {
    for (DeviatedNoteUpdateListener l : listeners)
      l.noteUpdated(dn);
  }

  /**
   * DeviatedNoteへの変更を加えたDeviationInstanceWrapperを返す．
   * 
   * @return
   * @throws InvalidFileTypeException
   */
  public DeviationInstanceWrapper calcDeviation()
      throws InvalidFileTypeException {
    DeviationDataSet dds = new DeviationDataSet(musicxml);
    double baseTempo = 0.0;
    for (int t : ticks2tempo.values())
      baseTempo += t;
    baseTempo /= ticks2tempo.size();
    dds.addNonPartwiseControl(1, 1.0, "tempo", baseTempo);
    for (Entry<Integer, Integer> e : ticks2tempo.entrySet())
      dds.addNonPartwiseControl(e.getKey() / (TICKS_PER_BEAT * 4) + 1,
          (double) (e.getKey() % (TICKS_PER_BEAT * 4)) / TICKS_PER_BEAT + 1,
          "tempo-deviation", e.getValue() / baseTempo);
    for (DeviatedNote dn : deviatedNotes)
      dn.write(dds);
    return dds.toWrapper();
  }

  public CSVWrapper toTempoBaseCSV(int division) {
    CSVWrapper csv = new CSVWrapper();
    csv.addRow();
    csv.addValue(0, "measure");
    csv.addValue(0, "beat");
    csv.addValue(0, "tempo");

    Iterator<Entry<Integer, Integer>> t2t = ticks2tempo.entrySet().iterator();
    Entry<Integer, Integer> tempoHead = t2t.next();
    int currentTempo = tempoHead.getValue();
    if (t2t.hasNext())
      tempoHead = t2t.next();

    Iterator<DeviatedNote> dn = deviatedNotes.iterator();
    DeviatedNote dnHead = null;
    if (dn.hasNext())
      dnHead = dn.next();

    // Iterator<Entry<Integer, Double>> t2d =
    // getTicks2Dynamics().entrySet().iterator();
    // Entry<Integer, Double> dynamicsHead = t2d.next();
    // double currentDynamics = 0;

    int maxCol = 0;
    for (int i = 0; i < (int) (sequence.getTickLength() / division) + 1; i++) {
      csv.addRow();
      csv.addValue(i + 1, i / 4 + 1 + "");
      csv.addValue(i + 1, i % 4 + 1 + "");
      if (i * division >= tempoHead.getKey() && t2t.hasNext()) {
        tempoHead = t2t.next();
        currentTempo = tempoHead.getValue();
      }
      csv.addValue(i + 1, currentTempo + "");
      int col = 0;
      while (dnHead != null && (i + 1) * division > dnHead.onset(division)) {
        csv.addValue(i + 1, dnHead.onsetOriginal() / (double)division + "");
        csv.addValue(i + 1, dnHead.getAttack() + "");
        csv.addValue(i + 1, dnHead.offsetOriginal() / (double)division + "");
        csv.addValue(i + 1, dnHead.getRelease() + "");
        csv.addValue(i + 1, dnHead.notenum() + "");
        csv.addValue(i + 1, dnHead.velocity() + "");
        if (dn.hasNext())
          dnHead = dn.next();
        else
          dnHead = null;
        col++;
      }
      maxCol = Math.max(col, maxCol);
      // if(i * division >= dynamicsHead.getKey() && t2d.hasNext()) {
      // dynamicsHead = t2d.next();
      // currentDynamics += dynamicsHead.getValue();
      // }
      // csv.addValue(i + 1, currentDynamics + "");
    }
    for(int i=0; i<maxCol; i++) {
      csv.addValue(0, "onset");
      csv.addValue(0, "onset deviation");
      csv.addValue(0, "offset");
      csv.addValue(0, "offset deviation");
      csv.addValue(0, "notenum");
      csv.addValue(0, "velocity");
    }
    return csv;
  }

  public CSVWrapper toSccBaseCSV(int division) {
    CSVWrapper csv = new CSVWrapper();
    csv.addRow();
    csv.addValue(0, "measure");
    csv.addValue(0, "beat");
    csv.addValue(0, "onset");
    csv.addValue(0, "onset deviation");
    csv.addValue(0, "offset");
    csv.addValue(0, "offset deviation");
    csv.addValue(0, "notenum");
    csv.addValue(0, "velocity");
    int i = 1;
    for (DeviatedNote dn : deviatedNotes) {
      csv.addRow();
      int onset = dn.onset();
      csv.addValue(i, (onset / (division * 4) + 1) + "");
      csv.addValue(i, ((onset % (division * 4)) / division + 1) + "");
      csv.addValue(i, dn.onsetOriginal() / (double)division + "");
      csv.addValue(i, dn.getAttack() + "");
      csv.addValue(i, dn.offsetOriginal() / (double)division + "");
      csv.addValue(i, dn.getRelease() + "");
      csv.addValue(i, dn.notenum() + "");
      csv.addValue(i, dn.velocity() + "");
      i++;
    }
    return csv;
  }

  // private TreeMap<Integer, Double> getTicks2Dynamics() {
  // TreeMap<Integer, Double> ticks2dyn = new TreeMap<Integer, Double>();
  // for (DeviatedNote dn : deviatedNotes) {
  // Double dyn = ticks2dyn.get(dn.onset());
  // if (dyn == null)
  // dyn = 0.0;
  // ticks2dyn.put(dn.onset(), dyn + Math.exp(dn.getDynamics() * 100));
  // dyn = ticks2dyn.get(dn.offset());
  // if (dyn == null)
  // dyn = 0.0;
  // ticks2dyn.put(dn.offset(), dyn - Math.exp(dn.getDynamics() * 100));
  // }
  // return ticks2dyn;
  // }

  /**
   * このクラスは一つのノートを表します． noteフィールドが元のMusicXMLのNote一つを表し、 attack, release,
   * dynamics, endDynamicsは それぞれDeviationInstanceの形式で演奏表情を表します．
   * isMissNoteがtrueならミスノートとして扱われ、 noteがnullの場合extra noteとして扱われます．
   * 
   * @author ntotani
   */
  public class DeviatedNote extends MutableNote {

    private MusicXMLWrapper.Note note;
    private double attack;
    private double release;
    private double dynamics;
    private double endDynamics;
    private boolean isMissNote;
    private double baseDynamics;
    private String dynamicsType;
    private String partid;
    private Track track;
    private MidiEvent noteOn;
    private MidiEvent noteOff;

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote,
        Track track, double baseDynamics) throws InvalidMidiDataException {
      this(note, isMissNote, track, 0.0, 0.0, 1.0, 1.0, baseDynamics, "rate");
    }

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote,
        Track track, double attack, double release, double dynamics,
        double endDynamics, double baseDynamics, String dynamicsType)
        throws InvalidMidiDataException {
      this(note, isMissNote, track, note.onset(TICKS_PER_BEAT),
          note.offset(TICKS_PER_BEAT), note.notenum(), attack, release,
          dynamics, endDynamics, null, baseDynamics, dynamicsType);
    }

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote,
        Track track, int onset, int offset, int notenum, double attack,
        double release, double dynamics, double endDynamics, String partid,
        double baseDynamics, String dynamicsType)
        throws InvalidMidiDataException {
      super(onset, offset, notenum, BASE_VELOCITY, BASE_VELOCITY,
          TICKS_PER_BEAT);
      this.note = note;
      this.attack = attack;
      this.release = release;
      this.dynamics = dynamics;
      this.endDynamics = endDynamics;
      this.partid = partid;
      this.baseDynamics = baseDynamics;
      this.dynamicsType = dynamicsType;
      this.track = track;
      ShortMessage on = new ShortMessage();
      on.setMessage(ShortMessage.NOTE_ON, notenum(), velocity());
      noteOn = new MidiEvent(on, onset());
      ShortMessage off = new ShortMessage();
      off.setMessage(ShortMessage.NOTE_OFF, notenum(), offVelocity());
      noteOff = new MidiEvent(off, offset());
      track.add(noteOn);
      track.add(noteOff);
      setMissNote(isMissNote);
    }

    /**
     * 表情付きonsetを返す
     */
    public int onset() {
      return super.onset() + (int) (TICKS_PER_BEAT * attack);
    }

    /**
     * 表情なしonsetを返す．
     */
    public int onsetOriginal() {
      return super.onset();
    }

    /**
     * 表情付きonsetをミリ秒単位で返す
     */
    public int onsetInMSec() {
      return tickInMSec(new OnsetHandler());
    }

    /**
     * 表情なしonsetをミリ秒単位で返す．
     */
    public int onsetOriginalInMSec() {
      return tickInMSec(new OnsetOriginalHandler());
    }

    /**
     * 表情付きoffsetを返す
     */
    public int offset() {
      return super.offset() + (int) (TICKS_PER_BEAT * release);
    }

    /**
     * 表情なしoffsetを返す．
     */
    public int offsetOriginal() {
      return super.offset();
    }

    /**
     * 表情付きoffsetをミリ秒単位で返す
     */
    public int offsetInMSec() {
      return tickInMSec(new OffsetHandler());
    }

    /**
     * 表情なしoffsetをミリ秒単位で返す．
     */
    public int offsetOriginalInMSec() {
      return tickInMSec(new OffsetOriginalHandler());
    }

    private int tickInMSec(TickHandler handler) {
      int prevTicks = 0;
      for (int ticks : ticks2msec.keySet()) {
        prevTicks = ticks;
        if (ticks > handler.tick())
          break;
      }
      return ticks2msec.get(prevTicks)
          + (int) ((handler.tick() - prevTicks) / (double) TICKS_PER_BEAT
              / ticks2tempo.get(prevTicks) * 60 * 1000);
    }

    /**
     * 表情付きvelocityを返す
     */
    public int velocity() {
      if (isMissNote)
        return 0;
      int ret;
      if (dynamicsType.equals("diff"))
        ret = (int) (super.velocity() * (baseDynamics + dynamics));
      else
        ret = (int) (super.velocity() * baseDynamics * dynamics);
      return Math.min(ret, 127);
    }
    
    public double velocity2dynamics(int velocity) {
      if(dynamicsType.equals("diff"))
        return velocity / (double)super.velocity() - baseDynamics;
      return velocity / (super.velocity() * baseDynamics);
    }

    /**
     * 表情付きoffVelocityを返す
     */
    public int offVelocity() {
      int ret;
      if (dynamicsType.equals("diff"))
        ret = (int) (super.offVelocity() * (baseDynamics + dynamics));
      else
        ret = (int) (super.offVelocity() * baseDynamics * dynamics);
      return Math.min(ret, 127);
    }

    public MusicXMLWrapper.Note getNote() {
      return note;
    }

    public boolean isExtraNote() {
      return note == null;
    }

    public double getAttack() {
      return attack;
    }

    public double getRelease() {
      return release;
    }

    public double getDynamics() {
      return dynamics;
    }

    public double getEndDynamics() {
      return endDynamics;
    }

    public boolean getIsMissNote() {
      return isMissNote;
    }

    public void setMissNote(boolean isMissNote) throws InvalidMidiDataException {
      this.isMissNote = isMissNote;
      updateMidiEvent();
      notifyUpdate(this);
    }

    /**
     * このNoteのDeviationを変更する 引数はそれぞれ相対指定
     * 
     * @param attack
     * @param release
     * @throws InvalidMidiDataException
     */
    public boolean changeDeviation(double attack, double release)
        throws InvalidMidiDataException {
      return changeDeviation(attack, release, this.dynamics, this.endDynamics);
    }

    /**
     * このNoteのDeviationを変更する attack, releaseは相対指定 dynamics, endDynamicsは絶対指定
     * 
     * @param attack
     * @param release
     * @param dynamics
     * @param endDynamics
     * @throws InvalidMidiDataException
     */
    public boolean changeDeviation(double attack, double release,
        double dynamics, double endDynamics) throws InvalidMidiDataException {
      this.attack += attack;
      this.release += release;
      if (onset() >= offset() || dynamics < 0 || endDynamics < 0) {
        this.attack -= attack;
        this.release -= release;
        return false;
      }
      this.dynamics = dynamics;
      this.endDynamics = endDynamics;
      updateMidiEvent();
      notifyUpdate(this);
      return true;
    }

    /**
     * このノートのonsetが指定した時刻になるようにattackを変更する．引数は実時刻の絶対位置をしていする．
     * 
     * @param targetMsec
     * @return
     * @throws InvalidMidiDataException
     */
    public boolean changeAttackInMsec(int targetMsec)
        throws InvalidMidiDataException {
      int nearestTick = ticks2msec.firstKey();
      for (Map.Entry<Integer, Integer> e : ticks2msec.entrySet()) {
        if (e.getValue() > targetMsec)
          break;
        nearestTick = e.getKey();
      }
      double beatPerSeconds = ticks2tempo.get(nearestTick) / 60.0;
      double seconds = (targetMsec - ticks2msec.get(nearestTick)) / 1000.0;
      int tickDist = (int) (beatPerSeconds * seconds * TICKS_PER_BEAT);
      int targetTick = nearestTick + tickDist;
      return changeDeviation((double) (targetTick - onset()) / TICKS_PER_BEAT,
          0.0);
    }

    /**
     * このノートのoffsetが指定した時刻になるようにreleaseを変更する．引数は実時刻の絶対位置をしていする．
     * 
     * @param targetMsec
     * @return
     * @throws InvalidMidiDataException
     */
    public boolean changeReleaseInMsec(int targetMsec)
        throws InvalidMidiDataException {
      int nearestTick = ticks2msec.firstKey();
      for (Map.Entry<Integer, Integer> e : ticks2msec.entrySet()) {
        if (e.getValue() > targetMsec)
          break;
        nearestTick = e.getKey();
      }
      double beatPerSeconds = ticks2tempo.get(nearestTick) / 60.0;
      double seconds = (targetMsec - ticks2msec.get(nearestTick)) / 1000.0;
      int tickDist = (int) (beatPerSeconds * seconds * TICKS_PER_BEAT);
      int targetTick = nearestTick + tickDist;
      return changeDeviation(0.0, (double) (targetTick - offset())
          / TICKS_PER_BEAT);
    }

    private void updateMidiEvent() throws InvalidMidiDataException {
      ShortMessage smon = (ShortMessage) noteOn.getMessage();
      smon.setMessage(smon.getStatus(), smon.getData1(), velocity());
      MidiEvent meon = new MidiEvent(smon, onset());
      ShortMessage smoff = (ShortMessage) noteOff.getMessage();
      smoff.setMessage(smoff.getStatus(), smoff.getData1(), offVelocity());
      MidiEvent meoff = new MidiEvent(smoff, offset());
      track.remove(noteOn);
      track.remove(noteOff);
      track.add(meon);
      track.add(meoff);
      noteOn = meon;
      noteOff = meoff;
    }

    /**
     * このノートをDeviationDataSetに書き出す．
     * 
     * @param dds
     */
    public void write(DeviationDataSet dds) {
      if (note == null) {
        if (isMissNote)
          return;
        int measure = onset() / (TICKS_PER_BEAT * 4) + 1;
        double beat = (onset() % (TICKS_PER_BEAT * 4))
            / (double) TICKS_PER_BEAT + 1;
        double duration = (offset() - onset()) / (double) TICKS_PER_BEAT;
        dds.addExtraNote(partid, measure, beat, notenum(), duration, dynamics,
            endDynamics);
      } else if (isMissNote)
        dds.addMissNote(note);
      else if (attack != 0.0 || release != 0.0 || dynamics != 1.0
          || endDynamics != 1.0)
        dds.addNoteDeviation(note, attack, release, dynamics, endDynamics);
    }

    public int compareTo(MutableMusicEvent another) {
      return onset() == another.onset() ? notenum()
          - ((DeviatedNote) another).notenum() : onset() - another.onset();
    }

    private abstract class TickHandler {
      abstract int tick();
    }

    private class OnsetHandler extends TickHandler {
      int tick() {
        return onset();
      }
    }

    private class OnsetOriginalHandler extends TickHandler {
      int tick() {
        return onsetOriginal();
      }
    }

    private class OffsetHandler extends TickHandler {
      int tick() {
        return offset();
      }
    }

    private class OffsetOriginalHandler extends TickHandler {
      int tick() {
        return offsetOriginal();
      }
    }
  }
}
