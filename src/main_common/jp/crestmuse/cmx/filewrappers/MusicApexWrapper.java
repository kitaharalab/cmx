package jp.crestmuse.cmx.filewrappers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.misc.ProgramBugException;

public class MusicApexWrapper extends CMXFileWrapper {

  public static final String TOP_TAG = "music-apex";

  private MusicXMLWrapper targetMusicXML = null;
  private String targetMusicXMLFileName = null;
  private HashMap<String, Note> xpathNoteView = null;

  private boolean inherited;
  private String aspect = null;
  private ApexWrapedGroup toplevel = null;
  private List<NoteGroup> depthFirstView = null;
  private List<NoteGroup> breadthFirstView = null;

  public static MusicApexWrapper createMusicApexWrapperFor(
      MusicXMLWrapper musicxml) {
    try {
      MusicApexWrapper apex = (MusicApexWrapper) createDocument(TOP_TAG);
      apex.targetMusicXML = musicxml;
      return apex;
    } catch (InvalidFileTypeException e) {
      throw new ProgramBugException(e.toString());
    }
  }

  public MusicXMLWrapper getTargetMusicXML() throws IOException {
    if (targetMusicXML == null) {
      if (getParentPath() != null)
        addPathFirst(getParentPath());
      targetMusicXML = (MusicXMLWrapper) readfile(getTargetMusicXMLFileName());
    }
    return targetMusicXML;
  }

  public String getTargetMusicXMLFileName() {
    if (targetMusicXMLFileName == null) {
      File f = new File(getTopTagAttribute("target"));
      if (f.getParent() != null)
        addPathFirst(f.getParent());
      targetMusicXMLFileName = f.getName();
    }
    return targetMusicXMLFileName;
  }

  /**
   * 深さ優先探索でグループを格納したリストを返します。
   * 
   * @return
   */
  public List<NoteGroup> getDepthFirstGroupView() {
    if (depthFirstView == null) {
      depthFirstView = makeDepthFirst(toplevel);
    }
    return depthFirstView;
  }

  private List<NoteGroup> makeDepthFirst(ApexWrapedGroup wg) {
    List<NoteGroup> dest = new ArrayList<NoteGroup>();
    dest.add(wg);
    for (NoteGroup g : wg.getSubgroups()) {
      dest.addAll(makeDepthFirst((ApexWrapedGroup) g));
    }
    return dest;
  }

  /**
   * 幅優先探索でグループを格納したリストを返します。
   * 
   * @return
   */
  public List<NoteGroup> getBreadthFirstView() {
    if (breadthFirstView == null) {
      breadthFirstView = makeBreadthFirst(toplevel);
    }
    return breadthFirstView;
  }

  private List<NoteGroup> makeBreadthFirst(ApexWrapedGroup src) {
    List<NoteGroup> dest = new ArrayList<NoteGroup>();
    Queue<ApexWrapedGroup> queue = new LinkedList<ApexWrapedGroup>();
    queue.offer(src);
    while (!queue.isEmpty()) {
      ApexWrapedGroup g = queue.poll();
      dest.add(g);
      for (NoteGroup subg : g.getSubgroups()) {
        queue.offer((ApexWrapedGroup) subg);
      }
    }
    return dest;
  }

  /**
   * 何に注目した音楽構造かを返します。
   * 
   * @return (未定義の時はnull)
   */
  public String getAspect() {
    return this.aspect;
  }

  public MusicApexDataSet toDataSet() {
    return new MusicApexDataSet(targetMusicXML, toplevel);
  }

