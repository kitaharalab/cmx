package jp.crestmuse.cmx.filewrappers;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

/**********************************************************************
 *<p>The <tt>MusicXMLWrapper</tt> class wraps a MusicXML document.</p>
 *
 *<p><tt>MusicXMLWrapper</tt>クラスは, MusicXMLドキュメントをラップします.</p>
 *
 *<p>MusicXMLについては, 
 *<a href="http://ja.wikipedia.org/wiki/MusicXML">ここ</a>や
 *<a href="http://www.recordare.com/xml.html">ここ</a>などを参照してください.
 *本クラスでは, PartwiseとTimewiseのうち, Partwiseのみをサポートします. 
 *また, ここの要素から情報を取り出すNodeInterfaceについては, 
 *MusicXMLのすべての要素に対応しているわけではありません. 
 *</p>
 *
 *<p>
 *MusicXMLWrapperオブジェクトからデータを取り出すためには, 主に2つの方法が
 *あります. 
 *1つは, getPartListメソッドを用いる方法です. 
 *これを用いると, Partクラスの配列が得られます. 
 *Partクラスはpart要素から情報を取り出すためのクラスです. 
 *その後, 各Partオブジェクトに対してgetMeasureListメソッドを呼び出します. 
 *そうすると, 当該part要素内のmeasure要素をラップするMeasureオブジェクトの
 *配列が得られます. 
 *同様に, 各Measureオブジェクトに対してgetMusicDataListメソッドを呼び出して
 *当該measure要素内の要素を得ます. 
 *getMusicDataListメソッドで返されるのはMusicDataクラスの配列ですが, 
 *実際は, その要素に合わせてNoteクラスだったりAttributeクラスだったり
 *ForwardクラスだったりBackupクラスだったりしますので, 
 *instanceof演算子でどのクラスのインスタンスなのかを確かめた上で
 *ダウンキャストしてすべき処理を行います. 
 *もう1つの方法は, processNotePartwiseメソッドを用いる方法です. 
 *NoteHandlerPartwiseインターフェイスを実装したクラスに
 *自分のしたい処理を記述して, processNotePartwiseメソッドの引数に渡すと, 
 *上記の処理を自動的に行ってくれます. 
 *</p>
 *
 *<h4>PartwiseNoteViewとTimewiseNoteView</h4>
 *<p>
 *MusicXMLWrapperクラスでは, 各音符(Noteオブジェクト)を二分木に納めたTreeViewを
 *提供します. 
 *TreeViewで提供されている各種メソッドを利用することで, 
 *指定した音(Noteオブジェクト)の次の音や同時になる他の音を探索することが可能です. 
 *現在のバージョンでは, TreeViewは2種類提供されます. 
 *1つがPartwiseTreeViewで, パートごとに別々の二分木を構成したものです. 
 *このTreeViewでは, backup要素を使って声部をわけてある場合に, 
 *枝わかれするように要素が追加されていきます. 
 *これは, ある音に対して同一パート(あるいは声部内)で次の音や前の音をたどるのに
 *便利です. 
 *もう1つがTimewiseTreeViewで, すべてのパートの音を1つの二分木にまとめて
 *格納したものです. 
 *これは, ある音と同時に鳴る音をパートや声部を越えてすべて洗い出すといった場面で
 *便利です. 
 *また, スラーを自動的に検出し, スラーに続する音の列もTreeViewで管理します. 
 *</p>
 *
 *<h4>スラーの管理</h4>
 *<p>
 *スラーもTreeViewを利用して管理します. 
 *スラーごとにTreeViewオブジェクトが生成され, 
 *たとえば指定した音からはじまるスラーに対応するTreeViewのリストを取得するといった
 *ことが簡単にできます. 
 *ただし, 現在のバージョンでは声部にまたがるスラーは対応しておらず, 
 *無視されます. 
 *</p>
 *
 *@author Tetsuro Kitahara (t.kitahara@ksc.kwansei.ac.jp)
 *@version 0.21
 *********************************************************************/
public class MusicXMLWrapper extends CMXFileWrapper implements PianoRollCompatible {

  //  public static final enum Dynamics {p, pp, ppp, pppp, ppppp, pppppp, 
  //      f, ff, fff, ffff, fffff, ffffff, mp, mf, sf, sfp, sfpp, 
  //      fp, rf, rfz, sfz, sffz, fz, other};

  private List<TreeView<Note>> partwiseNoteView = null;
  private TreeView<Note> timewiseNoteView = null;
  private SlurredNoteViewList slurredNoteView = null;
  private Part[] partlist = null;
  // changed Integer -> Long 20080609
  private List<Long> cumulativeTicksList = new MyArrayList(); 
  private List<Long> cumulativeTicksList2 = new MyArrayList();

  private String movementTitle = null;
  private boolean startsWithZerothMeasure;
  private boolean zerothMeasureChecked = false;

  static final int INTERNAL_TICKS_PER_BEAT = 10080;
  //  static final int INTERNAL_TICKS_PER_BEAT = 1920;

  //  private DeviationInstanceWrapper dev;

  // changed Integer -> Long 20080609
  private class MyArrayList extends ArrayList<Long> {
    public Long set(int index, Long value) {
      if (index > size()) {
        add(null);
        set(index, value);
        return null;
      } else if (index == size()) {
        add(value);
        return null;
      } else {
        return super.set(index, value);
      }
    }
  }


  /**********************************************************************
   *<p>Repeats the same process for each of music data in the 
   *MusicXML document based on the specified handler.</p>
   *
   *<p>
   *指定されたハンドラに基づいて, MusicXMLドキュメントに含まれる各音楽要素に対して
   *同じ処理を繰り返します. このメソッドは基本的には以下の処理と等価です.
   *<pre>
   *for (Part part : getPartList()) {
   *  handler.beginPart(part, this);
   *  for (Measure measure : part.getMeasureList()) {
   *    handler.beginMeasure(measure, this);
   *    for (MusicData md : measure.getMusicDataList())
   *      handler.processMusicData(md, this);
   *    handler.endMeasure(measure, this);
   *  }
   *  handler.endPart(part, this);
   *}
   *</pre>
   *********************************************************************/
  public void processNotePartwise(NoteHandlerPartwise handler) {
    Part[] partlist = getPartList();					
    for (Part part : partlist) {
      handler.beginPart(part, this);
      Measure[] measurelist = part.getMeasureList();
      for (Measure measure : measurelist) {
        handler.beginMeasure(measure, this);
        MusicData[] mdlist = measure.getMusicDataList();
        //	int noteindex = 0;
        for (MusicData md : mdlist) {
          //          if (md instanceof Note)
          //            ((Note)md).xpath = measure.getXPathExpression() + 
          //              "/note[" + (++noteindex) + "]";
          handler.processMusicData(md, this);
        }
        handler.endMeasure(measure, this);
      }
      handler.endPart(part, this);
    }
  }

  public void processNotes(CommonNoteHandler h) {
    Part[] partlist = getPartList();
    for (Part part : partlist) {
      h.beginPart(part.id(), this);
      Measure[] measurelist = part.getMeasureList();
      for (Measure measure : measurelist) {
        MusicData[] mdlist = measure.getMusicDataList();
        for (MusicData md : mdlist) 
          if (md instanceof Note) {
            Note note = (Note)md;
            if (!note.rest())
              h.processNote(note, this);
          }
      }
      h.endPart(part.id(), this);
    }
  }

  //  void setDeviationInstance(DeviationInstanceWrapper dev) {
  //    this.dev = dev;
  //  }

  /**********************************************************************
   *<p>Returns the array of the parts contained in the MusicXML document.</p>
   *<p>MusicXMLドキュメントに含まれるパート(Partオブジェクト)の配列を返します.</p>
   *********************************************************************/
  public Part[] getPartList() {
    checkFinalized();
    if (partlist == null) {
      Node info = selectSingleNode("/score-partwise/part-list");
      NodeList parts = selectNodeList("/score-partwise/part");
      int size = parts.getLength();
      partlist = new Part[size];
      for (int i = 0; i < size; i++)
        partlist[i] = new Part(parts.item(i), info);
    }
    return partlist;
  }

