package jp.crestmuse.cmx.commands;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Notations;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;

/**
 * Rencon kit 配布用サンプル（例１）
 * 
 * このプログラムでは，楽譜（MusicXML）を読み込み，以下ふたつの演奏表情を生成した
 * deviationXMLならびに標準MIDIファイル（SMF）を出力します．
 * 
 * <ol>
 * <li>スタッカートのついた音符に対し，演奏時間を楽譜上の半分の長さにします．
 * <li>フェルマータがついた時刻で，テンポ（BPM）を1/2倍にします．
 * </ol>
 * 
 * run() 関数内にある各音符を呼び出す処理の記述方法が２通りありますので，<br>
 * PerformanceRenderingTest2.java の 記述例も参考にしてください．
 * 
 * @author renconmusic.org
 * @since 2007.07.31
 */
public class PerformanceRenderingTest2 extends CMXCommand {

	/**
	 * processNotePartWise() 関数を実行する際に呼び出すクラスです．
	 * 
	 * @author renconmusic.org
	 * 
	 */
	private class ProcessHandler implements NoteHandlerPartwise {

		public void beginMeasure(MusicXMLWrapper.Measure m, MusicXMLWrapper w) {
			System.out.println("Measure " + m.number());
		}

		public void beginPart(MusicXMLWrapper.Part p, MusicXMLWrapper w) {
			System.out.println("Part " + p.id());
		}

		public void endMeasure(MusicXMLWrapper.Measure m, MusicXMLWrapper w) {
		}

		public void endPart(MusicXMLWrapper.Part p, MusicXMLWrapper w) {
		}

		public void processMusicData(MusicXMLWrapper.MusicData md,
				MusicXMLWrapper w) {
			makeExpressionOfNote(md);
		}
	}

	/**
	 * 出力するSMFファイル名
	 */
	private String smffilename = null;

	/**
	 * deviationデータセット．
	 */
	private DeviationDataSet dds;

	/**
	 * MIDI分解能を表します．コマンドラインで -division <i>number</i>を指定すると任意の数字(<i>number</i>)に置き換えることができます．何も指定しない場合は480に設定されます．
	 */
	private int ticksPerBeat = 480;

