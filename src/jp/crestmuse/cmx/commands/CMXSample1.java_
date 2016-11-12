package jp.crestmuse.cmx.commands;

import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.filewrappers.*;

// コマンドはCMXCommandというクラスのサブクラスとして定義する。
// ここでは、入力データがDeviationInstanceWrapperオブジェクトとして取得されることを
// 指定している。この例では出力データはないので、出力データの方パラメータには、
// ダミーとしてFileWrapperCompatibleインターフェースを指定している。
public class CMXSample1 
  extends CMXCommand<DeviationInstanceWrapper,FileWrapperCompatible> {

  // メインの処理の内容はrunに書く.
  protected FileWrapperCompatible run(final DeviationInstanceWrapper dev) 
    throws IOException, ParserConfigurationException, 
    TransformerException, SAXException, InvalidFileTypeException {
    // throwsの後はとりあえずおまじないとしてこの通り書きましょう.
    
    // target属性を見て，対象となるMusicXMLを読み込む.
    final MusicXMLWrapper musicxml = dev.getTargetMusicXML();

    // 匿名クラスという仕組みを使って、runメソッド内でNoteHandlerAdapterPartwise
    // クラスのサブクラスを定義し、それを引数としてprocessNotePartwiseを実行する。
    musicxml.processNotePartwise(new NoteHandlerAdapterPartwise() {	

        // noteタグを始めとする音楽データ系タグに出会ったときの処理.
	public void processMusicData(MusicXMLWrapper.MusicData md,  
				     MusicXMLWrapper w) {
	  // 出会った音楽データ系タグの正体がnoteタグの場合には, 
	  // MusicXMLWrapper.MusicDataオブジェクトは, 実際には
	  // そのサブクラスであるMusicXMLWrapper.Noteなので, 
	  // こうすればnoteタグかどうか検査できる. 
	  if (md instanceof MusicXMLWrapper.Note) {
	    System.out.println("Note: ");
	    // キャスト
	    MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
	    // このようにNoteオブジェクトから簡単に
	    // Noteタグ内の情報を取ってこられる.
	    System.out.println("pitch-step: " + note.pitchStep());
	    DeviationInstanceWrapper.NoteDeviation nd = 
	      dev.getNoteDeviation(note);
	    if (nd != null) {
	      //例としてdeviationからdynamicsを取得・表示.
	      System.out.println("dynamics: " + nd.dynamics());
	    }
	  }
	}
      });
    // 今回の例では出力すべきデータはないので、nullを返す。
    return null;
    
  }

  // mainメソッド
  // mainメソッド内でstartメソッドを呼び出すと、必要な処理がなされた、後に
  // 上で定義したrunメソッドが呼び出される。
  public static void main(String[] args) {
    CMXSample1 sample1 = new CMXSample1();
    try {
      sample1.start(args);
    } catch (Exception e) {
      sample1.showErrorMessage(e);
      System.exit(1);
    }
  }
}