package jp.crestmuse.cmx.filewrappers;

import java.util.ArrayList;
import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

public class MusicApexDataSet {

  private MusicApexWrapper mawxml = null;
  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = false;
  private String aspect = null;
  private ApexDataGroup grouptop = null;
  
  //private List<ApexDataGroup> groupcollection;
  private List<Note> allnotes;
  
  public MusicApexDataSet(){
    throw new RuntimeException("MusicXMLFile is essential for creating MusicApexDataSet.");
  }
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
//    this.groupcollection = new ArrayList<ApexDataGroup>();
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited){
    return createTopLevelGroup(inherited, null);
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited, String aspect){
    this.inherited = inherited;
    this.aspect = aspect;
    this.grouptop = new ApexDataGroup();
    this.allnotes = new ArrayList<Note>();
    grouptop.depth = 1;
//    groupcollection.add(grouptop);
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
    return grouptop;
  }
  
  public NoteGroup createGroup(){
    return new ApexDataGroup();
  }
  
  public NoteGroup createGroup(List<Note> notes){
    return new ApexDataGroup(notes, null, Double.NaN);
  }
  
  public NoteGroup createGroup(List<Note> notes, Note apex){
    return new ApexDataGroup(notes, apex, Double.NaN);
  }
  
  public NoteGroup createGroup(List<Note> notes, Note apex, Double saliency){
    return new ApexDataGroup(notes, apex, saliency);
  }
  
  public void setAspect(String aspect){
    this.aspect = aspect;
    return;
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
    if(aspect != null)  mawxml.setAttribute("aspect", aspect);
    
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
      if(!(group.getApexSaliency().isNaN())){
        mawxml.setAttribute("saliency", group.getApexSaliency());
      }
      mawxml.returnToParent();
    }
    
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

    private int depth = -1;
    private List<Note> ownnotes = new ArrayList<Note>();  //自分のグループのみが持つノート
    private List<Note> undernotes = new ArrayList<Note>();  //自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    private NoteGroup groupParent = null;
    private Note apex = null;
    private Double saliency = Double.NaN;
    
    public ApexDataGroup(){
      return;
    }
    
    public ApexDataGroup(List<Note> notes, Note apex, Double saliency){
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
      //groupcollection.add((ApexDataGroup) g);
      return;
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
      makeSubgroup(notes, null);
      return;
    }
    
    public void makeSubgroup(List<Note> notes, Note apex){
      makeSubgroup(notes, apex, Double.NaN);
      return;
    }
    
    public void makeSubgroup(List<Note> notes, Note apex, Double saliency){
//      各ノートがグループを作成する親グループまたはそのdepth+1の範囲に含まれるかチェック
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
//      groupcollection.add(g);
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

    @Override
    public void setApexInherited(boolean b) {
      throw new RuntimeException("Inherited has to set at TopLevel construction in MusicApexDataSet");
    }    

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
    
    public NoteGroup getParentGroup(ApexDataGroup g){
      return g.groupParent;
    }
  }
  
  public static void main(String[] args){
    MusicXMLWrapper musicxml = new MusicXMLWrapper();
    try {
      musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile("./sample.xml");
      MusicApexDataSet mad = new MusicApexDataSet(musicxml);
      mad.createTopLevelGroup(true);
      mad.setAspect("hoge");
      mad.grouptop.setApex(mad.allnotes.get(8));
      mad.grouptop.makeSubgroup(mad.getNotesByRange(5, 10), mad.allnotes.get(6));
      mad.grouptop.makeSubgroup(mad.getNotesByRange(20, 30));
      ((ApexDataGroup)mad.grouptop.subGroups.get(1)).makeSubgroup(mad.getNotesByRange(24, 28), mad.allnotes.get(28));
      //ApexDataGroup gp = (ApexDataGroup)mad.createGroup(mad.getNotesByRange(10, 15));
      //mad.grouptop.addSubgroup(gp);
       mad.toWrapper();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
