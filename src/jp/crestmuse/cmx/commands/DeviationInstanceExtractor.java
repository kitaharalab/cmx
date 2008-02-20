package jp.crestmuse.cmx.commands;

import java.io.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public class DeviationInstanceExtractor extends CMXCommand {

  private String targetMusicXMLFileName = null;
  private boolean isSMF = false;

  private String scoreFileName = "score.mid";
  private String midiXmlFileName = "midi.xml";
  private String sccXmlFileName = "scc.xml";
  private String remadeSmfFileName = "result.mid";
  private boolean remakeSMF = false;

  private int division = 0;

  public String getDestDir() {
    return super.getDestDir();
  }

protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-target")) {
      targetMusicXMLFileName = value;
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
    } else {
      return false;
    }
  }

  protected boolean setBoolOptionsLocal(String option) {
    if (option.equals("-smf")) {
      isSMF = true;
      return true;
    } else if (option.equals("-remakeSMF")) {
      remakeSMF = true;
      return true;
    } else {
      return false;
    }
  }

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

  protected void run() throws IOException,ParserConfigurationException,
    SAXException,TransformerException {
    /* java DeviationInstanceExtractor -target (MusicXML) -smf (SMF)
     * で実行．
     * 出力フォルダに注意．
     * 
     * １．元の楽譜データをSMFに変換して出力（省略可）
     * ２．Deviationを抽出して出力
     * ３．元の楽譜データに抽出したDeviationを付加してSMFに変換して出力
     * ４．元の演奏データと比較
     * */
    String destdir = getDestDir();
    MusicXMLWrapper musicxml = 
      (MusicXMLWrapper)CMXFileWrapper.readfile(targetMusicXMLFileName);
    MIDIXMLWrapper smf = (MIDIXMLWrapper)indata();
    
 // 元の楽譜データをSMFとして出力
    SCCXMLWrapper sccxml = musicxml.makeDeadpanSCCXML(smf.ticksPerBeat());
    MIDIXMLWrapper midixml = 
      (MIDIXMLWrapper) CMXFileWrapper.createDocument(MIDIXMLWrapper.TOP_TAG);
    if (remakeSMF) {
      sccxml.toMIDIXML(midixml);
      midixml.writefileAsSMF(new File(destdir, scoreFileName));
    }
    
    DeviationInstanceWrapper diw; 
    if (division == 0)
      diw = PerformanceMatcher3.extractDeviation(musicxml, smf);
    else 
      diw = PerformanceMatcher3.extractDeviation(musicxml, smf, division);
    diw.finalizeDocument();
    setOutputData(diw);
	
    if (remakeSMF) {
      // 出来たDeviationを元の楽譜データに適用して出来たMIDIをMIDIXMLとSMFで出力
      SCCXMLWrapper sccxml2 = diw.toSCCXML(smf.ticksPerBeat());
      //sccxml2.write(System.out);
      sccxml2.writefile(new File(destdir, sccXmlFileName));
      MIDIXMLWrapper midixml2 = sccxml2.toMIDIXML();
      midixml2.writefile(new File(destdir, midiXmlFileName));
      midixml2.writefileAsSMF(new File(destdir, remadeSmfFileName));
    }
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

