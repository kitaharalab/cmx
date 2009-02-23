package jp.crestmuse.cmx.filewrappers;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;

import org.xml.sax.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.xml.processors.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.misc.TimeFreqRepresentation.TimeFreqElement;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.HeaderElement;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.Note;
import jp.crestmuse.cmx.handlers.*;

/*******************************************************************************
 * The <tt>DeviationInstanceWrapper</tt> class wraps a DeviationInstance
 * document. <br>
 * <tt>DeviationInstanceWrapper</tt>クラス(仮)は, 表情付けインスタンスを記述するXMLドキュメントをラップします.
 * 
 * @author Tetsuro Kitahara <kitahara@kuis.kyoto-u.ac.jp>
 * @version 0.10.000
 ******************************************************************************/
public class DeviationInstanceWrapper extends CMXFileWrapper {
  /**
   * newOutputData()に指定するトップタグ名．スペルミス防止．
   * 
   * @author Hashida
   * @since 2007.7.30 final 追加
   * @since 2007.7.18
   */

  public static final String TOP_TAG = "deviation";

  private MusicXMLWrapper targetMusicXML = null;
  private String targetMusicXMLFileName = null;
  private String targetMusicXMLDirName = null;
  private static String defaultDirName = ".";
  private TimewiseControlView tctrlview = null;
  private HashMap<String, TimewiseControlView> pctrlviews = new HashMap<String, TimewiseControlView>();
  private HashMap<String, TreeView<ExtraNote>> extraNotes = new HashMap<String, TreeView<ExtraNote>>();

  private Map<MusicXMLWrapper.Note, NoteDeviation> noteDevMap = new HashMap<MusicXMLWrapper.Note, NoteDeviation>();
  private Map<MusicXMLWrapper.Note, ChordDeviation> chordDevMap = new HashMap<MusicXMLWrapper.Note, ChordDeviation>();
  private Map<MusicXMLWrapper.Note, MissNote> missNoteMap = new HashMap<MusicXMLWrapper.Note, MissNote>();

  // private boolean nonPartwiseStarted = false;
  // private boolean partwiseStarted = false;
  // private boolean notewiseStarted = false;

  private int baseVelocity = 100;
  private double baseDynamics = 1.0;

  private boolean alreadyAnalyzed = false;

  public void setBaseDynamics(double dynamics){
    this.baseDynamics = dynamics;
  }
  public double getBaseDynamics(){
    return baseDynamics;
  }
  public void setBaseVelocity(int velocity){
    this.baseVelocity = velocity;
  }
  public int getBaseVelocity(){
    return baseVelocity;
  }
  
  // protected void init() {
  // setTopTagAttributeNS(null, "xmlns:xlink",
  // "http://www.w3.org/1999/xlink");
  // }

  /*****************************************************************************
   * Reads and returns the target MusicXML document. <br>
   * この表情付けインスタンスがターゲットとしているMusicXMLドキュメントを 読み込んで返します.
   ****************************************************************************/
  public MusicXMLWrapper getTargetMusicXML() throws IOException {
    if (targetMusicXML == null) {
      try {
        targetMusicXML = (MusicXMLWrapper) readfile(targetMusicXMLDirName
            + File.separator + getTargetMusicXMLFileName());
      } catch (FileNotFoundException e) {
        targetMusicXML = (MusicXMLWrapper) readfile(defaultDirName
            + File.separator + getTargetMusicXMLFileName());
      }
    }
    return targetMusicXML;
  }

  /*****************************************************************************
   * Reads the file name of the target MusicXML document. <br>
   * この表情付けインスタンスがターゲットとしているMusicXMLドキュメントの ファイル名を返します.
   ****************************************************************************/
  public String getTargetMusicXMLFileName() {
    if (targetMusicXMLFileName == null)
      targetMusicXMLFileName = getTopTagAttribute("target");
    // targetMusicXMLFileName =
    // getDocument().getDocumentElement().getAttribute("target");
    if (targetMusicXMLFileName.contains("/")) {
      int i = targetMusicXMLFileName.lastIndexOf("/");
      targetMusicXMLDirName = targetMusicXMLFileName.substring(0, i);
      targetMusicXMLFileName = targetMusicXMLFileName.substring(i + 1);
    }
    if (targetMusicXMLFileName.contains("\\")) {
      int i = targetMusicXMLFileName.lastIndexOf("\\");
      targetMusicXMLDirName = targetMusicXMLFileName.substring(0, i);
      targetMusicXMLFileName = targetMusicXMLFileName.substring(i + 1);
    }
    return targetMusicXMLFileName;
  }

  public void setTargetMusicXMLFileName(String filename) {
    if (targetMusicXMLFileName == null)
      targetMusicXMLFileName = filename;
    if (targetMusicXMLFileName.contains("/")) {
      int i = targetMusicXMLFileName.lastIndexOf("/");
      targetMusicXMLDirName = targetMusicXMLFileName.substring(0, i);
      targetMusicXMLFileName = targetMusicXMLFileName.substring(i + 1);
    }
    if (targetMusicXMLFileName.contains("\\")) {
      int i = targetMusicXMLFileName.lastIndexOf("\\");
      targetMusicXMLDirName = targetMusicXMLFileName.substring(0, i);
      targetMusicXMLFileName = targetMusicXMLFileName.substring(i + 1);
    }

    if (targetMusicXMLFileName != null && !isFinalized())
      setTopTagAttribute("target", targetMusicXMLFileName);
    // getDocument().getDocumentElement().setAttribute("target",
    // targetMusicXMLFileName);
  }

  public static void changeDefaultMusicXMLDirName(String dirname) {
    defaultDirName = dirname;
  }

  // public void setTargetMusicXMLDirName(String dirname) {
  // targetMusicXMLDirName = dirname;
  // }