	/**
	 * Rencon Kit サンプルプログラムです．
	 * <p>
	 * 実行するには，以下の引数を用意してください．
	 * 
	 * <code>PerformanceRenderingTest <i>inputfilename</i> -o[ut] <i>outputfilename</i> -smf <i>smffilename</i></code>
	 * 
	 * @param inputfilename
	 *            入力する楽譜ファイル名（MusicXML形式）
	 * @param ouputfilename
	 *            出力するファイル名（deviationXML形式）
	 * @param smffilename
	 *            出力する標準MIDIファイル名（SMF形式）
	 */
	public static void main(String[] args) {

		// 本クラスをインスタンス化し，start()関数を実行します．
		// 実行内容は main.run() 関数をオーバーライドして記述します．
		PerformanceRenderingTest2 main = new PerformanceRenderingTest2();
		try {
			main.start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.crestmuse.cmx.commands.CMXCommand#run()
	 */
	@Override
	protected void run() throws IOException, ParserConfigurationException,
			SAXException, TransformerException, InvalidFileTypeException {
		// 入力ファイルはindata()関数によって得ることができます．
		// このサンプルでは，想定するindata()の内容はMusicXML形式なので，
		// MusicXMLWrapperクラスにキャストしておきます．
		MusicXMLWrapper xml = (MusicXMLWrapper) indata();
		//xml.removeBlankTextNodes();

		// deviationXML形式の演奏表情データファイルを用意します．
		// newOutputData() 関数を呼び出すことで，出力先outdata()を得ることが
		// できるようになります．
		// deviationXMLはDeviationInstanceWrapperクラスとして記述されます．
		// このサンプルでは，出力先 outdata() を，DeviationInstanceWrapper
		// クラスのオブジェクト
		// outputDeviationData に代入しておきます．
		newOutputData(DeviationInstanceWrapper.TOP_TAG);
		DeviationInstanceWrapper outputDeviationData = (DeviationInstanceWrapper) outdata();

		// deviationXML が参照するMusicXMLファイルを指定します．
		outputDeviationData.setTargetMusicXMLFileName(xml.getFileName());

		// outputDeviationData にdeviation情報の格納先(dds)を用意します．
		dds = outputDeviationData.createDeviationDataSet();
		// 1小節目1拍目に，BPM=120 をセットするには addNonPartwiseControl()
		// 関数を用いて
		// 以下のように記述します．
		dds.addNonPartwiseControl(1, 1, "tempo", 120.);

		// 以下，MusicXMLから一音ずつ音符を取得し，条件に合えば演奏表情を付与します．
		// --------------------------------
		// （記述方法 例２）processNotePartwise()関数を呼び出す．
		// --------------------------------
		// この関数は，引数に NoteHandlerPartwise
		// インタフェースを持つクラス（ここではProcessHandler()）を取ります．
		// ProcessHandler()クラス内で，表情付け処理に必要なメソッドを実装します．
		// 実装例はProcessHandlerクラス内を参照してください．
		xml.processNotePartwise(new ProcessHandler());

		// 表情付与処理が終わったら以下の２文を実行します．
		dds.toWrapper();
                outputDeviationData.finalizeDocument();
//		outputDeviationData.analyze();

		// コマンドラインで -smf filename が指定された場合，SMFに出力します．
		// SMF出力にあたっては，SCCXMLという内部形式を経由します．
		// outputDeviationData -> sccxml -> midifile
		if (smffilename != null) {
			SCCXMLWrapper sccxml = (SCCXMLWrapper) CMXFileWrapper
					.createDocument(SCCXMLWrapper.TOP_TAG);
			outputDeviationData.toSCCXML(sccxml, ticksPerBeat);

			MIDIXMLWrapper midifile = (MIDIXMLWrapper) CMXFileWrapper
					.createDocument(MIDIXMLWrapper.TOP_TAG);
			sccxml.toMIDIXML(midifile);

			midifile.writefileAsSMF(smffilename);

			System.out.println("SMF output: " + smffilename);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.crestmuse.cmx.commands.CMXCommand#setOptionsLocal(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected boolean setOptionsLocal(String option, String value) {
		if (option.equals("-smf")) {
			smffilename = value;
			return true;
		} else if (option.equals("-division")) {
			ticksPerBeat = Integer.parseInt(value);
			return false;
		} else {
			return false;
		}
	}

	/**
	 * ひとつの音符に対し，関数内で記述した条件に合う音符に対して 演奏表情を付与します．<br>
	 * このサンプルプログラムでは，以下２つの条件を設定しています．
	 * 
	 * <ol>
	 * <li>スタッカートのついた音符に対し，演奏時間を楽譜上の半分の長さにします．
	 * <li>フェルマータがついた時刻で，テンポ（BPM）を1/2倍にします．
	 * </ol>
	 * 
	 * @param md
	 *            MusicXMLWrapper.MusicData クラスのオブジェクトです．
	 */
	private void makeExpressionOfNote(MusicXMLWrapper.MusicData md) {
		if (!(md instanceof MusicXMLWrapper.Note))
			return;

		MusicXMLWrapper.Note note = (MusicXMLWrapper.Note) md;
		String s = "Note[" + note.noteName();
		if (!note.rest())
			s += ", " + note.notenum();
		s += "] ";
		System.out.print(s);

		Notations notations = note.getFirstNotations();
		if (notations != null) {

			// サンプル１：staccato のついた音符の長さを半分に短くする
			// -----------------------
			// addNoteDeviation() 関数を用いて，各音の発音時刻(attack)，消音時刻(release),
			// 発音時打鍵速度(dynamics), 消音時離鍵速度(end-dynamics)を設定します．
			// attack, release については，表情なしを0.0として,
			// 四分音符単位の１拍に対する時刻の増減がdeviation情報になります．
			// end-dynamics については， 表情なしの初期値(現行は100)を1.0として，
			// 初期値に対する割合(0.0～1.27)がdeviation情報になります．
			// １拍あたりの音符の長さは，四分音符に対する音価(note.duration() / 4.)によって
			// 求めることができます．
			// -----------------------
			// MusicXML中，<chord/>タグのついた音符は，直前の<chord/>
			// なし音符（ここでは仮にroot音と呼びます）と同時に発音する和音をあらわします．
			// もしroot音にdeviationがあり，付随する和音にも同じ
			// deviationを付与したい場合は，root音に対してchord-deviationをセットします．
			if (notations.hasArticulation("staccato")) {
				System.out.print(" staccato");
				if (note.chordNotes() == null) {
					dds.addNoteDeviation(note, 0., -0.5 * note.duration() / 4.,
							1., 1.);
				} else {
					System.out.print(" w/chord ");
					dds.addChordDeviation(note, 0.,
							-0.5 * note.duration() / 4., 1., 1.);
				}
			}

			// サンプル２：フェルマータ時のテンポを1/2倍にゆっくりさせる
			if (notations.fermataType() != null) {
				System.out.print(" fermata");
				dds.addNonPartwiseControl(note.measure().number(), note.beat(),
						"tempo-deviation", 0.5);
			}
		}

		// 装飾音符については，MusicXMLに<duration>が記述されていないため，
		// 何か処理を行わないと音価が 0 のまま出力されます．それを避けるために，
		// ここでは 発音時刻（attack）を三十二分音符の分だけ前にずらしておきます．
		if (note.grace()) {
			dds.addNoteDeviation(note, -0.125, 0., 1.2, 1.);
		}

		System.out.println();
	}
}
