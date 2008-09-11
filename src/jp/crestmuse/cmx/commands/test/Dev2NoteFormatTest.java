package jp.crestmuse.cmx.commands.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.misc.TreeView;

public class Dev2NoteFormatTest {

	
	public Dev2NoteFormatTest(DeviationInstanceWrapper dev){
				
		//System.out.println(dev.getTargetMusicXMLFileName()); #=> wiener-p012-013.xml 読めてはいる
		try{
			//File file = new File("output.txt");
			//BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			/*
			//DeviationXMLに対応するMusicXML取得
			MusicXMLWrapper mus = dev.getTargetMusicXML();
			
			//パート取得
			MusicXMLWrapper.Part[] partlist = mus.getPartList();
			for (MusicXMLWrapper.Part part : partlist) {
				//パート内の小節リスト取得
				MusicXMLWrapper.Measure[] measurelist = part.getMeasureList();
				for (MusicXMLWrapper.Measure measure : measurelist) {
					//小節内のMusicDataList(NoteやらAttributeやら)取得
					MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
					for (MusicXMLWrapper.MusicData md : mdlist) {
						//TODO mdがNoteなら書き出す
						if(md instanceof MusicXMLWrapper.Note){
							
							
							
							DeviationInstanceWrapper.NoteDeviation currentdev = dev.getNoteDeviation((MusicXMLWrapper.Note)md);
							// !! アタック取れたrelease, dynamics, end-dynamicsはこの要領でいける !!
							//if(currentdev != null) System.out.println(currentdev.attack());
							
							
							
						}
					}
				}
			}
			*/
			
			TreeView<DeviationInstanceWrapper.Control> npartwise = dev.getNonPartwiseControlView();
			npartwise.getRoot();
			
			while (npartwise.hasNextL()) {
				while (npartwise.hasNextR()) {
					System.out.println(npartwise.nextR().measure());
					
					//check
					System.out.println(npartwise.hasElementsAtNextTime());
					System.out.println(npartwise.hasNextL());
					System.out.println(npartwise.hasNextR());
				}
				npartwise.nextL();
			}
			//check
			System.out.println(npartwise.hasElementsAtNextTime());
			System.out.println(npartwise.hasNextL());
			System.out.println(npartwise.hasNextR());
			
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void main(String[] args) {

		//setting testfile
		
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
