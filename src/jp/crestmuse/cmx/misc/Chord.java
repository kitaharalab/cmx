package jp.crestmuse.cmx.misc;

import java.util.ArrayList;

/**
 * ひとつの和音を扱うクラスです。
 * 
 * @author R.Tokuami
 *
 */
public class Chord {
  private String name;
  private ArrayList<Integer> notes = new ArrayList<Integer>();;
  private int inversion = 0;
  
  private int basenote = 48;
  public String NAMELESS = "noname";
  
  /**
   * ノートナンバーから和音オブジェクトを作成します。
   * @param notes ノートナンバー(可変長引数)
   */
  public Chord(int... vlnotes){
    for (int i : vlnotes) {
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
      invertChord(inversion);
    }
    else{
      this.inversion = 0;
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
   * コードを転回します。
   * 転回は引数回一番高い|低い音を1オクターブ下げる|上げることで実現しています。
   * @param inversion
   */
  public void invertChord(int inversion){
    ChordUtils.sortNotes(this.notes);
    if(inversion != 0){
      if(inversion>0){
        this.notes.set(0, this.notes.get(0)+12);
        ChordUtils.sortNotes(this.notes);
        this.inversion++;
        invertChord(inversion-1);
      }
      else if(inversion<0){
        this.notes.set(this.notes.size()-1, this.notes.get(this.notes.size()-1)-12);
        ChordUtils.sortNotes(this.notes);
        this.inversion--;
        invertChord(inversion+1);
      }
    }
    return;
  }
  /**
   * コードが転回されたものかどうかを返します。
   * 現在のコードが既知の和音の転回形に当るかどうかではなく、
   * 作成されたコードが転回したものかどうかを返します。
   * @return
   */
  public boolean isInverted(){
    return inversion != 0 ? true : false;
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
  /**
   * 和音の名前を返します。
   * @return
   */
  public String getChordName(){
    return name;
  }
  /**
   * 和音に含まれるノートのリストを返します。
   * @return
   */
  public ArrayList<Integer> getNotesList(){
    return notes;
  }
  /**
   * コードに含まれるノートナンバーを文字列表現で返します。
   */
  public String toString(){
    String dest = new String();
    for (int note : this.notes) {
      dest += note;
      if(this.notes.size()-1 != notes.lastIndexOf(note)){
        dest += ",";
      }
    }
    return dest;
  }
  
  public void printNotes(){
    System.out.println(this.toString());
  }
  
  /**
   * コードの状態を文字列表現で表示します。
   * [デバッグ用]
   */
  public void printChordStats(){
    System.out.println(this.name+":"+toString()+" Inv:"+this.inversion+" Base:"+this.basenote);
  }
  
  public static void main(String[] args){
    //test
    Chord c = new Chord(48, 52, 55);
    c.printChordStats();
    c.invertChord(2);
    c.printChordStats();
    c.invertChord(-2);
    c.printChordStats();
  }
  
}
