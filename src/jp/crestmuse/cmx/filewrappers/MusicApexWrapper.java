package jp.crestmuse.cmx.filewrappers;

import java.io.File;

import jp.crestmuse.cmx.misc.ProgramBugException;

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
  
  
  
}
