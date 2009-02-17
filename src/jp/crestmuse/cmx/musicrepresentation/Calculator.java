package jp.crestmuse.cmx.musicrepresentation;

public interface Calculator {
  /**
   * この計算が渡されたオブジェクトに依存する場合trueを返す
   */
  public boolean isCalc(MusicElement me, int track, int index);

  /**
   * 計算する
   */
  public void update(MusicElement me, int track, int index);
}