  /*****************************************************************************
   * Returns the tick unit.
   ****************************************************************************/
  // public int getTicksPerBeat() {
  // return Integer.parseInt(
  // getDocument().getDocumentElement().getAttribute("tickunit")
  // );
  // }
  /*
   * public void beginNonPartwiseControls() { checkReadOnly();
   * checkElementAddition(!nonPartwiseStarted); addChild("non-partwise");
   * nonPartwiseStarted = true; }
   * 
   * public void endNonPartwiseControls() {
   * checkElementAddition(nonPartwiseStarted); returnToParent();
   * nonPartwiseStarted = false; }
   * 
   * public void beginPartwiseControls() { checkReadOnly();
   * checkElementAddition(!partwiseStarted); addChild("partwise"); }
   * 
   * public void endPartwiseControls() { checkElementAddition(partwiseStarted);
   * returnToParent(); }
   * 
   * public void beginNotewiseDeviations() { checkReadOnly();
   * checkElementAddition(!notewiseStarted); addChild("notewise"); }
   * 
   * public void endNotewiseDeviations() {
   * checkElementAddition(notewiseStarted); returnToParent(); }
   * 
   * void addNoteDeviation(double attack, double release, double dynamics,
   * double endDynamics, MusicXMLWrapper.Note note) {
   * checkElementAddition(notewiseStarted); // under construction }
   */

  public double getInitialSilence() {
    String initSil = getDocument().getDocumentElement().getAttribute(
        "init-silence");
    if (initSil == null || initSil.length() == 0)
      return 0.0;
    else
      return Double.parseDouble(initSil);
  }

  public static DeviationInstanceWrapper createDeviationInstanceFor(
      MusicXMLWrapper musicxml) {
    try {
      DeviationInstanceWrapper dev = (DeviationInstanceWrapper) createDocument(TOP_TAG);
      dev.targetMusicXML = musicxml;
      dev.setTargetMusicXMLFileName(musicxml.getFileName());
      // dev.targetMusicXMLFileName = musicxml.getFileName();
      return dev;
    } catch (InvalidFileTypeException e) {
      throw new ProgramBugException(e.toString());
    }
  }

  /**
   * @deprecated
   * 表情付けされたDeviationInstanceWrapperを生成する時は、以下のようにしてください。
   * DeviationDataSet dds = new DeviationDataSet(musicxml);
   * DeviationInstanceWrapper dev = dds.toWrapper();
   * 
   * (旧来の方法)
   * DeviationInstanceWrapper dev = 
   *  DeviationInstanceWrapper.createDeviationInstanceFor(musicxml);
   * DeviationDataSet dds = dev.createDeviationDataSet();
   * dds.addElementsToWrapper();
   * 
   * DeviationInstanceWrapperをもとに、DeviationDataSetを生成します。
   * @return DeviationDataSet
   */
  public DeviationDataSet createDeviationDataSet() {
    return new DeviationDataSet(this);
  }

  public NoteDeviation getNoteDeviation(MusicXMLWrapper.Note note) {
    if (noteDevMap.containsKey(note)) {
      return noteDevMap.get(note);
    } else {
      Node linkednode = linkmanager.getNodeLinkedTo(note.node(),
          "note-deviation");
      if (linkednode != null) {
        NoteDeviation nd = new NoteDeviation(linkednode);
        noteDevMap.put(note, nd);
        return nd;
      } else {
        return null;
      }
    }
  }

  public ChordDeviation getChordDeviation(MusicXMLWrapper.Note note) {
    if (chordDevMap.containsKey(note)) {
      return chordDevMap.get(note);
    } else {
      Node linkednode = linkmanager.getNodeLinkedTo(note.topNoteOfChord()
          .node(), "chord-deviation");
      if (linkednode != null) {
        ChordDeviation cd = new ChordDeviation(linkednode);
        chordDevMap.put(note, cd);
        return cd;
      } else {
        return null;
      }
    }
  }

  public MissNote getMissNote(MusicXMLWrapper.Note note) {
    if (missNoteMap.containsKey(note)) {
      return missNoteMap.get(note);
    } else {
      Node linkednode = linkmanager.getNodeLinkedTo(note.node(), "miss-note");
      if (linkednode != null) {
        MissNote mn = new MissNote(linkednode);
        missNoteMap.put(note, mn);
        return mn;
      } else {
        return null;
      }
    }
  }

  // public NoteDeviation getNoteDeviationNodeInterface(Node node) {
  // return new NoteDeviation(node);
  // }

  public Control searchNonPartwiseControl(int measure, double beat) {
    return getTimewiseControlView().search(measure, beat);
  }

  public Control searchNonPartwiseControl(int measure, double beat, String type) {
    return getTimewiseControlView().search(measure, beat, type);
  }

  @Override
  protected void analyze() throws IOException {
    try {
//      getTargetMusicXML().analyze();
      addLinks("//note-deviation", getTargetMusicXML());
      addLinks("//chord-deviation", getTargetMusicXML());
      addLinks("//miss-note", getTargetMusicXML());
    } catch (TransformerException e) {
      throw new XMLException(e);
    }
    alreadyAnalyzed = true;
  }

  private void analyzeControls() {
    tctrlview = new TimewiseControlView();
    NodeList nl = selectNodeList("/deviation/non-partwise/measure");
    int size = nl.getLength();
    for (int i = 0; i < size; i++) {
      Node measurenode = nl.item(i);
      int measure = NodeInterface.getAttributeInt(measurenode, "number");
      NodeList nl2 = selectNodeList(measurenode, "control");
      int size2 = nl2.getLength();
      for (int j = 0; j < size2; j++) {
        Control c = new Control(nl2.item(j), measure);
        tctrlview.addControl(c);
      }
    }
  }

