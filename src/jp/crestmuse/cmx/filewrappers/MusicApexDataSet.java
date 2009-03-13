package jp.crestmuse.cmx.filewrappers;

import java.util.ArrayList;
import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

public class MusicApexDataSet {

  private MusicApexWrapper mawxml = null;
  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = false;
  private ApexDataGroup grouptop = null;
  private List<ApexDataGroup> groupcollection;
  private List<Note> allnotes;
  
  public MusicApexDataSet(){
    throw new RuntimeException("MusicXMLWrapper is essential for creating MusicApexDataSet.");
  }
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
    this.groupcollection = new ArrayList<ApexDataGroup>();
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited){
    this.inherited = inherited;
    this.grouptop = new ApexDataGroup();
    this.allnotes = new ArrayList<Note>();
    grouptop.depth = 1;

    //MusicXMLのすべてのNote要素をグループに追加する
    MusicXMLWrapper.Part[] partlist = musicxml.getPartList();
    for (MusicXMLWrapper.Part part : partlist) {
      MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
      for (MusicXMLWrapper.Measure measure : measurelist) {
        MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
        for (MusicXMLWrapper.MusicData md : mdlist) {
          if(md instanceof MusicXMLWrapper.Note){
            MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md; //Noteにダウンキャスト
            grouptop.addNote(note);
            allnotes.add(note);
          }
        }
      }
    }
    groupcollection.add(grouptop);
    return grouptop;
  }
  
  public NoteGroup createGroup(){
    return new ApexDataGroup();
  }
  
  public MusicApexWrapper toWrapper(){
    /*
    if(mawxml == null){
      MusicApexWrapper.createMusicApexWrapperFor(musicxml);
    }
    */
    mawxml = new MusicApexWrapper();
    mawxml = MusicApexWrapper.createMusicApexWrapperFor(musicxml);
    
    mawxml.setAttribute("target", musicxml.getFileName());
    mawxml.setAttribute("apex-inherited", (inherited ? "yes" : "no"));
    //mawxml.setAttribute("aspect", "composition");
    
    writeApexDataGroup(grouptop);
    
    try{
      mawxml.write(System.out);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return mawxml;
  }
 
  private void writeApexDataGroup(NoteGroup group){
    mawxml.addChild("group");
    mawxml.setAttribute("depth", group.depth());
    
    //write subgroups
    if(!group.getSubgroups().isEmpty()){
      for(NoteGroup adg : group.getSubgroups()){
        writeApexDataGroup(adg);
      }
    }
    
    //write ownnote
    if(!group.getNotes().isEmpty()){
      for(Note n : group.getNotes()){
        mawxml.addChild("note");
        mawxml.setAttributeNS("http://www.w3.org/1999/xlink", 
            "xlink:href", 
            "#xpointer(" + 
            n.getXPathExpression() + ")");
        mawxml.returnToParent();
      }
    }
    
    //write apex
    if(group.getApex() != null){
      mawxml.addChild("apex");
      mawxml.setAttributeNS("http://www.w3.org/1999/xlink", 
          "xlink:href", 
          "#xpointer(" + 
          group.getApex().getXPathExpression() + ")");
      if(group.getApexSaliency() != null)
        mawxml.setAttribute("saliency", group.getApexSaliency());
    }
    mawxml.returnToParent();
    
    mawxml.returnToParent();
    return;
  }
  
  private ArrayList<Note> getNotesByRange(int start, int end){
    ArrayList<Note> dest = new ArrayList<Note>();
    for(int i=start; i<=end; i++){
      dest.add(allnotes.get(i));
    }
    return dest;
  }
  
  class ApexDataGroup implements NoteGroup{

    private int depth;
    private List<Note> ownnotes;  //自分のグループのみが持つノート
    private List<Note> undernotes;  //自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups;
    private NoteGroup groupParent = null;
    private Note apex = null;
    private Double saliency = null;
    
    public ApexDataGroup(){
      depth = -1;
      ownnotes = new ArrayList<Note>();
      undernotes = new ArrayList<Note>();
      subGroups = new ArrayList<NoteGroup>();
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
    public Double getApexSaliency() {
      return saliency;
    }
    
    @Override
    public List<Note> getNotes() {
      return ownnotes;
    }
    
    @Override
    public List<Note> getAllNotes() {
      return undernotes;
      /*
      //再帰で
      List<Note> notelist = new ArrayList<Note>();
      if(getSubgroups().isEmpty()){
        return getNotes();
      }
      else{
        Iterator<NoteGroup> it = getSubgroups().iterator();
        while(it.hasNext()){
          NoteGroup g = it.next();
          notelist.addAll(g.getAllNotes());
        }
      }
      notelist.addAll(getNotes());
      
      //HashSetに一度格納し重複除去
      HashSet<Note> set = new HashSet<Note>();
      set.addAll(notelist);
      notelist = new ArrayList<Note>();
      notelist.addAll(set);
      
      return notelist;
      */
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
      groupcollection.add((ApexDataGroup) g);
      return;
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
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
      ApexDataGroup g = new ApexDataGroup();
      g.groupParent = this;
      g.ownnotes.addAll(notes);
      g.undernotes.addAll(notes);
      g.depth = this.depth + 1;
      g.apex = this.apex;
      g.saliency = this.saliency;
      ownnotes.removeAll(notes);
      groupcollection.add(g);
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
      this.apex = n;
      if(this.saliency != null)
        throw new RuntimeException("This Apex already has Saliency. : "+n.getXPathExpression());
      this.saliency = value;
      return;
    }

    @Override
    public void setApexInherited(boolean b) {
      throw new RuntimeException("Inherited has to set at TopLevel construction in MusicApexDataSet");
    }    

    /*
    private void removeNotes(List<Note> target, List<Note> delnotes){
      for(Note deln : delnotes){
        Boolean removed = false;
        for(int i=0; i<target.size(); i++){
          if(deln == target.get(i)){
            target.remove(i);
            removed = true;
            break;
          }
        }
        if(!removed) throw new RuntimeException("Note not found : "+deln.toString());
      }
      return;
    }
    */
    
    private void refreshSubGroupApex(ApexDataGroup g){
      g.apex = this.apex;
      g.saliency = this.saliency;
      if(!(g.subGroups.isEmpty())){
        for(NoteGroup sg : g.getSubgroups()){
          g.refreshSubGroupApex((ApexDataGroup)sg);
        }
      }
      return;
    }
    
  }
  
  public static void main(String[] args){
    MusicXMLWrapper musicxml = new MusicXMLWrapper();
    try {
      musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile("./devset/dev_inv02-schiff-b/wiener-p014-015.xml");
      MusicApexDataSet mad = new MusicApexDataSet(musicxml);
      mad.createTopLevelGroup(false);
      mad.grouptop.makeSubgroup(mad.getNotesByRange(5, 10));
      mad.toWrapper();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
