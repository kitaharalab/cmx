package jp.crestmuse.cmx.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
   * コード名から和音オブジェクトを作成します。
   * @param chordname コード名
   */
  public Chord(String chordname) throws RuntimeException{
    //TODO ダイアトニックコードのみ暫定
    //Chord(chordname, defaultBasenote);
    this.name = chordname;
    this.inversion = 0;
    if(chordname.equals("C")) setNoteList(basenote,basenote+4,basenote+7);
    else if(chordname.equals("Dm")) setNoteList(basenote+2,basenote+5,basenote+9);
    else if(chordname.equals("Em")) setNoteList(basenote+4,basenote+7,basenote+11);
    else if(chordname.equals("F")) setNoteList(basenote+5,basenote+9,basenote+12);
    else if(chordname.equals("G")) setNoteList(basenote+7,basenote+11,basenote+14);
    else if(chordname.equals("Am")) setNoteList(basenote+9,basenote+12,basenote+16);
    else if(chordname.equals("Bm(b5)")) setNoteList(basenote+11,basenote+14,basenote+17);
    else throw new RuntimeException("It is not Diatonic Chord.");
  }
  
  /**
   * コード名とBaseNoteを指定して和音オブジェクトを作成します。
   * 作成される和音は、BaseNoteを基準とします。
   * BaseNoteは作成される和音の根から最も近く低いC(ド)の音です。
   * @param chordname コード名
   * @param basenote
   */
  public Chord(String chordname, int basenote){
    this.basenote = basenote;
    this.name = formatChord(chordname);
    this.inversion = 0;
    this.notes = encodeChord(this.name, this.basenote);
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
  
  

  static String[] allKeys = new String[]{"C","B#","Db","C#","D","Eb","D#","E","Fb","F","F#","Gb","G","Ab","G#","A","Bb","A#","B","Cb"};;
  static Map<String, Integer> key2dist = new HashMap<String, Integer>();
  static Map<Integer, String[]> dist2key = new HashMap<Integer, String[]>();
  static{
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
    String dest = new String();
    //dest.concat(src.substring(0, getPrefixLength(src)));
    
    return dest;
  }
  
  static public ArrayList<Integer> encodeChord(String src, int basenote){
    String temp = new String();
    ArrayList<Integer> dest = new ArrayList<Integer>();
    dest.add(0); dest.add(4); dest.add(7);
    //delete root
    temp = src.substring(getPrefixLength(src));
    //3rd
    if(temp.startsWith("m")){
    }
    else if(temp.startsWith("sus4")){
    }
    //
    
    
    return dest;
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
