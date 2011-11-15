package jp.crestmuse.cmx.misc;

/** @deprecated */
public class ChordOperator {

  
  
  
  
  

  /**
   * 自動伴奏のコードSMFで用いる、ダイアトニックコードの根音から
   * 相対的なノートナンバーの差をint配列で返すメソッドです。
   * @param chord 
   * @return
   */
  public static int[] diatonic_map_inC(String chord){
    int[] diatonic = new int[3];
    if(chord.equals("C")) diatonic = new int[] {0, 0, 0};
    else if(chord.equals("Dm")) diatonic = new int[] {2, 1, 2};
    else if(chord.equals("Em")) diatonic = new int[] {4, 3, 4};
    else if(chord.equals("F")) diatonic = new int[] {5, 5, 5};
    else if(chord.equals("G")) diatonic = new int[] {7, 7, 7};
    else if(chord.equals("Am")) diatonic = new int[] {9, 8, 9};
    else diatonic =  new int[] {11, 10, 10}; //Bdim

    return diatonic;
  }

}
