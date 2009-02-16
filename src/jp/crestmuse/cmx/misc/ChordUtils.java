package jp.crestmuse.cmx.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * コードを外部的に操作するメソッドをまとめたものです。
 * @author R.Tokuami
 *
 */
public class ChordUtils {
  
  static Map<String, Integer> keydist = new HashMap<String, Integer>();
  static{
    keydist.put("C", 0);  keydist.put("B#", 0);
    keydist.put("C#", 1); keydist.put("Db", 1);
    keydist.put("D", 2);
    keydist.put("D#", 3); keydist.put("Eb", 3);
    keydist.put("E", 4);  keydist.put("Fb", 4);
    keydist.put("F", 5);
    keydist.put("F#", 6); keydist.put("Gb", 6);
    keydist.put("G", 7);  
    keydist.put("G#", 8); keydist.put("Ab", 8);
    keydist.put("A", 9);
    keydist.put("A#", 10);keydist.put("Bb", 10);
    keydist.put("B", 11); keydist.put("Cb", 11);
    
  }
  
  
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
