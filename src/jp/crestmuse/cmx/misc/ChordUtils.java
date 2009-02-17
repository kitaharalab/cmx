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
  
  static String[] allKeys;
  static Map<String, Integer> key2dist = new HashMap<String, Integer>();
  static Map<Integer, String[]> dist2key = new HashMap<Integer, String[]>();
  static{
    allKeys = new String[]{"C","B#","Db","C#","D","Eb","D#","E","Fb","F","F#","Gb","G","Ab","G#","A","Bb","A#","B","Cb"};
    key2dist.put("C", 0);  key2dist.put("B#", 0);
    key2dist.put("C#", 1); key2dist.put("Db", 1);
    key2dist.put("D", 2);
    key2dist.put("D#", 3); key2dist.put("Eb", 3);
    key2dist.put("E", 4);  key2dist.put("Fb", 4);
    key2dist.put("F", 5);
    key2dist.put("F#", 6); key2dist.put("Gb", 6);
    key2dist.put("G", 7);
    key2dist.put("G#", 8); key2dist.put("Ab", 8);
    key2dist.put("A", 9);
    key2dist.put("A#", 10);key2dist.put("Bb", 10);
    key2dist.put("B", 11); key2dist.put("Cb", 11);

    dist2key.put(0, new String[]{"C","B#"});
    dist2key.put(1, new String[]{"Db","C#"});
    dist2key.put(2, new String[]{"D"});
    dist2key.put(3, new String[]{"Eb","D#"});
    dist2key.put(4, new String[]{"E","Fb"});
    dist2key.put(5, new String[]{"F"});
    dist2key.put(6, new String[]{"F#","Gb"});
    dist2key.put(7, new String[]{"G"});
    dist2key.put(8, new String[]{"Ab","G#"});
    dist2key.put(9, new String[]{"A"});
    dist2key.put(10, new String[]{"Bb","A#"});
    dist2key.put(11, new String[]{"B","Cb"});
  }
  
  static public int getPrefixLength(String src) throws RuntimeException{
    int dest = -1;
    for (String str : allKeys) {
      if(src.startsWith(str)){
        dest = str.length();
        break;
      }
    }
    if(dest == -1) throw new RuntimeException("Processing Invalid Chord Data");
    return dest;
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
