package jp.crestmuse.cmx.filewrappers;

import java.util.List;

public interface NoteGroup {
  
  public int depth();
  public boolean isApexInherited();
  public Double getApexSaliency();
  public List<MusicXMLWrapper.Note> getNotes();
  public List<MusicXMLWrapper.Note> getAllNotes();
  public MusicXMLWrapper.Note getApex();
  public List<NoteGroup> getSubgroups();  
  public void addNote(MusicXMLWrapper.Note n);
  public void addSubgroup(NoteGroup g);
  public void makeSubgroup(List<MusicXMLWrapper.Note> notes);
  public void setApex(MusicXMLWrapper.Note n);
  public void setApex(MusicXMLWrapper.Note n, double value);
  public void setApexInherited(boolean b);  
  
}
