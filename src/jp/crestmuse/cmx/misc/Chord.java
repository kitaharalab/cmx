package jp.crestmuse.cmx.misc;

import java.util.ArrayList;

public class Chord {
  private String name;
  private ArrayList<Integer> notes;
  private int inversion;
  private boolean inverted;
  
  private int basenote = 48;
  public String NAMELESS = null;
  
  /**
   * ノートナンバーから和音オブジェクトを作成します。
   * @param notes ノートナンバー(可変長引数)
   */
  public Chord(int... notes){
    for (int i : notes) {
      this.notes.add(i);
    }
    this.name = NAMELESS;
    this.inversion = 0;
  }
  
  /**
   * コード名と転回形から和音オブジェクトを作成します。
   * 作成される和音は、BaseNoteより高く最も近い音を根とする和音です。
   * @param chordname コード名
   * @param inversion 転回形(基本形=0)
   */
  public Chord(String chordname, int inversion){
    //TODO chordname = format(chordname); 
    this.name = chordname;
    
    if(inversion != 0){
      //TODO invert(inversion)
      this.inverted = true;
    }
    else{
      this.inverted = false;
    }
  }
  
  /**
   * コード名から和音オブジェクトを作成します。
   * 作成される和音は、BaseNoteより高く最も近い音を根とする転回基本形の和音です。
   * @param chordname コード名
   */
  public Chord(String chordname){
    this(chordname, 0);
  }
  
  /**
   * コードが転回されたものかどうかを返します。
   * @return
   */
  public boolean isInverted(){
    return inverted;
  }
  public int getBaseNote(){
    return basenote;
  }
  /**
   * 基準となるC(ド)の位置を指定します。
   * @param num ノートナンバー
   * @throws RuntimeException
   */
  public void setBaseNote(int num) throws RuntimeException{
    if(num%12 != 0 || num<0 || 128<num) 
      throw new RuntimeException("BaseNoteは12の倍数でかつMidiノートナンバーの範囲内でなければいけません");
    basenote = num;
    return;
  }
  
}
