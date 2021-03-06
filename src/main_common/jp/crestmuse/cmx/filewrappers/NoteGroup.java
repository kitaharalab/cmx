package jp.crestmuse.cmx.filewrappers;

import java.util.List;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

/**
 * 音符の集まりからなる1つのグループを示すインターフェイスです。
 */
public interface NoteGroup extends Cloneable {
  
  public Object clone() throws CloneNotSupportedException;

  /**
   * グループの深さを返します。 (トップレベルの深さは1)
   * 
   * @return
   */
  public int depth();

  /**
   * グループの頂点が子グループに遺伝するかどうかを返します。
   * 
   * @return 遺伝する時true
   */
  public boolean isApexInherited();

  /**
   * このグループの頂点がどのぐらい目立っているかを返します。 具体的な計算方法は規程されていません。
   * 定義されていない場合はDouble.NaNが入るよう実装してください。
   * 
   * @return
   */
  public double getApexSaliency();

  /**
   * このグループに所属するNoteオブジェクトのリストを返します。 グループが初期化されていれば、空のArrayListが返ります。
   * 
   * @return
   */
  public List<MusicXMLWrapper.Note> getNotes();

  /**
   * このグループ以下に所属するサブグループも含めて、 全てのNoteオブジェクトが含まれるリストを返します。
   * グループが初期化されていれば、空のArrayListが返ります。
   * 
   * @return
   */
  public List<MusicXMLWrapper.Note> getAllNotes();

  /**
   * このグループの頂点となるNoteオブジェクトを返します。 定義されていなければnullが返ります。
   * 
   * @return
   */
  public MusicXMLWrapper.Note getApex();

  /**
   * このグループに所属する子グループのリストを返します。 グループが初期化されていれば、空のArrayListが返ります。
   * 
   * @return
   */
  public List<NoteGroup> getSubgroups();

  /**
   * このグループにNoteを追加します。
   * 
   * @param n
   *          加えるNoteオブジェクト
   */
  public void addNote(MusicXMLWrapper.Note n);

  public void removeNote(MusicXMLWrapper.Note n);

  /**
   * このグループの子に引数として与えたグループを追加します。 別に作成したグループを追加するにはこのメソッドを利用してください。
   * 既にこのインスタンスに所属するノートをグループ化する場合はmakeSubgroupを利用してください。
   * 
   * @param g
   */
  public void addSubgroup(NoteGroup g);

  /**
   * このグループ内のノートをグループ化し、子グループとして追加します。
   * 既にグループに所属するノートを、そのグループの子としてグループ化するにはこのメソッドを利用してください。
   * 別に作成したグループを追加する場合はaddSubgroupを利用してください。
   * 
   * @param notes
   *          Noteオブジェクトのリスト
   */
  public NoteGroup makeSubgroup(List<Note> notes);

  public void removeSubgroup(NoteGroup g);

  public void setApexStart(Note n, double time);

  public void setApexStop(Note n, double time);

  /**
   * このグループの頂点となるNoteを設定します。
   * 
   * @param n
   */
  public void setApex(Note n);

  public Note getApexStart();

  public double getApexStartTime();

  public Note getApexStop();

  public double getApexStopTime();
  
  public void setApexSaliency(double saliency);

  public String getAttribute(String key);

  public void setAttribute(String key, String value);

  public boolean isImplicit();
  
  public void setImplicit(boolean value);

  public List<Note> getImplicitGroupNotes();

  public int type();

}
