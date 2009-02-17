package jp.crestmuse.cmx.amusaj.commands;

//import java.io.*;

/**
 * @deprecated
 * 今後の自動伴奏システムの実装にはChordUtilsを利用してください
 */
public class ChordConverter{
	
	//ChordTransfer field
	 final String default_chord = "C";
	 final String key_set_Regexp = "C|B#|C#|Db|D|D#|Eb|E|Fb|F|E#|F#|Gb|G|G#|Ab|A|A#|Bb|B|Cb";
	 final String[] key_list = {"C", "B#", "C#", "Db", "D", "D#", "Eb", "E", "Fb", "F", "E#", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B", "Cb"};
	//key_listの文字列のNoteNumberに対応しています
	 final int[] key_distance = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 8, 8, 9, 10, 10, 11, 11};

	//ChordOmitter field
	//削除･置換するコードを正規表現で表し、String.repraceAll(regexp, str)で置換します。
	//処理は、削除=>省略=>整形の順にデータの変化が無くなるまで行います
	//正規表現の一致は、先方優先なので、
	//ユニークな置換を前方に置くと意図した結果になりやすいです。
	 final String[] delete_chords = {"M7", "sus[0-9]", "\\(on.+\\)|on.+", "add[0-9]", "M$", "\\(\\)|\\(#\\)|\\(\\+\\)|\\(-\\)|\\(b\\)", "6|7|9|11|13"};
	//change_chord[0][n] => change_chord[1][n] に変更することを示す
	 final String[][] change_chords = {
			{"t", "bb", "m\\(\\-5\\)", "m\\-5", "mb5", "m\\(b5\\)", "([^m])\\(\\+5\\)", "([^m])\\+5", "([^m])\\(#5\\)", "([^m])#5"},
			{"", "b", "dim", "dim", "dim", "dim", "$1aug", "$1aug", "$1aug", "$1aug"  }
	};
	//コードの表記の揺れを補正します。b5、-5等を(b5)に統一します
	 final String[][] format_chords = {
			{"\\(+#([1-9]+)\\)+", "\\(+([1-9]+)\\)+","\\(*\\+([1-9]+)\\)*", "\\(*b([1-9]+)\\)*", "\\(*-([1-9]+)\\)*", "\\(B([1-9]+)\\)"},
			{"\\(#$1\\)", "\\(#$1\\)", "\\(#$1\\)", "\\(b$1\\)", "\\(b$1\\)", "\\(b$1\\)"}
			//{"\\(hoge\\)", "\\(baz\\)", "\\(foo\\)", "\\(bar\\)", "\\(piyo\\)"}
	};
	 
	/**ARFF上で無視するコード *互換性のために残しています。ARFFFunctionsのignore_***を参照してください。
	 */
	 final String ignore_chords = ".+\\(b5\\)|.+\\(#5\\)|.+maug|.+mdim";
	 
	 //ダイアトニックコード
	 //090204 Bm(b5) は Bdimに間違っていた？
	 final String[] diatonic_inC = {"C", "Dm", "Em", "F", "G", "Am", "Bm(b5)"};
	
	
	public  int noteTransfer(String from_key, int note){
		return noteTransfer(from_key, note, default_chord);
	}
	
	/**
	 * from_key 調の note を to_key での note に変換します
	 * to_keyは指定しなければデフォルトで"C"です
	 * (noteは12で割った余りを用いるものとします)
	 * 
	 * @param from_key
	 * @param note
	 * @param to_key
	 * @return
	 */
	public  int noteTransfer(String from_key, int note, String to_key){
		if(note < 0 || 11 < note){
			System.out.println("You attempt to transfer less than 0 or more than 11 number note.");
			note = note%12;
			note += 12;
		}
		
		int dist = (key2dist(to_key) - key2dist(from_key));
		while (dist < 0){
			dist += 12;
		}
		if(note+dist == 0) return note;
		return (note+dist)%12;
	}
	
	public  String chordTransfer(String from_key, String chord){
		return chordTransfer(from_key, chord, default_chord);
	}
	/**
	 * ある調のコードを別の調のコードに移調します
	 * to_keyを指定しなければデフォルトで"C"です
	 * 
	 * @param from_key 元のキー
	 * @param chord 対象のコード
	 * @param to_key 移調先のキー
	 * @return
	 */

