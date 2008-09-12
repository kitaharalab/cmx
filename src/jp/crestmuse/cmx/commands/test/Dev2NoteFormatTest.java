package jp.crestmuse.cmx.commands.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.Control;
import jp.crestmuse.cmx.misc.TreeView;

public class Dev2NoteFormatTest {

	
	public Dev2NoteFormatTest(DeviationInstanceWrapper dev){
				
		//System.out.println(dev.getTargetMusicXMLFileName()); #=> wiener-p012-013.xml 読めてはいる
		try{
			//かきこみじにつかう
			//File file = new File("output.txt");
			//BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			
			//DeviationXMLに対応するMusicXML取得
			MusicXMLWrapper mus = dev.getTargetMusicXML();
			//ヘッダ作成
			NoteFormatElement header = new NoteFormatElement();
			header.setHeaderInformation(mus);
			
			//パート取得
			MusicXMLWrapper.Part[] partlist = mus.getPartList();
			int partcount = 0; //パート識別ナンバー、NoteFormatでのidentで使う　メインパートかどうかの判定は置いとく
			for (MusicXMLWrapper.Part part : partlist) {
				//パート内の小節リスト取得
				MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
				for (MusicXMLWrapper.Measure measure : measurelist) {
					//小節内のMusicDataList(NoteやらAttributeやら)取得
					MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
					for (MusicXMLWrapper.MusicData md : mdlist) {
						//TODO mdがNoteなら書き出す
						if(md instanceof MusicXMLWrapper.Note){
							
							MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md; //Noteにダウンキャスト
							NoteFormatElement nf = new NoteFormatElement();
							nf.ident = String.valueOf(partcount);
							
							if(note.rest()){ //休符の時
								
							}
							else{
								nf.pitch = (note.pitchStep() + note.pitchOctave());

							}
							
							//TODO 基準となる四分音符の長さを取っとく
							DeviationInstanceWrapper.NoteDeviation currentdev = dev.getNoteDeviation((MusicXMLWrapper.Note)md);
							if(currentdev != null){
								//nf.vel = hogehoge() * currentdev.dynamics();
							}
							
							
							
						}
					}
				}
				//次のパートへ
				partcount++;
			}
			
			
			
			/* non-partwise
			for (Control cont : dev.getNonPartwiseList(dev)) {
				System.out.println(cont.value());
			}
			*/
			
			
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Note形式の各要素(Header,音符,休符等)を持つクラスです。
	 * いっしょくたにしてるので良くない
	 */
	class NoteFormatElement{
		
		//header attributes
		String beat = null;
		String bpm = null, bpmbeat = null;
		String beattime = null, beattimebeat = null;
		String tactus = null, tactusbeat = null;
		String timescale = "BEAT"; //必須
		String title = null;
		String composer = null;
		String perfomer = null;
		String accompanist = null;
		String arranger = null;
		String generator = null;
		
		//body attributes
		double starttime; //
		double on_devi;
		String ident;
		String pitch;
		double off_devi;
		int vel;
		double off_vel; //TODO CMXで未実装
		double len;
		
		
		/**
		 * NoteFormatの書式で音符を返します
		 * スタート時刻 (On_devi ident Pitch vel Len Off_devi)
		 * @return String
		 */
		String getFormedElement(){
			return starttime+" ("+on_devi+" "+ident+" "+pitch+" "+vel+" "+len+" "+off_devi+")";
		}
		
		/**
		 * 最低限の情報のNoteFormatの書式を返します
		 * 080912現在値にDeviation操作は加えていません
		 * @return String
		 */
		String getSimpleFormedElement(){
			return starttime+" ("+pitch+" "+len+")";
		}
		
		
		/**
		 * MusicXMLWrapperからHeader情報を取得し返します
		 * @param mus MusicXMLWrapper
		 * @return NoteFormatElement
		 */
		NoteFormatElement setHeaderInformation(MusicXMLWrapper mus){
			//TODO できるだけ多く取る
			NoteFormatElement nf = new NoteFormatElement();
			nf.title = mus.getMovementTitle();
			
			return nf;
		}
		
		/**
		 * ヘッダを返します
		 * 予め値がセットされている必要があります
		 * @return String
		 */
		String getHeader(){
			
			String header = "Header {\n";
			
			if(beat != null) 
				header += "  BEAT "+beat+"\n"; 
			if(bpm != null) 
				header += "  BPM "+bpm+" "+bpmbeat+"\n";
			if(beattime != null) 
				header += "  BEATTIME "+beattime+" "+beattimebeat+"\n";
			if(tactus != null) 
				header += "  TACTUS "+tactus+" "+tactusbeat+"\n";
			if(timescale != null)
				header += "  TIMESCALE "+timescale+"\n"; //delta未対応
			if(title != null)
				header += "  TITLE \""+title+"\"\n";
			if(composer != null)
				header += "  COMPOSER \""+composer+"\"\n";
			if(perfomer != null)
				header += "  PERFORMER \""+perfomer+"\"\n";
			if(accompanist != null)
				header += "  ACCOMPANIST \""+accompanist+"\"\n";
			if(arranger != null)
				header += "  ARRANGER \""+arranger+"\"\n";
			if(generator != null)
				header += "  GENERATOR \""+generator+"\"\n";
					
			
			return header+"}\n";
		}
	}
	
	public static void main(String[] args) {
		
		try {
			DeviationInstanceWrapper dev = (DeviationInstanceWrapper) CMXFileWrapper.readfile("deviation.xml");
			new Dev2NoteFormatTest(dev);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		for (String deviationXML : args) {
			
			try {
				DeviationInstanceWrapper dev = (DeviationInstanceWrapper) CMXFileWrapper.readfile(deviationXML);
				new Dev2NoteFormatTest(dev);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		
		
	}

}