  public SCCXMLWrapper makeDeadpanSCCXML(int ticksPerBeat) throws IOException {
    SCCXMLWrapper dest = 
      (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    makeDeadpanSCCXML(dest, ticksPerBeat);
    return dest;
  }

  public void makeDeadpanSCCXML(final SCCXMLWrapper dest, 
      final int ticksPerBeat) 
  throws IOException {
    DeviationInstanceWrapper dev = 
      DeviationInstanceWrapper.createDeviationInstanceFor(this);
    dev.finalizeDocument();
    dev.toSCCXML(dest, ticksPerBeat);
    dest.finalizeDocument();
  }

  /**********************************************************************
   *<p>Returns the partwise note view.</p>
   *<p>パートごとのノートビューを取得します.</p>
   *********************************************************************/
  public List<TreeView<Note>> getPartwiseNoteView() 
  throws TransformerException {
    if (partwiseNoteView == null) 
      createNoteView();
    return partwiseNoteView;
  }

  /**********************************************************************
   *<p>Returns the non-partwise note view.</p>
   *<p>パートごとでないノートビュー(すべての音符を1つのノートビューに納めてある)を
   *取得します.</p>
   *********************************************************************/
  public TreeView<Note> getTimewiseNoteView() throws TransformerException {
    if (timewiseNoteView == null)
      createTimewiseNoteView();
    return timewiseNoteView;
  }

  /**********************************************************************
   *<p>Returns the SlurredNoteView object that manages note views built 
   *for slurred notes.</p>
   *<p>スラーがかかった音符に対して作成したノートビューを管理する
   *SlurredNoteViewオブジェクトを
   *取得します.</p>
   *********************************************************************/
  private SlurredNoteViewList getSlurredNoteView() throws TransformerException{
    if (slurredNoteView == null)
      createNoteView();
    return slurredNoteView;
  }

  /**********************************************************************
   *<p>小節のattributeタグを取得します．</p>
   * @author Hashida
   * @param partIndex 声部番号（0始まり）
   * @return
   *********************************************************************/
  public List<Attributes> getMeasureAttributesList(int partIndex) {
    Measure[] m = getPartList()[partIndex].getMeasureList();
    List<Attributes> list = new ArrayList<Attributes>();
    for (int i = 0; i < m.length; i++) {
      Attributes attr = m[i].getAttributesNodeInterface();
      if (attr != null)
        list.add(attr);
    }
    return list;
  }

  /**********************************************************************
   *<p>楽曲タイトルを取得します．</p>
   *@author Hashida
   *@since 20007.8.31
   *********************************************************************/
  public String getMovementTitle() {
    if (movementTitle != null)
      return movementTitle;
    else
      if (hasMovementTitle())
        return movementTitle;
      else
        return null;
    //    NodeList header = selectNodeList("/score-partwise/movement-title");
    //    Node title=header.item(0);
    //    return title.getTextContent();
  }

  public boolean hasMovementTitle() {
    Node title = selectSingleNode("/score-partwise/movement-title");
    if (title != null) {
      movementTitle = title.getTextContent();
      return true;
    } else {
      return false;
    }
  }

  private long getCumulativeTicksLocal(int measure, int ticksPerBeat) {
    if (measure >= 0) {
      if (ticksPerBeat == INTERNAL_TICKS_PER_BEAT)
        return cumulativeTicksList.get(measure);
      else
        return cumulativeTicksList.get(measure) 
        * ticksPerBeat / INTERNAL_TICKS_PER_BEAT;
    } else {
      if (ticksPerBeat == INTERNAL_TICKS_PER_BEAT)
        return cumulativeTicksList2.get(-measure);
      else
        return cumulativeTicksList2.get(-measure)
        * ticksPerBeat / INTERNAL_TICKS_PER_BEAT;
    }
  }

  public int getCumulativeTicks(int measure, int ticksPerBeat) {
    return (int)getCumulativeTicksLocal(measure, ticksPerBeat);
  }

  /*
  public int getCumulativeTicks(int measure, int ticksPerBeat) {
    if (measure >= 0) {
      if (ticksPerBeat == INTERNAL_TICKS_PER_BEAT)
        return (int)cumulativeTicksList.get(measure);
      else
        return (int)(cumulativeTicksList.get(measure) 
   * ticksPerBeat / INTERNAL_TICKS_PER_BEAT);
    } else {
      if (ticksPerBeat == INTERNAL_TICKS_PER_BEAT)
        return (int)cumulativeTicksList2.get(-measure);
      else
        return (int)(cumulativeTicksList2.get(-measure)
   * ticksPerBeat / INTERNAL_TICKS_PER_BEAT);
    }
  }
   */

  public List<SimpleNoteList> getPartwiseNoteList
  (final int ticksPerBeat) throws TransformerException {
    final List<SimpleNoteList> l = new ArrayList<SimpleNoteList>();
    processNotePartwise(new NoteHandlerPartwise() {
      private int serial = 0;
      private SimpleNoteList nl;
      public void beginPart(Part part, MusicXMLWrapper wrapper) {
        nl = new SimpleNoteList(++serial, part.id(), ticksPerBeat);
      }
      public void endPart(Part part, MusicXMLWrapper wrapper) {
        l.add(nl);
      }
      public void beginMeasure(Measure m, MusicXMLWrapper w) {}
      public void endMeasure(Measure m, MusicXMLWrapper w) {}
      public void processMusicData(MusicData md, MusicXMLWrapper w) {
        if (md instanceof Note) {
          Note note = (Note)md;
          if (!note.rest())
            nl.add((Note)md);
        }
      }
    });
    return l;
  }

  public InputStream getMIDIInputStream() throws IOException, TransformerException, SAXException, ParserConfigurationException {
    return makeDeadpanSCCXML(INTERNAL_TICKS_PER_BEAT).toMIDIXML().getMIDIInputStream();
  }


  /*
  public boolean startsWithZerothMeasure() {
    if (!zerothMeasureChecked) {
      zerothMeasureChecked = true;
      Part[] partlist = getPartList();
      for (Part part : partlist) {
        int num = part.firstMeasureNumber();
        if (num == 0) 
          return startsWithZerothMeasure = true;
        else if (num < 0)
          throw new InvalidElementException
                    ("Measure with a negative number included.");
        return startsWithZerothMeasure = false;
      }
    }
    return startsWithZerothMeasure;
  }
   */

  /**********************************************************************
   *各種処理の準備として, 内部で各種ノートビューを作成します.
   **********************************************************************/
  @Override
  protected void analyze() {
    // レイジーにすべきか?
    createTimewiseNoteView();
    //    createNoteView();
  }

  private void createTimewiseNoteView() {
    timewiseNoteView = new TreeView<Note>("all");
    processNotePartwise(new NoteHandlerPartwise() {
      public void beginPart(Part part, MusicXMLWrapper wrapper) {}
      public void endPart(Part part, MusicXMLWrapper wrapper) {}
      public void beginMeasure(Measure measure, MusicXMLWrapper wrapper) {}
      public void endMeasure(Measure measure, MusicXMLWrapper wrapper) {}
      public void processMusicData(MusicData md, MusicXMLWrapper wrapper) {
        if (md instanceof Note) {
          Note note = (Note)md;
          timewiseNoteView.add(note, "");
        }
      }
    });
  }


  // not tested yet
  private void createNoteView() {
    partwiseNoteView = new ArrayList<TreeView<Note>>();
    //    timewiseNoteView = new TreeView<Note>("all");
    slurredNoteView = new SlurredNoteViewList();
    processNotePartwise(new NoteHandlerPartwise() {
      private TreeView<Note> noteview;
      //        private int divisions = 1;
      //        private double currentTick = 0;
      //        private double prevTick = 0;
      //        private Note topnote;
      //        private int nBranches = 0;
      public void beginPart(Part part, MusicXMLWrapper wrapper) {
        noteview = new TreeView<Note>(part.id());
      }
      public void endPart(Part part, MusicXMLWrapper wrapper) {
        partwiseNoteView.add(noteview);
      }
      public void beginMeasure(Measure measure, MusicXMLWrapper wrapper) {
        //          currentTick = 1;
        //          prevTick = 1;
      }
      public void endMeasure(Measure measure, MusicXMLWrapper wrapper) {
        //          for (int i = 0; i < nBranches; i++)
        //            noteview.endBranch();
        //          nBranches = 0;
      }
      public void processMusicData(MusicData md, MusicXMLWrapper wrapper) {
        if (md instanceof Note) {
          Note note = (Note)md;
          noteview.add(note, (byte)note.voice(), "");
          //            timewiseNoteView.add(note, "");
          // slur
          Notations notations = note.getFirstNotations();
          if (notations != null) {
            List<StartStopElement> slurs = notations.getSlurList();
            if (slurs != null)
              for (StartStopElement slur : slurs) {
                if (slur.type().equals("start"))
                  slurredNoteView.newSlur(note, slur);
                else if (slur.type().equals("stop"))
                  slurredNoteView.endSlur(note, slur);
              }
          }
          slurredNoteView.addNote(note);
        } else if (md instanceof Backup) {
          //            noteview.newBranch(md.ordinal(), md.subordinal());
          //            nBranches++;
          slurredNoteView.stopSlur();
        }
      }
    });
    slurredNoteView.postproc();
  }

  private class SlurredNoteView extends TreeView<Note> {
    //    private Note head = null;
    //    private Note tail = null;
    private StartStopElement startslur = null;
    private StartStopElement endslur = null;
    private SlurredNoteView(String id) {
      super(id);
    }
  }

  private class SlurredNoteViewList {
    private MultiMap<Byte,SlurredNoteView> map;
    private MultiMap<Note,SlurredNoteView> viewFrom;
    private MultiMap<Note,SlurredNoteView> viewIncluding;
    private MultiMap<Note,SlurredNoteView> viewUntil;
    private Map<Byte,SlurredNoteView> current;
    private int i = 0;
    private int nCurrent = 0;
    private List<StartStopElement> pendingSlursB;
    private List<StartStopElement> pendingSlursE;

    private SlurredNoteViewList() {
      map = new MultiHashMap<Byte,SlurredNoteView>();
      current = new HashMap<Byte,SlurredNoteView>();
      viewFrom = new MultiHashMap<Note,SlurredNoteView>();
      viewIncluding = new MultiHashMap<Note,SlurredNoteView>();
      viewUntil = new MultiHashMap<Note,SlurredNoteView>();
      pendingSlursB = new ArrayList<StartStopElement>();
      pendingSlursE = new ArrayList<StartStopElement>();
    }

    private void newSlur(Note note, StartStopElement slur) {
      byte number = slur.number();
      SlurredNoteView view = new SlurredNoteView("slur:" + (++i));
      view.startslur = slur;
      map.add(number, view);
      current.put(number, view);
      viewFrom.put(note, view);
      nCurrent++;
      //      System.out.println("NewSlur: " + slur);
    }

    private void addNote(Note note) {
      if (nCurrent > 0) 
        for (SlurredNoteView view : current.values()) {
          view.add(note, ""); 
          viewIncluding.put(note,view);
          //          System.out.println("AddNote: " + note);
        }
    }
    //仮
    private void stopSlur() {
      if (nCurrent > 0) {
        current.clear();
        nCurrent = 0;
        //        System.out.println("StopSlur");
      }
    }

    private void endSlur(Note note, StartStopElement slur) {
      byte number = slur.number();
      SlurredNoteView view = current.get(number);
      if (view != null) {
        view.endslur = slur;
        view.add(note, "");
        viewIncluding.put(note,view);
        viewUntil.put(note,view);
        current.remove(number);
        nCurrent--;
        //        System.out.println("EndSlur: " + note + slur);
      } else {
        pendingSlursE.add(slur);
        //        System.out.println("PendingSlur");
      }
    }

    private void postproc() {
      invalidateImcompleteSlurs(map, true);
      invalidateImcompleteSlurs(viewFrom, false);
      invalidateImcompleteSlurs(viewIncluding, false);
      invalidateImcompleteSlurs(viewUntil, false);
    }

    private void invalidateImcompleteSlurs
    (MultiMap<?,SlurredNoteView> noteview, boolean push) {
      Collection<List<SlurredNoteView>> values = noteview.values();
      for (List<SlurredNoteView> list : values) {
        Iterator<SlurredNoteView> it = list.iterator();
        while (it.hasNext()) {
          SlurredNoteView slur = it.next();
          if (slur.endslur == null) {
            if (push)
              pendingSlursB.add(slur.startslur);
            it.remove();
          }
        }
      }
    }

    private List<? extends TreeView<Note>> getSlurredNoteViews(byte number) {
      return map.get(number);
    }
    private List<? extends TreeView<Note>> getNoteViewsIncluding(Note note) {
      return viewIncluding.get(note);
    }
    private List<? extends TreeView<Note>> getNoteViewsStartingWith(Note note) {
      return viewFrom.get(note);
    }
    private List<? extends TreeView<Note>> getNoteViewsEndingWith(Note note) {
      return viewUntil.get(note);
    }
  }

  public List<? extends TreeView<Note>> getSlurredNoteViews(byte number) {
    return slurredNoteView.getSlurredNoteViews(number);
  }
  public List<? extends TreeView<Note>> getNoteViewsIncluding(Note note) {
    return slurredNoteView.getNoteViewsIncluding(note);
  }
  public List<? extends TreeView<Note>> getNoteViewsStartingWith(Note note) {
    return slurredNoteView.getNoteViewsStartingWith(note);
  }
  public List<? extends TreeView<Note>> getNoteViewsEndingWith(Note note) {
    return slurredNoteView.getNoteViewsEndingWith(note);
  }


  /**********************************************************************
   *<p>This class provides methods for getting information from a "part" 
   *element.</p>
   *<p>part要素から情報を取り出すためのメソッドを提供します. 
   *********************************************************************/
  public class Part extends NodeInterface {
    private String xpath = null;
    private String id;
    private Measure[] measurelist = null;
    private Node info;
    private Node midi = null;
    //    private Node midiChannel;
    //    private Node midiProgram;
    private Part(Node node, Node partlist) {
      super(node);
      id = getAttribute(node(), "id");
      info = selectSingleNode(partlist, "score-part[@id='" + id() + "']");
      //      Node midi=getChildByTagName("midi-instrument",info);
      //      midiChannel=getChildByTagName("midi-channel",midi);
      //      midiProgram=getChildByTagName("midi-program",midi);
    }
    /**********************************************************************
     *<p>Returns "part".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "part";
    }
    /**********************************************************************
     *<p>Returns the array of the measure elements included in this part 
     *element.
     *</p>
     *<p>このpart要素に含まれるmeasure要素の配列を返します.
     *********************************************************************/
    public Measure[] getMeasureList() {
      if (measurelist == null) {
        long cumulativeTicks = 0;
        //        int ticks = 4 * INTERNAL_TICKS_PER_BEAT;
        NodeList measures = selectNodeList(node(), "measure");
        int size = measures.getLength();
        measurelist = new Measure[size];
        for (int i = 0; i < size; i++) {
          Measure measure = new Measure(measures.item(i), 
              i > 0 ? measurelist[i-1] : null, 
                  this);
          measure.setCumulativeTicks(cumulativeTicks);
          //          measure.cumulativeTicks = cumulativeTicks;
          //          Attributes attr = measure.getAttributesNodeInterface();
          //          if (attr != null && attr.beatType() != 0)
          //            ticks = 
          //              INTERNAL_TICKS_PER_BEAT * attr.beats() * 4 / attr.beatType();
          //          cumulativeTicks += ticks;
          cumulativeTicks += measure.duration(INTERNAL_TICKS_PER_BEAT);
          measure.getMusicDataList();
          measurelist[i] = measure;
        }
      }
      return measurelist;
    }

    public int firstMeasureNumber() {
      Measure[] measurelist = getMeasureList();
      if (measurelist != null && measurelist.length > 0)
        return measurelist[0].number();
      else
        return 0;
    }

    /**********************************************************************
     *<p>Returns the ID of this part.
     *</p>
     *<p>このパートのIDを返します.</p>
     *********************************************************************/
    public final String id() {
      return id;
    }
    /**********************************************************************
     *<p>Returns an XPath expression coresponding to this part element.
     *</p>
     *<p>このpart要素に対応するXPath表現を返します.</p>
     *********************************************************************/
    public String getXPathExpression() {
      if (xpath == null) {
        Node parent = node().getParentNode();
        if (parent.isSameNode(getDocument().getDocumentElement())) 
          xpath = "/" + parent.getNodeName() + "/part[@id='" + id() + "']";
        else
          throw new InvalidElementException();
      }
      return xpath;
    }
    public int midiChannel(){
      if (midi == null)
        midi = getChildByTagName("midi-instrument",info);
      return getTextInt(getChildByTagName("midi-channel",midi));
    }
    public int midiProgram(){
      if (midi == null)
        midi = getChildByTagName("midi-instrument", info);
      return getTextInt(getChildByTagName("midi-program", midi));
    }
  }

  /**********************************************************************
   *<p>This class provides methods for getting information from a 
   *measure element.</p>
   *<p>measure要素から情報を取り出すためのメソッドを提供します.</p>
   *********************************************************************/
  public class Measure extends NodeInterface {
    private Part part;
    private int number;
    private String strNumber;
    private String xpath = null;
    private MusicData[] mdlist = null;
    private Node sound;
    private Attributes attr = null;
    // changed int -> long 20080609
    private long cumulativeTicks;
    private int duration = -1;
    private Measure prevMeasure;
    private Note[] tiedNotes = null;

    private Measure(Node node, Measure prevMeasure, Part part) {
      super(node);
      this.part = part;
      this.prevMeasure = prevMeasure;
      if (prevMeasure == null)
        tiedNotes = new Note[128];
      else
        tiedNotes = prevMeasure.tiedNotes;
      strNumber = getAttribute(node(), "number");
      try {
        number = Integer.parseInt(strNumber);
      } catch (NumberFormatException e) {
        if (strNumber.startsWith("X"))
          number =  - Integer.parseInt(strNumber.substring(1));
      }
      sound = getChildByTagName("sound");
    }
    /**********************************************************************
     *<p>Returns "measure".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "measure";
    }

    // changed int -> long 20080609
    private void setCumulativeTicks(long cumulativeTicks) {
      if (number >= 0) {
        if (cumulativeTicksList.size() > number) {
          if (cumulativeTicksList.get(number) != cumulativeTicks)
            throw new InvalidElementException();
        } else 
          cumulativeTicksList.set(number, cumulativeTicks);
      } else {
        if (cumulativeTicksList2.size() > -number) {
          if (cumulativeTicksList2.get(-number) != cumulativeTicks)
            throw new InvalidElementException();
        } else
          cumulativeTicksList2.set(-number, cumulativeTicks);
      }
    }

    public final int cumulativeTicks(int ticksPerBeat) {
      return getCumulativeTicks(number, ticksPerBeat);
    }

    public int duration(int ticksPerBeat) {
      if (duration < 0) {
        MusicData[] mdlist = getMusicDataList();
        duration = 0;
        for (MusicData md : mdlist)
          duration += 
            (md instanceof Note && ((Note)md).chord()) ?
                0 : md.actualDuration(INTERNAL_TICKS_PER_BEAT);
      }
      if (ticksPerBeat == INTERNAL_TICKS_PER_BEAT)
        return duration;
      else
        return Math.round((float)(duration * ticksPerBeat) 
            / (float)INTERNAL_TICKS_PER_BEAT);
    }

    /**********************************************************************
     *<p>Returns the array of music data included in this measure element.</p>
     *<p>このmeasure要素に含まれる音楽データの配列を返します.
     *ここで, 音楽データとはmeasure要素のすべての子を表し, attributes, note, 
     *forward, backupなどが該当します. </p>
     *********************************************************************/
    public MusicData[] getMusicDataList() {
      Note topnote = null;
      int noteindex = 0;
      if (mdlist == null) {
        int nextonset = 0;
        int prevduration = 0;
        NodeList nl = node().getChildNodes();
        int size = nl.getLength();
        mdlist = new MusicData[size];
        for (int i = 0; i < size; i++) {
          MusicData md = getMusicDataNodeInterface(nl.item(i));
          if (md instanceof Note) {
            Note note = (Note)md;
            if (note.chord()) {
              nextonset -= prevduration;
              note.addToChordNoteList(topnote);
            } else {
              topnote = note;
            }
            if (!note.rest()) {
              int notenum = note.notenum();
              if (note.containsTieType("stop") && tiedNotes[notenum] != null) {
                tiedNotes[notenum].tiedNote = note;
                tiedNotes[notenum] = null;
              }
              if (note.containsTieType("start"))
                tiedNotes[notenum] = note;
            }
            note.xpath = 
              getXPathExpression() + "/note[" + (++noteindex) + "]";
          }
          //            if (&& ((Note)md).chord())
          //            nextonset -= prevduration;
          md.onset = nextonset;
          prevduration = md.actualDuration(INTERNAL_TICKS_PER_BEAT);
          nextonset += prevduration;
          mdlist[i] = md;
          if (md instanceof Attributes)
            attr = (Attributes)md;
        }
      }
      return mdlist;
    }

    //    private int ticksPerBeat;

    /*
    public void calcOnsets(int ticksPerBeat) {
      this.ticksPerBeat = ticksPerBeat;
      getMusicDataList();
      int nextonset = 0;
      int prevduration = 0;
      for (MusicData md : mdlist) {
        if (md instanceof Note && ((Note)md).chord())
          nextonset -= prevduration;
        md.onsetInt = nextonset;
        prevduration = md.actualDurationInt();
        nextonset += prevduration;
      }
    }
     */

    /**********************************************************************
     *<p>Returns the measure number.</p>
     *<p>小節番号を返します.</p>
     *********************************************************************/
    public final int number() {
      return number;
    }
    private MusicData getMusicDataNodeInterface(Node node) {
      String nodename = node.getNodeName();
      if (nodename.equals("note"))
        return new Note(node, this);
      else if (nodename.equals("attributes"))
        return new Attributes(node, this);
      else if (nodename.equals("backup"))
        return new Backup(node, this);
      else if (nodename.equals("forward"))
        return new Forward(node, this);
      else if (nodename.equals("direction"))
        return new Direction(node, this);
      else
        return new MusicData(node, this);
    }
    /**********************************************************************
     *<p>Returns the Part object.
     *<p>この要素の親にあたるpart要素のオブジェクトを返します.</p>
     *********************************************************************/
    public final Part part() {
      return part;
    }

    /**********************************************************************
     *<p>Returns the Attributes object.</p>
     *<p>このmeasure要素内のattributes要素に対応するAttributesオブジェクトを
     *返します. </p>
     *********************************************************************/
    public Attributes getAttributesNodeInterface() {
      getMusicDataList();
      return attr;
      //      Node n = getChildByTagName("attributes");
      //      if (n == null) return null;
      //      return new Attributes(n, this);
    }

    /*
     // Directionタグは複数出現することが多いため不適
    public Direction getDirectionNodeInterface() {
        Node n = getChildByTagName("direction");
        if (n == null) return null;
        return new Direction(n, this);
    }
     */

    /**********************************************************************
     *<p>Returns an XPath expression for this node.</p>
     *<p>このノードのXPath表現を返します.</p>
     *********************************************************************/
    public String getXPathExpression() {
      if (xpath == null)
        xpath = part.getXPathExpression() + "/measure[@number='" + 
        strNumber + "']";
      return xpath;
    }
    public int tempo() {
      if(sound!=null)
        return getAttributeInt(sound, "tempo");
      return 120;
    }
  }

  /**********************************************************************
   *<p>MusicXMLドキュメントの要素のうち, score.dtdでmusic-dataエンティティに
   *指定されているもの, 具体的には note, backup, forward, direction, attributes, 
   *harmony, figured-bass, print, sound, barline, grouping, link, bookmark
   *をラップするクラスです. 
   *ただし, これらの要素で記述されるすべての情報を取り出す手段が提供されている
   *わけではありません. 
   *noteなどの主要な要素は専用のクラスがこのクラスを継承して設計されており, 
   *通常はこういったサブクラスにダウンキャストして情報を取り出します.</p>
   *********************************************************************/
  public class MusicData extends NodeInterface implements Ordered  {
    private Measure measure;
    private int divisions;
    private int onset; 
    // chantged int -> long 20080609
    private long measureTick;
    //    private double onset;
    //    private int onsetInt;
    private MusicData(Node node, Measure measure) {
      super(node);
      this.measure = measure;
      divisions = lastDivisions;
      measureTick = 
        getCumulativeTicksLocal(measure().number(),
            INTERNAL_TICKS_PER_BEAT);
      //  measureTick = measure().cumulativeTicks(INTERNAL_TICKS_PER_BEAT);
    }
    /**********************************************************************
     *<p>Returns "note|backup|forward|direction|attributes|harmony|figured-bass|print|sound|barline|grouping|link|bookmark".</p>
     *********************************************************************/
    @Override
    protected String getSupportedNodeName() {
      return "note|backup|forward|direction|attributes|harmony|" + 
      "figured-bass|print|sound|barline|grouping|link|bookmark";
    }
    public final Measure measure() {
      return measure;
    }
    public int duration() {
      return 0;
    }
    public int duration(int ticksPerBeat) {
      return actualDuration(ticksPerBeat);
    }
    public double actualDuration() {
      return (double)duration() / (double)divisions;
    }
    public int actualDuration(int ticksPerBeat) {
      return Math.round((float)(duration() * ticksPerBeat) 
          / (float)divisions);
    }
    // NOTE: onset time from the beginning of the MEASURE
    public double onsetWithinMeasure() {
      return (double)onset / (double)INTERNAL_TICKS_PER_BEAT;
    }
    // NOTE: onset time from the beginning of the MUSICAL PIECE
    public int onset(int ticksPerBeat) {
      return measure().cumulativeTicks(ticksPerBeat)
      + Math.round((float)(onset * ticksPerBeat) 
          / (float)INTERNAL_TICKS_PER_BEAT);
    }
    public int offset(int ticksPerBeat) {
      return onset(ticksPerBeat) + duration(ticksPerBeat);
    }
    public int ordinal() {
      return (int)measureTick;
    }
    // kari
    public int subordinal() {
      return onset;
      //      return onset(INTERNAL_TICKS_PER_BEAT) - measureTick;
    }
  }

  /**********************************************************************
   *<p>note要素からの情報を取り出すためのメソッドを提供します. 
   *ただし, 現バージョンでは, note要素内で記述されるすべての要素に対応しているわけでは
   *なく, pitch, rest, duration, chord, grace, voice, type, 
   *modification, stem, staff, notationsのみ対応しています. 
   *対応していない要素から情報を取り出すには, nodeメソッドでnote要素の
   *Nodeオブジェクトを取得してから, 自分でDOMメソッドを用いる必要があります. 
   *なお, notationsについては, 現状では最初に出現したものだけしか扱えません.</p>
   *********************************************************************/
  public class Note extends MusicData implements NoteCompatible  {
    private String pitchStep;
    private int pitchOctave;
    private int pitchAlter = 0;
    private int duration;
    private int notenum = -1;
    private int staff;
    private boolean chord = false;
    private boolean rest = false;
    private boolean grace = false;
    private String type = null;
    private int voice = 0;
    private String stem = null;
    private String notehead = null;
    private String ties = "";
    private Node timeModification = null;
    private Notations notations1st = null;
    private Note tiedNote = null;

    private String xpath = null;
    //    private double beat = Double.NaN;

    private Note topchordnote = this;
    private List<Note> chordnotes = null;

    //    private NodeList notations = null;



    //    private DeviationInstanceWrapper.NoteDeviation nd = null;


    private Note(Node node, Measure measure) {
      super(node, measure);
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      for (int i = 0; i < size; i++) {
        Node node1 = nl.item(i);
        String nodename = node1.getNodeName();
        String value = getText(node1);

        if (nodename.equals("rest"))
          rest = true;
        else if (nodename.equals("grace"))
          grace = true;
        else if (nodename.equals("chord"))
          chord = true;
        else if (nodename.equals("rest"))
          rest = true;
        else if (nodename.equals("type"))
          type = value;
        else if (nodename.equals("duration"))
          duration = Integer.parseInt(value);
        else if (nodename.equals("voice"))
          voice = Integer.parseInt(value);
        else if (nodename.equals("stem"))
          stem = value;
        else if (nodename.equals("time-modification"))
          timeModification = node1;
        else if (nodename.equals("notations")) {
          if (notations1st == null)
            notations1st = new Notations(node1, this);
        } else if (nodename.equals("pitch"))
          analyzePitch(node1);
        else if (nodename.equals("notehead"))
          notehead = value;
        else if (nodename.equals("tie")) 
          ties += getAttribute(node1, "type") + " ";
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

    /*
    private Note(Node node, Measure measure) {
      super(node, measure);
      rest = (getChildByTagName("rest") != null);
      if (!rest) {
        Node pitch = getChildByTagName("pitch");
        pitchStep = getText(getChildByTagName("step", pitch));
        pitchOctave = getTextInt(getChildByTagName("octave", pitch));
        Node alterNode = getChildByTagName("alter", pitch);
        if (alterNode != null) pitchAlter = getTextInt(alterNode);
      }
      grace = (getChildByTagName("grace") != null);
      if (!grace)
        duration = getTextInt(getChildByTagName("duration"));
      chord = (getChildByTagName("chord") != null);
    }
     */
    /**********************************************************************
     *<p>Returns "note".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "note";
    }

    /**********************************************************************
     *<p>CやDなどの音名を返します. </p>
     *********************************************************************/
    public final String pitchStep() {
      if (!rest)
        return pitchStep;
      else
        throw new InvalidElementException("This is a rest note");
    }

    /**********************************************************************
     *<p>オクターブ番号を返します. C4=ノートナンバー60を前提としています. </p>
     *********************************************************************/
    public final int pitchOctave() {
      if (!rest)
        return pitchOctave;
      else
        throw new InvalidElementException("This is a rest note");
    }

    /**********************************************************************
     *<p>音符にシャープが付いていれば1, フラットが付いていれば-1を返します.</p>
     *********************************************************************/
    public final int pitchAlter() {
      if (!rest) 
        return pitchAlter;
      else
        throw new InvalidElementException("This is a rest note");
    }

    /**********************************************************************
     *<p>音符の長さを整数で返します. Attributes要素の中のdivisions要素の値が
     *分母となり, duration/divisions=1.0のときに4分音符1個分の長さとみなされます.</p>
     *********************************************************************/
    public final int duration() {
      if (!grace)
        return duration;
      else
        return 0;
    }

    public double tiedDuration() {
      if (tiedNote == null)
        return actualDuration();
      else
        return actualDuration() + tiedNote.tiedDuration();
    }

    public int tiedDuration(int ticksPerBeat) {
      if (tiedNote == null)
        return actualDuration(ticksPerBeat);
      else
        return actualDuration(ticksPerBeat) 
        + tiedNote.tiedDuration(ticksPerBeat);
    }

    public int offset(int ticksPerBeat) {
      return onset(ticksPerBeat) + tiedDuration(ticksPerBeat);
    }


    /**********************************************************************
     *<p>直前の音符と和音をなすときにtrueを返します.</p>
     *********************************************************************/
    public final boolean chord() {
      return chord;
    }

    /**********************************************************************
     *<p>休符のときにtrueを返します. </p>
     *********************************************************************/
    public final boolean rest() {
      return rest;
    }

    /**********************************************************************
     *<p>装飾音符のときにtrueを返します. </p>
     *********************************************************************/
    public final boolean grace() {
      return grace;
    }

    public final String stem() {
      return stem;
    }

    public final String type() {
      //      if (type == null)
      //        type = getText(getChildByTagName("type"));
      return type;
    }

    /**********************************************************************
     *<p>time-modification要素が存在するときにtrueを返します.</p>
     *********************************************************************/
    public final boolean hasTimeModification() {
      return timeModification != null;
    }

    public int timeModificationActualNotes() {
      return getTextInt(getChildByTagName("actual-notes", timeModification));
    }

    public int timeModificationNormalNotes() {
      return getTextInt(getChildByTagName("normal-notes", timeModification));
    }

    public final int voice(){
      return voice;
    }

    public final int staff(){
      return staff;
    }

    public final boolean containsTieType(String type) {
      return ties.contains(type);
    }

    //    public final List<String> ties() {
    //      return ties;
    //    }

    //    public final String tie() {
    //      return tie;
    //    }

    public final Note tiedTo() {
      return tiedNote;
    }

    public final String notehead() {
      return notehead;
    }


    /**
     * 音名
     * @author Mitsuyo Hashida
     */
    final public String noteName() {
      if(rest())return "rest";
      String s=pitchStep();
      if (pitchAlter() > 0)
        s += "#";
      else if (pitchAlter() < 0)
        s += "b";
      return s+pitchOctave();
    }

    /**********************************************************************
     *<p>音高をノートナンバー形式で返します.</p>
     *********************************************************************/
    public int notenum() {    // kari
      if (!rest) {
        if (notenum < 0) {
          int stepInt;
          if (pitchStep.equalsIgnoreCase("C")) stepInt = 0;
          else if (pitchStep.equalsIgnoreCase("D")) stepInt = 2;
          else if (pitchStep.equalsIgnoreCase("E")) stepInt = 4;
          else if (pitchStep.equalsIgnoreCase("F")) stepInt = 5;
          else if (pitchStep.equalsIgnoreCase("G")) stepInt = 7;
          else if (pitchStep.equalsIgnoreCase("A")) stepInt = 9;
          else if (pitchStep.equalsIgnoreCase("B")) stepInt = 11;
          else throw new InvalidElementException("Pitch is wrong.");
          notenum = stepInt + (pitchOctave + 1) * 12 + pitchAlter;
        }
        return notenum;
      } else {
        throw new InvalidElementException("This is a rest note");
      }
    }

    public int velocity() {
      throw new UnsupportedOperationException();
    }

    public int onsetInMSec() {
      throw new UnsupportedOperationException();
    }

    public int offsetInMSec() {
      throw new UnsupportedOperationException();
    }

    public String toString() {
      return "Note[" + measure().number() + ": " + onsetWithinMeasure() 
      + "--" + (actualDuration() + onsetWithinMeasure()) 
      + "  " + noteName() + "]";
    }

    /**********************************************************************
     *<p>この音符が和音をなすときに, その和音のトップノートを返します. </p>
     *********************************************************************/
    public final Note topNoteOfChord() {
      return topchordnote;
    }

    /**********************************************************************
     *<p>この音符が和音をなすときに, その和音の構成音を表すNoteオブジェクトの
     *リストを返します.</p>
     *********************************************************************/
    public List<Note> chordNotes() {
      return topchordnote.chordnotes;
    }

    // DO NOT CHANGE IT TO PUBLIC ETC.
    private void addToChordNoteList(Note topnote) {
      topchordnote = topnote;
      if (topnote.chordnotes == null)
        topnote.chordnotes = new ArrayList<Note>();
      topnote.chordnotes.add(this);
    }


    /**********************************************************************
     *<p>このnote要素に対応するXPath表現を返します.</p>
     *********************************************************************/
    public String getXPathExpression() {   // kari
      /*      if (xpath == null) {
	MusicData[] mdlist = measure().getMusicDataList();
	int noteindex = 0;
	for (MusicData md : mdlist) {
	  if (md instanceof Note) {
	    noteindex++;
	    if (md == this) {
	      xpath = measure().getXPathExpression() + 
                "/note[" + noteindex + "]";
	      break;
	    }
          }
        }
      }
       */
      /*        NodeList nl = measure().getMusicDataList();
	int size = nl.getLength();
	int noteindex = 0;
	for (int i = 0; i < size; i++) {
	  Node n = nl.item(i);
	  if (n.getNodeName().equals("note")) {
	    noteindex++;
	    if (node().isSameNode(n)) {
	      xpath = measure().getXPathExpression() + 
	              "/note[" + noteindex + "]";
              break;
	    }
          }
        }
      }
       */
      return xpath;
    }

    /** obsolete */
    public double beat() {
      return onsetWithinMeasure() + 1.0;
      //      if (Double.isNaN(beat))
      //        throw new IllegalStateException("'beat' has not been calculated.");
      //      else 
      //        return beat;
    }


    /*
    public NodeList getNotationsList() {
      if (notations == null)
        notations = selectNodeList(node(), "notations");
      return notations;
    }

    public Notations getNotationsNodeInterface(Node node) {
      return new Notations(node, this);
    }
     */
    /**********************************************************************
     *<p>このnote要素内で最初に出現したnotations要素をラップするNotationsオブジェクト
     *を返します.</p>
     *********************************************************************/
    public Notations getFirstNotations() {
      return notations1st;
    }
    //      if (notations == null)
    //        notations = selectNodeList(node(), "notations");
    //      if (notations != null && notations.getLength() >= 1)
    //        return getNotationsNodeInterface(notations.item(0));
    //      else
    //        return null;
    //    }

    // kari
    public boolean hasArticulation(String name) {
      if (notations1st == null)
        return false;
      return notations1st.hasArticulation(name);
    }

    /*
     * Noteが同一かどうかを判定します。
     * 文字列表現とXPath表現を比較し、等しい場合trueを返します。
     * @param arg
     * @return

    public boolean equals(Note arg){
      if(this.toString().equals(arg.toString())){
        if(this.xpath.equals(arg.xpath)) return true;
      }
      return false;
    }
     */

    /*
    public final boolean hasArticulation(String name){
      NodeList nl = getNotationsList();
      if(nl==null)
        return false;
      for (int i = 0; i < nl.getLength(); i++) {
        Node node = nl.item(i);
        Notations notation = getNotationsNodeInterface(node);
        if (notation.hasArticulation(name))
          return true;
      }
      return false;
    }
     */

    //    public DeviationInstanceWrapper.NoteDeviation 
    //    getNodeDeviationNodeInterface() {
    //      if (nd == null) {
    //        Node linkednode = 
    //	  linkmanager.getNodeLinkedTo(node(), "note-deviation");
    //	nd = dev.getNoteDeviationNodeInterface(linkednode);
    //      }
    //      return nd;
    //    }
  }

  /**********************************************************************
   *<p>notations要素内で記述される情報を取り出すためのメソッドを提供します. 
   *notations要素はnote要素の子として記述され, 主に対象となる音符になんらかの
   *情報を付加します. たとえばスラーやフェルマータなどが該当します. 
   *現バージョンで対応しているのは, tied, slur, tuplet, glissando, slide, 
   *articulations, fermataのみです. 
   *これらのうち, tie, slur, tuplet, glissando, slideはそれぞれが2つ以上
   *存在していても処理が可能で, articulationsとfermataは複数あった場合には
   *最初の1つだけを扱います. 
   *********************************************************************/
  public class Notations extends NodeInterface {
    private Note note;
    private List<StartStopElement> tielist = null;
    private List<StartStopElement> slurlist = null;
    private List<StartStopElement> tupletlist = null;
    private List<StartStopElement> glissandolist = null;
    private List<StartStopElement> slidelist = null;
    private Node articulations = null;
    private Node fermata = null;
    private Notations(Node node, Note note) {
      super(node);
      this.note = note;
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      for (int i = 0; i < size; i++) {
        Node n = nl.item(i);
        String nodename = n.getNodeName();
        if (nodename.equals("tied"))
          tielist = addStartStopElement(n, tielist);
        else if (nodename.equals("slur")) 
          slurlist = addStartStopElement(n, slurlist);
        else if (nodename.equals("tuplet")) 
          tupletlist = addStartStopElement(n, tupletlist);
        else if (nodename.equals("glissando"))
          glissandolist = addStartStopElement(n, glissandolist);
        else if (nodename.equals("slide"))
          slidelist = addStartStopElement(n, slidelist);
        else if (nodename.equals("articulations")) {
          if (articulations == null)
            articulations = n;
        } else if (nodename.equals("fermata")) {
          if (fermata == null) 
            fermata = n;
        }
      }
    }

    private List<StartStopElement>
    addStartStopElement(Node node, List<StartStopElement> list) {
      if (list == null)
        list = new ArrayList<StartStopElement>();
      list.add(new StartStopElement(node, this));
      return list;
    }

    /**********************************************************************
     *<p>Returns "notations".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "notations";
    }
    //    public boolean hasNotation(String name) {
    //      return hasChild(name, node());
    //    }
    /**********************************************************************
     *articulations要素が存在し, かつ, 指定された名前の要素が
     *articulations要素の子として存在するときに限りtrueを返します.
     *(現バージョンでは, articulations要素から取り出せる情報はこれだけです.)
     *********************************************************************/
    public boolean hasArticulation(String name) {
      if (articulations == null)
        return false;
      else
        return hasChild(name, articulations);
    }
    /**********************************************************************
     *<p>fermata要素が存在するときにはそのテキストを返し, 
     *存在しないときはnullを返します. </p>
     *********************************************************************/
    public String fermata() {
      if (fermata == null)
        return null;
      else
        return getText(fermata);
    }
    /**********************************************************************
     *<p>fermata要素のtype属性の値を返します.
     *fermata要素が存在しないときはnullを返します.</p>
     *********************************************************************/
    public String fermataType() {
      if (fermata == null)
        return null;
      else
        return getAttribute(fermata, "type");
    }
    /**********************************************************************
     *<p>tied要素を表すStartStopElementオブジェクトのリストを返します.</p>
     *********************************************************************/
    public final List<StartStopElement> getTieList() {
      return tielist;
    }
    /**********************************************************************
     *<p>slur要素を表すStartStopElementオブジェクトのリストを返します.</p>
     *********************************************************************/
    public final List<StartStopElement> getSlurList() {
      return slurlist;
    }
    /**********************************************************************
     *<p>tuplet要素を表すStartStopElementオブジェクトのリストを返します.</p>
     *********************************************************************/
    public final List<StartStopElement> getTupletList() {
      return tupletlist;
    }
    /**********************************************************************
     *<p>glissando要素を表すStartStopElementオブジェクトのリストを返します.</p>
     *********************************************************************/
    public final List<StartStopElement> getGlissandoList() {
      return glissandolist;
    }
    /**********************************************************************
     *<p>slide要素を表すStartStopElementオブジェクトのリストを返します.</p>
     *********************************************************************/
    public final List<StartStopElement> getSlideList() {
      return slidelist;
    }


    //    // 速度要チェック
    //    public StartStopElement[] getSlurList() {
    //      if (slurlist == null) {
    //        NodeList nl = selectNodeList(node(), "slur");
    //        int size = nl.getLength();
    //        slurlist = new StartStopElement[size];
    //        for (int i = 0; i < size; i++) 
    //          slurlist[i] = new StartStopElement(nl.item(i));
    //      }
    //      return slurlist;
    //    }
    //    public NodeList getSlurList() {
    //      return selectNodeList(node(), "slur");
    //    }
    //    public StartStopElement getSlurNodeInterface(Node slur) {
    //      return new StartStopElement(slur);
    //    }
  }

  private int lastDivisions = 1;

  /**********************************************************************
   *<p>Attributes要素から情報を取り出すためのメソッドを提供します.
   *現バージョンでは, divisions, key, time要素に対応しています.</p>
   *********************************************************************/
  public class Attributes extends MusicData {
    private int fifths;
    private String mode = null;
    private int divisions = 0;
    private int beats = 0; 
    private int beatType = 0;
    private String timeSymbol = null;


    // 要チェック
    private Attributes(Node node, Measure measure) {
      super(node, measure);
      divisions = lastDivisions;
      Node divisionNode;
      if ((divisionNode = getChildByTagName("divisions")) != null)
        lastDivisions = divisions = getTextInt(divisionNode);
      Node key = getChildByTagName("key");
      if (key != null) {
        fifths = getTextInt(getChildByTagName("fifths", key));
        mode = getText(getChildByTagName("mode", key));
      }
      Node time = getChildByTagName("time");
      if (time != null) {
        beats = getTextInt(getChildByTagName("beats", time));
        beatType = getTextInt(getChildByTagName("beat-type", time));
        if (hasAttribute(time, "symbol"))
          timeSymbol = getAttribute(time, "symbol");
      }

    }

    /**********************************************************************
     *<p>Returns "attributes".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "attributes";
      //      return "attributes|divisions|key|fifths|mode|time|beats|beat-type";
    }

    /**********************************************************************
     *<p>音長表記における分母の値を返します. 詳しくはNote.duration()を
     *ご覧ください.</p>
     *********************************************************************/
    public int divisions() {
      return divisions;
    }

    public int fifths(){
      return fifths;
      //    	return getTextInt(fifths);
    }

    public String mode() {
      return mode;
      //    	return getText(mode);
    }

    /**********************************************************************
     *<p>拍子の分子(8分の6拍子なら6)を返します.</p>
     *********************************************************************/
    public int beats() {
      return beats;
      //    	return getTextInt(beats);
    }

    /**********************************************************************
     *<p>拍子の分母(8分の6拍子なら8)を返します.</p>
     *********************************************************************/
    public int beatType() {
      return beatType;
      //    	return getTextInt(beatType);
    }

    public String timeSymbol() {
      return timeSymbol;
      //    	return getAttribute(getChildByTagName("time"), "symbol");
    }
  }

  /**********************************************************************
   *<p>backup要素から情報を取り出すためのメソッドを提供します. 
   *backup要素は時刻を巻き戻すために使用されます. 
   *MusicXMLWrapperではbackup要素は声部の切り替えであると判断し, 
   *PartwiseなTreeViewを生成する際に, backup要素に出会うと枝を切り替えます.</p>
   *********************************************************************/
  public class Backup extends MusicData {
    private int duration;
    private Backup(Node node, Measure measure) {
      super(node, measure);
      duration = getTextInt(getChildByTagName("duration"));
    }
    /**********************************************************************
     *<p>Returns "backup".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "backup";
    }
    public final int duration() {
      return duration;
    }
    public double actualDuration() {
      return -super.actualDuration();
    }
    public int actualDuration(int ticksPerBeat) {
      return -super.actualDuration(ticksPerBeat);
    }
    public int subordinal() {
      return onset(INTERNAL_TICKS_PER_BEAT) 
      + actualDuration(INTERNAL_TICKS_PER_BEAT);
      //      return (int)(1000.0 * (onset() + actualDuration()));
    }
  }

  /**********************************************************************
   *<p>forward要素から情報を取り出すためのメソッドを提供します. 
   *<b>forward要素は時刻を空回しするときに使用されます.
   *********************************************************************/
  public class Forward extends MusicData {
    private int duration;
    private Forward(Node node, Measure measure) {
      super(node, measure);
      duration = getTextInt(getChildByTagName("duration"));
    }
    protected final String getSupportedNodeName() {
      return "forward";
    }
    public final int duration() {
      return duration;
    }
  }

  public class BarLine extends MusicData {
    private BarLine(Node node, Measure measure) {
      super(node, measure);
    }
    protected final String getSupportedNodeName() {
      return "barline";
    }
    public boolean repeat() {
      return getChildByTagName("repeat") != null;
    }
    public String repeatDirection() {
      return getAttribute(getChildByTagName("repeat"), "direction");
    }
    public int repeatTimes() {
      return getAttributeInt(getChildByTagName("repeat"), "times");
    }
  }

  /**********************************************************************
   *<p>Direction要素から情報を取り出すためのメソッドを提供します. 
   *Direction要素は, 楽譜における演奏者への指示のうち, 特定の音符に貼り付くタイプ
   *ではないものを表します. 現バージョンではdirection-type, voice, soundに
   *対応しています. direction-typeは複数存在してもかまいません. </p>
   *********************************************************************/
  public class Direction extends MusicData {
    private DirectionType[] directType = null;
    private Node sound=null;
    private Direction(Node node, Measure measure) {
      super(node, measure);
      sound=getChildByTagName("sound");
    }
    /**********************************************************************
     *<p>Returns "direction".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "direction";
    }
    /**********************************************************************
     *<p>direction-type要素をラップするDirectionTypeオブジェクトのリストを
     *返します.</p>
     *********************************************************************/
    public DirectionType[] getDirectionTypeList() {
      if (directType == null) {
        NodeList nl = selectNodeList(node(), "direction-type");
        int size = nl.getLength();
        directType = new DirectionType[size];
        for (int i = 0; i < size; i++)
          directType[i] = new DirectionType(nl.item(i));
      }
      return directType;
    }
    public final int voice() {
      return getChildTextInt("voice");
    }
    //    public double tempo(){
    //      return Double.parseDouble(getAttribute(sound, "tempo"));
    //    }
    public final String getSoundAttribute(String key) {
      return getAttribute(sound, key);
    }
    public final int getSoundAttributeInt(String key) {
      return getAttributeInt(sound, key);
    }
    public final double getSoundAttributeDouble(String key) {
      return getAttributeDouble(sound, key);
    }
    public final boolean hasSound() {
      return sound != null;
    }
    public final double tempo() {
      return getSoundAttributeDouble("tempo");
    }
  }

  // direction-typeは子ノード１つだけを仮定
  /**********************************************************************
   *<p>DirectionType要素から情報を取り出すためのメソッドを提供します. 
   *現バージョンでは, direction-type要素には子がただ1つだけ存在することを仮定しています.
   *********************************************************************/
  public class DirectionType extends NodeInterface {
    private final Node child;
    private final String nodename;
    private final String text;
    private DirectionType(Node node) {
      super(node);
      child = getFirstChild();
      nodename = child.getNodeName();
      text = getText(child);
    }

    /**********************************************************************
     *<p>Returns "direction-type".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "direction-type";
    }
    /**********************************************************************
     *<p>direction-type要素の子のノード名を返します.</p>
     *********************************************************************/
    public final String name() {
      return nodename;
    }
    /**********************************************************************
     *<p>direction-type要素の子が持つテキストを返します.</p>
     *********************************************************************/
    public final String text() {
      return text;
    }
    /**********************************************************************
     *<p>direction-type要素の子が持つtype属性を返します
     *(direction-type要素自身のtype属性ではありません).</p>
     *********************************************************************/
    public final String type() {
      return getAttribute("type");
    }
    /**********************************************************************
     *<p>direction-type要素の子が持つnumber属性を返します
     *(direction-type要素自身のnumber属性ではありません).</p>
     *********************************************************************/
    public final int number() {
      return getAttributeInt("number");
    }
    /**********************************************************************
     *<p>direction-type要素の子が持つ, 指定された名前の属性の値を返します
     *(direction-type要素自身の属性ではありません).</p>
     *********************************************************************/
    public final String getAttribute(String key) {
      return getAttribute(child, key);
    }
    /**********************************************************************
     *<p>direction-type要素の子が持つ, 指定された名前の属性の値を整数で返します
     *(direction-type要素自身の属性ではありません).</p>
     *********************************************************************/
    public final int getAttributeInt(String key) {
      return getAttributeInt(child, key);
    }
    /**********************************************************************
     *<p>direction-type要素の子がdynamics要素だったときに, その子要素の名前を
     *返します. 
     *とりうる値は, p, pp, ppp, pppp, ppppp, pppppp, 
     *f, ff, fff, ffff, fffff, ffffff, mp, mf, sf, sfp, sfpp, 
     *fp, rf, rfz, sfz, sffz, fz, otherです.</p> 
     *********************************************************************/
    public String dynamics() {
      if (nodename.equals("dynamics")) 
        return child.getFirstChild().getNodeName();
      else
        throw new InvalidElementException("not dynamics element");
    }
    /*
    // obsolete
    public final boolean hasDirection(String name) {
      return hasChild(name);
    }
    // obsolete
    public final String directionType(String name) {
      return getAttribute(getChildByTagName(name), "type");
    }

    // obsolete
    public final int directionNumber(String name) {
      return getAttributeInt(child, 
      return getAttributeInt(getChildByTagName(name), "number");
    }
     */
  }


  /**********************************************************************
   *<p>StartStopElementクラスは, type属性とnumber属性を持ち, 
   *type属性はstart, stop, continueのいずれか, number属性は正の整数を
   *値に持つ, という性質を満たす要素をラップするクラスです. 
   *********************************************************************/
  public class StartStopElement extends NodeInterface {
    private String type;    // start, stop, or continue
    private byte number = 1;
    private NodeInterface parent;
    private StartStopElement(Node node, NodeInterface parent) {
      super(node);
      this.parent = parent;
      type = getAttribute(node(), "type");
      if (hasAttribute("number"))
        number = (byte)getAttributeInt("number");
    }
    /**********************************************************************
     *<p>Returns "tied|slur|tuplet|glissando|slide".</p>
     *********************************************************************/
    @Override
    protected final String getSupportedNodeName() {
      return "tied|slur|tuplet|glissando|slide";
    }
    /**********************************************************************
     *<p>このオブジェクトがラップする要素の親ノードのNodeInterfaceオブジェクトを
     *返します.</p>
     *********************************************************************/
    public final NodeInterface getParentNodeInterface() {
      return parent;
    }
    /**********************************************************************
     *<p>type属性で指定されているstart, stop, continueのいずれかを返します.</p>
     *********************************************************************/
    public final String type() {
      return type;
    }
    /**********************************************************************
     *<p>number属性で指定されている正の整数を返します. 
     *number属性がないときは1を返します.</p>
     *********************************************************************/
    public final byte number() {
      return number;
    }
    /**********************************************************************
     *<p>このオブジェクトの文字列表現を返します.</p>
     *********************************************************************/
    public String toString() {
      return getNodeName() + "[type=" + type() + ", number=" + number() + "]";
    }
  }
}
