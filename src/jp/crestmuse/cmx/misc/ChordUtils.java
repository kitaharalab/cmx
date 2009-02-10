package jp.crestmuse.cmx.misc;

import java.util.ArrayList;
import java.util.Collections;

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
  
  static public ArrayList<Integer> invertChord(ArrayList<Integer> src, int invertion){
    ChordUtils.sortNotes(src);
    //TODO 要テスト
    if(invertion != 0){
      if(invertion>0){
        src.set(0, src.get(0)+12);
        ChordUtils.invertChord(src, invertion--);
      }
      else if(invertion<0){
        src.set(src.size()-1, src.get(src.size()-1)-12);
        ChordUtils.invertChord(src, invertion++);
      }
    }
    return src;
  }
  
  static public void sortNotes(ArrayList<Integer> src){
    Collections.sort(src);
    return;
  }
  
  public static void main(String[] args){
    //test
  }
}
