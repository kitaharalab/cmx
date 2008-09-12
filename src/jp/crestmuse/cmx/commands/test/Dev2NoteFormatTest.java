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
      FileWriter fw = new FileWriter(new File("output.txt"));
			
			
			//DeviationXMLに対応するMusicXML取得
			MusicXMLWrapper mus = dev.getTargetMusicXML();
			//ヘッダ作成
			NoteFormatElement header = new NoteFormatElement();
			header = header.setHeaderInformation(mus);
			//System.out.println(header.getHeader());
      fw.write(header.getHeader() + System.getProperty("line.separator"));
			
			//パート取得
			MusicXMLWrapper.Part[] partlist = mus.getPartList();
			int partcount = 1; //パート識別ナンバー、NoteFormatでのidentで使う　メインパートかどうかの判定は置いとく
			for (MusicXMLWrapper.Part part : partlist) {
				//パート内の小節リスト取得
				MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
				double currenttempo = measurelist[0].tempo();
				for (MusicXMLWrapper.Measure measure : measurelist) {
					//小節内のMusicDataList(NoteやらAttributeやら)取得
					MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
					double starttime_buf = 0;
					for (MusicXMLWrapper.MusicData md : mdlist) {
						//TODO mdがNoteなら書き出す
						if(md instanceof MusicXMLWrapper.Note){
							
							
							MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md; //Noteにダウンキャスト
							NoteFormatElement nf = new NoteFormatElement();
							nf.ident = String.valueOf(partcount);
							
							if(note.rest()){ //休符の時
								
							}
							else{
								nf.starttime = (measure.number()-1)*header.beattype + note.beat();
								nf.pitch = (note.pitchStep() + note.pitchOctave());
							
							
								//Deviation notewise
								DeviationInstanceWrapper.NoteDeviation currentdev = dev.getNoteDeviation((MusicXMLWrapper.Note)md);
								if(currentdev != null){
									nf.vel = (int)(dev.getBaseDynamics() * currentdev.dynamics());
									nf.on_devi = header.beattype/4 * currentdev.attack();
									nf.off_devi = header.beattype/4 * currentdev.release();
								}
								
								nf.len = note.actualDuration() ;
								
								//if(nf.pitch != null){ //休符でないnull音符の謎を回避・・・
									//Deviation non-partwise
									double currenttempo_dev = 1.0;
									for (Control cont : dev.getNonPartwiseList(dev)) {
										if(nf.starttime >= (cont.measure()-1)*header.beattype + cont.beat()){
											if(cont.type().equals("tempo")) currenttempo = cont.value();
											else if(cont.type().equals("tempo-deviation"))currenttempo_dev = cont.value();
										}
										else break;
									}
									if(starttime_buf != nf.starttime){
									  //System.out.println(nf.starttime+" BPM "+currenttempo*currenttempo_dev+" "+header.beattype);
									  fw.write(nf.starttime+" BPM "+currenttempo*currenttempo_dev+" "+header.beattype + System.getProperty("line.separator"));
										starttime_buf = nf.starttime;
									}
									//System.out.println(nf.getFormedElement());
									fw.write(nf.getFormedElement() + System.getProperty("line.separator"));
							
							}
						}
					}
				}
				//次のパートへ
				partcount++;
			}
			fw.close();
			
			
			

			
			
			
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Note形式の各要素(Header,音符,休符等)を持つクラスです。
	 * いっしょくたにしてるので良くないかも
	 */
	class NoteFormatElement{
		
		//header attributes
		int beat = -1; //分子
		int beattype = -1; //分母  beat/beattype で仕様書のBEATになります
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
		 * 080912現在、返す値にDeviation操作は加えていません
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
			nf.beat = Integer.parseInt(mus.getPartList()[0].getChildText("beats"));
			nf.beattype = Integer.parseInt(mus.getPartList()[0].getChildText("beat-type"));
			//nf.bpm = mus.getPartList()[0].getChildText();
			//nf.bpmbeat = mus.getPartList()[0].getChildText("beat-type");
			return nf;
		}
		
		/**
		 * ヘッダを返します
		 * 予めsetHeaderInfomation()などで値がセットされている必要があります
		 * @return String
		 */
		String getHeader(){
			
			String header = "Header {\n";
			
			if(beat != -1 && beattype != -1) 
				header += "  BEAT "+beat+"/"+beattype+"\n"; 
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
