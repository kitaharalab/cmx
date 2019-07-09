package jp.crestmuse.cmx.handlers;

import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;

public class SCCHandlerAdapter implements SCCHandler {
  public void beginHeader(SCCXMLWrapper w) {
    // do nothing
  }
  public void endHeader(SCCXMLWrapper w) {
    // do nothing
  }
  public void processHeaderElement(long timestamp, String name, 
                              String content, SCCXMLWrapper w) {
    // do nothing
  }
  public void beginPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
    // do nothing
  }
  public void endPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
    // do nothing
  }
  public void processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w) {
    // do nothing
  }
}
