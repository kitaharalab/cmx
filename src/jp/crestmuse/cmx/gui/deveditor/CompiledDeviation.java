package jp.crestmuse.cmx.gui.deveditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.ChordDeviation;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.Control;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.ExtraNote;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.NoteDeviation;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.MusicData;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;
import jp.crestmuse.cmx.misc.MutableNote;
import jp.crestmuse.cmx.misc.TreeView;

/**
 * このクラスは一つのSequenceと複数のDeviatedNoteを保持し、DeviationEditorで扱う曲の一曲を表します．
 * @author ntotani
 */
public class CompiledDeviation {

  public static int TICKS_PER_BEAT = 480;
  private int BASE_VELOCITY = 100;
  private int TEMPO = 72;
  private Sequence sequence;
  private ArrayList<DeviatedNote> deviatedNotes;
  private TreeMap<Integer, Integer> ticks2tempo;
  private TreeMap<Integer, Integer> ticks2msec;
  private final int linearDivision = 8;
  // TODO PerformanceMatcher緊急デバッグ用
  private HashMap<String, Integer> note2index;

  public CompiledDeviation(final DeviationInstanceWrapper deviation) throws IOException,
      InvalidMidiDataException {
    // TODO PerformanceMatcher緊急デバッグ用
    try {
      note2index = new HashMap<String, Integer>();
      MusicXMLWrapper score = (MusicXMLWrapper)CMXFileWrapper.readfile(deviation.getTargetMusicXMLFileName());
      SCCXMLWrapper scoreSCC = score.makeDeadpanSCCXML(480);
      SCCXMLWrapper.Note[] scoreNotes = scoreSCC.getPartList()[0].getSortedNoteOnlyList(1);
      for(int i=0; i<scoreNotes.length; i++)
        note2index.put(scoreNotes[i].getMusicXMLWrapperNote().toString(), i);
    } catch (TransformerException e1) {
      e1.printStackTrace();
    }

    // TODO MusicXMLにTempoが指定してあった場合それを反映
    // TODO initSilenceに対応
    // TODO deviation読み込みを完全再現
    sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
    deviatedNotes = new ArrayList<DeviatedNote>();
    ticks2tempo = new TreeMap<Integer, Integer>();
    ticks2msec = new TreeMap<Integer, Integer>();

    processNonPartwiseControls(deviation);
    calcMsecs();

    final HashMap<String, Track> part2track = new HashMap<String, Track>();
    final HashMap<String, TreeMap<Integer, Double>> part2tbd = new HashMap<String, TreeMap<Integer,Double>>();
    deviation.getTargetMusicXML().processNotePartwise(new NoteHandlerPartwise(){
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
        if(!(md instanceof MusicXMLWrapper.Note)) return;
        MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
        if (note.rest() || "none".equals(note.notehead()) || note.containsTieType("stop"))
          return;
        double baseDynamics = 1.0;
        for(Entry<Integer, Double> e : tick2basedynamics.entrySet()) {
          if(e.getKey() > note.onset(TICKS_PER_BEAT)) break;
          baseDynamics = e.getValue();
        }
        try {
          if(deviation.getMissNote(note) != null){
            deviatedNotes.add(new DeviatedNote(note, true, track, baseDynamics));
            return;
          }
          double attack = 0.0; double release = 0.0; double dynamics = 1.0; double endDynamics = 1.0;
          NoteDeviation nd = deviation.getNoteDeviation(note);
          if (nd != null) {
            attack += nd.attack();
            release += nd.release();
            dynamics *= nd.dynamics();
            endDynamics *= nd.endDynamics();
          }
          ChordDeviation cd = deviation.getChordDeviation(note);
          if(cd != null){
            attack += cd.attack();
            release += cd.release();
            dynamics *= cd.dynamics();
            endDynamics *= cd.endDynamics();
          }
          String dynamicsType = "rate";
          if(nd != null) dynamicsType = nd.dynamicsType();
          deviatedNotes.add(new DeviatedNote(note, false, track, attack, release, dynamics, endDynamics, baseDynamics, dynamicsType));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
      }
    });

    for(Entry<String, Track> e : part2track.entrySet())
      processExtraNotes(deviation, e.getKey(), e.getValue(), part2tbd.get(e.getKey()));
  }

  private void processNonPartwiseControls(DeviationInstanceWrapper deviation) {
    Track track = sequence.createTrack();
    setTempo(0, track, TEMPO);
    int[] prevTempo = { TEMPO };
    TreeView<Control> tv = deviation.getNonPartwiseControlView();
    int currentTempo = addTempo(tv.getRoot(), track, TEMPO, prevTempo);
    while (tv.hasElementsAtNextTime()) {
      currentTempo = addTempo(tv.getFirstElementAtNextTime(), track, currentTempo, prevTempo);
      while (tv.hasMoreElementsAtSameTime())
        currentTempo = addTempo(tv.getNextElementAtSameTime(), track, currentTempo, prevTempo);
    }
  }

