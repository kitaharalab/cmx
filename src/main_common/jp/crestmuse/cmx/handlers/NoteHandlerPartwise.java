package jp.crestmuse.cmx.handlers;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;

/**********************************************************************
 *The <tt>NoteHandlerPartwise</tt> interface provides a general interface 
 *for processing each note in the partwise manner. 
 *These methods are called by the <tt>processNotePartwise</tt> method 
 *defined in the <tt>MusicXMLWrapper</tt> class. 
 *<br>
 *<tt>NoteHandlerPartwise</tt>インターフェースは, 
 *partwiseに各ノートを処理するための汎用的なインターフェースを提供します. 
 *これらのメソッドは, <tt>MusicXMLWrapper</tt>クラス内で定義されている
 *<tt>processNotePartwise</tt>メソッドから呼出されます. 
 *@author Tetsuro Kitahara <kitahara@kuis.kyoto-u.ac.jp>
 *@version 0.10.000
 *********************************************************************/
public interface NoteHandlerPartwise {

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *begins to process a part node. <br>
   *このメソッドは, <tt>MusicXMLWrapper.processNotePartwise</tt>メソッドが
   *partノードの処理を始める時に呼出されます. 
   *********************************************************************/
  public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *ends to process a part node. <br>
   *このメソッドは, <tt>MusicXMLWrapper.processNotePartwise</tt>メソッドが
   *partノードの処理を終える時に呼出されます. 
   *********************************************************************/
  public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *begins to process a measure node. <br>
   *このメソッドは, <tt>MusicXMLWrapper.processNotePartwise</tt>メソッドが
   *measureノードの処理を始める時に呼出されます. 
   *********************************************************************/
  public void beginMeasure(MusicXMLWrapper.Measure measure, 
                           MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *ends to process a measure node. <br>
   *このメソッドは, <tt>MusicXMLWrapper.processNotePartwise</tt>メソッドが
   *measureノードの処理を終える時に呼出されます. 
   *********************************************************************/
    public void endMeasure(MusicXMLWrapper.Measure measure, 
                           MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *finds a note node. 
   *Please implement the processing for each note in this method. 
   *このメソッドは, <tt>MusicXMLWrapper.processNotePartwise</tt>メソッドが
   *noteノードを見つけたときに呼出されます. 
   *このメソッドに各noteに対して行う処理を実装してください. 
   *********************************************************************/
    public void processMusicData(MusicXMLWrapper.MusicData md, 
                                 MusicXMLWrapper wrapper);

}