  private void analyzePartwiseControls(String partid) {
    TimewiseControlView cv = new TimewiseControlView();
    NodeList nl = selectNodeList("/deviation/partwise/part[@id='" + partid
        + "']/measure");
    int size = nl.getLength();
    for (int i = 0; i < size; i++) {
      Node measurenode = nl.item(i);
      int measure = NodeInterface.getAttributeInt(measurenode, "number");
      NodeList nl2 = selectNodeList(measurenode, "control");
      int size2 = nl2.getLength();
      for (int j = 0; j < size2; j++) {
        Control c = new Control(nl2.item(j), measure);
        cv.addControl(c);
      }
    }
    pctrlviews.put(partid, cv);
  }

  private void analyzeExtraNotes(String partid) {
    TreeView<ExtraNote> treeview = new TreeView<ExtraNote>();
    NodeList nl = selectNodeList("/deviation/extra-notes/part[@id='" + partid
        + "']/measure");
    int size = nl.getLength();
    for (int i = 0; i < size; i++) {
      Node node = nl.item(i);
      int measure = NodeInterface.getAttributeInt(node, "number");
      NodeList nl2 = selectNodeList(node, "extra-note");
      int size2 = nl2.getLength();
      for (int j = 0; j < size2; j++) {
        ExtraNote en = new ExtraNote(nl2.item(j), measure);
        treeview.add(en, "");
      }
    }
    extraNotes.put(partid, treeview);
  }

  private double currentTempo = 120.0;
  private int initticks = 0;

    private boolean requiresTempoDevReturn(Control c1, Control c2, 
					   int ticksPerBeat) 
    throws IOException {
	int tick1 = getTicks(c1.measure(), Math.floor(c1.beat()+1), 
			     ticksPerBeat);
	int tick2 = getTicks(c2.measure(), c2.beat(), ticksPerBeat);
	return tick1 < tick2;
    }

    private int getTicks(int measure, double beat, int ticksPerBeat) 
    throws IOException {
	return getTargetMusicXML().getCumulativeTicks(measure, ticksPerBeat) 
	    + (int)(beat *  ticksPerBeat);
    }
	

  private void controlToSCCHeader(Control c, SCCXMLWrapper dest,
      int ticksPerBeat) throws IOException {
    if (c != null) {
      if (c.type().equals("tempo")) {
        currentTempo = c.value();
        Control nextTempoDev = tctrlview.lookAhead("tempo-deviation");
        if (nextTempoDev == null || c.measure() != nextTempoDev.measure()
            || c.beat() != nextTempoDev.beat()) {
          dest.addHeaderElement(c.timestamp(ticksPerBeat), "TEMPO",
              currentTempo);
        }
      } else if (c.type().equals("tempo-deviation")) {
        dest.addHeaderElement(c.timestamp(ticksPerBeat), "TEMPO", currentTempo
            * c.value()); //TODO curve
        Control nextTempoDev = tctrlview.lookAhead("tempo", "tempo-deviation");
	if (nextTempoDev == null 
	    || requiresTempoDevReturn(c, nextTempoDev, ticksPerBeat)) {
	// if (nextTempoDev == null || nextTempoDev.measure() > c.measure()
	//  || nextTempoDev.beat() > Math.floor(c.beat()) + 1) {
          int cumulativeTicks = getTargetMusicXML().getCumulativeTicks(
              c.measure(), ticksPerBeat);
          int t2 = initticks + cumulativeTicks + ticksPerBeat
              * (int) (Math.floor(c.beat()));
          dest.addHeaderElement(t2, "TEMPO", currentTempo);
        }
      }
    }
  }

  /*
   * private void controlToSCCHeader(Control c, SCCXMLWrapper dest, int
   * ticksPerBeat) throws IOException, ParserConfigurationException,
   * SAXException,TransformerException { if (c != null) { MusicXMLWrapper
   * musicxml = getTargetMusicXML(); int cumulativeTicks =
   * musicxml.getCumulativeTicks(c.measure(), ticksPerBeat); int timestamp =
   * initticks + cumulativeTicks + (int)(ticksPerBeat * (c.beat()-1)); if
   * (c.type().equals("tempo")) { currentTempo = c.value(); Control nextTempoDev =
   * tctrlview.lookAhead("tempo-deviation"); if (nextTempoDev == null ||
   * c.measure() != nextTempoDev.measure() || c.beat() != nextTempoDev.beat())
   * dest.addHeaderElement(timestamp, "TEMPO", currentTempo); } else if
   * (c.type().equals("tempo-deviation")) { dest.addHeaderElement(timestamp,
   * "TEMPO", currentTempo * c.value()); Control nextTempoDev =
   * tctrlview.lookAhead("tempo", "tempo-deviation"); if (nextTempoDev == null ||
   * nextTempoDev.measure() > c.measure() || nextTempoDev.beat() >
   * Math.floor(c.beat()) + 1) { int t2 = initticks + cumulativeTicks +
   * ticksPerBeat * (int)(Math.floor(c.beat())); dest.addHeaderElement(t2,
   * "TEMPO", currentTempo); } } } }
   */

  private void nonPartwiseControlsToSCCHeader(SCCXMLWrapper dest,
      int ticksPerBeat) throws IOException {
    TimewiseControlView tctrlview = getTimewiseControlView();
    dest.beginHeader();
    if (initticks > 0)
      dest.addHeaderElement(0, "TEMPO", 120);
    controlToSCCHeader(tctrlview.getRoot(), dest, ticksPerBeat);
    while (tctrlview.hasElementsAtNextTime()) {
      controlToSCCHeader(tctrlview.getFirstElementAtNextTime(), dest,
          ticksPerBeat);
      while (tctrlview.hasMoreElementsAtSameTime())
        controlToSCCHeader(tctrlview.getNextElementAtSameTime(), dest,
            ticksPerBeat);
    }
    dest.endHeader();
  }

