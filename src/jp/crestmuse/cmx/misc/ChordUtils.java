package jp.crestmuse.cmx.misc;

import java.util.ArrayList;
import java.util.Collections;

/**
 * コードを外部的に操作するメソッドをまとめたものです。
 * @author R.Tokuami
 *
 */
public class ChordUtils {
  static public String formatChord(String src){
    String dest = src;
    //TODO 表記を統一する
    return dest;
  }
  
  static public ArrayList<Integer> encodeChord(String src, int basenote){
    ArrayList<Integer> dest = new ArrayList<Integer>();
    //TODO 文字列からノートナンバーを求める
    
    return dest;
  }
  
  static public void sortNotes(ArrayList<Integer> src){
    Collections.sort(src);
    return;
  }
  
  public static void main(String[] args){
    //test
    
  }
}
