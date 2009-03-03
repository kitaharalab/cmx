package jp.crestmuse.cmx.filewrappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

public class MusicApexDataSet {

  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = false;
  private ApexDataGroup grouptop = null;
  private List<ApexDataGroup> groupcollection;
  
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
    this.groupcollection = new ArrayList<ApexDataGroup>();
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited){
    this.inherited = inherited;
    this.grouptop = new ApexDataGroup();
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
    MusicApexWrapper maw = null;
    //TODO wrap
    return maw;
  }
 
  public int getDepth(Note n){
    int thisdepth = -1;
    
    
    return thisdepth;
  }
  
  class ApexDataGroup implements NoteGroup{

    private int depth;
    private List<Note> ownnotes;  //自分のグループが持つノート
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
      //再帰できてるかなー
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
      
      //HashSetに一旦格納し重複除去
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
      if(g instanceof ApexDataGroup) ((ApexDataGroup)g).groupParent = this;
      undernotes.addAll(g.getAllNotes());
      subGroups.add(g);
      groupcollection.add((ApexDataGroup) g);
      return;
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
      // TODO 自動生成されたメソッド・スタブ
      ApexDataGroup g = new ApexDataGroup();
      g.groupParent = this;
      g.notes.addAll(notes);
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

  }
}
