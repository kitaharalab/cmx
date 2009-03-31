package jp.crestmuse.cmx.filewrappers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

/**
 * MusicXMLから音楽構造グルーピングを生成するためのクラスです。<br>
 * インスタンスの生成にはMusicXMLWrapperを渡す必要があり、
 * トップレベルのグループには全てのNoteが生成されます
 * 
 * -使い方<br>
 *  MusicXMLWrapper musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile("./sample.xml");<br>
 *    MusicXMLのインスタンス化<br>
 *  MusicApexDataSet ads = new MusicApexDataSet(musicxml);<br>
 *    MusicApexDataSetにMusicXMLを渡しインスタンス化<br>
 *  ads.createTopLevelGroup(true);<br>
 *    inheritedを指定し、トップレベルグループを作成<br>
 *  ads.setAspect("sample-aspect");<br>
 *    何に着目したかを設定(任意)<br>
 *  <br>
 *  ads.topgroup.makeSubgroup(notes);<br>
 *    トップレベルからグループを作成<br>
 *  〜〜<br>
 *  <br>
 *  ads.toWrapper();<br>
 *    MusicXMLWrapper化<br>
 *  
 * @author R.Tokuami
 */
public class MusicApexDataSet {

  private MusicApexWrapper mawxml = null;
  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = false;
  private String aspect = null;
  private ApexDataGroup toplevel = null;

  private HashMap<Node, MusicXMLWrapper.Note> noteMap = new HashMap(); 
  private List<Note> allnotes;
  
