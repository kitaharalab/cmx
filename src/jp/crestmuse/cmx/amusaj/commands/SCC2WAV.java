package jp.crestmuse.cmx.amusaj.commands;

import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.SP.*;
import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.handlers.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;

/*********************************************************************
 SCC2WAV generates the waveform of the musical piece described in 
 the given SCCXML document based on a musical instrument sound database.
 ********************************************************************/
public class SCC2WAV extends CMXCommand<SCCXMLWrapper,WAVWrapper> {

  private int cache_capacity;
  private int fs;
  private int length = -1;
  private double thresh = 0.0;
  private int n = -1;
  private String mdb = "rwc";
  private String path;
  private String pitchlistfilename;
  private String instlisttype = "default";
  private String pattern;
  private String sty = null;
  private String sty2 = null;
  private int var = 1;
  private boolean isStereo = false;
  private double mic2mic = 0.2;
  private double mach = 340.0;
  private double lSD = 0.0;
  private String impfilename = null;
  private String impLfilename = null;
  private String impRfilename = null;
  private DoubleArray imp = null;
  private DoubleArray impL = null;
  private DoubleArray impR = null;
  private int releasetime = 0;
  private WavCache wavcache;
  private PitchList pitchlist;
  private MutableWaveform wav;
  private Random random;



  /*******************************************************************
   *As local options, "-sty" and "-var" are defined.
   *******************************************************************/
  protected boolean setOptionsLocal(String option, String value) {
    if (option.equals("-mdb")) {
      mdb = value;
      return true;
    } else if (option.equals("-sty")) {
      sty = value;
      return true;
    } else if (option.equals("-sty2")) {
      sty2 = value;
      return true;
    } else if (option.equals("-var")) {
      var = Integer.parseInt(value);
      return true;
//      var = value;
    } else if (option.equals("-len")) {
      length = Integer.parseInt(value);
      return true;
    } else if (option.equals("-th")) {
      thresh = Double.parseDouble(value);
      return true;
//      thresh = -1.0 * Double.parseDouble(value);
    } else if (option.equals("-mic2mic") || option.equals("-m2m")) {
      mic2mic = Double.parseDouble(value);
      return true;
    } else if (option.equals("-mach")) {
      mach = Double.parseDouble(value);
      return true;
    } else if (option.equals("-lsd")) {
	lSD = Double.parseDouble(value);
	return true;
    } else if (option.equals("-imp")) {
      impfilename = value;
      return true;
    } else if (option.equals("-impL")) {
      impLfilename = value;
      return true;
    } else if (option.equals("-impR")) {
      impRfilename = value;
      return true;
    } else {
	return false;
    }
  }

  protected boolean setBoolOptionsLocal(String option) {
    if (option.equals("-st")) {
      isStereo = true;
      return true;
    } else {
      return false;
    }
  }
  

  /*******************************************************************
   *Reads some parameters and constructs some objects as pre-processing.
   *******************************************************************/
  protected void preproc() throws IOException {
      AmusaParameterSet param = AmusaParameterSet.getInstance();
    cache_capacity = param.getParamInt("scc2wav", "CACHE_CAPACITY");
    fs = param.getParamInt("mdb", mdb, "SAMPLE_RATE");
    if (length < 0) 
      length = param.getParamInt("param", "scc2wav", "LENGTH");
    if (thresh == 0.0)
      thresh = param.getParamDouble("mdb", mdb, "ONSET_POWER_THRESH");
    if (n < 0) n = param.getParamInt("mdb", mdb, "ONSET_N");
    path = param.getParam("mdb", mdb, "PATH");
    pitchlistfilename = param.getParam("mdb", mdb, "PITCH_LIST_FILE_NAME");
    pattern = param.getParam("mdb", mdb, "PATTERN");
    if (sty == null)
      sty = param.getParam("mdb", mdb, "DEFAULT_STYLE");
    instlisttype = param.getParam("mdb", mdb, "PATCH_LIST");
    wavcache = new WavCache();
    pitchlist = new PitchList();
    pitchlist.read(pitchlistfilename);
    random = new Random();
    if (impfilename != null) {
      imp = getImpulseResponse(impfilename);
      releasetime = Math.max(releasetime, imp.length());
    }
    if (impLfilename != null) {
      impL = getImpulseResponse(impLfilename);
      releasetime = Math.max(releasetime, impL.length());
    }
    if (impRfilename != null) {
      impR = getImpulseResponse(impRfilename);
      releasetime = Math.max(releasetime, impR.length());
    }
  }

  private DoubleArray getImpulseResponse(String filename) 
					throws IOException {
      WAVWrapper w = WAVWrapper.readfile(filename);
      MutableWaveform w2 = new MutableWaveform(w);
      DoubleArray ary = w2.getDoubleArrayWaveform()[0];
      ary = changeRate(ary, w2.sampleRate(), fs);
      return cutLastSmallSignal(ary, 0.01);
  }