  private int addTempo(Control c, Track track, int currentTempo, int[] prevTempo) {
    if (c == null)
      return currentTempo;
    int tempo;
    if (c.type().equals("tempo")) {
      currentTempo = (int) c.value();
      tempo = currentTempo;
    } else if (c.type().equals("tempo-deviation")) {
      tempo = (int) (currentTempo * c.value());
      String curve = null;
      if(c.containsAttributeInChild("curve"))
        curve = c.getChildAttribute("curve");
      if(curve != null && curve.equals("linear")){
        double timeDiv = TICKS_PER_BEAT / (double)linearDivision;
        double tempoDiv = (tempo - prevTempo[0]) / (double)linearDivision;
        for(int i = linearDivision - 1; i >= 1; i--) {
          try {
            setTempo(c.timestamp(TICKS_PER_BEAT) - (int)(timeDiv * i), track, tempo - (int)(tempoDiv * i));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } else
      return currentTempo;
    try {
      setTempo(c.timestamp(TICKS_PER_BEAT), track, tempo);
    } catch (IOException e) {
      e.printStackTrace();
    }
    prevTempo[0] = tempo;
    return currentTempo;
  }

  private void setTempo(int timeStamp, Track track, int tempo) {
    MetaMessage mmessage = new MetaMessage();
    int l = 60 * 1000000 / tempo;
    try {
      mmessage.setMessage(0x51, new byte[] { (byte) (l / 65536),
          (byte) (l % 65536 / 256), (byte) (l % 256) }, 3);
      track.add(new MidiEvent(mmessage, timeStamp));
      ticks2tempo.put(timeStamp, tempo);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  
  private void calcMsecs(){
    int msec = 0, prevTicks = 0, prevTempo = 0;
    for(Entry<Integer, Integer> e : ticks2tempo.entrySet()){
      msec += (e.getKey() - prevTicks)/(double)TICKS_PER_BEAT/prevTempo*60*1000;
      ticks2msec.put(e.getKey(), msec);
      prevTicks = e.getKey();
      prevTempo = e.getValue();
    }
  }

  private TreeMap<Integer, Double> getTick2BaseDynamics(DeviationInstanceWrapper deviation, String partid) {
    TreeMap<Integer, Double> map = new TreeMap<Integer, Double>();
    TreeView<Control> cv = deviation.getPartwiseControlView(partid);
    cv.getRoot();
    while(cv.hasElementsAtNextTime()) {
      addBaseDynamics(cv.getFirstElementAtNextTime(), map);
      while(cv.hasMoreElementsAtSameTime())
        addBaseDynamics(cv.getNextElementAtSameTime(), map);
    }
    return map;
  }

  private void addBaseDynamics(Control c, TreeMap<Integer, Double> map) {
    if(c == null || !c.type().equals("base-dynamics")) return;
    try {
      map.put(c.timestamp(TICKS_PER_BEAT), c.value());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private void processExtraNotes(DeviationInstanceWrapper dev, String partid, Track track, TreeMap<Integer, Double> tick2basedynamics){
    TreeView<ExtraNote> tv = dev.getExtraNoteView(partid);
    addExtraNote(tv.getRoot(), partid, track, tick2basedynamics);
    while (tv.hasElementsAtNextTime()) {
      addExtraNote(tv.getFirstElementAtNextTime(), partid, track, tick2basedynamics);
      while (tv.hasMoreElementsAtSameTime())
        addExtraNote(tv.getNextElementAtSameTime(), partid, track, tick2basedynamics);
    }
  }
  
  private void addExtraNote(ExtraNote en, String partid, Track track, TreeMap<Integer, Double> tick2basedynamics){
    if(en == null) return;
    try {
      int onset = en.timestamp(TICKS_PER_BEAT);
      int offset = onset + (int) (en.duration() * TICKS_PER_BEAT);
      double baseDynamics = 1.0;
      for(Entry<Integer, Double> e : tick2basedynamics.entrySet()) {
        if(e.getKey() > onset) break;
        baseDynamics = e.getValue();
      }
      deviatedNotes.add(new DeviatedNote(null, false, track, onset, offset, en.notenum(), 0.0, 0.0, en.dynamics(), en.endDynamics(), partid, baseDynamics, "rate"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Sequence getSequence() {
    return sequence;
  }

  public ArrayList<DeviatedNote> getDeviatedNotes() {
    return deviatedNotes;
  }
  
  public Map<Integer, Integer> getTicks2Tempo(){
    return ticks2tempo;
  }
  
  /**
   * DeviatedNoteへの変更を加えたDeviationInstanceWrapperを返す．
   * @return
   * @throws InvalidFileTypeException
   */
  public DeviationInstanceWrapper calcDeviation() throws InvalidFileTypeException{
    DeviationInstanceWrapper deviation = (DeviationInstanceWrapper)CMXFileWrapper.createDocument(DeviationInstanceWrapper.TOP_TAG);
    DeviationDataSet dds = deviation.createDeviationDataSet();
    double baseTempo = 0.0;
    for(int t : ticks2tempo.values()) baseTempo += t;
    baseTempo /= ticks2tempo.size();
    dds.addNonPartwiseControl(1, 1.0, "tempo", baseTempo);
    for(Entry<Integer, Integer> e : ticks2tempo.entrySet())
      dds.addNonPartwiseControl(e.getKey()/(TICKS_PER_BEAT*4) + 1, (double)(e.getKey()%(TICKS_PER_BEAT*4))/TICKS_PER_BEAT + 1, "tempo-deviation", e.getValue()/baseTempo);
    for(DeviatedNote dn : deviatedNotes) dn.write(dds);
    dds.toWrapper();
    return deviation;
  }

  /**
   * このクラスは一つのノートを表します．
   * noteフィールドが元のMusicXMLのNote一つを表し、
   * attack, release, dynamics, endDynamicsは
   * それぞれDeviationInstanceの形式で演奏表情を表します．
   * isMissNoteがtrueならミスノートとして扱われ、
   * noteがnullの場合extra noteとして扱われます．
   * @author ntotani
   */
  public class DeviatedNote extends MutableNote {
    // TODO PerformanceMatcher緊急デバッグ用
    public int getIndex() {
      if(note == null) return -1;
      return note2index.get(note.toString());
    }

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

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote, Track track, double baseDynamics) throws InvalidMidiDataException{
      this(note, isMissNote, track, 0.0, 0.0, 1.0, 1.0, baseDynamics, "rate");
    }

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote, Track track, double attack, double release, double dynamics, double endDynamics, double baseDynamics, String dynamicsType) throws InvalidMidiDataException {
      this(note, isMissNote, track, note.onset(TICKS_PER_BEAT), note.offset(TICKS_PER_BEAT), note.notenum(), attack, release, dynamics, endDynamics, null, baseDynamics, dynamicsType);
    }

    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote, Track track, int onset, int offset, int notenum, double attack, double release, double dynamics, double endDynamics, String partid, double baseDynamics, String dynamicsType) throws InvalidMidiDataException {
      super(onset, offset, notenum, BASE_VELOCITY, BASE_VELOCITY, TICKS_PER_BEAT);
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
      return super.onset() + (int)(TICKS_PER_BEAT*attack);
    }
    
    /**
     * 表情なしonsetを返す．
     */
    public int onsetOriginal(){
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
    public int onsetOriginalInMSec(){
      return tickInMSec(new OnsetOriginalHandler());
    }
    
    /**
     * 表情付きoffsetを返す
     */
    public int offset(){
      return super.offset() + (int)(TICKS_PER_BEAT*release);
    }
    
    /**
     * 表情なしoffsetを返す．
     */
    public int offsetOriginal(){
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
    public int offsetOriginalInMSec(){
      return tickInMSec(new OffsetOriginalHandler());
    }
    
    private int tickInMSec(TickHandler handler){
      int prevTicks = 0;
      for(int ticks : ticks2msec.keySet()){
        prevTicks = ticks;
        if(ticks > handler.tick()) break;
      }
      return ticks2msec.get(prevTicks) + (int)((handler.tick() - prevTicks)/(double)TICKS_PER_BEAT/ticks2tempo.get(prevTicks)*60*1000);
    }
    
    /**
     * 表情付きvelocityを返す
     */
    public int velocity() {
      if(isMissNote) return 0;
      int ret;
      if(dynamicsType.equals("diff"))
        ret = (int)(super.velocity() * (baseDynamics + dynamics));
      else
        ret = (int)(super.velocity() * baseDynamics * dynamics);
      return Math.min(ret, 127);
    }
    
    /**
     * 表情付きoffVelocityを返す
     */
    public int offVelocity() {
      int ret;
      if(dynamicsType.equals("diff"))
        ret = (int)(super.offVelocity() * (baseDynamics + dynamics));
      else
        ret = (int)(super.offVelocity() * baseDynamics * dynamics);
      return Math.min(ret, 127);
    }

    public MusicXMLWrapper.Note getNote(){
      return note;
    }

    public boolean isExtraNote(){
      return note == null;
    }
    
    public double getAttack() { return attack; }
    
    public double getRelease() { return release; }
    
    public double getDynamics() { return dynamics; }
    
    public double getEndDynamics() { return endDynamics; }
    
    public boolean getIsMissNote() { return isMissNote; }

    public void setMissNote(boolean isMissNote) throws InvalidMidiDataException{
      this.isMissNote = isMissNote;
      updateMidiEvent();
    }

    /**
     * このNoteのDeviationを変更する
     * 引数はそれぞれ相対指定
     * @param attack
     * @param release
     * @throws InvalidMidiDataException
     */
    public boolean changeDeviation(double attack, double release) throws InvalidMidiDataException{
      return changeDeviation(attack, release, this.dynamics, this.endDynamics);
    }

    /**
     * このNoteのDeviationを変更する
     * attack, releaseは相対指定
     * dynamics, endDynamicsは絶対指定
     * @param attack
     * @param release
     * @param dynamics
     * @param endDynamics
     * @throws InvalidMidiDataException 
     */
    public boolean changeDeviation(double attack, double release, double dynamics, double endDynamics) throws InvalidMidiDataException{
      this.attack += attack;
      this.release += release;
      if(onset() >= offset() || dynamics < 0 || endDynamics < 0){
        this.attack -= attack;
        this.release -= release;
        return false;
      }
      this.dynamics = dynamics;
      this.endDynamics = endDynamics;
      updateMidiEvent();
      return true;
    }
    
    /**
     * このノートのonsetが指定した時刻になるようにattackを変更する．引数は実時刻の絶対位置をしていする．
     * @param targetMsec
     * @return
     * @throws InvalidMidiDataException
     */
    public boolean changeAttackInMsec(int targetMsec) throws InvalidMidiDataException{
      int nearestTick = ticks2msec.firstKey();
      for(Map.Entry<Integer, Integer> e : ticks2msec.entrySet()){
        if(e.getValue() > targetMsec) break;
        nearestTick = e.getKey();
      }
      double beatPerSeconds = ticks2tempo.get(nearestTick)/60.0;
      double seconds = (targetMsec - ticks2msec.get(nearestTick))/1000.0;
      int tickDist = (int)(beatPerSeconds*seconds*TICKS_PER_BEAT);
      int targetTick = nearestTick + tickDist;
      return changeDeviation((double)(targetTick - onset())/TICKS_PER_BEAT, 0.0);
    }
    
    /**
     * このノートのoffsetが指定した時刻になるようにreleaseを変更する．引数は実時刻の絶対位置をしていする．
     * @param targetMsec
     * @return
     * @throws InvalidMidiDataException
     */
    public boolean changeReleaseInMsec(int targetMsec) throws InvalidMidiDataException{
      int nearestTick = ticks2msec.firstKey();
      for(Map.Entry<Integer, Integer> e : ticks2msec.entrySet()){
        if(e.getValue() > targetMsec) break;
        nearestTick = e.getKey();
      }
      double beatPerSeconds = ticks2tempo.get(nearestTick)/60.0;
      double seconds = (targetMsec - ticks2msec.get(nearestTick))/1000.0;
      int tickDist = (int)(beatPerSeconds*seconds*TICKS_PER_BEAT);
      int targetTick = nearestTick + tickDist;
      return changeDeviation(0.0, (double)(targetTick - offset())/TICKS_PER_BEAT);
    }

    private void updateMidiEvent() throws InvalidMidiDataException{
      ShortMessage smon = (ShortMessage)noteOn.getMessage();
      smon.setMessage(smon.getStatus(), smon.getData1(), velocity());
      MidiEvent meon = new MidiEvent(smon, onset());
      ShortMessage smoff = (ShortMessage)noteOff.getMessage();
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
     * @param dds
     */
    public void write(DeviationDataSet dds){
      if(note == null){
        if(isMissNote) return;
        int measure = onset()/(TICKS_PER_BEAT*4) + 1;
        double beat = (onset()%(TICKS_PER_BEAT*4))/(double)TICKS_PER_BEAT + 1;
        double duration = (offset() - onset())/(double)TICKS_PER_BEAT;
        dds.addExtraNote(partid, measure, beat, notenum(), duration, dynamics, endDynamics);
      }
      else if(isMissNote) dds.addMissNote(note);
      else if(attack!=0.0 || release!=0.0 || dynamics!=1.0 || endDynamics!=1.0)
        dds.addNoteDeviation(note, attack, release, dynamics, endDynamics);
    }
    
    private abstract class TickHandler{
      abstract int tick();
    }
    
    private class OnsetHandler extends TickHandler{
      int tick() { return onset(); }
    }
    
    private class OnsetOriginalHandler extends TickHandler{
      int tick() { return onsetOriginal(); }
    }
    
    private class OffsetHandler extends TickHandler{
      int tick() { return offset(); }
    }
    
    private class OffsetOriginalHandler extends TickHandler{
      int tick() { return offsetOriginal(); }
    }
  }
}
