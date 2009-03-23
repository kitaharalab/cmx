package jp.crestmuse.cmx.filewrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.misc.ProgramBugException;
import jp.crestmuse.cmx.misc.XMLException;

public class MusicApexWrapper extends CMXFileWrapper{

  public static final String TOP_TAG = "music-apex";
  
  private MusicXMLWrapper targetMusicXML = null;
  private String targetMusicXMLFileName = null;
  
  private boolean inherited;
  private String aspect = "undefined";
  private ApexWrapedGroup toplevel = null;
  
  
  public static MusicApexWrapper createMusicApexWrapperFor(MusicXMLWrapper musicxml){
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
      targetMusicXML = (MusicXMLWrapper)readfile(getTargetMusicXMLFileName());
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
  
  @Override
  protected void analyze() throws IOException{
    Node top = selectSingleNode("/music-apex");
    if(NodeInterface.hasAttribute(top, "apex-inherited")){
      this.inherited = (NodeInterface.getAttribute(top, "apex-inherited").equals("yes") ? true : false);
    }
    if(NodeInterface.hasAttribute(top, "aspect")){
      this.aspect = NodeInterface.getAttribute(top, "aspect");
    }
    if(NodeInterface.hasAttribute(top, "target")){
      this.targetMusicXMLFileName = NodeInterface.getAttribute(top, "target");
    }
    return;
  }
  
  
  class ApexWrapedGroup implements NoteGroup{

    private int depth = -1;
    private List<Note> ownnotes = new ArrayList<Note>();  //自分のグループのみが持つノート
    private List<Note> undernotes = new ArrayList<Note>();  //自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    private NoteGroup groupParent = null;
    private Note apex = null;
    private double saliency = Double.NaN;
    
    public ApexWrapedGroup(Node node){
      
      
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
    public void addNote(Note n) throws UnsupportedOperationException{
      throw new UnsupportedOperationException();
    }

    
    @Override
    public void addSubgroup(NoteGroup g) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setApex(Note n) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setApex(Note n, double value) {
      throw new UnsupportedOperationException();
    } 
    
    public NoteGroup getParentGroup(){
      return groupParent;
    }
  }
  
  public static void main(String[] args){
    try {
      MusicApexWrapper maw = (MusicApexWrapper)MusicApexWrapper.readfile("sampleapex.xml");
      System.out.println(maw.inherited);
      System.out.println(maw.aspect);
      System.out.println(maw.getTargetMusicXMLFileName());
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }
}
