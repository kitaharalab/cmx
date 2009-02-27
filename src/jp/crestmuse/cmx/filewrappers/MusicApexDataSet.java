package jp.crestmuse.cmx.filewrappers;

import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

public class MusicApexDataSet {

  private MusicApexWrapper apexmxl = null;
  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = null;
  private ApexDataGroup grouptop = null;
  
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited){
    this.inherited = inherited;
    this.grouptop = new ApexDataGroup();
    //TODO add all notes
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
  
  
  class ApexDataGroup implements NoteGroup{

    private int depth;
    private List<Note> notes;
    private List<ApexDataGroup> groups;
    private Note apex = null;
    private Double saliency = null;
    
    public ApexDataGroup(){
      
    }

    @Override
    public void addNote(Note n) {
      // TODO 自動生成されたメソッド・スタブ
      return;
    }

    @Override
    public void addSubgroup(NoteGroup g) {
      // TODO 自動生成されたメソッド・スタブ
      return;      
    }

    @Override
    public int depth() {
      return depth;
    }

    @Override
    public List<Note> getAllNotes() {
      // TODO 自動生成されたメソッド・スタブ
      return null;
    }

    @Override
    public Note getApex() {
      // TODO 自動生成されたメソッド・スタブ
      return null;
    }

    @Override
    public double getApexSaliency() {
      // TODO 自動生成されたメソッド・スタブ
      return 0;
    }

    @Override
    public List<Note> getNotes() {
      // TODO 自動生成されたメソッド・スタブ
      return null;
    }

    @Override
    public List<NoteGroup> getSubgroups() {
      // TODO 自動生成されたメソッド・スタブ
      return null;
    }



    @Override
    public void makeSubgroup(List<Note> notes) {
      // TODO 自動生成されたメソッド・スタブ
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
    public boolean isApexInherited() {
      return inherited;
    }
    
    @Override
    public void setApexInherited(boolean b) {
      inherited = b;
      return;
    }
  }
}