  private void processPartwiseForSCC(String partid, int ticksPerBeat,
      Map<String, NoteListForSCC> partwiseNoteList) throws IOException {
    TreeView<Control> ctrlview = getPartwiseControlView(partid);
    NoteListForSCC notelist = partwiseNoteList.get(partid);
    processControlForSCCNoteList(ctrlview.getRoot(), ticksPerBeat, notelist);
    while (ctrlview.hasElementsAtNextTime()) {
      processControlForSCCNoteList(ctrlview.getFirstElementAtNextTime(),
          ticksPerBeat, notelist);
      while (ctrlview.hasMoreElementsAtSameTime())
        processControlForSCCNoteList(ctrlview.getNextElementAtSameTime(),
            ticksPerBeat, notelist);
    }
  }

  private void processExtraNotesForSCC(String partid, int ticksPerBeat,
      Map<String, NoteListForSCC> partwiseNoteList) throws IOException {
    TreeView<ExtraNote> enview = getExtraNoteView(partid);
    NoteListForSCC notelist = partwiseNoteList.get(partid);
    processExtraNoteForSCCNoteList(enview.getRoot(), ticksPerBeat, notelist);
    while (enview.hasElementsAtNextTime()) {
      processExtraNoteForSCCNoteList(enview.getFirstElementAtNextTime(),
          ticksPerBeat, notelist);
      while (enview.hasMoreElementsAtSameTime())
        processExtraNoteForSCCNoteList(enview.getNextElementAtSameTime(),
            ticksPerBeat, notelist);
    }
  }

  private void processExtraNoteForSCCNoteList(ExtraNote en, int ticksPerBeat,
      NoteListForSCC notelist) throws IOException {
    if (en != null) {
      int onset = en.timestamp(ticksPerBeat);
      int offset = onset + (int) (en.duration() * ticksPerBeat);
      int velocity = (int) (baseVelocity * en.dynamics()); //TODO velocity 式の変更
      int offVelocity = (int) (baseVelocity * en.endDynamics());
      notelist.list.add(new MyNote(onset, offset, en.notenum(), velocity,
          offVelocity, ticksPerBeat, null));
    }
  }

  private void processControlForSCCNoteList(Control c, int ticksPerBeat,
      NoteListForSCC notelist) throws IOException {
    if (c != null) {
      if (c.type().equals("pedal")) {
        String action = c.getChildAttribute("action");
        int depth;
        if (action.equals("on") || action.equals("continue")) {
          if (c.containsAttributeInChild("depth"))
            depth = (int) (127 * c.getChildAttributeDouble("depth"));
          else
            depth = 127;
        } else if (action.equals("off")) {
          depth = 0;
        } else {
          throw new InvalidElementException();
        }
        notelist.list.add(new MutableControlChange(c.timestamp(ticksPerBeat),
            64, depth, ticksPerBeat));
      }
      if(c.type().equals("base-dynamics")){
        this.baseDynamics = Double.valueOf(c.getText());
      }
    }
  }

