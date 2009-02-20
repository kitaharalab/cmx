package jp.crestmuse.cmx.musicrepresentation;

import jp.crestmuse.cmx.musicrepresentation.MusicRepresentation.MusicElement;

public interface Calculator {
  /**
   * この計算オブジェクトがどのイベントで発火するかを返す
   */
  public MusicRepresentation.Type[] drivenBy();
  /**
   * この計算が渡されたオブジェクトに依存する場合trueを返す
   */
  //public boolean isCalc(MusicElement me, int track, int index);
  /**
   * 計算する
   */
  public void update(MusicElement me, int index);
}
