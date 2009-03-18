package jp.crestmuse.cmx.filewrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.misc.ProgramBugException;
import jp.crestmuse.cmx.misc.XMLException;

public class MusicApexWrapper extends CMXFileWrapper{

  public static final String TOP_TAG = "music-apex";
  
  private MusicXMLWrapper targetMusicXML = null;
  private String targetMusicXMLFileName = null;
  
  
  
  public static MusicApexWrapper createMusicApexWrapperFor(MusicXMLWrapper musicxml){
      try {
        MusicApexWrapper apex = (MusicApexWrapper) createDocument(TOP_TAG);
        apex.targetMusicXML = musicxml;
        apex.setTargetMusicXMLFileName(musicxml.getFileName());
        return apex;
      } catch (InvalidFileTypeException e) {
        throw new ProgramBugException(e.toString());
      }
  }
  
  public void setTargetMusicXMLFileName(String filename) {
    File f = new File(filename);
    if (f.getParent() != null)
      addPathFirst(f.getParent());
    targetMusicXMLFileName = f.getName();
    if (!isFinalized())
      setTopTagAttribute("target", targetMusicXMLFileName);
  }

  public MusicXMLWrapper getTargetMusicXML(){
    //TODO
    return new MusicXMLWrapper();
  }
  /*
  @Override
  protected void analyze() throws IOException{
    try{
      addLinks("", getTargetMusicXML);
    } catch(TransformerException e){
      throw new XMLException(e);
    }
    alreadyAnalyzed = true;
  }
  */
  
  class ApexWrapedGroup implements NoteGroup{

    private int depth = -1;
    private List<Note> ownnotes = new ArrayList<Note>();  //自分のグループのみが持つノート
    private List<Note> undernotes = new ArrayList<Note>();  //自分のグループ以下にあるノート、自分も含む
    private List<NoteGroup> subGroups = new ArrayList<NoteGroup>();
    private NoteGroup groupParent = null;
    private Note apex = null;
    private double saliency = Double.NaN;
    
    @Override
    public void addNote(Note n) {
      // TODO 自動生成されたメソッド・スタブ
      
    }

    @Override
    public void addSubgroup(NoteGroup g) {
      // TODO 自動生成されたメソッド・スタブ
      
    }

    @Override
    public int depth() {
      // TODO 自動生成されたメソッド・スタブ
      return 0;
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
    public boolean isApexInherited() {
      // TODO 自動生成されたメソッド・スタブ
      return false;
    }

    @Override
    public void makeSubgroup(List<Note> notes) {
      // TODO 自動生成されたメソッド・スタブ
      
    }

    @Override
    public void setApex(Note n) {
      // TODO 自動生成されたメソッド・スタブ
      
    }

    @Override
    public void setApex(Note n, double saliency) {
      // TODO 自動生成されたメソッド・スタブ
      
    }
    
  }
  
}
