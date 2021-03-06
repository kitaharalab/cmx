package jp.crestmuse.cmx.commands;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;

/*********************************************************************
 *<p>This class reads a DeviationInstanceXML file and the corresponding 
 *MusicXML file and generates a MIDI XML document and/or a standard 
 *MIDI file.</p>
 *
 *<p>このクラスはDeviationInstanceXMLファイルと対応するMusicXMLファイルを
 *読み込んで, MIDI XMLドキュメントやスタンダードMIDIファイル(SMF)を生成します.
 *CMXCommandを使ったコマンドを作る際のサンプルとしても参考になると思います.
 ********************************************************************/

public class ApplyDeviationInstance extends CMXCommand<DeviationInstanceWrapper,SCCXMLWrapper> {

    private int ticksPerBeat = 480;
    private boolean scc = false;
    private String smffilename = null;
    private String targetMusicXMLFileName = null;

    static {
	addOptionHelpMessage("-smf <filename>", "standard MIDI file to be output");
	addOptionHelpMessage("-target <filename>", "target MusicXML file");
    }

  public ApplyDeviationInstance() {
    super();
    //    appendHelpMessage("-smf <filename>: specify the name of the standard MIDI file");
    //    appendHelpMessage("-target <filename>: specify the target MusicXML file");
  }

  /******************************************************************
   *以下のオプションを受け付けます.<br>
   * -smf : MIDI XMLだけでなくSMFも出力します(出力ファイル名を指定).<br> 
   * -division : <br>
   * -target : 
   ******************************************************************/
    protected boolean setOptionsLocal(String option, String value) {
	if (option.equals("-smf")) {
	    smffilename = value;
	    return true;
	} else if (option.equals("-division")) {
	    ticksPerBeat = Integer.parseInt(value);
	    return false;
	} else if (option.equals("-target")) {
	    targetMusicXMLFileName = value;
	    return true;
	} else {
	    return false;
	}
    }

  /******************************************************************
   *<p></p>
   ******************************************************************/
    protected boolean setBoolOptionsLocal(String option) {
	if (option.equals("-scc")) {
	    scc = true;
	    return true;
	} else {
	    return false;
	}
    }

  public void init(CMXFileWrapper f) {
    if (f instanceof DeviationInstanceWrapper) {
      if (targetMusicXMLFileName != null)
        ((DeviationInstanceWrapper)f).setTargetMusicXMLFileName(targetMusicXMLFileName);
    }
  }

  /******************************************************************
   *<p></p>
   ******************************************************************/
  protected SCCXMLWrapper run(DeviationInstanceWrapper dev)
    throws IOException, ParserConfigurationException, 
    TransformerException, SAXException, InvalidFileTypeException {
//	dev.setTargetMusicXMLFileName(targetMusicXMLFileName);
    SCCXMLWrapper scc = dev.toSCCXML(ticksPerBeat);
    if (smffilename != null && !smffilename.equals("")) {
      scc.toMIDIXML().writefileAsSMF(smffilename);
    }
    return scc;
  }

/*
    protected void run()
	throws IOException, ParserConfigurationException, 
	       TransformerException, SAXException, InvalidFileTypeException {
	//	readInputData();
	DeviationInstanceWrapper dev = (DeviationInstanceWrapper)indata();
//	dev.setTargetMusicXMLFileName(targetMusicXMLFileName);
	if (scc) {
	    newOutputData("scc");
	    dev.toSCCXML((SCCXMLWrapper)outdata(), ticksPerBeat);
	} else {
	    SCCXMLWrapper sccxml = 
		(SCCXMLWrapper)CMXFileWrapper.createDocument("scc");
	    dev.toSCCXML(sccxml, ticksPerBeat);
	    newOutputData("MIDIFile");
	    sccxml.toMIDIXML((MIDIXMLWrapper)outdata());
	    if (smffilename != null)
		((MIDIXMLWrapper)outdata()).writefileAsSMF(smffilename);
	}
	//	writeOutputData();
    }
*/
    public static void main(String[] args) {
	ApplyDeviationInstance a = new ApplyDeviationInstance();
	try {
	    a.start(args);
	} catch (Exception e) {
	    a.showErrorMessage(e);
	    System.exit(1);
	}
    }
}