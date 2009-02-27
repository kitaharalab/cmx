package jp.crestmuse.cmx.filewrappers;

import java.util.List;

public interface NoteGroup {
  
  public int depth();
  public List<NoteGroup> getSubgroups();
  public List<MusicXMLWrapper.Note> getNotes();
  public List<MusicXMLWrapper.Note> getAllNotes();
  public MusicXMLWrapper.Note getApex();
  public double getApexSaliency();
  public void addSubgroup(NoteGroup g);
  public void addNote(MusicXMLWrapper.Note n);
  public void setApex(MusicXMLWrapper.Note n);
  public void setApex(MusicXMLWrapper.Note n, double value);
  public void makeSubgroup(List<MusicXMLWrapper.Note> notes);
  public void setApexInherited(boolean b);
  public boolean isApexInherited();
  
//TODO # 上記においてXXX[]はList<XXX>の方がいいかも．
  
}
