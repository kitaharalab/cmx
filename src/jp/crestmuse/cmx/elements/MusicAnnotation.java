package jp.crestmuse.cmx.elements;

public interface MusicAnnotation {
  int onset(int ticksPerBeat);
  int offset(int ticksPerBeat);
  String name();
  String content();
}
