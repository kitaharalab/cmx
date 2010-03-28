package jp.crestmuse.cmx.filewrappers;

import java.util.List;

/**
 * 音符の集まりからなる1つのグループを示すインターフェイスです。
 */
public interface NoteGroup {

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
  public NoteGroup makeSubgroup(List<MusicXMLWrapper.Note> notes);

  public void removeSubgroup(NoteGroup g);

  /**
   * このグループの頂点となるNoteを設定します。
   * 
   * @param n
   */
  public void setApex(MusicXMLWrapper.Note n);

  /**
   * このグループの頂点となるNoteと、どの程度目立っているかを設定します。 どの程度目立っているかに関しての計算方法は規程されていません。
   * 定義されていない場合は、SetApex(Note n)を利用するか、Double.NaNをセットしてください。
   * 
   * @param n
   *          頂点となるNoteオブジェクト
   * @param saliency
   *          どの程度目立っているか
   */
  public void setApex(MusicXMLWrapper.Note n, double saliency);

  public String getAttribute(String key);

  public void setAttribute(String key, String value);

}
