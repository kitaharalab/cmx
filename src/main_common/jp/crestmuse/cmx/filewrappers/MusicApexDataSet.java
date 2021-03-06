package jp.crestmuse.cmx.filewrappers;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.MusicData;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;

//import jp.crestmuse.cmx.handlers.CommonNoteHandler;
//import jp.crestmuse.cmx.misc.NoteCompatible;
//import jp.crestmuse.cmx.misc.PianoRollCompatible;

/**
 * <p>
 * MusicXMLから音楽構造グルーピングを生成するためのクラスです。 インスタンスの生成にはMusicXMLWrapperを渡す必要があり、
 * トップレベルのグループには全てのNoteが生成されます
 * 
 * <ul>
 * <li>使い方
 * 
 * <pre>
 *  MusicXMLWrapper musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile("./sample.xml");
 *    MusicXMLのインスタンス化
 *  MusicApexDataSet ads = new MusicApexDataSet(musicxml);
 *    MusicApexDataSetにMusicXMLを渡しインスタンス化
 *  ads.createTopLevelGroup(true);
 *    inheritedを指定し、トップレベルグループを作成
 *  ads.setAspect("sample-aspect");
 *    何に着目したかを設定(任意)
 *  ads.topgroup.makeSubgroup(notes);
 *    トップレベルからグループを作成
 *  〜〜
 *  
 *  ads.toWrapper();
 *    MusicXMLWrapper化
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * @author R.Tokuami
 */
public class MusicApexDataSet {

  private final MusicXMLWrapper musicxml;
  private boolean inherited = false;
  private String aspect = null;
  private AbstractGroup topGroup = null;
  private HashMap<Node, Note> noteMap = new HashMap<Node, Note>();
  private final int type;

  /**
   * MuscXMLを元に、MusicApexDataSetオブジェクトを作成します。
   * 
   * @param musicxml
   */
  public MusicApexDataSet(MusicXMLWrapper musicxml, int type,
      boolean apexInherited) {
    this.musicxml = musicxml;
    this.type = type;
    this.inherited = apexInherited;
  }

  MusicApexDataSet(MusicXMLWrapper musicxml, NoteGroup topGroup) {
    this.musicxml = musicxml;
    type = 1;
    inherited = topGroup.isApexInherited();
    this.topGroup = copyGroup(topGroup, (AbstractGroup) createGroup());
  }

  private AbstractGroup copyGroup(NoteGroup src, AbstractGroup dst) {
    dst.ownNotes.addAll(src.getNotes());
    dst.underNotes.addAll(src.getAllNotes());
    // TODO copy attribute
    dst.depth = src.depth();
    dst.apexStart = src.getApexStart();
    dst.apexStop = src.getApexStop();
    dst.apexStartTime = src.getApexStartTime();
    dst.apexStopTime = src.getApexStopTime();
    dst.saliency = src.getApexSaliency();
    dst.implicit = src.isImplicit();
    for (NoteGroup ng : src.getSubgroups()) {
      AbstractGroup subGroup = copyGroup(ng, (AbstractGroup) createGroup());
      subGroup.parent = dst;
      if (inherited && dst.apexStart == subGroup.apexStart
          && dst.apexStartTime == subGroup.apexStartTime
          && dst.apexStop == subGroup.apexStop
          && dst.apexStopTime == subGroup.apexStopTime)
        subGroup.inheritedApexFromParent = true;
      dst.subGroups.add(subGroup);
    }
    return dst;
  }

  /**
   * MusicXMLに含まれるすべてのノートを含むトップレベルグループを作成します。
   * 
   * @return トップレベルグループのNoteGroupオブジェクト
   */
  public NoteGroup createTopLevelGroup() {
    topGroup = (AbstractGroup) createGroup();
    topGroup.depth = 1;
    // MusicXMLのすべてのNote要素をグループに追加する
    musicxml.processNotePartwise(new NoteHandlerPartwise() {
        public void beginPart(Part part, MusicXMLWrapper w) {}
        public void endPart(Part part, MusicXMLWrapper w) {}
        public void beginMeasure(Measure m, MusicXMLWrapper w) {}
        public void endMeasure(Measure m, MusicXMLWrapper w) {}
        public void processMusicData(MusicData md, MusicXMLWrapper w) {
          if (md instanceof MusicXMLWrapper.Note) {
            MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
            if (!note.rest()) {
              topGroup.addNote(note);
              noteMap.put(note.node(), note);
            }
          }
        }
      });
    return topGroup;
  }          

/*        
    musicxml.processNotes(new CommonNoteHandler() {

      public void processNote(NoteCompatible note,
          PianoRollCompatible filewrapper) {
        Note n = (Note) note;
        topGroup.addNote(n);
        noteMap.put(n.node(), n);
      }


      public void endPart(String id, PianoRollCompatible filewrapper) {
      }

      public void beginPart(String id, PianoRollCompatible filewrapper) {
      }

    });
    return topGroup;
  }
*/

  /**
   * どこのグループにも属さない空のApexDataGroupオブジェクトを作成します。
   * 
   * @return
   */
  public NoteGroup createGroup() {
    return createGroup(new ArrayList<Note>(0));
  }

  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * 
   * @param notes
   *          このグループに含まれるNoteオブジェクトのリスト
   * @return
   */
  public NoteGroup createGroup(List<Note> notes) {
    return createGroup(notes, null);
  }

  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * 
   * @param notes
   *          このグループに含まれるNoteオブジェクトのリスト
   * @param apex
   *          このグループの頂点のNote
   * @return
   */
  public NoteGroup createGroup(List<Note> notes, Note apex) {
    return createGroup(notes, apex, Double.NaN);
  }

  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * 
   * @param notes
   *          このグループに含まれるNoteオブジェクトのリスト
   * @param apex
   *          このグループの頂点のNote
   * @param saliency
   *          頂点のNoteがどのぐらい目立っているか
   * @return
   */
  public NoteGroup createGroup(List<Note> notes, Note apex, double saliency) {
    NoteGroup group;
    switch (type) {
    case 0:
      group = new NoteGroupType0();
      break;
    case 1:
      group = new NoteGroupType1();
      break;
    default:
      throw new ExceptionIllegalType();
    }
    for (Note n : notes)
      group.addNote(n);
    if (apex != null)
      group.setApex(apex);
    group.setApexSaliency(saliency);
    return group;
  }

