package jp.crestmuse.cmx.commands.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.Control;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper.ExtraNote;

/**
 * DeviationInstanceWrapperからNote形式に変換するクラスです。
 * @author R.Tokuami
 */

public class Dev2NoteFormatTest {

	boolean debug_mode = false; //デバッグモードのフラグ
	FileWriter fw;
	public Dev2NoteFormatTest(DeviationInstanceWrapper dev){
  	
  	//debug_modeでないならファイルに出力
  	if(!debug_mode){
  		try{
  			fw = new FileWriter(new File("output.txt"));
  		}
  		catch(Exception e){
  			e.printStackTrace();
  		}
  	}
  		
  	
  	try{			
  		
  		//DeviationXMLに対応するMusicXML取得
  		MusicXMLWrapper mus = dev.getTargetMusicXML();
  		//ヘッダ作成&出力
  		NoteFormatElement header = new NoteFormatElement();
  		header = header.setHeaderInformation(mus);
  		output(header.getHeader() + System.getProperty("line.separator"));
  		
  		//パート取得
  		MusicXMLWrapper.Part[] partlist = mus.getPartList();
  		for (MusicXMLWrapper.Part part : partlist) {
  			String partID = part.getAttribute("id"); //partID パートの識別の他に、Note形式のIdentで用いる
  			MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
  			double currenttempo = measurelist[0].tempo(); //MusicXML上の初期テンポを取得
  			for (MusicXMLWrapper.Measure measure : measurelist) {
  				//小節内のMusicDataList(NoteやらAttributeやら)取得
  				MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
  				double starttime_buf = 0;
  				for (MusicXMLWrapper.MusicData md : mdlist) {
  					if(md instanceof MusicXMLWrapper.Note){
  						MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md; //Noteにダウンキャスト
  						
  						NoteFormatElement nf = new NoteFormatElement();
  						nf.starttime = (measure.number()-1)*header.beattype + note.beat();
  						nf.ident = partID;
  						nf.len = 4/header.beattype * note.actualDuration() ;
  						
  						
  						//Deviation partwise
  						//※PEDB 1.0に収録される楽曲にはペダルが使用されているものがないため，これらの要素は登場しない．
  						
  						if(note.rest()){ //休符の時
  							
  						}
  						else{
  							nf.pitch = (note.pitchStep() + note.pitchOctave());
  						
  							
  							//Deviation notewise
  							DeviationInstanceWrapper.NoteDeviation notedev = dev.getNoteDeviation((MusicXMLWrapper.Note)md);
  							if(notedev != null){
  								nf.vel = (int)(dev.getBaseVelocity() * notedev.dynamics());
  								nf.off_vel = (int)(dev.getBaseVelocity() * notedev.endDynamics());//CMXで未実装
  								nf.on_devi = header.beattype/4 * notedev.attack();
  								nf.off_devi = header.beattype/4 * notedev.release();
  							}
  							DeviationInstanceWrapper.ChordDeviation chorddev = dev.getChordDeviation((MusicXMLWrapper.Note)md);
  							if(chorddev != null){
  							//※PEDB 1.0のDeviationInstanceXMLドキュメントでは使用されていない
  							//  from http://www.crestmuse.jp/pedb/DeviationInstanceXML.txt
  							}
  							DeviationInstanceWrapper.MissNote missnote = dev.getMissNote((MusicXMLWrapper.Note)md);
  							boolean missed = false;
  							if(missnote != null){
  								if(missnote.hasAttribute("xlink:href")) missed = true;
  							}
  							
  							//Extra-Notes
  							for(ExtraNote enote : dev.getExtraNotesList(dev, partID)){
  								if(nf.starttime > (enote.measure()-1)*header.beattype + enote.beat()){
  									if(starttime_buf <= (enote.measure()-1)*header.beattype + enote.beat()){
  										NoteFormatElement enf = new NoteFormatElement();
  										enf.starttime = (enote.measure()-1)*header.beattype + enote.beat();
  										enf.ident = partID;
  										enf.len = 4/header.beattype * enote.duration();
  										enf.pitch = (enote.pitchStep() + enote.pitchOctave());
  										enf.vel = (int)(dev.getBaseVelocity() * enote.dynamics());
  										enf.off_vel = (int)(dev.getBaseVelocity() * enote.endDynamics()); //CMX未実装
  										enf.on_devi = 0.0; //仕様に無い
  										enf.off_devi = 0.0; //仕様に無い
  										output(enf.getFormedElement()+System.getProperty("line.separator"));
  									}
  								}
  								else break;
  							}
  							
  							//Deviation non-partwise
  							double tempo_dev = 1.0;
  							for (Control cont : dev.getNonPartwiseList(dev)) {									
  								if(nf.starttime >= (cont.measure()-1)*header.beattype + cont.beat()){
  									if(cont.type().equals("tempo")) currenttempo = cont.value();
  									else if(cont.type().equals("tempo-deviation"))tempo_dev = cont.value();
  									else break;
  								}
  								if(starttime_buf != nf.starttime){
  									output(nf.starttime+" BPM "+currenttempo*tempo_dev+" "+header.beattype + System.getProperty("line.separator"));
  									starttime_buf = nf.starttime;
  								}
  							}
  							
  							//Note情報を出力
  							if(!missed)
  								output(nf.getFormedElement() + System.getProperty("line.separator"));
  							
  							
  						}
  					}//END instance of Note
  					
  					
  				}//END foreach on MusicDataList
  			}//END foreach on MeasureList
  		}//END foreach on PartList
  		
  		output(NoteFormatElement.getEnd());
  	}
  	catch(Exception e){
  		e.printStackTrace();
  	}
  	finally{
  		if(!debug_mode){
  			try{
  				fw.close();
  			}
  			catch(Exception e){
  				e.printStackTrace();
  			}
  		}
  	}
  	
  	
  }
	
	/**
	 * debug_modeの時は標準出力に、
	 * そうでない時はファイルに書き出します。
	 * @param str 出力する文字列
	 */
	public void output(String str){
		if(debug_mode) System.out.print(str);
		else{
			try{fw.write(str);}
			catch(Exception e){e.printStackTrace();}
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


/**
 * Note形式の各要素(Header,音符,休符等)を持つクラスです。
 * Headerも個々の音符いっしょくたにしてるので良くないかも
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
	double starttime;
	double on_devi;
	String ident;
	String pitch;
	double off_devi;
	int vel;
	double off_vel; //CMXで未実装
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
		nf.generator = "Dev2NoteFormatTest";
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
	
	/**
	 * ファイル終端を示すENDを返します
	 * @return
	 */
	static public String getEnd(){
		return "END";
	}
	
	/**
	 * 与えた小説番号からNote形式での小節表現を返します
	 */
	static public String getMeasureLine(String measureNo){
		return "="+measureNo;
	}
}
