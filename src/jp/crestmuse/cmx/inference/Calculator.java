package jp.crestmuse.cmx.inference;

//import jp.crestmuse.cmx.inference.MusicRepresentation;
//import jp.crestmuse.cmx.inference.MusicRepresentation.MusicElement;

/** @deprecated */
public interface Calculator {
  /**
   * この計算オブジェクトがどのイベントで発火するかを返す
   */
  // public MusicRepresentation.Type[] drivenBy();
  /**
   * この計算が渡されたオブジェクトに依存する場合trueを返す
   */
  // public boolean isCalc(MusicElement me, int track, int index);
  /**
   * 計算する
   */
  public void update(MusicRepresentation musRep, MusicElement me, int index);
}