  /**
   * 作成するMusicApexDataSetクラスが何に着目した楽曲構造かをセットします。
   * 1つのMusiApexDataSetクラスは一つのaspectを持ちます。
   * 
   * @param aspect
   *          着目した対象
   */
  public void setAspect(String aspect) {
    this.aspect = aspect;
  }

  /**
   * トップレベルグループのインスタンスを返します。
   * 
   * @return トップレベルのApexDataGroupオブジェクト
   */
  public NoteGroup topgroup() {
    return topGroup;
  }

  /**
   * 作成したDataSetからXMLを作成し、MusicApexWrapperを生成して返します。
   * XMLテキスト上に出力されるグループの順番は、親グループに先に追加されたものが先に記述されます。
   * 
   * @return このMusicApexDataSetを元にしたMusicApexWrapperオブジェクト
   * @throws ExceptionNoTopGroup
   *           トップレベルグループが作られていない、MusicXMLが指定されていない場合
   */
  public MusicApexWrapper toWrapper() {
    if (topGroup == null)
      throw new ExceptionNoTopGroup();
    MusicApexWrapper mawxml = MusicApexWrapper.createMusicApexWrapperFor(musicxml);
    mawxml.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink",
        "http://www.w3.org/1999/xlink");
    mawxml.setAttribute("target", musicxml.getFileName());
    mawxml.setAttribute("apex-inherited", (inherited ? "yes" : "no"));
    if (aspect != null)
      mawxml.setAttribute("aspect", aspect);
    writeApexDataGroup(mawxml, topGroup);
    return mawxml;
  }

  private void writeApexDataGroup(MusicApexWrapper mawxml, NoteGroup group) {
    mawxml.addChild("group");
    if (group.depth() == -1)
      throw new ExceptionInvalidGroupDepth();
    mawxml.setAttribute("depth", group.depth());
    if (group.isImplicit())
      mawxml.setAttribute("implicit", "yes");
    for (NoteGroup adg : group.getSubgroups())
      writeApexDataGroup(mawxml, adg);
    if (!(group.getNotes().isEmpty()))
      for (Note n : group.getNotes()) {
        mawxml.addChild("note");
        mawxml.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
            "#xpointer(" + n.getXPathExpression() + ")");
        mawxml.returnToParent();
      }
    else
      throw new ExceptionEmptyNoteGroup();
    // write apex
    // if (group.getApex() != null) {
    // mawxml.addChild("apex");
    // mawxml.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
    // "#xpointer(" + group.getApex().getXPathExpression() + ")");
    // if (!(Double.isNaN(group.getApexSaliency())))
    // mawxml.setAttribute("saliency", group.getApexSaliency());
    // mawxml.returnToParent();
    // } else
    if (group.getApexStart() != null && group.getApexStop() != null) {
      mawxml.addChild("apex");
      if (!(Double.isNaN(group.getApexSaliency())))
        mawxml.setAttribute("saliency", group.getApexSaliency());
      mawxml.addChild("start");
      mawxml.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
          "#xpointer(" + group.getApexStart().getXPathExpression() + ")");
      mawxml.setAttribute("time", group.getApexStartTime());
      mawxml.returnToParent();
      mawxml.addChild("stop");
      mawxml.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
          "#xpointer(" + group.getApexStop().getXPathExpression() + ")");
      mawxml.setAttribute("time", group.getApexStopTime());
      mawxml.returnToParent();
      mawxml.returnToParent();
    }
    mawxml.returnToParent();
    return;
  }

  // /**
  // * MusicXMLに含まれるNoteのリストのindexが start番目からend番目のNoteをListにして返します
  // *
  // * @param start
  // * @param end
  // * @return
  // */
  // private List<Note> getNotesByRange(int start, int end) {
  // ArrayList<Note> dest = new ArrayList<Note>();
  // for (int i = start; i <= end; i++)
  // dest.add(allnotes.get(i));
  // return dest;
  // }

  private abstract class AbstractGroup implements NoteGroup {

    final List<Note> ownNotes = new ArrayList<Note>();
    final List<Note> underNotes = new ArrayList<Note>();
    final List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    final HashMap<String, String> attribute = new HashMap<String, String>();
    int depth = -1;
    Note apexStart, apexStop;
    double apexStartTime, apexStopTime;
    double saliency = Double.NaN;
    boolean implicit = false;
    AbstractGroup parent;
    boolean inheritedApexFromParent = false;

    AbstractGroup() {
      this(null);
    }

    AbstractGroup(AbstractGroup parent) {
      this.parent = parent;
    }

    public int depth() {
      return depth;
    }

    public List<Note> getNotes() {
      return ownNotes;
    }

    public List<Note> getAllNotes() {
      return underNotes;
    }

    public List<NoteGroup> getSubgroups() {
      return subGroups;
    }

    public Note getApexStart() {
      return apexStart;
    }

    public void setApexStart(Note n, double time) {
      if (inheritedApexFromParent)
        throw new ExceptionSetApex();
      Note prevNote = apexStart;
      double prevTime = apexStartTime;
      apexStart = n;
      apexStartTime = time;
      try {
        inheritedApex();
      } catch (IllegalStateException e) {
        apexStart = prevNote;
        apexStartTime = prevTime;
        throw e;
      }
    }

    public double getApexStartTime() {
      return apexStartTime;
    }

    public Note getApexStop() {
      return apexStop;
    }

    public void setApexStop(Note n, double time) {
      if (inheritedApexFromParent)
        throw new ExceptionSetApex();
      Note prevNote = apexStop;
      double prevTime = apexStopTime;
      apexStop = n;
      apexStopTime = time;
      try {
        inheritedApex();
      } catch (IllegalStateException e) {
        apexStop = prevNote;
        apexStopTime = prevTime;
        throw e;
      }
    }

    public double getApexStopTime() {
      return apexStopTime;
    }

    public Note getApex() {
      if (apexStart != apexStop)
        return apexStart;
      throw new ExceptionGetApex();
    }

    public void setApex(Note n) {
      setApex(n, false);
    }

    public void setApex(Note n, boolean clearInheritedApex) {
      if (inheritedApexFromParent)
        throw new ExceptionSetApex();
      apexStart = apexStop = n;
      apexStartTime = 0;
      if (n == null) {
        apexStopTime = 0;
        for (NoteGroup ng : subGroups) {
          if (((AbstractGroup) ng).inheritedApexFromParent) {
            ((AbstractGroup) ng).inheritedApexFromParent = false;
            if (clearInheritedApex)
              ((NoteGroupType1) ng).setApex(null, true);
            break;
          }
        }
      } else {
        apexStopTime = n.actualDuration();
        inheritedApex();
      }
    }

    protected void inheritedApex() {
      if (!inherited || apexStart == null || apexStop == null)
        return;
      int TPB = 480;
      double apexOnset = apexStart.onset(TPB) + TPB * apexStartTime;
      double apexOffset = apexStop.onset(TPB) + TPB * apexStopTime;
      for (NoteGroup ng : subGroups) {
        double onset = Double.MAX_VALUE;
        double offset = Double.MIN_VALUE;
        for (Note n : ng.getAllNotes()) {
          int on = n.onset(TPB);
          int off = n.offset(TPB);
          onset = Math.min(onset, on);
          offset = Math.max(offset, off);
        }
        if (apexOnset < onset && onset < apexOffset || apexOnset < offset
            && offset < apexOffset)
          throw new ExceptionSetApexOverSubgroups();
        if (onset <= apexOnset && apexOffset <= offset) {
          ((AbstractGroup) ng).inheritedApexFromParent = false;
          ng.setApexStart(apexStart, apexStartTime);
          ng.setApexStop(apexStop, apexStopTime);
          ((AbstractGroup) ng).inheritedApexFromParent = true;
          break;
        }
      }
    }

    public double getApexSaliency() {
      return saliency;
    }

    public void setApexSaliency(double saliency) {
      this.saliency = saliency;
    }

    public String getAttribute(String key) {
      return attribute.get(key);
    }

    public List<Note> getImplicitGroupNotes() {
      ArrayList<Note> result = new ArrayList<Note>();
      result.addAll(getNotes());
      for (NoteGroup ng : subGroups)
        if (ng.isImplicit())
          result.addAll(ng.getAllNotes());
      return result;
    }

    public boolean isApexInherited() {
      return inherited;
    }

    public boolean isImplicit() {
      return implicit;
    }

    public void setImplicit(boolean value) {
      implicit = value;
    }

    public int type() {
      return type;
    }

    public void setAttribute(String key, String value) {
      attribute.put(key, value);
    }

    public Object clone() throws CloneNotSupportedException {
      AbstractGroup result = (AbstractGroup) createGroup();
      result.ownNotes.addAll(ownNotes);
      result.underNotes.addAll(underNotes);
      for (NoteGroup sg : subGroups) {
        AbstractGroup subGroup = (AbstractGroup) sg.clone();
        subGroup.parent = result;
        subGroup.inheritedApexFromParent = ((AbstractGroup) sg).inheritedApexFromParent;
        result.subGroups.add(subGroup);
      }
      result.attribute.putAll(attribute);
      result.depth = depth;
      result.apexStart = apexStart;
      result.apexStop = apexStop;
      result.apexStartTime = apexStartTime;
      result.apexStopTime = apexStopTime;
      result.saliency = saliency;
      result.implicit = implicit;
      return result;
    };

  }

  private class NoteGroupType0 extends AbstractGroup {

    public void addNote(Note n) {
    }

    public void addSubgroup(NoteGroup g) {
    }

    public NoteGroup makeSubgroup(List<Note> notes) {
      return null;
    }

    public void removeNote(Note n) {
    }

    public void removeSubgroup(NoteGroup g) {
    }

  }

  public class NoteGroupType1 extends AbstractGroup {

    private NoteGroupType1() {
    }

    public void addNote(Note n) {
      if (underNotes.contains(n))
        throw new IllegalArgumentException("already contain");
      if (depth <= 1) {
        ownNotes.add(n);
        underNotes.add(n);
        return;
      }
      underNotes.add(n);
      NoteGroup brother = null;
      for (NoteGroup ng : parent.subGroups)
        if (ng != this && ng.getAllNotes().contains(n)) {
          ((NoteGroupType1) ng).removeNoteWithoutForce(n);
          brother = ng;
          break;
        }
      if (brother != null && brother.getAllNotes().isEmpty())
        parent.subGroups.remove(brother);
      if (subGroups.isEmpty())
        ownNotes.add(n);
      else
        for (NoteGroup ng : subGroups)
          try {
            ng.addNote(n);
          } catch (IllegalArgumentException e) {
          }
    }

    public void addSubgroup(NoteGroup g) {
      // if (isImplicit())
      // throw new IllegalStateException("group is implicit");
      if (!subGroups.isEmpty())
        throw new ExceptionAddSubgroup();
      if (!isContinuation(g.getAllNotes()))
        throw new ExceptionAddSubgroupNotContinuous();
      addSubgroupForce(g, ownNotes);
    }

    private void addSubgroupForce(NoteGroup g, List<Note> notes) {
      int TPB = 480;
      int leastOnset = Integer.MAX_VALUE;
      int biggestOffset = 0;
      for (Note n : g.getAllNotes()) {
        leastOnset = Math.min(leastOnset, n.onset(TPB));
        biggestOffset = Math.max(biggestOffset, n.offset(TPB));
      }
      LinkedList<Note> forwardGroup = new LinkedList<Note>();
      LinkedList<Note> backwardGroup = new LinkedList<Note>();
      for (Note n : notes)
        if (n.onset(TPB) < leastOnset)
          forwardGroup.add(n);
        else if (n.offset(TPB) > biggestOffset)
          backwardGroup.add(n);
      if (!forwardGroup.isEmpty()) {
        NoteGroupType1 ng = (NoteGroupType1) createGroup(forwardGroup);
        ng.setImplicit(true);
        ng.parent = this;
        ng.depth = this.depth + 1;
        subGroups.add(ng);
      }
      subGroups.add(g);
      ((NoteGroupType1) g).parent = this;
      ((NoteGroupType1) g).depth = this.depth + 1;
      if (!backwardGroup.isEmpty()) {
        NoteGroupType1 ng = (NoteGroupType1) createGroup(backwardGroup);
        ng.setImplicit(true);
        ng.parent = this;
        ng.depth = this.depth + 1;
        subGroups.add(ng);
      }
    }

    public NoteGroup makeSubgroup(List<Note> notes) {
      if (!isContinuation(notes))
        throw new ExceptionAddSubgroupNotContinuous();
      for (Note n : notes)
        if (!underNotes.contains(n))
          throw new ExceptionMakeSubgroup();
      if (inherited && !validApex(notes))
        throw new ExceptionInvalidApex();
      if (!subGroups.isEmpty()) {
        IllegalArgumentException iae = new IllegalArgumentException();
        for (NoteGroup ng : subGroups)
          try {
            return ng.makeSubgroup(notes);
          } catch (IllegalArgumentException e) {
            iae = e;
          }
        throw iae;
      }
      NoteGroupType1 ng = (NoteGroupType1) createGroup(notes);
      // if (isImplicit()) {
      // parent.subGroups.remove(this);
      // ((NoteGroupType1) parent).addSubgroupForce(ng, getAllNotes());
      // } else
      // addSubgroup(ng);
      addSubgroup(ng);
      inheritedApex();
      return ng;
    }

    private boolean validApex(List<Note> notes) {
      if (apexStart == null || apexStop == null)
        return true;
      int TPB = 480;
      double onset = Double.MAX_VALUE;
      double offset = Double.MIN_VALUE;
      for (Note n : notes) {
        int on = n.onset(TPB);
        int off = n.offset(TPB);
        onset = Math.min(onset, on);
        offset = Math.max(offset, off);
      }
      double apexOnset = apexStart.onset(TPB) + TPB * apexStartTime;
      double apexOffset = apexStop.onset(TPB) + TPB * apexStopTime;
      if (apexOnset < onset && onset < apexOffset || apexOnset < offset
          && offset < apexOffset)
        return false;
      return true;
    }

    public void removeNote(Note n) {
      if (depth == 1)
        throw new ExceptionRemoveTopGroup();
      int TPB = 480;
      NoteGroup prev, current = this.parent;
      while (!current.getSubgroups().isEmpty()) {
        prev = current;
        for (NoteGroup ng : current.getSubgroups())
          if (ng.getAllNotes().contains(n)) {
            current = ng;
            break;
          }
        NoteGroup imp = createGroup();
        imp.setImplicit(true);
        for (Note note : current.getAllNotes())
          if (note.onset(TPB) >= n.onset(TPB)
              && note.offset(TPB) <= n.offset(TPB)) {
            current.getNotes().remove(n);
            current.getAllNotes().remove(n);
            imp.addNote(note);
          }
        ((NoteGroupType1) prev).subGroups.add(imp);
      }
    }

    private void removeNoteWithoutForce(Note n) {
      for (NoteGroup ng : subGroups)
        ((NoteGroupType1) ng).removeNoteWithoutForce(n);
      ownNotes.remove(n);
      underNotes.remove(n);
    }

    public void removeSubgroup(NoteGroup g) {
      if (!subGroups.contains(g))
        throw new ExceptionRemoveSubGroup();
      if (g.getSubgroups().isEmpty()) {
        List<NoteGroup> brother = subGroups;
        if (brother.size() == 1) {
          subGroups.remove(g);
          return;
        }
        boolean allImp = true;
        for (NoteGroup ng : brother) {
          if (ng == g)
            continue;
          allImp &= ng.isImplicit();
        }
        if (allImp) {
          for (NoteGroup ng : new ArrayList<NoteGroup>(brother))
            subGroups.remove(ng);
          return;
        }
        Collections.sort(brother, new Comparator<NoteGroup>() {
          int noteGroupOnset(NoteGroup ng) {
            int TPB = 480;
            int onset = Integer.MAX_VALUE;
            for (Note n : ng.getAllNotes())
              onset = Math.min(onset, n.onset(TPB));
            return onset;
          }

          public int compare(NoteGroup o1, NoteGroup o2) {
            return noteGroupOnset(o1) - noteGroupOnset(o2);
          }
        });
        int selectedGroupIndex;
        for (selectedGroupIndex = 0; selectedGroupIndex < brother.size(); selectedGroupIndex++)
          if (brother.get(selectedGroupIndex) == g)
            break;
        if (selectedGroupIndex == 0) {
          NoteGroup next = brother.get(selectedGroupIndex + 1);
          if (next.isImplicit()) {
            subGroups.remove(g);
            for (Note n : g.getAllNotes()) {
              ((NoteGroupType1) next).ownNotes.add(n);
              ((NoteGroupType1) next).underNotes.add(n);
            }
          } else
            g.setImplicit(true);
        } else if (selectedGroupIndex == brother.size() - 1) {
          NoteGroup prev = brother.get(selectedGroupIndex - 1);
          if (prev.isImplicit()) {
            subGroups.remove(g);
            for (Note n : g.getAllNotes()) {
              ((NoteGroupType1) prev).ownNotes.add(n);
              ((NoteGroupType1) prev).underNotes.add(n);
            }
          } else
            g.setImplicit(true);
        } else {
          NoteGroup prev = brother.get(selectedGroupIndex - 1);
          NoteGroup next = brother.get(selectedGroupIndex + 1);
          if (!prev.isImplicit() && !next.isImplicit())
            g.setImplicit(true);
          else if (prev.isImplicit() && next.isImplicit()) {
            subGroups.remove(g);
            subGroups.remove(next);
            for (Note n : g.getAllNotes())
              prev.addNote(n);
            for (Note n : next.getAllNotes())
              prev.addNote(n);
          } else {
            subGroups.remove(g);
            NoteGroup dst = prev.isImplicit() ? prev : next;
            for (Note n : g.getAllNotes())
              dst.addNote(n);
          }
        }
      } else {
        decrementDepth(g);
        subGroups.remove(g);
        for (NoteGroup ng : g.getSubgroups()) {
          ((NoteGroupType1) ng).parent = this;
          subGroups.add(ng);
        }
      }
    }

    private void decrementDepth(NoteGroup ng) {
      ((NoteGroupType1) ng).depth--;
      for (NoteGroup g : ng.getSubgroups())
        decrementDepth(g);
    }

    private boolean isContinuation(List<Note> notes) {
      final int tpb = 480;
      class Event implements Comparable<Event> {

        boolean onset;
        int tick;

        Event(Note note, boolean onset) {
          this.onset = onset;
          if (onset)
            tick = note.onset(tpb);
          else
            tick = note.offset(tpb);
        }

        public int compareTo(Event o) {
          if (tick == o.tick) {
            if (onset && !o.onset)
              return -1;
            if (!onset && o.onset)
              return 1;
          }
          return tick - o.tick;
        }

        public String toString() {
          return String.format("tick:%d, onset:%s", tick, onset);
        }

      }
      LinkedList<Event> events = new LinkedList<Event>();
      for (Note n : notes) {
        events.add(new Event(n, true));
        events.add(new Event(n, false));
      }
      Collections.sort(events);
      events.removeLast();
      int stack = 0;
      for (Event e : events)
        if (e.onset)
          stack++;
        else {
          stack--;
          if (stack <= 0)
            return false;
        }
      return true;
    }

    public void divide(Note note) {
      if (depth == 1)
        throw new ExceptionDivideTopGroup();
      parent.subGroups.remove(this);
      try {
        parseDivideGroup(parent, this, note);
        parent.inheritedApex();
      } catch (IllegalArgumentException e) {
        parent.subGroups.add(this);
        throw e;
      }
    }

    private void parseDivideGroup(NoteGroup dst, NoteGroup src, Note note) {
      ArrayList<Note> forward = new ArrayList<Note>();
      ArrayList<Note> backward = new ArrayList<Note>();
      int TPB = 480;
      int sep = note.onset(TPB);
      for (Note n : src.getAllNotes())
        if (!dst.getAllNotes().contains(n))
          continue;
        else if (n.onset(480) < sep)
          forward.add(n);
        else
          backward.add(n);
      if (inherited && (!validApex(forward) || !validApex(backward)))
        throw new ExceptionInvalidApex();
      NoteGroupType1 fwdDst = null, bwdDst = null;
      if (!forward.isEmpty()) {
        fwdDst = (NoteGroupType1) createGroup(forward);
        fwdDst.parent = (AbstractGroup) dst;
        fwdDst.depth = dst.depth() + 1;
        for (NoteGroup ng : src.getSubgroups())
          parseDivideGroup(fwdDst, ng, note);
      }
      if (!backward.isEmpty()) {
        bwdDst = (NoteGroupType1) createGroup(backward);
        bwdDst.parent = (AbstractGroup) dst;
        bwdDst.depth = dst.depth() + 1;
        for (NoteGroup ng : src.getSubgroups())
          parseDivideGroup(bwdDst, ng, note);
      }
      if (!forward.isEmpty())
        ((NoteGroupType1) dst).subGroups.add(fwdDst);
      if (!backward.isEmpty())
        ((NoteGroupType1) dst).subGroups.add(bwdDst);
    }

    public NoteGroup mergeGroup(NoteGroup dst) {
      NoteGroupType1 left, right;
      if (noteGroupOnset(this) < noteGroupOnset(dst)) {
        left = this;
        right = (NoteGroupType1) dst;
      } else {
        left = (NoteGroupType1) dst;
        right = this;
      }
      parent.subGroups.remove(left);
      parent.subGroups.remove(right);
      ArrayList<Note> all = new ArrayList<Note>(left.getAllNotes());
      all.addAll(right.getAllNotes());
      NoteGroupType1 mergedGroup = (NoteGroupType1) createGroup(all);
      mergedGroup.depth = left.depth;
      mergedGroup.implicit = left.implicit && right.implicit;
      mergedGroup.parent = parent;
      parent.subGroups.add(mergedGroup);
      ArrayList<NoteGroup> cat = new ArrayList<NoteGroup>(left.subGroups);
      cat.addAll(right.subGroups);
      for (NoteGroup ng : cat) {
        mergedGroup.addSubgroupWithoutForce((NoteGroupType1) ng);
        all.removeAll(ng.getAllNotes());
      }
      NoteGroupType1 imp = null;
      if (cat.size() > 0 && all.size() > 0) {
        imp = (NoteGroupType1) createGroup(all);
        imp.implicit = true;
        mergedGroup.addSubgroupWithoutForce(imp);
      }
      if (!left.subGroups.isEmpty() || !right.subGroups.isEmpty()) {
        NoteGroup leftEdge = left.subGroups.isEmpty() ? imp
            : getLastSubGroup(left);
        NoteGroup rightEdge = right.subGroups.isEmpty() ? imp
            : getFirstSubGroup(right);
        ((NoteGroupType1) leftEdge).mergeGroup(rightEdge);
      }
      return mergedGroup;
    }

    private void addSubgroupWithoutForce(NoteGroupType1 subGroup) {
      subGroup.setDepthRec(depth + 1);
      subGroup.parent = this;
      subGroups.add(subGroup);
    }

    private void setDepthRec(int depth) {
      this.depth = depth;
      for (NoteGroup ng : subGroups)
        ((NoteGroupType1) ng).setDepthRec(depth + 1);
    }

    // public void mergeGroup(NoteGroup dst) {
    // boolean srcLeft = noteGroupOnset(this) < noteGroupOnset(dst);
    // parent.subGroups.remove(this);
    // margeNoEmpties(this, dst, srcLeft);
    // }

    // private void margeNoEmpties(NoteGroup src, NoteGroup dst, boolean
    // srcLeft) {
    // if (src.getSubgroups().isEmpty() && dst.getSubgroups().isEmpty()) {
    // for (Note n : src.getAllNotes()) {
    // ((NoteGroupType1) dst).ownNotes.add(n);
    // ((NoteGroupType1) dst).underNotes.add(n);
    // }
    // } else if (src.getSubgroups().isEmpty() && !dst.getSubgroups().isEmpty())
    // {
    // for (Note n : src.getAllNotes())
    // ((NoteGroupType1) dst).underNotes.add(n);
    // margeEmptyAndNoempty(src, dst, srcLeft);
    // } else if (dst.getSubgroups().isEmpty() && !src.getSubgroups().isEmpty())
    // {
    // parent.subGroups.remove(dst);
    // for (Note n : dst.getAllNotes())
    // ((NoteGroupType1) src).underNotes.add(n);
    // margeEmptyAndNoempty(dst, src, !srcLeft);
    // parent.subGroups.add(src);
    // } else {
    // for (Note n : src.getAllNotes())
    // ((NoteGroupType1) dst).underNotes.add(n);
    // NoteGroup srcEdge, dstEdge;
    // if (srcLeft) {
    // srcEdge = getLastSubGroup(src);
    // dstEdge = getFirstSubGroup(dst);
    // } else {
    // srcEdge = getFirstSubGroup(src);
    // dstEdge = getLastSubGroup(dst);
    // }
    // margeNoEmpties(srcEdge, dstEdge, srcLeft);
    // for (NoteGroup ng : src.getSubgroups())
    // if (ng != srcEdge)
    // ((NoteGroupType1) dst).subGroups.add(ng);
    // }
    // }

    // private void margeEmptyAndNoempty(NoteGroup empty, NoteGroup full,
    // boolean emptyLeft) {
    // NoteGroup edge = full;
    // while (!edge.getSubgroups().isEmpty()) {
    // if (emptyLeft)
    // edge = getFirstSubGroup(edge);
    // else
    // edge = getLastSubGroup(edge);
    // for (Note n : empty.getAllNotes())
    // ((NoteGroupType1) edge).underNotes.add(n);
    // }
    // for (Note n : empty.getAllNotes())
    // ((NoteGroupType1) edge).ownNotes.add(n);
    // }

    private int noteGroupOnset(NoteGroup ng) {
      int TPB = 480;
      int onset = Integer.MAX_VALUE;
      for (Note n : ng.getAllNotes())
        onset = Math.min(onset, n.onset(TPB));
      return onset;
    }

    private NoteGroup getFirstSubGroup(NoteGroup superGroup) {
      NoteGroup result = superGroup.getSubgroups().get(0);
      int minOnset = noteGroupOnset(result);
      for (NoteGroup sub : superGroup.getSubgroups()) {
        int onset = noteGroupOnset(sub);
        if (onset < minOnset) {
          minOnset = onset;
          result = sub;
        }
      }
      return result;
    }

    private NoteGroup getLastSubGroup(NoteGroup superGroup) {
      NoteGroup result = superGroup.getSubgroups().get(0);
      int maxOnset = noteGroupOnset(result);
      for (NoteGroup sub : superGroup.getSubgroups()) {
        int onset = noteGroupOnset(sub);
        if (onset > maxOnset) {
          maxOnset = onset;
          result = sub;
        }
      }
      return result;
    }

    public void asSubGroup(NoteGroup dst) {
      if (depth == 1)
        throw new ExceptionAsSubGroup();
      parent.subGroups.remove(this);
      for (Note n : underNotes)
        ((NoteGroupType1) dst).underNotes.add(n);
      if (dst.getSubgroups().isEmpty()) {
        NoteGroupType1 ng = (NoteGroupType1) createGroup(dst.getNotes());
        for (Note n : ownNotes)
          ((NoteGroupType1) dst).underNotes.add(n);
        ng.implicit = true;
        ng.parent = (NoteGroupType1) dst;
        ng.depth = dst.depth() + 1;
        ((NoteGroupType1) dst).subGroups.add(ng);
      }
      parent = (NoteGroupType1) dst;
      incrementDepth(this);
      ((NoteGroupType1) dst).subGroups.add(this);
    }

    private void incrementDepth(NoteGroup ng) {
      ((NoteGroupType1) ng).depth++;
      for (NoteGroup g : ng.getSubgroups())
        incrementDepth(g);
    }

    public NoteGroup makeSuperGroup(List<NoteGroup> ng) {
      if (depth == 1)
        throw new ExceptionMakeSuperGroup();
      parent.subGroups.remove(this);
      for (NoteGroup nngg : ng)
        parent.subGroups.remove(nngg);
      ArrayList<Note> notes = new ArrayList<Note>();
      notes.addAll(underNotes);
      for (NoteGroup nngg : ng)
        notes.addAll(nngg.getAllNotes());
      NoteGroupType1 newNg = (NoteGroupType1) createGroup(notes);
      newNg.depth = depth;
      newNg.parent = parent;
      newNg.setImplicit(true);
      parent.subGroups.add(newNg);
      incrementDepth(this);
      for (NoteGroup nngg : ng)
        incrementDepth(nngg);
      parent = newNg;
      newNg.subGroups.add(this);
      for (NoteGroup nngg : ng) {
        ((NoteGroupType1) nngg).parent = newNg;
        newNg.subGroups.add(nngg);
      }
      return newNg;
    }

    public void changeDividePos(NoteGroup dst, Note srcEdge) {
      boolean srcLeft = noteGroupOnset(this) < noteGroupOnset(dst);
      int TPB = 480;
      ArrayList<Note> moveNotes = new ArrayList<Note>();
      if (srcLeft) {
        for (Note n : underNotes)
          if (n.onset(TPB) > srcEdge.onset(TPB))
            moveNotes.add(n);
      } else
        for (Note n : underNotes)
          if (n.onset(TPB) < srcEdge.onset(TPB))
            moveNotes.add(n);
      if (inherited && !validApex(moveNotes))
        throw new ExceptionInvalidApex();
      for (Note n : moveNotes)
        removeNoteWithoutForce(n);
      for (Note n : moveNotes)
        ((NoteGroupType1) dst).underNotes.add(n);
      if (srcLeft) {
        while (!dst.getSubgroups().isEmpty()) {
          dst = getFirstSubGroup(dst);
          for (Note n : moveNotes)
            ((NoteGroupType1) dst).underNotes.add(n);
        }
        for (Note n : moveNotes)
          ((NoteGroupType1) dst).ownNotes.add(n);
      } else {
        while (!dst.getSubgroups().isEmpty()) {
          dst = getLastSubGroup(dst);
          for (Note n : moveNotes)
            ((NoteGroupType1) dst).underNotes.add(n);
        }
        for (Note n : moveNotes)
          ((NoteGroupType1) dst).ownNotes.add(n);
      }
    }

  }

  /**
   * typeが0か1でなかったときcreateGroupで投げられる
   */
  private class ExceptionIllegalType extends IllegalStateException {
    ExceptionIllegalType() {
      super(String.format("illegal type: %d", type));
    }
  }

  /**
   * トップグループが無いときtoWrapperで投げられる
   */
  private class ExceptionNoTopGroup extends IllegalStateException {
    ExceptionNoTopGroup() {
      super("Top Group has not created");
    }
  }

  /**
   * 引数のNoteGroupのdepthが-1のときwriteApexDataGroupで投げられる
   */
  private class ExceptionInvalidGroupDepth extends IllegalArgumentException {
    ExceptionInvalidGroupDepth() {
      super("Invalid GroupDepth");
    }
  }

  /**
   * 引数のNoteGroupにNoteが入っていない場合writeApexDataGroupで投げられる
   */
  private class ExceptionEmptyNoteGroup extends IllegalArgumentException {
    ExceptionEmptyNoteGroup() {
      super("Empty NoteGroup");
    }
  }

  /**
   * 継承してるapexを変更しようとしたときsetApexStartおよびsetApexStopで投げられる
   */
  private class ExceptionSetApex extends IllegalStateException {
    ExceptionSetApex() {
      super("apex inherited from parent");
    }
  }

  /**
   * ApexStartとApexStopが同じノートを指していない場合getApexで投げられる
   */
  private class ExceptionGetApex extends IllegalStateException {
    ExceptionGetApex() {
      super("apex start and apex stop is not same");
    }
  }

  /**
   * inherited状態で二つのサブグループをまたぐようにapexを設定しようとしたときinheritedApexで投げられる
   */
  private class ExceptionSetApexOverSubgroups extends IllegalArgumentException {
    ExceptionSetApexOverSubgroups() {
      super("can't set apex");
    }
  }

  /**
   * 既にサブグループを持っているグループにサブグループを追加しようとしたとき NoteGroupType1のaddSubgroupで投げられる
   */
  private class ExceptionAddSubgroup extends IllegalStateException {
    ExceptionAddSubgroup() {
      super("group already has sub group");
    }
  }

  /**
   * 保持しているノートが連続していないグループを追加しようとしたとき NoteGroupType1のaddSubgroupで投げられる
   */
  private class ExceptionAddSubgroupNotContinuous extends
      IllegalArgumentException {
    ExceptionAddSubgroupNotContinuous() {
      super("argument group is not continuous note sequence");
    }
  }

  /**
   * サブグループに無いノートでmakeSubgroupしようとしたときmakeSubgroupで投げられる
   */
  private class ExceptionMakeSubgroup extends IllegalArgumentException {
    ExceptionMakeSubgroup() {
      super("The specified notes are separated into more than one groups.");
    }
  }

  /**
   * inherited状態で既にsetされてるapexを分断しようとしたとき投げられる
   */
  private class ExceptionInvalidApex extends IllegalStateException {
    ExceptionInvalidApex() {
      super("invalid apex");
    }
  }

  /**
   * TopGroupをremoveしようとしたとき投げられる
   */
  private class ExceptionRemoveTopGroup extends IllegalStateException {
    ExceptionRemoveTopGroup() {
      super("can't remove from top group");
    }
  }

  /**
   * サブグループに含まれていないグループをremoveSubgroupしよとしたとき投げられる
   */
  private class ExceptionRemoveSubGroup extends IllegalArgumentException {
    ExceptionRemoveSubGroup() {
      super("argument is not sub group");
    }
  }

  /**
   * トップグループを分割しようとしたとき投げられる
   */
  private class ExceptionDivideTopGroup extends IllegalStateException {
    ExceptionDivideTopGroup() {
      super("top can't group divide");
    }
  }

  /**
   * トップグループをサブグループ化しようとしたとき投げられる
   */
  private class ExceptionAsSubGroup extends IllegalStateException {
    ExceptionAsSubGroup() {
      super("top group has no parent");
    }
  }

  /**
   * トップグループをmakeSuperGroupしようとしたとき投げられる
   */
  private class ExceptionMakeSuperGroup extends IllegalStateException {
    ExceptionMakeSuperGroup() {
      super("top group");
    }
  }

  // /**
  // * MusicApexDataSetクラスで用いる、音楽構造グループ1つを表すクラスです。
  // *
  // * グループの深さ、グループに属するNoteのリスト、子グループのリスト、 頂点、頂点がどのぐらい目立っているかなどを保持し、
  // * グループの状態を取得するメソッド、グループの親子関係を設定するメソッドを提供します。
  // *
  // */
  // private class ApexDataGroup implements NoteGroup {
  //
  // private int depth = -1;
  // private List<Note> ownnotes = new ArrayList<Note>(); // 自分のグループのみが持つノート
  // private List<Note> undernotes = new ArrayList<Note>(); //
  // 自分のグループ以下にあるノート、自分も含む
  // private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
  // private Note apex = null;
  // private double saliency = Double.NaN;
  // private HashMap<String, String> attribute = new HashMap<String, String>();
  //
  // public ApexDataGroup() {
  // }
  //
  // public ApexDataGroup(List<Note> notes, Note apex, double saliency) {
  // this.ownnotes.addAll(notes);
  // this.undernotes.addAll(notes);
  // this.apex = apex;
  // this.saliency = saliency;
  // }
  //
  // public int depth() {
  // return depth;
  // }
  //
  // public boolean isApexInherited() {
  // return inherited;
  // }
  //
  // public double getApexSaliency() {
  // return saliency;
  // }
  //
  // public List<Note> getNotes() {
  // return ownnotes;
  // }
  //
  // public List<Note> getAllNotes() {
  // return undernotes;
  // }
  //
  // public Note getApex() {
  // return apex;
  // }
  //
  // public List<NoteGroup> getSubgroups() {
  // return subGroups;
  // }
  //
  // public void addNote(Note n) {
  // ownnotes.add(n);
  // undernotes.add(n);
  // return;
  // }
  //
  // public void removeNote(Note n) {
  // for (NoteGroup ng : subGroups)
  // ng.removeNote(n);
  // ownnotes.remove(n);
  // undernotes.remove(n);
  // }
  //
  // public void addSubgroup(NoteGroup g) {
  // if (g instanceof ApexDataGroup)
  // refreshSubGroup((ApexDataGroup) g, this.depth() + 1);
  // undernotes.addAll(g.getAllNotes());
  // subGroups.add(g);
  // return;
  // }
  //
  // private void refreshSubGroup(ApexDataGroup g, int depth) {
  // g.depth = depth;
  // if (isApexInherited() && g.getNotes().contains(this.apex)) {
  // g.apex = this.apex;
  // g.saliency = this.saliency;
  // }
  // for (NoteGroup sg : g.getSubgroups())
  // g.refreshSubGroup((ApexDataGroup) sg, depth + 1);
  // return;
  // }
  //
  // public NoteGroup makeSubgroup(List<Note> notes) {
  // return makeSubgroup(notes, null);
  // }
  //
  // /**
  // * このインスタンスから、子としてグループを作成し、追加します。
  // *
  // * @param notes
  // * グループ化するNoteオブジェクトのリスト
  // * @param apex
  // * 作成する子グループの頂点
  // * @throws RuntimeException
  // * このインスタンスにグループ化するノートが含まれていない
  // */
  // public NoteGroup makeSubgroup(List<Note> notes, Note apex) {
  // return makeSubgroup(notes, apex, Double.NaN);
  // }
  //
  // /**
  // * このインスタンスから、子としてグループを作成し、追加します。
  // *
  // * @param notes
  // * グループ化するNoteオブジェクトのリスト
  // * @param apex
  // * 作成する子グループの頂点のNote
  // * @param saliency
  // * 頂点のNoteがどれぐらい目立っているか
  // * @throws RuntimeException
  // * このインスタンスにグループ化するノートが含まれていない
  // */
  // public NoteGroup makeSubgroup(List<Note> notes, Note apex, double saliency)
  // {
  // // 各ノートがグループを作成する親グループまたはそのdepth+1の範囲に含まれるかチェック
  // for (Note checknote : notes) {
  // Boolean included = false;
  // if (!(included = ownnotes.contains(checknote))) {
  // for (NoteGroup ng : getSubgroups()) {
  // if (ng.getNotes().contains(checknote)) {
  // included = true;
  // break;
  // }
  // }
  // }
  // if (included == false)
  // throw new RuntimeException(
  // "Note is not included Parent and Parent's subgroups");
  // }
  // // making new group
  // ApexDataGroup g = new ApexDataGroup();
  // // g.groupParent = this;
  // g.ownnotes.addAll(notes);
  // g.undernotes.addAll(notes);
  // g.depth = this.depth + 1;
  //
  // if (isApexInherited() == true && this.getApex() != null
  // && g.ownnotes.contains(this.apex)) {
  // g.apex = this.apex;
  // g.saliency = this.saliency;
  // } else {
  // g.apex = apex;
  // g.saliency = saliency;
  // }
  // // add to parent group
  // subGroups.add(g);
  // ownnotes.removeAll(notes);
  // return g;
  // }
  //
  // public void setApex(Note n) {
  // if (this.apex != null)
  // throw new RuntimeException("This group already has Apex. : "
  // + n.getXPathExpression());
  // this.apex = n;
  // }
  //
  // public void setApex(Note n, double value) {
  // if (inherited)
  // throw new RuntimeException("This Apex is inherited");
  // this.apex = n;
  // this.saliency = value;
  // }
  //
  // public void removeSubgroup(NoteGroup g) {
  // ownnotes.addAll(g.getAllNotes());
  // subGroups.remove(g);
  // }
  //
  // public String getAttribute(String key) {
  // return attribute.get(key);
  // }
  //
  // public void setAttribute(String key, String value) {
  // attribute.put(key, value);
  // }
  //
  // public List<Note> getImplicitGroupNotes() {
  // return null;
  // }
  //
  // public boolean isImplicit() {
  // return false;
  // }
  //
  // public int type() {
  // return 0;
  // }
  //
  // public Note getApexStart() {
  // return null;
  // }
  //
  // public double getApexStartTime() {
  // return 0;
  // }
  //
  // public Note getApexStop() {
  // return null;
  // }
  //
  // public double getApexStopTime() {
  // return 0;
  // }
  //
  // public void setApexSaliency(double saliency) {
  // }
  //
  // public void setApexStart(Note n, double time) {
  // }
  //
  // public void setApexStop(Note n, double time) {
  // }
  //
  // public void setImplicit(boolean value) {
  // }
  //
  // }

  // public static void main(String[] args) {
  // MusicXMLWrapper musicxml = new MusicXMLWrapper();
  // try {
  // musicxml = (MusicXMLWrapper) CMXFileWrapper.readfile("./sample.xml");
  // MusicApexDataSet ads = new MusicApexDataSet(musicxml);
  // ads.createTopLevelGroup(true);
  // ads.setAspect("hoge");
  // ads.toplevel.setApex(ads.allnotes.get(8));
  // ads.toplevel.makeSubgroup(ads.getNotesByRange(5, 10), ads.allnotes.get(6));
  // ads.toplevel.makeSubgroup(ads.getNotesByRange(20, 30));
  // ((ApexDataGroup) ads.toplevel.subGroups.get(1)).makeSubgroup(
  // ads.getNotesByRange(24, 28), ads.allnotes.get(28));
  // // ApexDataGroup gp =
  // // (ApexDataGroup)mad.createGroup(mad.getNotesByRange(10, 15));
  // // mad.grouptop.addSubgroup(gp);
  // // ads.toWrapper().writefile(new File("sampleapex.xml"));
  //
  // NodeList nl =
  // musicxml.selectNodeList("/score-partwise/part/measure[@number='1']/note");
  // for (int i = 0; i < nl.getLength(); i++) {
  // System.out.println(nl.item(i).getTextContent());
  // printNote(ads.noteMap.get(nl.item(i)));
  // }
  //
  // Note a = ads.allnotes.get(1);
  // Class c = Note.class;
  // System.out.println(a instanceof NodeInterface);
  // System.out.println(c.getName());
  // System.out.println(a);
  // System.out.println(a.getClass().equals(c));
  // // ads.toWrapper().write(System.out);
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }
  //
  // @Deprecated
  // public static void printNote(Note n) {
  // System.out.println("Type:" + n.type() + " m:" + n.measure().number()
  // + " b:" + n.beat() + " text:" + n.getText());
  // System.out.println(n.getText());
  // }
}
