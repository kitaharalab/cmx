package jp.crestmuse.cmx.inference;

public class MusicRepresentationFactory {
  public static MusicRepresentation create(int measure, int divisions) {
    return new MusicRepresentationImpl4(measure, divisions);
  }
}
