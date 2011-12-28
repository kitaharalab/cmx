package jp.crestmuse.cmx.elements;

public class MutableAnnotation extends MutableMusicEvent 
  implements MusicAnnotation  {
  private String content;
  private String name;
  
  public MutableAnnotation(int onset, int offset, String name, String content, 
                           int ticksPerBeat) {
    super(Type.ANNOTATION, onset, offset, ticksPerBeat);
    this.content = content;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public String content() {
    return content;
  }
  public String name() {
    return name;
  }
}
