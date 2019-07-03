package jp.crestmuse.cmx.commands;

import java.io.*;

import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public class DeviationInstanceExtractor extends CMXCommand<MusicXMLWrapper, DeviationInstanceWrapper> {

  //private String targetMusicXMLFileName = null;
  //private boolean isSMF = false;
  private MIDIXMLWrapper midixml;

  private String scoreFileName = "score.mid";
  private String midiXmlFileName = "midi.xml";
  private String sccXmlFileName = "scc.xml";
  private String remadeSmfFileName = "result.mid";
  private boolean remakeSMF = false;

  private int division = 0;
  private double rowRiscInc = -1;
  private double colRiscInc = -1;
  private double ioiWeight = -1;

  private String pathFileName = null;

  public String getDestDir() {
    return super.getDestDir();
  }

  protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-smf")){
      try {
        midixml = MIDIXMLWrapper.readSMF(value);
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    } else if(option.equals("-midixml")) {
      try {
        midixml = (MIDIXMLWrapper)CMXFileWrapper.readfile(value);
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    } else if (option.equals("-score")) {
      scoreFileName = value;
      return true;
    } else if (option.equals("-midi")) {
      midiXmlFileName = value;
      return true;
    } else if (option.equals("-result")) {
      remadeSmfFileName = value;
      return true;
    } else if (option.equals("-division")) {
      division = Integer.parseInt(value);
      return true;
    } else if(option.equals("-rowrisc")) {
      rowRiscInc = Double.parseDouble(value);
      return true;
    } else if(option.equals("-colrisc")) {
      colRiscInc = Double.parseDouble(value);
      return true;
    } else if(option.equals("-ioi")) {
      ioiWeight = Double.parseDouble(value);
      return true;
    } else if (option.equals("-miss")) {
      PerformanceMatcher3.MISS_EXTRA_ONSET_DIFF = Double.parseDouble(value);
      return true;
    } else if (option.equals("-pathwrite")) {
      PerformanceMatcher3.DTW_PATH_FILENAME = value;
      return true;
    } else if (option.equals("-pathread")) {
      pathFileName = value;
      return true;
    } else {
      return false;
    }
  }

  protected boolean setBoolOptionsLocal(String option) {
    if (option.equals("-remakeSMF")) {
      remakeSMF = true;
      return true;
    }
    return false;
  }
/*
  protected final FileWrapperCompatible readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException {
    if (isSMF) 
      return MIDIXMLWrapper.readSMF(filename);
    else
      return super.readInputData(filename);
  }

  protected void preproc() throws InvalidOptionException {
    if (targetMusicXMLFileName == null)
      throw new InvalidOptionException("'-target' is not specfied");
  }
*/
  protected DeviationInstanceWrapper run(MusicXMLWrapper musicxml) throws IOException,ParserConfigurationException,
    SAXException,TransformerException {
    /* java DeviationInstanceExtractor (MusicXML) -smf (SMF)
     * で実行．
     * 出力フォルダに注意．
     * 
     * １．元の楽譜データをSMFに変換して出力（省略可）
     * ２．Deviationを抽出して出力
     * ３．元の楽譜データに抽出したDeviationを付加してSMFに変換して出力
     * ４．元の演奏データと比較
     * */
    String destdir = getDestDir();
    // 元の楽譜データをSMFとして出力
    if (remakeSMF) {
      musicxml.makeDeadpanSCCXML(midixml.ticksPerBeat()).toMIDIXML().writefileAsSMF(new File(destdir, scoreFileName));
    }

    if(rowRiscInc != -1)
      PerformanceMatcher3.setRowRiscInc(rowRiscInc);
    if(colRiscInc != -1)
      PerformanceMatcher3.setColRiscInc(colRiscInc);
    if(ioiWeight != -1)
      PerformanceMatcher3.setIoiWeight(ioiWeight);
    DeviationInstanceWrapper diw; 
    if (pathFileName == null) {
      if (division == 0)
        diw = PerformanceMatcher3.extractDeviation(musicxml, midixml);
      else 
        diw = PerformanceMatcher3.extractDeviation(musicxml,midixml,division);
    } else {
      if (division == 0)
        diw = PerformanceMatcher3.extractDeviation(musicxml, midixml, 
                                                   new File(pathFileName));
      else
        diw = PerformanceMatcher3.extractDeviation(musicxml,midixml,division,
                                                   new File(pathFileName));
    }
    diw.finalizeDocument();
    //setOutputData(diw);
	
    if (remakeSMF) {
      // 出来たDeviationを元の楽譜データに適用して出来たMIDIをMIDIXMLとSMFで出力
      SCCXMLWrapper sccxml2 = diw.toSCCXML(midixml.ticksPerBeat());
      //sccxml2.write(System.out);
      sccxml2.writefile(new File(destdir, sccXmlFileName));
      MIDIXMLWrapper midixml2 = sccxml2.toMIDIXML();
      midixml2.writefile(new File(destdir, midiXmlFileName));
      midixml2.writefileAsSMF(new File(destdir, remadeSmfFileName));
    }
    
    return diw;
  }

  public static void main(String[] args) {
    DeviationInstanceExtractor devExtract = new DeviationInstanceExtractor();
    try {
      devExtract.start(args);
    } catch (Exception e) {
      devExtract.showErrorMessage(e);
      System.exit(1);
    }
  }
}

