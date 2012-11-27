package jp.crestmuse.cmx.elements;
import jp.crestmuse.cmx.filewrappers.*;

public class MutableAnnotation extends MutableMusicEvent 
  implements SCC.Annotation  {
  private String content;
  private String name;
  
  public MutableAnnotation(int onset, int offset, String type, String content, 
                           int ticksPerBeat) {
    super(Type.ANNOTATION, onset, offset, ticksPerBeat);
    this.content = content;
    this.name = type;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public String content() {
    return content;
  }
  public String type() {
    return name;
  }
  public String toString() {
    return "[type: " + type() + ", onset: " + onset() + 
      ", offset: " + offset() + 
      (content == null ? "" : ", content: " + content()) + "]";
  }
  public boolean equals(MutableAnnotation another) {
    return super.equals(another) && (name.equals(another.name)) || 
      (content.equals(another.content));
  }
}
