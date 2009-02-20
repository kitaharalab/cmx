package jp.crestmuse.cmx.misc;

import java.util.ArrayList;
import java.util.Collections;

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
  public final int defaultBasenote = 48;
  private int basenote = defaultBasenote;
  public String NAMELESS = "noname";
  
  //TODO 検討：ノートナンバーのリストを持つか、Basenoteとの差のリストを持つか
  //TODO 実装：basenote指定のコンストラクタ
  
  /**
   * ノートナンバーから和音オブジェクトを作成します。
   * @param notes ノートナンバー(可変長引数)
   */
  public Chord(int... vlnotes){
    setNoteList(vlnotes);
    this.name = NAMELESS;
    this.inversion = 0;
    this.basenote = -1; //暫定
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
  public Chord(String chordname) throws RuntimeException{
    //TODO 暫定
    if(chordname.equals("C")) setNoteList(basenote,basenote+4,basenote+7);
    else if(chordname.equals("Dm")) setNoteList(basenote+2,basenote+5,basenote+9);
    else if(chordname.equals("Em")) setNoteList(basenote+4,basenote+7,basenote+11);
    else if(chordname.equals("F")) setNoteList(basenote+5,basenote+9,basenote+12);
    else if(chordname.equals("G")) setNoteList(basenote+7,basenote+11,basenote+14);
    else if(chordname.equals("Am")) setNoteList(basenote+9,basenote+12,basenote+16);
    else if(chordname.equals("Bm(b5)")) setNoteList(basenote+11,basenote+14,basenote+17);
    else throw new RuntimeException("It is note Diatonic Chord.");
    this.name = chordname;
    this.inversion = 0;
    //this(chordname, 0);
  }
  
  /**
   * コードを転回します。
   * 転回は引数回一番高い|低い音を1オクターブ下げる|上げることで実現しています。
   * @param inversion
   */
  public void invertChord(int inversion){
    sortNotes(this.notes);
    if(inversion != 0){
      if(inversion>0){
        this.notes.set(0, this.notes.get(0)+12);
        sortNotes(this.notes);
        this.inversion++;
        invertChord(inversion-1);
      }
      else if(inversion<0){
        this.notes.set(this.notes.size()-1, this.notes.get(this.notes.size()-1)-12);
        sortNotes(this.notes);
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
  
  /**
   * 基準となるC(ド)の位置を返します。
   * @return
   */
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
  
  private void setNoteList(int... vlnotes){
    for (int i : vlnotes) {
      this.notes.add(i);
    }
  }
  
  private void sortNotes(ArrayList<Integer> src){
    Collections.sort(src);
    return;
  }
  
  public static void main(String[] args){
    //test
    Chord c = new Chord("C");
    c.printChordStats();
    c.invertChord(2);
    c.printChordStats();
    c.invertChord(-2);
    c.printChordStats();
  }
  
}