  protected void analyze() throws IOException {
    try {
      addLinks("//note", getTargetMusicXML());
      addLinks("//apex/start", getTargetMusicXML());
      addLinks("//apex/stop", getTargetMusicXML());

      Node top = selectSingleNode("/music-apex");
      if (NodeInterface.hasAttribute(top, "apex-inherited")) {
        this.inherited = (NodeInterface.getAttribute(top, "apex-inherited").equals(
            "yes") ? true : false);
      }
      if (NodeInterface.hasAttribute(top, "aspect")) {
        this.aspect = NodeInterface.getAttribute(top, "aspect");
      }
      if (NodeInterface.hasAttribute(top, "target")) {
        this.targetMusicXMLFileName = NodeInterface.getAttribute(top, "target");
      }

      this.toplevel = analyzeGroups(
          (Element) selectSingleNode("/music-apex/group"),
          (ApexWrapedGroup) null);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    return;
  }

  private HashMap<String, Note> getXPathNoteView() {
    try {
      this.xpathNoteView = new HashMap<String, Note>();
      MusicXMLWrapper musicxml = getTargetMusicXML();
      MusicXMLWrapper.Part[] partlist = musicxml.getPartList();
      for (MusicXMLWrapper.Part part : partlist) {
        MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
        for (MusicXMLWrapper.Measure measure : measurelist) {
          MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
          for (MusicXMLWrapper.MusicData md : mdlist) {
            if (md instanceof MusicXMLWrapper.Note) {
              MusicXMLWrapper.Note note = (MusicXMLWrapper.Note) md;
              this.xpathNoteView.put(note.getXPathExpression(), note);
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return xpathNoteView;
  }

  private ApexWrapedGroup analyzeGroups(Element node, ApexWrapedGroup parent) {
    if (!(node.getNodeName().equals("group")))
      throw new RuntimeException("Node is not group Element");
    if (xpathNoteView == null)
      xpathNoteView = getXPathNoteView();

    ApexWrapedGroup g = new ApexWrapedGroup();

    g.depth = Integer.parseInt(node.getAttribute("depth"));
    g.implicit = node.hasAttribute("implicit")
        && node.getAttribute("implicit").equals("yes");

    String nodepath = getXPathPosition(node);

    NodeList onlist = selectNodeList(nodepath + "/note");
    for (int i = 0; i < onlist.getLength(); i++) {
      g.ownnotes.add(xpathNoteView.get(cutXPath(onlist.item(i))));
    }

    NodeList unlist = selectNodeList(nodepath + "//note");
    for (int i = 0; i < unlist.getLength(); i++) {
      g.undernotes.add(xpathNoteView.get(cutXPath(unlist.item(i))));
    }

    Element apexn = (Element) selectSingleNode(nodepath + "/apex[1]");
    if (apexn != null) {
      Node start = apexn.getElementsByTagName("start").item(0);
      Node stop = apexn.getElementsByTagName("stop").item(0);
      g.apexStart = xpathNoteView.get(cutXPath(start));
      g.apexStartTime = Double.parseDouble(start.getAttributes().getNamedItem(
          "time").getNodeValue());
      g.apexStop = xpathNoteView.get(cutXPath(stop));
      g.apexStopTime = Double.parseDouble(stop.getAttributes().getNamedItem(
          "time").getNodeValue());
      g.saliency = (apexn.hasAttribute("saliency") ? Double.parseDouble(apexn.getAttribute("saliency"))
          : Double.NaN);
    }

    g.groupParent = parent;

    NodeList subglist = selectNodeList(nodepath + "/group");
    for (int i = 0; i < subglist.getLength(); i++) {
      g.subGroups.add(analyzeGroups((Element) subglist.item(i), g));
    }
    return g;
  }

  private String cutXPath(Node el) {
    if (!(el instanceof Element))
      throw new UnsupportedOperationException();
    String path = ((Element) el).getAttribute("xlink:href");
    if (path == null)
      throw new RuntimeException("This Element has no xpointer : "
          + el.toString());
    path = path.substring(path.indexOf("(") + 1, path.indexOf(")"));
    return path;
  }

  private String getXPathPosition(Node n) {
    if (n.getParentNode() == null)
      return "";
    else {
      return getXPathPosition(n.getParentNode()) +
      // getAttributeXPath(n.getParentNode()) +
          "/" + n.getNodeName() + "[" + getNodeSiblingPosition(n) + "]";
    }
  }

  private int getNodeSiblingPosition(Node n) {
    int count = 1;
    String name = n.getNodeName();
    short type = n.getNodeType();
    while (n.getPreviousSibling() != null) {
      if ((n.getPreviousSibling().getNodeName().equals(name) && n.getPreviousSibling().getNodeType() == type))
        count++;
      n = n.getPreviousSibling();
    }
    return count;
  }

  private class ApexWrapedGroup implements NoteGroup {

    private int depth = -1;
    private List<Note> ownnotes = new ArrayList<Note>(); // 自分のグループのみが持つノート
    private List<Note> undernotes = new ArrayList<Note>(); // 自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    private HashMap<String, String> attribute = new HashMap<String, String>();
    private NoteGroup groupParent = null;
    private Note apexStart, apexStop;
    private double apexStartTime, apexStopTime;
    private double saliency = Double.NaN;
    private boolean implicit = false;

    public int depth() {
      return depth;
    }

    public boolean isApexInherited() {
      return inherited;
    }

    public double getApexSaliency() {
      return saliency;
    }

    public List<Note> getNotes() {
      return ownnotes;
    }

    public List<Note> getAllNotes() {
      return undernotes;
    }

    public Note getApex() {
      if (apexStart == apexStop)
        return apexStart;
      throw new IllegalStateException("apexStart don't match apexStart");
    }

    public List<NoteGroup> getSubgroups() {
      return subGroups;
    }

    public void addNote(Note n) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    public void removeNote(Note n) {
      throw new UnsupportedOperationException();
    }

    public void addSubgroup(NoteGroup g) {
      throw new UnsupportedOperationException();
    }

    public NoteGroup makeSubgroup(List<Note> notes) {
      throw new UnsupportedOperationException();
    }

    public void setApex(Note n) {
      throw new UnsupportedOperationException();
    }

    public void removeSubgroup(NoteGroup g) {
      throw new UnsupportedOperationException();
    }

    public String getAttribute(String key) {
      return attribute.get(key);
    }

    public void setAttribute(String key, String value) {
      attribute.put(key, value);
    }

    public NoteGroup getParentGroup() {
      return groupParent;
    }

    /**
     * for debugging method
     */
    @Deprecated
    public void printGroupStat() {
      System.out.println("toString:" + this.toString());
      System.out.println("depth:" + this.depth() + " subgroups:"
          + this.subGroups.size());
      System.out.println("notes:" + this.getNotes().size() + " parent:"
          + (this.groupParent != null ? this.groupParent.toString() : "null"));
      System.out.println();
    }

    public List<Note> getImplicitGroupNotes() {
      throw new UnsupportedOperationException();
    }

    public boolean isImplicit() {
      return implicit;
    }

    public int type() {
      throw new UnsupportedOperationException();
    }

    public Note getApexStart() {
      return apexStart;
    }

    public double getApexStartTime() {
      return apexStartTime;
    }

    public Note getApexStop() {
      return apexStop;
    }

    public double getApexStopTime() {
      return apexStopTime;
    }

    public void setApexStart(Note n, double time) {
      throw new UnsupportedOperationException();
    }

    public void setApexStop(Note n, double time) {
      throw new UnsupportedOperationException();
    }

    public void setApexSaliency(double saliency) {
      throw new UnsupportedOperationException();
    }

    public void setImplicit(boolean value) {
      throw new UnsupportedOperationException();
    }

    public Object clone() throws CloneNotSupportedException {
      throw new UnsupportedOperationException();
    }

  }

  public static void main(String[] args) {
    try {
      MusicApexWrapper maw = (MusicApexWrapper) CMXFileWrapper.readfile(args[0]);
      /*
       * System.out.println(maw.inherited); System.out.println(maw.aspect);
       * System.out.println(maw.getTargetMusicXMLFileName()); Node ap =
       * maw.selectSingleNode("/music-apex/group/note"); printNodeStat(ap); Node
       * mxmlnote = linkmanager.getNodeLinkedFrom(ap,"note");
       * printNodeStat(mxmlnote);
       * 
       * printNodeStat(mxmlnote.getParentNode());
       * System.out.println(((Element)mxmlnote
       * .getParentNode()).getAttribute("number"));
       * 
       * String path = ((Element)ap).getAttribute("xlink:href"); path =
       * path.substring(path.indexOf("(")+1, path.indexOf(")"));
       * System.out.println(maw.getXPathNoteView().get(path).toString());
       * 
       * 
       * NodeList nl = maw.selectNodeList("/music-apex/group/note");
       * 
       * for(int i=0; i<nl.getLength(); i++){
       * System.out.println(nl.item(i).getNodeName());
       * 
       * }
       * 
       * System.out.println(nl.getLength());
       * System.out.println(maw.selectNodeList
       * ("/music-apex/group/group[2]/note").getLength());
       * System.out.println(maw
       * .selectNodeList("/music-apex//group[@depth='2']").getLength()); String
       * hoge =maw.getXPathPosition((Element)maw.selectSingleNode(
       * "/music-apex/group/group[2]")); System.out.println(hoge);
       * //System.out.println(maw.selectNodeList(
       * "/music-apex[@apex-inherited='yes'][@aspect='hoge'][@target='sample.xml']/group[@depth='1']/group"
       * ).getLength());
       * System.out.println(maw.selectNodeList(hoge).getLength());
       * System.out.println(maw.selectNodeList(hoge+"//note").getLength());
       * 
       * 
       * maw.toplevel.printGroupStat(); ((ApexWrapedGroup)
       * maw.toplevel.getSubgroups().get(0)).printGroupStat();
       * ((ApexWrapedGroup)
       * maw.toplevel.getSubgroups().get(1)).printGroupStat();
       * ((ApexWrapedGroup)
       * maw.toplevel.getSubgroups().get(1).getSubgroups().get
       * (0)).printGroupStat();
       */

      for (NoteGroup g : maw.getDepthFirstGroupView()) {
        ((ApexWrapedGroup) g).printGroupStat();
      }

      System.out.println(maw.getAspect());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * for debugging method
   * 
   * @param n
   *          Note
   */
  @Deprecated
  public static void printNodeStat(Node n) {
    System.out.print("Name:" + n.getNodeName() + " Value:" + n.getNodeValue());
    System.out.println("Text:" + n.getTextContent());
    System.out.print("Attrs: ");
    for (int i = 0; i < n.getAttributes().getLength(); i++) {
      System.out.print(n.getAttributes().item(i) + ",");
    }
    System.out.print("Childs: ");
    for (int i = 0; i < n.getChildNodes().getLength(); i++) {
      System.out.print(n.getChildNodes().item(i).getNodeName() + ",");
    }
    System.out.println("\n");
    return;
  }
}
