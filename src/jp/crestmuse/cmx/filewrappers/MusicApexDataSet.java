package jp.crestmuse.cmx.filewrappers;

import java.util.List;

import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.misc.TreeView;

public class MusicApexDataSet {

  private MusicXMLWrapper musicxml = null;
  private Boolean inherited = null;
  private ApexDataGroup grouptop = null;
  
  public MusicApexDataSet(MusicXMLWrapper musicxml){
    this.musicxml = musicxml;
  }
  
  public NoteGroup createTopLevelGroup(Boolean inherited){
    this.inherited = inherited;
    this.grouptop = new ApexDataGroup();
    grouptop.depth = 1;
    //TODO add all notes from MusicXML
    
   
    //grouptop.addNote(n);
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
    private List<ApexDataGroup> subGroups;
    private NoteGroup groupParent;
    private Note apex = null;
    private Double saliency = null;
    
    public ApexDataGroup(){
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
      return notes;
    }
    
    @Override
    public List<Note> getAllNotes() {
      // TODO 自動生成されたメソッド・スタブ
      return null;
    }

    @Override
    public Note getApex() {
      return apex;
    }

    @Override
    public List<NoteGroup> getSubgroups() {
      //TODO
      return null;
    }

    @Override
    public void addNote(Note n) {
      notes.add(n);
      return;
    }

    @Override
    public void addSubgroup(NoteGroup g) {
      // TODO 自動生成されたメソッド・スタブ
      return;
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
    public void setApexInherited(boolean b) {
      throw new RuntimeException("Inherited has to set at TopLevel construction in MusicApexDataSet");
    }    

  }
}
