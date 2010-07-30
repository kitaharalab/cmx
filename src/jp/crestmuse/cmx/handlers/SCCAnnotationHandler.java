package jp.crestmuse.cmx.handlers;
import jp.crestmuse.cmx.filewrappers.*;

public interface SCCAnnotationHandler {
  public void processAnnotation(SCCXMLWrapper.Annotation a, 
				SCCXMLWrapper w);
}