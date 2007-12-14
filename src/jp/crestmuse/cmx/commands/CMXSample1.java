package jp.crestmuse.cmx.commands;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;

public class CMXSample1 extends CMXCommand {

    // ここのサンプルで使うハンドラ.
    // ハンドラはCMX独自の用語で, process系メソッド(独自用語)で
    // 特定のタグに出会ったときに呼び出され, 出会ったタグに対して
    // すべき処理の内容を記述する.
  private NoteHandlerPartwise handler = new HandlerSample();

  private DeviationInstanceWrapper dev;

    // メインの処理の内容はrunに書く.
  protected void run() 
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
               // throwsの後はとりあえずおまじないとしてこの通り書きましょう.
//    readInputData();	// コマンドラインで指定したファイルを読み込む．
                        // 読み込んだ結果は CMXFileWrapper オブジェクトとして
                        // 保持され, indata()でアクセスできる.
    dev = (DeviationInstanceWrapper)indata();
    MusicXMLWrapper musicxml = dev.getTargetMusicXML();
                        // target属性を見て，対象となるMusicXMLを読み込む.
//    dev.addLinks("//note-deviation", musicxml);
                        // note-deviationタグに書かれたURL(http://...）を見て
                        // リンク元とリンク先(リンク先は指定されたXMLドキュメント
                        // のどこかのノードと仮定)を結びつける.
    musicxml.processNotePartwise(handler);
                        // このサンプルで使うprocess系メソッド.
                        // MusicXMLドキュメントを順番に読み, 
                        // part, measure, noteなどのタグに出会ったときに
                        // イベントを発生させる.
  }

    // mainメソッドは基本的にはこのように書く.
  public static void main(String[] args) {
    CMXSample1 sample1 = new CMXSample1();
    try {
      sample1.start(args);
    } catch (Exception e) {
      sample1.showErrorMessage(e);
      System.exit(1);
    }
  }

    // ハンドラをインナークラスとして記述.
    private class HandlerSample implements NoteHandlerPartwise {

        // part開始タグに出会ったときの処理.
        // MusicXMLWrapper.Partなどはノードインターフェイス(独自用語)と言い, 
        // XMLノードから情報を簡単に取ってこられるようにしたもの. 
        // XMLノードから情報を取ってくる場合には, 基本的にはここから取ってくる. 
        // ノードインターフェイスで対応していない情報が欲しい場合には, 
        // ノードインターフェイスからNodeオブジェクトを取ってきて, 
        // 直接DOM系メソッドを叩くこともできる.
      public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper w) {
        System.out.println("New Part: " + part.id());
      }

        // part終了タグに出会ったときの処理.
      public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper w) {
      }

        // measure開始タグに出会ったときの処理
      public void beginMeasure(MusicXMLWrapper.Measure measure, 
                               MusicXMLWrapper w) {
      }

        // measure終了タグに出会ったときの処理
      public void endMeasure(MusicXMLWrapper.Measure measure, 
                             MusicXMLWrapper w) {
      }

        // noteタグを始めとする音楽データ系タグに出会ったときの処理.
      public void processMusicData(MusicXMLWrapper.MusicData md,  
                                   MusicXMLWrapper w) {
        // 出会った音楽データ系タグの正体がnoteタグの場合には, 
        // MusicXMLWrapper.MusicDataオブジェクトは, 実際には
        // そのサブクラスであるMusicXMLWrapper.Noteなので, 
        // こうすればnoteタグかどうか検査できる. 
        if (md instanceof MusicXMLWrapper.Note) {
	  System.out.println("Note: ");
	  MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
                             // キャスト
          System.out.println("pitch-step: " + note.pitchStep());
                             // このようにNoteオブジェクトから簡単に
                             // Noteタグ内の情報を取ってこられる.
//          Node linkednode = 
//            w.linkmanager.getNodeLinkedTo(note.node(), "note-deviation");
//                             // noteタグに対してリンクを張ってある
//                             // note-deviationという名前のノードを取得.
//          if (linkednode != null) {    // リンク元が見付かれば.
//             DeviationInstanceWrapper.NoteDeviation nd = 
//               dev.getNoteDeviationNodeInterface(linkednode);
          DeviationInstanceWrapper.NoteDeviation nd = 
            dev.getNoteDeviation(note);
          if (nd != null) {
            System.out.println("dynamics: " + nd.dynamics());
                             //例としてdeviationからdynamicsを取得・表示.
          }
        }
      }
    }
}