  /*******************************************************************
   *Main processing. This method reads an SCC file, generates 
   *a waveform based on the SCC file, and writes the waveform in 
   *"wav" format. 
   *******************************************************************/
  protected WAVWrapper run(SCCXMLWrapper scc) throws ParserConfigurationException, SAXException, TransformerException, IOException {
	final MutableWaveform wav = new MutableWaveform(length, fs, 
						  isStereo ? 2 : 1);
	scc.processNotes(new SCCHandlerAdapter() {
		public void processNote(SCCXMLWrapper.Note note, 
					SCCXMLWrapper scc2) {
		    int onset = note.onsetInMilliSec() * fs / 1000;
		    int offset = note.offsetInMilliSec() * fs / 1000;
		    AudioDataCompatible w = readWaveform(note.part().prognum(), 
							 note.notenum(), 
							 sty, var);
		    if (w == null && sty2 != null)
			w = readWaveform(note.part().prognum(), note.notenum(),
					 sty2, var);
		    if (w != null) {
			MutableWaveform w2 = new MutableWaveform(w);
			w2 = (MutableWaveform)w2.clone();
			w2.cutAfter(offset - onset);
			w2.smoothOffset();
			if (isStereo) {
			    int itd_2 = 
				(int)(fs * 0.5 * mic2mic * 
				      Math.sin((Math.PI * note.part().panpot()
						+ random.nextGaussian() * lSD)
					       / 180.0) / mach);
			    wav.mix_st(w, onset+itd_2, onset-itd_2, 
				       note.part().volume() * note.velocity() / 10000, 
				       note.part().volume() * note.velocity() / 10000);
			}
		    }
		}
	    });
	wav.trim(releasetime + fs);
	if (isStereo && impL != null && impR != null)
	    wav.conv_st(impL, impR);
	else if (imp != null)
	    wav.conv(imp);
	wav.normalize();
	return new WAVWrapper(wav);
    }
    
  private MutableWaveform readWaveform
		(int prognum, int notenum, String sty, int var) {
    MutableWaveform w;
    String filename = pitchlist.getWaveFileName(prognum, notenum, sty, var);
    if (filename != null) {
      try {
        w = wavcache.readWaveform(filename, path);
      } catch (IOException e) {
        w = null;
        e.printStackTrace();
      }
    } else {
      w = null;
    } 
    return w;
  }

  /*******************************************************************
   *Main method.
   *******************************************************************/
  public static void main(String[] args) {
    SCC2WAV scc2wav = new SCC2WAV();
    try {
	scc2wav.start(args);
    } catch (Exception e) {
      scc2wav.showErrorMessage(e);
      System.exit(1);
    }
  }

  private class WavCache {
      private Hashtable<String,MutableWaveform> cache;
      private Hashtable<String,Long> timestamp;

    WavCache() {
	cache = new Hashtable<String,MutableWaveform>();
	timestamp = new Hashtable<String,Long>();
    }

    private MutableWaveform readWaveform(String filename, String dir) throws IOException {
      MutableWaveform w;
      if (cache.containsKey(filename)) {
        w = cache.get(filename);
        timestamp.put(filename, System.currentTimeMillis());
      } else {
        w = new MutableWaveform
	    (WAVWrapper.readfile(dir + File.separator + filename));
        w.cutBeforeOnset(n, thresh);
        w.normalize();
	add(filename, w);
      }
      return w;
    }

    private void add(String filename, MutableWaveform w) {
      if (isfull()) removeone();
      cache.put(filename, w);
      timestamp.put(filename, System.currentTimeMillis());
    }

    private boolean isfull() {
      return cache.size() >= cache_capacity;
    }

    private void removeone() {
      String oldestKey = getOldestKey();
      cache.remove(oldestKey);
      timestamp.remove(oldestKey);
    }

    private String getOldestKey() {
      long oldestTime = System.currentTimeMillis();
      String oldestKey = "";
      Enumeration<String> keys = cache.keys();
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        Long t = timestamp.get(key);
        if (oldestTime > t.longValue()) {
          oldestTime = t.longValue();
          oldestKey = key;
        }
      }
      return oldestKey;
    }
  }

  private class PitchList {
    MultiHashMap h;

    private PitchList() {
      h = new MultiHashMap();
    }

    private void read(String pitchlistfilename) 
				throws InvalidPitchListException {
      String S;
      try {
        BufferedReader reader = 
		new BufferedReader(new FileReader(pitchlistfilename));
        while ((S = reader.readLine()) != null) {
          if (S.length() > 0) {
            int i = S.indexOf(":");
            String filename = S.substring(0, i).trim();
            String notenum = 
              String.valueOf(Integer.parseInt(S.substring(i+1).trim()));
            h.add(notenum, filename);
          }
        }
        reader.close();
      } catch (IOException e) {
        throw new InvalidPitchListException();
      }
    }

      private String getInstName(int prognum) {
	  return AmusaParameterSet.getInstance().
	      getParam("prognum", instlisttype, "PN_" + prognum);
      }

    private String getWaveFileName(int prognum, int notenum, String sty, 
							int var) {
	String instname = getInstName(prognum);
      String pattern1 = pattern.replaceAll("%i", instname);
      pattern1 = pattern1.replaceAll("%s", sty);
      pattern1 = pattern1.replaceAll("%1v", addZeroToInt(var, 1));
      pattern1 = pattern1.replaceAll("%2v", addZeroToInt(var, 2));
      Pattern p = Pattern.compile(pattern1);
      String strNoteNum = String.valueOf(notenum);
      for (int i = 0; i < h.size(strNoteNum); i++) {
        String filename = (String)h.get(strNoteNum, i);
        Matcher m = p.matcher(filename);
        if (m.matches())
          return filename;
      }
      return null;
    }
  }

  class InvalidPitchListException extends IOException {
    InvalidPitchListException() {
      super();
    }
    InvalidPitchListException(String s) {
      super(s);
    }
  }

  private static String addZeroToInt(int n, int newsize) {
    String S = String.valueOf(n);
    while (S.length() < newsize) {
      S = '0' + S;
    }
    return S;
  }

}