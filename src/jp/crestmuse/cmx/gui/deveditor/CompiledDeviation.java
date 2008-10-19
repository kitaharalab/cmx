package jp.crestmuse.cmx.gui.deveditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
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

public class CompiledDeviation {
  // TODO Deviation書き出し
  public static int TICKS_PER_BEAT = 480;
  private int BASE_DYNAMICS = 100;
  private int TEMPO = 60;
  private Sequence sequence;
  private ArrayList<DeviatedNote> deviatedNotes;
  private TreeMap<Integer, Integer> ticks2tempo;
  private TreeMap<Integer, Integer> ticks2msec;

  public CompiledDeviation(final DeviationInstanceWrapper deviation) throws IOException,
      InvalidMidiDataException {
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
    deviation.getTargetMusicXML().processNotePartwise(new NoteHandlerPartwise(){
      
      private Track track;

      public void beginPart(Part part, MusicXMLWrapper wrapper) {
        track = sequence.createTrack();
        part2track.put(part.id(), track);
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
        try {
          if(deviation.getMissNote(note) != null){
            deviatedNotes.add(new DeviatedNote(note, true, track));
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
          deviatedNotes.add(new DeviatedNote(note, attack, release, dynamics, endDynamics, false, track));
        } catch (InvalidMidiDataException e) {
          e.printStackTrace();
        }
      }

    });
    
    for(Entry<String, Track> e : part2track.entrySet())
      processExtraNotes(deviation, e.getKey(), e.getValue());
  }

  private void processNonPartwiseControls(DeviationInstanceWrapper deviation) {
    Track track = sequence.createTrack();
    TreeView<Control> tv = deviation.getNonPartwiseControlView();
    int currentTempo = addTempo(tv.getRoot(), track, TEMPO);
    while (tv.hasElementsAtNextTime()) {
      addTempo(tv.getFirstElementAtNextTime(), track, currentTempo);
      while (tv.hasMoreElementsAtSameTime())
        addTempo(tv.getNextElementAtSameTime(), track, currentTempo);
    }
  }

  private int addTempo(Control c, Track track, int currentTempo) {
    if (c == null)
      return currentTempo;
    int tempo;
    if (c.type().equals("tempo")) {
      currentTempo = (int) c.value();
      tempo = currentTempo;
    } else if (c.type().equals("tempo-deviation")) {
      tempo = (int) (currentTempo * c.value());
    } else
      return currentTempo;
    MetaMessage mmessage = new MetaMessage();
    int l = 60 * 1000000 / tempo;
    try {
      // セットテンポイベント
      mmessage.setMessage(0x51, new byte[] { (byte) (l / 65536),
          (byte) (l % 65536 / 256), (byte) (l % 256) }, 3);
      track.add(new MidiEvent(mmessage, c.timestamp(TICKS_PER_BEAT)));
      ticks2tempo.put(c.timestamp(TICKS_PER_BEAT), tempo);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return currentTempo;
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
  
  private void processExtraNotes(DeviationInstanceWrapper dev, String partid, Track track){
    TreeView<ExtraNote> tv = dev.getExtraNoteView(partid);
    addExtraNote(tv.getRoot(), partid, track);
    while (tv.hasElementsAtNextTime()) {
      addExtraNote(tv.getFirstElementAtNextTime(), partid, track);
      while (tv.hasMoreElementsAtSameTime())
        addExtraNote(tv.getNextElementAtSameTime(), partid, track);
    }
  }
  
  private void addExtraNote(ExtraNote en, String partid, Track track){
    if(en == null) return;
    try {
      int onset = en.timestamp(TICKS_PER_BEAT);
      int offset = onset + (int) (en.duration() * TICKS_PER_BEAT);
      deviatedNotes.add(new DeviatedNote(null, onset, offset, en.notenum(), 0.0, 0.0, en.dynamics(), en.endDynamics(), false, partid, track));
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

  public class DeviatedNote extends MutableNote {
    // TODO 挙動テスト
    private MusicXMLWrapper.Note note = null;
    private double attack;
    private double release;
    private double dynamics;
    private double endDynamics;
    private MidiEvent noteOn = null;
    private MidiEvent noteOff = null;
    private boolean isMissNote;
    private String partid = null;
    
    private DeviatedNote(MusicXMLWrapper.Note note, boolean isMissNote, Track track) throws InvalidMidiDataException{
      this(note, 0.0, 0.0, 1.0, 1.0, isMissNote, track);
    }

    private DeviatedNote(MusicXMLWrapper.Note note, double attack, double release, double dynamics, double endDynamics, boolean isMissNote, Track track) throws InvalidMidiDataException {
      this(note, note.onset(TICKS_PER_BEAT), note.offset(TICKS_PER_BEAT), note.notenum(), attack, release, dynamics, endDynamics, isMissNote, null, track);
    }

    private DeviatedNote(MusicXMLWrapper.Note note, int onset, int offset, int notenum, double attack, double release, double dynamics, double endDynamics, boolean isMissNote, String partid, Track track) throws InvalidMidiDataException {
      super(onset, offset, notenum, BASE_DYNAMICS, BASE_DYNAMICS, TICKS_PER_BEAT);
      this.note = note;
      this.attack = attack;
      this.release = release;
      this.dynamics = dynamics;
      this.endDynamics = endDynamics;
      this.partid = partid;
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
     * 表情付きonsetをミリ秒単位で返す
     */
    public int onsetInMSec() {
      int prevTicks = 0;
      for(int ticks : ticks2msec.keySet()){
        if(ticks > onset()) break;
        prevTicks = ticks;
      }
      return ticks2msec.get(prevTicks) + (int)((onset() - prevTicks)/(double)TICKS_PER_BEAT/ticks2tempo.get(prevTicks)*60*1000);
    }
    
    /**
     * 表情付きoffsetを返す
     */
    public int offset(){
      return super.offset() + (int)(TICKS_PER_BEAT*release);
    }
    
    /**
     * 表情付きoffsetをミリ秒単位で返す
     */
    public int offsetInMSec() {
      int prevTicks = 0;
      for(int ticks : ticks2msec.keySet()){
        if(ticks > offset()) break;
        prevTicks = ticks;
      }
      return ticks2msec.get(prevTicks) + (int)((offset() - prevTicks)/(double)TICKS_PER_BEAT/ticks2tempo.get(prevTicks)*60*1000);
    }
    
    /**
     * 表情付きvelocityを返す
     */
    public int velocity() {
      if(isMissNote) return 0;
      return (int)(super.velocity() * dynamics);
    }
    
    /**
     * 表情付きoffVelocityを返す
     */
    public int offVelocity() {
      return (int)(super.offVelocity()*endDynamics);
    }

    public int ticksPerBeat() {
      return super.ticksPerBeat();
    }

    public void setMissNote(boolean isMissNote) throws InvalidMidiDataException{
      this.isMissNote = isMissNote;
      updateMidiEvent();
    }

    /**
     * このNoteのDeviationを変更する。
     * attack, releaseは相対指定
     * dynamics, endDynamicsは絶対指定
     * @param attack
     * @param release
     * @param dynamics
     * @param endDynamics
     * @throws InvalidMidiDataException 
     */
    public void changeDeviation(double attack, double release, double dynamics, double endDynamics) throws InvalidMidiDataException{
      this.attack += attack;
      this.release += release;
      this.dynamics = dynamics;
      this.endDynamics = endDynamics;
      updateMidiEvent();
    }

    private void updateMidiEvent() throws InvalidMidiDataException{
      noteOn.setTick(onset());
      ShortMessage sm = (ShortMessage)noteOn.getMessage();
      sm.setMessage(sm.getStatus(), sm.getData1(), velocity());
      noteOff.setTick(offset());
      sm = (ShortMessage)noteOff.getMessage();
      sm.setMessage(sm.getStatus(), sm.getData1(), offVelocity());
    }
    
    public void write(DeviationDataSet dds){
      if(note == null){
        if(isMissNote) return;
        int measure = onset()/(TICKS_PER_BEAT*4);
        double beat = (onset() - measure)/(double)TICKS_PER_BEAT;
        double duration = (offset() - onset())/(double)TICKS_PER_BEAT;
        dds.addExtraNote(partid, measure, beat, notenum(), duration, dynamics, endDynamics);
      }
      else if(isMissNote) dds.addMissNote(note);
      else if(attack!=0.0 || release!=0.0 || dynamics!=1.0 || endDynamics!=1.0)
        dds.addNoteDeviation(note, attack, release, dynamics, endDynamics);
    }

  }

}