	public  String chordTransfer(String from_key, String chord, String to_key){

		
		if(chord.equals("/")) return "/";
		
		int dist = (key2dist(to_key) - key2dist(from_key));
		if(dist < 0){
			dist += 12;
		}
		

		int chord_strlen;
		if(chord.length() == 1 || (chord.charAt(1) != '#' && chord.charAt(1) != 'b')){
			chord_strlen = 1;
		}
		else if(chord.charAt(1) != '#' || chord.charAt(1) != 'b'){
			chord_strlen = 2;
		}
		else{
			System.out.println("ERROR: chord is invalid");
			chord_strlen = 0;
		}
		
		String chord_prefix = chord.substring(0, chord_strlen);
		int chord_newposition = key2dist(chord_prefix) + dist;
		if(chord_newposition > 11){
			chord_newposition -= 12;
		}
		
		String chord_newprefix = dist2key(chord_newposition);	
		return chord.replaceFirst(chord_prefix, chord_newprefix);

	}
	
	
	public  int key2dist(String key){
		
		for(int i=0; i<key_list.length; i++){
			if(key.equals(key_list[i])){
				int dist = key_distance[i];
				return dist;
			}
		}
		
		if(key.equals("/")){
			//System.out.println("/ key can't convert");
			return -1;
		}
		
		
		System.out.println("Invalid Chord: "+key);
		//System.exit(0);
		return -1;
	}
	
	
	public  String dist2key(int dist){
		
		for(int i=0; i<key_distance.length; i++){
			if(dist == key_distance[i]){
				return key_list[i];
			}
		}
		return "ERROR";
	}
	
	/**
	 * chordを省略化･整形します
	 * 
	 * @param chord 対象のコード
	 */
	public  String chordOmitter(String chord){

		String omittedchord = chord;
		String chordbackup = null;
		
		//コード削除
		chordbackup = null;
		while(!omittedchord.equals(chordbackup)){
			chordbackup = omittedchord;
			for(int i=0; i<delete_chords.length; i++){
				omittedchord = omittedchord.replaceAll(delete_chords[i], "");
			}
		}
		
		//コード変更
		chordbackup = null;
		while(!omittedchord.equals(chordbackup)){
			chordbackup = omittedchord;
			for(int i=0; i<change_chords[0].length; i++){
				omittedchord = omittedchord.replaceAll(change_chords[0][i], change_chords[1][i]);
			}
		}
		
		
		//コードフォーマット
		chordbackup = null;
		while(!omittedchord.equals(chordbackup)){
			chordbackup = omittedchord;
			for(int i=0; i<format_chords[0].length; i++){
				omittedchord = omittedchord.replaceAll(format_chords[0][i], format_chords[1][i]);
			}
		}
		
		return chordTransfer("C", omittedchord);
		
		//return omittedchord;
	}
	
	
	/**
	 * コードの先頭(主音)を取得します
	 * 2文字目(charAt(1))が#またはbであれば先頭から2文字、
	 * そうでなければ1文字目を返します
	 * 
	 * @param chord コード
	 * @return
	 */
	public String getPrefix(String chord){
		if(chord.length() == 1 || (chord.charAt(1) != '#' && chord.charAt(1) != 'b')){
			return chord.substring(0, 1);
		}
		return chord.substring(0, 2);
	}
	/**
	 * 2つのキーが鍵盤上で近いかどうかを判定します
	 * 鍵盤上の距離がdist以下であればtrueを返します
	 * 
	 * @param key1 キー1
	 * @param key2 キー2
	 * @param dist 鍵盤上の距離
	 * @return
	 */
	public boolean keyNearIs(String key1, String key2, int dist){
		if(Math.abs(key2dist(key1) - key2dist(key2)) > dist){
			return false;
		}
		else{
			return true;
		}
	}
	
	/**
	 * 引数のコードがC調でのダイアトニックコードに一致するかどうか判定します
	 * @param chord
	 * @return
	 */
	public boolean isDiatonic(String chord){
		for(int i=0; i<diatonic_inC.length; i++){
			if(chord.equals(diatonic_inC[i])) return true;
		}
		return false;
	}
	
	public static void main(String[] args){
	
		ChordConverter cc = new ChordConverter ();
		//File chord_list = new File("ChordList.txt");
		
		//System.out.println("Key:"+fromchord+" \t=> TransPosition:" + cc.default_chord);
			
		try{
			System.out.println(cc.chordOmitter("B(b5)"));
			/*
			FileReader fr = new FileReader(chord_list);
			BufferedReader br = new BufferedReader(fr);
			
			System.out.println(cc.chordTransfer("C", "Ebm"));
			String str;
			for(; (str=br.readLine()) != null; ){
				//System.out.println(str+" \t=> "+cc.chordTransfer(fromchord, str));
				//System.out.println(str+" \t=> "+cc.chordOmitter(str));
				//System.out.println("\""+cc.chordOmitter(str)+"\"");
				
				for(int i=0; i<cc.key_list.length; i++){
					System.out.println("\""+cc.chordOmitter(cc.chordTransfer(cc.key_list[i], str))+"\"");
				}
				
			}
			*/
			//System.out.println("Cmdim".matches(cc.ignore_chords));
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		//System.out.println(cc.noteTransfer("C", ));
	}


	
}
