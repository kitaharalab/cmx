package jp.crestmuse.cmx.handlers;

import jp.crestmuse.cmx.filewrappers.*;

public interface SCCHandler {
  public void beginHeader(SCCXMLWrapper w);
  public void endHeader(SCCXMLWrapper w);
  public void processHeaderElement(long timestamp, String name,
                                   String content, SCCXMLWrapper w);
  public void beginPart(SCCXMLWrapper.Part part, SCCXMLWrapper w);
  public void endPart(SCCXMLWrapper.Part part, SCCXMLWrapper w);
  public void processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w);
//    public void processNote(SCCXMLWrapper.Note note, 
//		       SCCXMLWrapper.Part part, SCCXMLWrapper w);
}
