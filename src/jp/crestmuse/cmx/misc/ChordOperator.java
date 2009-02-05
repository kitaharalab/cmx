package jp.crestmuse.cmx.misc;

import java.util.HashMap;
import java.util.Map;

public class ChordOperator {

  /**
   * 自動伴奏のコードSMFで用いる、ダイアトニックコードの根音から
   * 相対的なノートナンバーの差をint配列で返すメソッドです。
   * @param chord 
   * @return
   */
  static int[] diatonic_map_inC(String chord){
    Map<String, int[]> diatonic_map = new HashMap<String, int[]>();
    diatonic_map.put("C", new int[] {0, 0, 0});
    diatonic_map.put("Dm", new int[] {2, 1, 2});
    diatonic_map.put("Em", new int[] {4, 3, 4});
    diatonic_map.put("F", new int[] {5, 5, 5});
    diatonic_map.put("G", new int[] {7, 7, 7});
    diatonic_map.put("Am", new int[] {9, 8, 9});
    diatonic_map.put("Bdim", new int[] {11, 10, 10});

    return diatonic_map.get(chord);
  }
}