  /**
   * MuscXMLを元に、MusicApexDataSetオブジェクトを作成します。
   * @param musicxml
   */
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
  }
  
  /**
   * MusicXMLに含まれるすべてのノートを含むトップレベルグループを作成します。
   * トップレベルグループ作成の時点でinheritedなグループかどうかを指定する必要があります。
   * グループの頂点の音符がサブグループに含まれていたときに、
   * 頂点であることを継承するならtrue
   * それぞれのグループで頂点を指定するならfalse
   * を指定してください。
   * 
   * @param inherited 親グループの頂点を子グループで継承するかどうか
   * @return トップレベルグループのApexDataGroupオブジェクト
   */
  public NoteGroup createTopLevelGroup(Boolean inherited){
    return createTopLevelGroup(inherited, null);
  }
  
  /**
   * MusicXMLに含まれるすべてのノートを含むトップレベルグループを作成します。
   * inheritedとaspectを指定する必要があります。
   * 
   * @param inherited
   * @param aspect 何に着目した構造記述か
   * @return トップレベルグループを示すApexDataGroupオブジェクト
   */
  public NoteGroup createTopLevelGroup(Boolean inherited, String aspect){
    this.inherited = inherited;
    this.aspect = aspect;
    this.toplevel = new ApexDataGroup();
    this.allnotes = new ArrayList<Note>();
    toplevel.depth = 1;
    toplevel.groupParent = null;
    
    //MusicXMLのすべてのNote要素をグループに追加する
    MusicXMLWrapper.Part[] partlist = musicxml.getPartList();
    for (MusicXMLWrapper.Part part : partlist) {
      MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
      for (MusicXMLWrapper.Measure measure : measurelist) {
        MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
        for (MusicXMLWrapper.MusicData md : mdlist) {
          if(md instanceof MusicXMLWrapper.Note){
            MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
            toplevel.addNote(note);
            noteMap.put(note.node(), note);
            allnotes.add(note);
          }
        }
      }
    }
    return toplevel;
  }
  
  /**
   * どこのグループにも属さない空のApexDataGroupオブジェクトを作成します。
   * @return
   */
  public NoteGroup createGroup(){
    return new ApexDataGroup();
  }
  
  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * @param notes このグループに含まれるNoteオブジェクトのリスト
   * @return
   */
  public NoteGroup createGroup(List<Note> notes){
    return new ApexDataGroup(notes, null, Double.NaN);
  }
  
  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * @param notes このグループに含まれるNoteオブジェクトのリスト
   * @param apex このグループの頂点のNote
   * @return
   */
  public NoteGroup createGroup(List<Note> notes, Note apex){
    return new ApexDataGroup(notes, apex, Double.NaN);
  }
  
  /**
   * どこのグループにも属さないApexDataGroupオブジェクトを作成します。
   * @param notes このグループに含まれるNoteオブジェクトのリスト
   * @param apex このグループの頂点のNote
   * @param saliency 頂点のNoteがどのぐらい目立っているか
   * @return
   */
  public NoteGroup createGroup(List<Note> notes, Note apex, double saliency){
    return new ApexDataGroup(notes, apex, saliency);
  }
  
  /**
   * 作成するMusicApexDataSetクラスが何に着目した楽曲構造かをセットします。
   * 1つのMusiApexDataSetクラスは一つのaspectを持ちます。
   * @param aspect 着目した対象
   */
  public void setAspect(String aspect){
    this.aspect = aspect;
    return;
  }
  
  /**
   * トップレベルグループのインスタンスを返します。
   * @return トップレベルのApexDataGroupオブジェクト
   */
  public ApexDataGroup topgroup(){
    return this.toplevel;
  }
  
  /**
   * 作成したDataSetからXMLを作成し、MusicApexWrapperを生成して返します。
   * XMLテキスト上に出力されるグループの順番は、親グループに先に追加されたものが先に記述されます。
   * @return このMusicApexDataSetを元にしたMusicApexWrapperオブジェクト
   * @throws RuntimeException トップレベルグループが作られていない、MusicXMLが指定されていない場合
   */
  public MusicApexWrapper toWrapper(){
    //check initalized
    if(toplevel == null) throw new RuntimeException("TopLevelGroup not created.");
    //create apexxml
    mawxml = new MusicApexWrapper();
    mawxml = MusicApexWrapper.createMusicApexWrapperFor(musicxml);
    //write toplevel and attributes
    mawxml.setAttribute("target", musicxml.getFileName());
    mawxml.setAttribute("apex-inherited", (inherited ? "yes" : "no"));
    if(aspect != null)  mawxml.setAttribute("aspect", aspect);
    //write groups
    writeApexDataGroup(toplevel);

    return mawxml;
  }
 
  private void writeApexDataGroup(NoteGroup group){
    mawxml.addChild("group");
    if(group.depth() == -1) throw new RuntimeException("Invalid GroupDepth");
    mawxml.setAttribute("depth", group.depth());
    //write subgroups
    if(!(group.getSubgroups().isEmpty())){
      for(NoteGroup adg : group.getSubgroups()){
        writeApexDataGroup(adg);
      }
    }
    //write ownnote
    if(!(group.getNotes().isEmpty())){
      for(Note n : group.getNotes()){
        mawxml.addChild("note");
        mawxml.setAttributeNS("http://www.w3.org/1999/xlink", 
            "xlink:href", 
            "#xpointer(" + 
            n.getXPathExpression() + ")");
        mawxml.returnToParent();
      }
    }
    else{
      throw new RuntimeException("Creating No Notes Group");
    }
    //write apex
    if(group.getApex() != null){
      mawxml.addChild("apex");
      mawxml.setAttributeNS("http://www.w3.org/1999/xlink", 
          "xlink:href", 
          "#xpointer(" + 
          group.getApex().getXPathExpression() + ")");
      if(!(Double.isNaN(group.getApexSaliency()))){
         mawxml.setAttribute("saliency", group.getApexSaliency());
      }
      mawxml.returnToParent();
    }
    mawxml.returnToParent();
    return;
  }
  
  /**
   * MusicXMLに含まれるNoteのリストのindexが
   * start番目からend番目のNoteをListにして返します
   * @param start
   * @param end
   * @return
   */
  private List<Note> getNotesByRange(int start, int end){
    ArrayList<Note> dest = new ArrayList<Note>();
    for(int i=start; i<=end; i++){
      dest.add(allnotes.get(i));
    }
    return dest;
  }
  
  /**
   * MusicApexDataSetクラスで用いる、音楽構造グループ1つを表すクラスです。
   * 
   * グループの深さ、グループに属するNoteのリスト、子グループのリスト、
   * 頂点、頂点がどのぐらい目立っているかなどを保持し、
   * グループの状態を取得するメソッド、グループの親子関係を設定するメソッドを提供します。
   *
   */
  class ApexDataGroup implements NoteGroup{

    private int depth = -1;
    private List<Note> ownnotes = new ArrayList<Note>();  //自分のグループのみが持つノート
    private List<Note> undernotes = new ArrayList<Note>();  //自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    private NoteGroup groupParent = null;
    private Note apex = null;
    private double saliency = Double.NaN;
    
    public ApexDataGroup(){
      return;
    }
    
    public ApexDataGroup(List<Note> notes, Note apex, double saliency){
      this.ownnotes.addAll(notes);
      this.undernotes.addAll(notes);
      this.apex = apex;
      this.saliency = saliency;
      return;
    }

    @Override
    public int depth() {
      return depth;
    }
    
    @Override
    public boolean isApexInherited() {
      return inherited;
    }
   
    @Override
    public double getApexSaliency() {
      return saliency;
    }
    
    @Override
    public List<Note> getNotes() {
      return ownnotes;
    }
    
    @Override
    public List<Note> getAllNotes() {
      return undernotes;
    }

    @Override
    public Note getApex() {
      return apex;
    }

    @Override
    public List<NoteGroup> getSubgroups() {
      return subGroups;
    }

    @Override
    public void addNote(Note n) {
      ownnotes.add(n);
      undernotes.add(n);
      return;
    }
    
    @Override
    public void addSubgroup(NoteGroup g) {
      if(g instanceof ApexDataGroup){
        ((ApexDataGroup)g).groupParent = this;
        ((ApexDataGroup)g).depth = this.depth() + 1;        
        if(isApexInherited() == true){
          refreshSubGroupApex((ApexDataGroup)g);
        }
      }
      undernotes.addAll(g.getAllNotes());
      subGroups.add(g);
      return;
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
      makeSubgroup(notes, null);
      return;
    }
    
    /**
     * このインスタンスから、子としてグループを作成し、追加します。
     * @param notes グループ化するNoteオブジェクトのリスト
     * @param apex 作成する子グループの頂点
     * @throws RuntimeException このインスタンスにグループ化するノートが含まれていない
     */
    public void makeSubgroup(List<Note> notes, Note apex){
      makeSubgroup(notes, apex, Double.NaN);
      return;
    }
    
    /**
     * このインスタンスから、子としてグループを作成し、追加します。
     * @param notes グループ化するNoteオブジェクトのリスト
     * @param apex 作成する子グループの頂点のNote
     * @param saliency 頂点のNoteがどれぐらい目立っているか
     * @throws RuntimeException このインスタンスにグループ化するノートが含まれていない
     */
    public void makeSubgroup(List<Note> notes, Note apex, double saliency){
      //各ノートがグループを作成する親グループまたはそのdepth+1の範囲に含まれるかチェック
      for(Note checknote : notes){
        Boolean included = false;
        if(! (included = ownnotes.contains(checknote))){
          for(NoteGroup ng : getSubgroups()){
            if(ng.getNotes().contains(checknote)){
              included = true;        
              break;
            }
          }
        }
        if(included == false) throw new RuntimeException("Note is not included Parent and Parent's subgroups");
      }
      //making new group
      ApexDataGroup g = new ApexDataGroup();
      g.groupParent = this;
      g.ownnotes.addAll(notes);
      g.undernotes.addAll(notes);
      g.depth = this.depth + 1;
      
      if(isApexInherited() == true && this.getApex() != null
          && g.ownnotes.contains(this.apex)){
        g.apex = this.apex;
        g.saliency = this.saliency;
      }
      else{
        g.apex = apex;
        g.saliency = saliency;
      }
      //add to parent group
      subGroups.add(g);
      ownnotes.removeAll(notes);
      return;
    }

    @Override
    public void setApex(Note n) {
      if(this.apex != null) 
        throw new RuntimeException("This group already has Apex. : "+n.getXPathExpression());
      this.apex = n;
      return;
    }

    @Override
    public void setApex(Note n, double value) {
      if(inherited) throw new RuntimeException("This Apex is inherited");
      this.apex = n;
      this.saliency = value;
      return;
    } 

    private void refreshSubGroupApex(ApexDataGroup g){
      if(g.getNotes().contains(this.apex)){
        g.apex = this.apex;
        g.saliency = this.saliency;
      }
      if(!(g.subGroups.isEmpty())){
        for(NoteGroup sg : g.getSubgroups()){
          g.refreshSubGroupApex((ApexDataGroup)sg);
        }
      }
      return;
    }
    
    /**
     * このインスタンスの親のグループを返します。
     * @return 親のApexDataGroupオブジェクト(存在しないならnull)
     */
    public NoteGroup getParentGroup(){
      return groupParent;
    }
  }
  
  public static void main(String[] args){
    MusicXMLWrapper musicxml = new MusicXMLWrapper();
    try {
      musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile("./sample.xml");
      MusicApexDataSet ads = new MusicApexDataSet(musicxml);
      ads.createTopLevelGroup(true);
      ads.setAspect("hoge");
      ads.toplevel.setApex(ads.allnotes.get(8));
      ads.toplevel.makeSubgroup(ads.getNotesByRange(5, 10), ads.allnotes.get(6));
      ads.toplevel.makeSubgroup(ads.getNotesByRange(20, 30));
      ((ApexDataGroup)ads.toplevel.subGroups.get(1)).makeSubgroup(ads.getNotesByRange(24, 28), ads.allnotes.get(28));
      //ApexDataGroup gp = (ApexDataGroup)mad.createGroup(mad.getNotesByRange(10, 15));
      //mad.grouptop.addSubgroup(gp);
       //ads.toWrapper().writefile(new File("sampleapex.xml"));

      
      NodeList nl = musicxml.selectNodeList("/score-partwise/part/measure[@number='1']/note");
      for(int i=0; i<nl.getLength(); i++){
        System.out.println(nl.item(i).getTextContent());
        printNote(ads.noteMap.get(nl.item(i)));
      }
      
      //ads.toWrapper().write(System.out);
       }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Deprecated
  public static void printNote(Note n){
    System.out.println("Type:"+n.type()+" m:"+n.measure().number()+" b:"+n.beat()+" text:"+n.getText());
    System.out.println(n.getText());
  }
}