  private void processNotewiseForSCC(final int ticksPerBeat,
      final Map<String, NoteListForSCC> partwiseNoteList) throws IOException {
    getTargetMusicXML().processNotePartwise(new NoteHandlerPartwise() {
      private int currentPart = 0;
      private NoteListForSCC notelist;

      public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper w) {
        currentPart++;
        notelist = new NoteListForSCC(currentPart, currentPart);
      }

      public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper w) {
        partwiseNoteList.put(part.id(), notelist);
      }

      public void beginMeasure(MusicXMLWrapper.Measure m, MusicXMLWrapper w) {
      }

      public void endMeasure(MusicXMLWrapper.Measure m, MusicXMLWrapper w) {
      }

      public void processMusicData(MusicXMLWrapper.MusicData md,
          MusicXMLWrapper w) {
        if (md instanceof MusicXMLWrapper.Note) {
          MusicXMLWrapper.Note note = (MusicXMLWrapper.Note) md;
          NoteDeviationInterface cd = getChordDeviation(note);
          if (cd == null)
            cd = getDefaultNoteDeviation();
          NoteDeviationInterface nd = getNoteDeviation(note);
          if (nd == null)
            nd = getDefaultNoteDeviation();
          int attack = (int) (ticksPerBeat * (cd.attack() + nd.attack()));
          int release = (int) (ticksPerBeat * (cd.release() + nd.release()));
          int dynamics = (int) (baseVelocity * cd.dynamics() * nd.dynamics()); //TODO base-dynamicseの式
          int endDynamics = (int) (baseVelocity * cd.endDynamics() * nd
              .endDynamics());
          if (!note.rest() && getMissNote(note) == null
              && !"none".equals(note.notehead())
              && !note.containsTieType("stop")) {
            int onset = initticks
            // + note.measure().cumulativeTicks(ticksPerBeat)
                + note.onset(ticksPerBeat);
            int offset = initticks + note.offset(ticksPerBeat);
            // int offset = note.actualDuration(ticksPerBeat) + onset;
            notelist.list.add(new MyNote(onset + attack, offset + release, note
                .notenum(), dynamics, endDynamics, ticksPerBeat, note));
          }
        }
      }
    });
  }

  private void addNoteListToSCC(SCCXMLWrapper dest,
      Map<String, NoteListForSCC> partwiseNoteList) {
    SortedSet<NoteListForSCC> ss = new TreeSet<NoteListForSCC>(
        new Comparator<NoteListForSCC>() {
          public int compare(NoteListForSCC nl1, NoteListForSCC nl2) {
            return nl1.serial - nl2.serial;
          }
        });
    ss.addAll(partwiseNoteList.values());
    for (NoteListForSCC nl : ss) {
      dest.newPart(nl.serial, nl.ch, nl.pn, nl.vol);
      Collections.sort(nl.list);
      for (MutableMusicEvent e : nl.list) {
        if (e instanceof MyNote) {
          MyNote note = (MyNote) e;
          dest.addNoteElement(note.onset(), note.offset(), note.notenum(), note
              .velocity(), note.offVelocity(), note.note);
        } else if (e instanceof MutableControlChange) {
          dest.addControlChange(e.onset(), e.offset(),
              ((MutableControlChange) e).ctrlnum(), ((MutableControlChange) e)
                  .value());
        }
      }
      dest.endPart();
    }
  }

    private void addBarlinesToSCC(SCCXMLWrapper dest, 
				  MusicXMLWrapper musicxml, 
				  int ticksPerBeat) {
	MusicXMLWrapper.Measure[] measurelist = 
	    musicxml.getPartList()[0].getMeasureList();
	dest.beginAnnotations();
	for (MusicXMLWrapper.Measure measure : measurelist) 
	    dest.addBarline(measure.cumulativeTicks(ticksPerBeat), "");
	dest.endAnnotations();
    }

  public void toSCCXML(SCCXMLWrapper dest, final int ticksPerBeat)
      throws IOException {
    MusicXMLWrapper musicxml = getTargetMusicXML();
    if (!alreadyAnalyzed)
      analyze();
    double initSil = getInitialSilence();
    initticks = (int) Math.round(initSil * ticksPerBeat * 2);
    dest.setDivision(ticksPerBeat);
    nonPartwiseControlsToSCCHeader(dest, ticksPerBeat);
    Map<String, NoteListForSCC> partwiseNoteList = new HashMap<String, NoteListForSCC>();
    processNotewiseForSCC(ticksPerBeat, partwiseNoteList);
    Set<String> partlist = partwiseNoteList.keySet();
    for (String partid : partlist) {
      processExtraNotesForSCC(partid, ticksPerBeat, partwiseNoteList);
      processPartwiseForSCC(partid, ticksPerBeat, partwiseNoteList);
    }
    addNoteListToSCC(dest, partwiseNoteList);
    addBarlinesToSCC(dest, musicxml, ticksPerBeat);
    dest.finalizeDocument();
  }

  public SCCXMLWrapper toSCCXML(int ticksPerBeat) throws TransformerException,
      IOException, ParserConfigurationException, SAXException {
    SCCXMLWrapper dest = (SCCXMLWrapper) CMXFileWrapper
        .createDocument(SCCXMLWrapper.TOP_TAG);
    toSCCXML(dest, ticksPerBeat);
    return dest;
  }

  public CSVWrapper toCSV(int divisionPerMeasure, int windowPerMeasure) {
    CSVWrapper result = new CSVWrapper();
    result.addRow();
    result.addValue(0, "tempo");
    result.addValue(0, "velocity");
    result.addValue(0, "velocitySD");
    result.addValue(0, "attackSD");
    result.addValue(0, "releaseSD");
    try {
      int ticksPerBeat = 480;
      SCCXMLWrapper scc = toSCCXML(ticksPerBeat);
      TimeFreqRepresentation tfr = TimeFreqRepresentation
          .getTimeFreqRepresentation(scc, ticksPerBeat, divisionPerMeasure,
              getTargetMusicXML());
      double[] tempos = new double[tfr.length()];
      HeaderElement[] headers = scc.getHeaderElementList();
      for (int i = 0; i < headers.length; i++) {
        if (headers[i].name().equals("TEMPO")) {
          int from = headers[i].time() * divisionPerMeasure / 4 / ticksPerBeat;
          int to;
          if (i < headers.length - 1) {
            to = headers[i + 1].time() * divisionPerMeasure / 4 / ticksPerBeat;
          } else {
            to = tempos.length - 1;
          }
          from = Math.min(from, tempos.length - 1);
          to = Math.min(to, tempos.length - 1);
          for (int j = from; j <= to; j++)
            tempos[j] = Double.parseDouble(headers[i].content());
        }
      }
      for (int i = 0; i < tfr.length(); i++) {
        result.addRow();
        result.addValue(i + 1, tempos[i] + "");
        double velocity = 0.;
        LinkedList<NoteDeviation> notedeviations = new LinkedList<NoteDeviation>();
        double dynamicsAve = 0.;
        double attackAve = 0.;
        double releaseAve = 0.;
        for (int j = i; j < i + divisionPerMeasure / windowPerMeasure; j++) {
          if (j >= tfr.length())
            break;
          for (byte b : tfr.get(j).values())
            if (b > 0)
              velocity += Math.exp(b);
          for (NoteCompatible n : tfr.get(j).data()) {
            try {
              NoteDeviation nd = getNoteDeviation(((Note) n)
                  .getMusicXMLWrapperNote());
              dynamicsAve += nd.dynamics();
              attackAve += nd.attack();
              releaseAve += nd.release();
              notedeviations.add(nd);
            } catch (NullPointerException e) {}
          }
        }
        dynamicsAve /= notedeviations.size();
        attackAve /= notedeviations.size();
        releaseAve /= notedeviations.size();
        if (velocity > 0)
          velocity = Math.log(velocity);
        result.addValue(i + 1, velocity + "");
        double velocitySD = 0.;
        double attackSD = 0.;
        double releaseSD = 0.;
        if (notedeviations.size() > 0) {
          for (NoteDeviation nd : notedeviations) {
            velocitySD += Math.pow(
                (nd.dynamics() - dynamicsAve) * baseVelocity, 2);
            attackSD += Math.pow(nd.attack() - attackAve, 2);
            releaseSD += Math.pow(nd.release() - releaseAve, 2);
          }
          velocitySD /= notedeviations.size();
          attackSD /= notedeviations.size();
          releaseSD /= notedeviations.size();
        } else
          velocitySD = attackSD = releaseSD = Double.NaN;
        result.addValue(i + 1, velocitySD + "");
        result.addValue(i + 1, attackSD + "");
        result.addValue(i + 1, releaseSD + "");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private class NoteListForSCC {
    private int serial;
    private int ch;
    private int pn;
    private int vol;
    private static final int DEFAULT_PROG_NUM = 0;
    private static final int DEFAULT_VOLUME = 100;
    private List<MutableMusicEvent> list;

    // private SortedSet<MutableMusicEvent> set;
    private NoteListForSCC(int serial, int ch, int pn, int vol) {
      this.serial = serial;
      this.ch = ch;
      this.pn = pn;
      this.vol = vol;
      list = new ArrayList<MutableMusicEvent>();
      // set = new TreeSet<MutableMusicEvent>();
    }

    private NoteListForSCC(int serial, int ch) {
      this(serial, ch, DEFAULT_PROG_NUM, DEFAULT_VOLUME);
    }
  }

  private class MyNote extends MutableNote {
    private MusicXMLWrapper.Note note;

    private MyNote(int onset, int offset, int notenum, int velocity,
        int offVelocity, int ticksPerBeat, MusicXMLWrapper.Note note) {
      super(onset, offset, notenum, velocity, offVelocity, ticksPerBeat);
      this.note = note;
    }
  }

  /*
   * private class MyControlChange extends MutableNote { int ctrlnum; int value;
   * private MyControlChange(int timestamp, int ctrlnum, int value) {
   * super(timestamp, timestamp, -1, -1); this.ctrlnum = ctrlnum; this.value =
   * value; } }
   */

  /*
   * public void toSCCXML(final SCCXMLWrapper dest, final int ticksPerBeat)
   * throws TransformerException, IOException, ParserConfigurationException,
   * SAXException { MusicXMLWrapper musicxml = getTargetMusicXML();
   * addLinks("//note-deviation", musicxml); double initSil =
   * getInitialSilence(); initticks = (int)Math.round(initSil * ticksPerBeat *
   * 2); TimewiseControlView ctrlview = getTimewiseControlView();
   * dest.setDivision(ticksPerBeat); dest.beginHeader(); if (initticks > 0)
   * dest.addHeaderElement(0, "TEMPO", 120);
   * controlToSCCHeader(ctrlview.getRoot(), dest, ticksPerBeat); while
   * (ctrlview.hasElementsAtNextTime()) {
   * controlToSCCHeader(ctrlview.getFirstElementAtNextTime(), dest,
   * ticksPerBeat); while (ctrlview.hasMoreElementsAtSameTime())
   * controlToSCCHeader(ctrlview.getNextElementAtSameTime(), dest,
   * ticksPerBeat); } dest.endHeader(); musicxml.processNotePartwise(new
   * NoteHandlerPartwise() { // private int divisions = 1; private int
   * currentPart = 0; // private int currentTick = 0; // private int prevTick =
   * 0; public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper w) {
   * currentPart++; // currentTick = 0; dest.newPart(currentPart, currentPart,
   * 0, 100); } public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper
   * w) { dest.endPart(); } public void beginMeasure(MusicXMLWrapper.Measure
   * measure, MusicXMLWrapper w) { // measure.calcOnsets(ticksPerBeat); } public
   * void endMeasure(MusicXMLWrapper.Measure measure, MusicXMLWrapper w) { }
   * public void processMusicData(MusicXMLWrapper.MusicData md, MusicXMLWrapper
   * w) { // if (md instanceof MusicXMLWrapper.Attributes) { //
   * MusicXMLWrapper.Attributes attr = // (MusicXMLWrapper.Attributes)md; // if
   * (attr.hasChild("divisions")) // divisions =
   * attr.getChildTextInt("divisions"); //// divisions =
   * ((MusicXMLWrapper.Attributes)md).divisions(); // } else if (md instanceof
   * MusicXMLWrapper.Note) { MusicXMLWrapper.Note note =
   * (MusicXMLWrapper.Note)md; // Node linkednode = //
   * w.linkmanager.getNodeLinkedTo(note.node(), "note-deviation"); // int attack =
   * 0; // int release = 0; // int dynamics = 100; // if (linkednode != null) { //
   * NoteDeviation nd = new NoteDeviation(linkednode); NoteDeviationInterface cd =
   * getChordDeviation(note); if (cd == null) cd = getDefaultNoteDeviation();
   * NoteDeviationInterface nd = getNoteDeviation(note); if (nd == null) nd =
   * getDefaultNoteDeviation(); int attack = (int)(ticksPerBeat * (cd.attack() +
   * nd.attack())); int release = (int)(ticksPerBeat * (cd.release() +
   * nd.release())); int dynamics = (int)(baseDynamics * cd.dynamics() *
   * nd.dynamics()); // if (note.chord()) // currentTick = prevTick; // int
   * onset = currentTick + attack; // prevTick = currentTick; // currentTick +=
   * note.actualDuration(ticksPerBeat); //// currentTick += note.duration() *
   * ticksPerBeat / divisions; // int offset = currentTick + release; if
   * (!note.rest() && getMissNote(note) == null) { int onset =
   * note.measure().cumulativeTicks(ticksPerBeat) + note.onset(ticksPerBeat);
   * int offset = note.actualDuration(ticksPerBeat) + onset;
   * dest.addNoteElement(onset + attack, offset + release, note.notenum(),
   * dynamics, note); // dest.addNoteElement(onset, offset, notenum, dynamics); } // }
   * else if (md instanceof MusicXMLWrapper.Backup) { // currentTick -= //
   * ((MusicXMLWrapper.Backup)md).actualDuration(ticksPerBeat); //// currentTick -=
   * ((MusicXMLWrapper.Backup)md).duration() //// * ticksPerBeat / divisions; } }
   * }); dest.finalizeDocument(); }
   */

  public TreeView<Control> getNonPartwiseControlView() {
    return getTimewiseControlView();
  }
  
  
  /**
   * DeviationInstanceのnon-partwise(tempo, tempo-deviation)
   * を時系列順にリストで返します
   * @param dev
   * @author R.Tokuami
   */
  public LinkedList<Control> getNonPartwiseList(DeviationInstanceWrapper dev){
    LinkedList<Control> list = new LinkedList<Control>();
    TimewiseControlView tcv = dev.getTimewiseControlView();
    
    tcv.getRoot();
    while (tcv.hasElementsAtNextTime()) {
      list.add(tcv.getFirstElementAtNextTime());
      while (tcv.hasMoreElementsAtSameTime())
        list.add(tcv.getNextElementAtSameTime());
    }
    
    return list;
  }
  
  /**
   * DeviationInstanceのExtraNotesをリストで取得します
   * @param dev 対象とするDeviationInstanceWrapper
   * @param partID パートID
   * @author R.Tokuami
   * @return
   */
  public LinkedList<ExtraNote> getExtraNotesList(DeviationInstanceWrapper dev, String partID){
	  LinkedList<ExtraNote> list = new LinkedList<ExtraNote>();
	  TreeView<ExtraNote> tv = dev.getExtraNoteView(partID);
	  
	  tv.getRoot();
	  while(tv.hasElementsAtNextTime()){
		  list.add(tv.getFirstElementAtNextTime());
		  while(tv.hasMoreElementsAtSameTime())
			  list.add(tv.getNextElementAtSameTime());
	  }
	  
	  return list;
  }
  

  private TimewiseControlView getTimewiseControlView() {
    if (tctrlview == null)
      analyzeControls();
    return tctrlview;
  }

  public TreeView<Control> getPartwiseControlView(String partid) {
    if (!pctrlviews.containsKey(partid))
      analyzePartwiseControls(partid);
    return pctrlviews.get(partid);
  }

  public TreeView<ExtraNote> getExtraNoteView(String partid) {
    if (!extraNotes.containsKey(partid))
      analyzeExtraNotes(partid);
    return extraNotes.get(partid);
  }

  private class TimewiseControlView extends TreeView<Control> {
    // private AsymmetricBinaryTree<Control> tree;
    // private TimewiseControlView() {
    // tree = new AsymmetricBinaryTree<Control>();
    // }
    private void addControl(Control c) { // 暫定
      add(c, "");
    }

    // public final AsymmetricBinaryTree<Control> getTree() {
    // tree.forbidAddition();
    // return tree;
    // }
    /*
     * public final Control getFirstControl() { return tree.getRoot(); } public
     * final Control getControlAt(int measure, double meter) { return
     * tree.get(measure, (int)((double)MusicXMLWrapper.INTERNAL_TICKS_PER_BEAT *
     * meter)); } public final boolean hasMoreControlsAtSameTime() { return
     * tree.hasNextL(); } public final Control getNextControlAtSameTime() {
     * return tree.nextL(); } public final boolean hasControlsAtNextTime() {
     * return tree.hasNextR(); } public final Control
     * getFirstControlAtNextTime() { return tree.nextR(); }
     */
    private Control lookAhead(final String type) {
      return lookAhead(new NodeSearchFilter<Control>() {
        public boolean accept(Control c) {
          return c != null && c.type().equals(type);
        }
      });
    }

    private Control lookAhead(final String... types) {
      return lookAhead(new NodeSearchFilter<Control>() {
        public boolean accept(Control c) {
          for (String type : types)
            if (c != null && c.type().equals(type))
              return true;
          return false;
        }
      });
    }

    private Control search(int measure, double beat) {
      return search(measure,
          (int) (MusicXMLWrapper.INTERNAL_TICKS_PER_BEAT * beat));
    }

    private Control search(int measure, double beat, final String type) {
      return search(measure,
          (int) (MusicXMLWrapper.INTERNAL_TICKS_PER_BEAT * beat),
          new NodeSearchFilter<Control>() {
            public boolean accept(Control c) {
              return c != null && c.type().equals(type);
            }
          });
    }
  }

  public class Control extends NodeInterface implements Ordered {
    private int measure;
    private double beat;
    private Node child;

    private Control(Node node, int measure) {
      super(node);
      this.measure = measure;
      beat = getAttributeDouble(node(), "beat");
      child = node().getFirstChild();
    }

    @Override
    protected final String getSupportedNodeName() {
      return "control";
    }

    public final int measure() {
      return measure;
    }

    public final double beat() {
      return beat;
    }

    public final String type() {
      return child.getNodeName();
    }

    public final double value() {
      return getTextDouble(child);
    }

    public final int ordinal() {
      return measure;
    }

    public final int subordinal() {
      return (int) (1920.0 * beat);
    }

    @Override
    public final String toString() {
      return "Control (measure: " + measure + ", beat: " + beat + ", type: "
          + type() + (child.hasChildNodes() ? ", value: " + value() : "") + ")";
    }

    public final int timestamp(int ticksPerBeat) throws IOException {
      return initticks
          + getTargetMusicXML().getCumulativeTicks(measure, ticksPerBeat)
          + (int) Math.round(ticksPerBeat * (beat - 1));
    }

    public final String getChildAttribute(String key) {
      return getAttribute(child, key);
    }

    public final int getChildAttributeInt(String key) {
      return getAttributeInt(child, key);
    }

    public final double getChildAttributeDouble(String key) {
      return getAttributeDouble(child, key);
    }

    public final boolean containsAttributeInChild(String key) {
      return hasAttribute(child, key);
    }
  }

  public class ExtraNote extends NodeInterface implements Ordered {
    private int measure;
    private double beat;
    private String pitchStep;
    private int pitchAlter;
    private int pitchOctave;
    private int notenum = -1;
    private double duration;
    private double dynamics;
    private String dynamicsType = null;
    private double endDynamics;

    private ExtraNote(Node node, int measure) {
      super(node);
      this.measure = measure;
      beat = getAttributeDouble(node(), "beat");
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      for (int i = 0; i < size; i++) {
        Node node1 = nl.item(i);
        String nodename = node1.getNodeName();
        String value = getText(node1);
        if (nodename.equals("pitch"))
          analyzePitch(node1);
        else if (nodename.equals("duration"))
          duration = Double.parseDouble(value);
        else if (nodename.equals("dynamics")){
          dynamics = Double.parseDouble(value);
          Node typeNode = node1.getAttributes().getNamedItem("type");
          if(typeNode == null) dynamicsType = "rate";
          else if(typeNode.getNodeValue().equals("rate")) dynamicsType = "rate";
          else if(typeNode.getNodeValue().equals("diff")) dynamicsType = "diff";
          else{
            dynamicsType = "rate";
            System.err.println("warning: unsupported type");
          }
        }
        else if (nodename.equals("end-dynamics"))
          endDynamics = Double.parseDouble(value);
      }
    }

    private void analyzePitch(Node node) {
      NodeList nl = node.getChildNodes();
      int size = nl.getLength();
      for (int i = 0; i < size; i++) {
        Node n = nl.item(i);
        String nodename = n.getNodeName();
        String value = getText(n);
        if (nodename.equals("step"))
          pitchStep = value;
        else if (nodename.equals("octave"))
          pitchOctave = Integer.parseInt(value);
        else if (nodename.equals("alter"))
          pitchAlter = Integer.parseInt(value);
      }
    }

    protected final String getSupportedNodeName() {
      return "extra-note";
    }
    
    private String dynamicsType(){
      return dynamicsType;
    }

    /**
     * ExtraNoteが位置する小節を返します
     * @author R.Tokuami
     * @return 小節番号
     */
    public final int measure(){
    	return measure;
    }
    
    public final double beat() {
      return beat;
    }

    public final String pitchStep() {
      return pitchStep;
    }

    public final int pitchAlter() {
      return pitchAlter;
    }

    public final int pitchOctave() {
      return pitchOctave;
    }

    /***************************************************************************
     * <p>
     * 音高をノートナンバー形式で返します.
     * </p>
     **************************************************************************/
    public int notenum() { // kari
      if (notenum < 0) {
        int stepInt;
        if (pitchStep.equalsIgnoreCase("C"))
          stepInt = 0;
        else if (pitchStep.equalsIgnoreCase("D"))
          stepInt = 2;
        else if (pitchStep.equalsIgnoreCase("E"))
          stepInt = 4;
        else if (pitchStep.equalsIgnoreCase("F"))
          stepInt = 5;
        else if (pitchStep.equalsIgnoreCase("G"))
          stepInt = 7;
        else if (pitchStep.equalsIgnoreCase("A"))
          stepInt = 9;
        else if (pitchStep.equalsIgnoreCase("B"))
          stepInt = 11;
        else
          throw new InvalidElementException("Pitch is wrong.");
        notenum = stepInt + (pitchOctave + 1) * 12 + pitchAlter;
      }
      return notenum;
    }

    public final double duration() {
      return duration;
    }

    public final double dynamics() {
      return dynamics;
    }

    public final double endDynamics() {
      return endDynamics;
    }

    public final int ordinal() {
      return measure;
    }

    public final int subordinal() {
      return (int) (1920.0 * beat);
    }

    public final int timestamp(int ticksPerBeat) throws IOException {
      if (measure < 0)
        return (int) Math.round(ticksPerBeat * (beat - 1));
      else
        return initticks
            + getTargetMusicXML().getCumulativeTicks(measure, ticksPerBeat)
            + (int) Math.round(ticksPerBeat * (beat - 1));
    }
  }

  private interface NoteDeviationInterface {
    public double attack();

    public double release();

    public double dynamics();

    public double endDynamics();
  }

  private static NoteDeviationInterface defaultND = new DefaultNoteDeviation();

  private static NoteDeviationInterface getDefaultNoteDeviation() {
    return defaultND;
  }

  private static class DefaultNoteDeviation implements NoteDeviationInterface {
    private DefaultNoteDeviation() {
    }

    public final double attack() {
      return 0.0;
    }

    public final double release() {
      return 0.0;
    }

    public final double dynamics() {
      return 1.0;
    }

    public final double endDynamics() {
      return 1.0;
    }
  }

  public class NoteDeviation extends NodeInterface implements
      NoteDeviationInterface {
    private double attack, release, dynamics, endDynamics;
    private String dynamicsType;
    private NodeList note = null;

    private NoteDeviation(Node node) {
      super(node);
      attack = getTextDouble(getChildByTagName("attack"));
      release = getTextDouble(getChildByTagName("release"));
      Node dyn = getChildByTagName("dynamics");
      dynamics = getTextDouble(dyn);
      endDynamics = getTextDouble(getChildByTagName("end-dynamics"));
      Node typeNode = dyn.getAttributes().getNamedItem("type");
      if(typeNode == null) dynamicsType = "rate";
      else if(typeNode.getNodeValue().equals("rate")) dynamicsType = "rate";
      else if(typeNode.getNodeValue().equals("diff")) dynamicsType = "diff";
      else{
        dynamicsType = "rate";
        System.err.println("warning: unsupported type");
      }
    }

    @Override
    protected String getSupportedNodeName() {
      return "note-deviation";
    }

    // public NodeList note() throws IOException, TransformerException,
    // ParserConfigurationException, SAXException {
    // if (note == null)
    // note = SimplifiedXPointerProcessor.getRemoteResource
    // (node(), getTargetMusicXML().getDocument());
    // return note;
    // }

    public final double attack() {
      
      return attack;
      
      
    }

    public final double release() {
      return release;
    }

    public final double dynamics() {
      return dynamics;
    }

    public final double endDynamics() {
      return endDynamics;
    }
    
    public final String dynamicsType() {
      return dynamicsType;
    }
  }

  public class ChordDeviation extends NoteDeviation {
    private ChordDeviation(Node node) {
      super(node);
    }

    @Override
    protected String getSupportedNodeName() {
      return "chord-deviation";
    }
  }

  public class MissNote extends NodeInterface {
    private MissNote(Node node) {
      super(node);
    }

    @Override
    protected String getSupportedNodeName() {
      return "miss-note";
    }
  }

}
