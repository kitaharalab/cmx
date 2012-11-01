package jp.crestmuse.cmx.processing;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.inference.*;
import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import javazoom.jl.decoder.*;
import groovy.lang.Closure;

/**********************************************************************
このクラスは，CrestMuse Toolkit (CMX)の主要な機能を簡単に呼び出せるようにしたクラスです．
ただし，現時点ではCMXのすべての機能を呼び出せるようになっているわけではありません．

このクラスを利用する際には，getInstanceメソッドでインスタンスを取得してから，各種メソッドを
利用します．
 **********************************************************************/

public class CMXController implements TickTimer,MIDIConsts {

  private static final CMXController me = new CMXController();

  private SPExecutor spexec = null;
  private MusicPlayer[] musicPlayer = new MusicPlayer[256];
//  private MusicPlayer musicPlayer = null;
  private MusicPlaySynchronizer[] musicSync = new MusicPlaySynchronizer[256];
//  private MusicPlaySynchronizer musicSync = null;
  private AudioInputStreamWrapper mic = null;
  private AudioDataCompatible wav = null;

  private MidiDevice.Info midiin = null;
  private MidiDevice.Info midiout = null;
  private Mixer.Info mixer = null;

  private CMXController() {

  }

  /** このクラスのインスタンスを返します．*/
  public static CMXController getInstance() {
    return me;
  }

  /** CMXが対応しているXML形式の文書オブジェクトを生成します．
      たとえば，SCCXML形式の文書オブジェクトを生成する際には，
      <tt> createDocument(SCCXMLWrapper.TOP_TAG) </tt> とします．*/
  public static CMXFileWrapper createDocument(String toptag) {
    try {
      return CMXFileWrapper.createDocument(toptag);
    } catch (InvalidFileTypeException e) {
      throw new IllegalArgumentException("Invalid file type: " + toptag);
    }
  }

  /** CMXが対応しているXML形式の文書を読み込みます．*/
  public static CMXFileWrapper readfile(String filename) {
    try {
      return CMXFileWrapper.readfile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    }
  }

  /** CMXが対応しているXML形式の文書を読み込みます．*/
  public static CMXFileWrapper read(InputStream input) {
    try {
      return CMXFileWrapper.read(input);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    }
  }

  /** 標準MIDIファイルをMIDIXML形式で読み込みます． */
  public static MIDIXMLWrapper readSMFAsMIDIXML(String filename) {
    try {
      return MIDIXMLWrapper.readSMF(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (TransformerException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    }
  }

  /** 標準MIDIファイルをMIDIXML形式で読み込みます． */
  public static MIDIXMLWrapper readSMFAsMIDIXML(InputStream input) {
    try {
      return MIDIXMLWrapper.readSMF(input);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (TransformerException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    }
  }

  /** CMXFileWrapperオブジェクトを，対応するXML形式でファイルに保存します．*/
  public static void writefile(CMXFileWrapper f, String filename) {
    try {
      f.writefile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file: " + filename);
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error: " + filename);
    }
  }

  /** CMXFileWrapperオブジェクトを，対応するXML形式で出力ストリームに書き出します．*/
  public static void write(CMXFileWrapper f, OutputStream output) {
    try {
      f.write(output);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }

  /** SCCXMLドキュメントを標準MIDIファイルとしてファイルに保存します．*/
  public static void writefileAsSMF(SCCXMLWrapper f, String filename) {
    try {
      f.toMIDIXML().writefile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file: " + filename);
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error: " + filename);
//    } catch (ParserConfigurationException e) {
//      throw new IllegalStateException("Parser error: " + filename);
//    } catch (TransformerException e) {
//      throw new IllegalArgumentException("XML error: " + filename);
    }
  }

  /** MIDIXMLドキュメントを標準MIDIファイルとしてファイルに保存します．*/
  public static void writefileAsSMF(MIDIXMLWrapper f, String filename) {
    try {
      f.writefile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file: " + filename);
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error: " + filename);
    }
  }

  /** SCCXMLドキュメントを標準MIDIファイルとして出力ストリームに書き出します．*/
  public static void writeAsSMF(SCCXMLWrapper f, OutputStream output) {
    try {
      f.toMIDIXML().write(output);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
//    } catch (ParserConfigurationException e) {
//      throw new IllegalStateException("Parser error");
//    } catch (TransformerException e) {
//      throw new IllegalArgumentException("XML error");
    }
  }

  /** MIDIXMLドキュメントを標準MIDIファイルとして出力ストリームに書き出します．*/
  public static void writeAsSMF(MIDIXMLWrapper f, OutputStream output) {
    try {
      f.write(output);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }

  /** CMXFileWrapperオブジェクトをXML形式で標準出力に書き出します．*/
  public static void println(CMXFileWrapper f) {
    try {
      f.write(System.out);
    } catch (IOException e) {
      throw new IllegalArgumentException("I/O Error");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }

  /** リアルタイム処理用の「モジュール」を登録します．*/
  public void addSPModule(ProducerConsumerCompatible module) {
    if (spexec == null)
      spexec = new SPExecutor();
    spexec.addSPModule(module);
  }

    public ProducerConsumerCompatible newSPModule(Map args) 
	throws ClassNotFoundException {
	final Closure execute = (Closure)args.get("execute");
	final Class[] inputClasses = getClassArray(args.get("inputs"));
	final Class[] outputClasses = getClassArray(args.get("outputs"));
	SPModule module = new SPModule() {
		public void execute(Object[] src, TimeSeriesCompatible[] dest) {
		    execute.call(new Object[]{src, dest});
		}
		public Class[] getInputClasses() {
		    return inputClasses;
		}
		public Class[] getOutputClasses() {
		    return outputClasses;
		}
	    };
	addSPModule(module);
	return module;
    }

    private Class[] getClassArray(Object inputs) throws ClassNotFoundException {
	Class[] classes;
	if (inputs == null) {
	    classes = new Class[0];
	} else if (inputs instanceof Class) {
	    classes = new Class[]{(Class)inputs};
	} else if (inputs instanceof String) {
	    classes = new Class[]{Class.forName((String)inputs)};
	} else if (inputs instanceof Object[]) {
	    Object[] a = (Object[])inputs;
	    classes = new Class[a.length];
	    for (int i = 0; i < a.length; i++) {
		if (a[i] instanceof Class)
		    classes[i] = (Class)a[i];
		else if (a[i] instanceof String)
		    classes[i] = Class.forName((String)a[i]);
		else
		    throw new IllegalArgumentException("Illegal argument: " + a[i]);
	    }
	} else if (inputs instanceof java.util.List) {
	    java.util.List l = (java.util.List)inputs;
	    classes = new Class[l.size()];
	    for (int i = 0; i < l.size(); i++) {
		if (l.get(i) instanceof Class)
		    classes[i] = (Class)l.get(i);
		else if (l.get(i) instanceof String)
		    classes[i] = Class.forName((String)l.get(i));
		else
		    throw new IllegalArgumentException("Illegal argument: " + l.get(i));
	    }
	} else {
	    throw new IllegalArgumentException("Illegal argument: " + inputs);
	}
	return classes;
    }


  /** 登録済みの「モジュール」の接続方法を定義します．*/
  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    spexec.connect(output, ch1, input, ch2);
  }

  /** 登録済みの「モジュール」を実行開始します．*/
  public void startSP() {
    if (mic != null) mic.getLine().start();
    if (spexec != null) spexec.start();
  }

  public void wavread(AudioDataCompatible w) {
    wavread(0, w);
  }

  public void wavread(int i, AudioDataCompatible w) {
    try {
      wav = w;
      musicPlayer[i] = new WAVPlayer(wav);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void wavread(String filename) {
    wavread(0, filename);
  }

  /** 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void wavread(int i, String filename) {
    try {
      wav = WAVWrapper.readfile(filename);
      musicPlayer[i] = new WAVPlayer(wav);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void wavread(InputStream input) {
    wavread(0, input);
  }

  /** 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void wavread(int i, InputStream input) {
    try {
      wav = WAVWrapper.read(input);
      musicPlayer[i] = new WAVPlayer(wav);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void mp3read(String filename) {
    mp3read(0, filename);
  }

  /** 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void mp3read(int i, String filename) {
    try {
      wav = MP3Wrapper.readfile(filename);
      musicPlayer[i] = new WAVPlayer(wav);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read file: " + filename);
    } catch (DecoderException e) {
      throw new IllegalStateException("Cannot decode MP3 file: " + filename);
    } catch (BitstreamException e) {
      throw new IllegalStateException("Cannot decode MP3 file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  /** 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void mp3read(int i, InputStream input) {
    try {
      wav = MP3Wrapper.read(input);
      musicPlayer[i] = new WAVPlayer(wav);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read file");
    } catch (DecoderException e) {
      throw new IllegalStateException("Cannot decode MP3 file");
    } catch (BitstreamException e) {
      throw new IllegalStateException("Cannot decode MP3 file");
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void smfread(String filename) {
    smfread(0, filename);
  }

  /** 指定された標準MIDIファイルを読み込みます．読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(int i, String filename) {
    try {
//      System.err.println(midiout);
      if (midiout == null) {
        musicPlayer[i] =new SMFPlayer();
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
        musicPlayer[i] = new SMFPlayer(dev);
      }
      ((SMFPlayer)musicPlayer[i]).readSMF(filename);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data: " + filename);
    }
  }

  public void smfread(InputStream input) {
    smfread(0, input);
  }

  public void smfread(int i, Sequence seq) {
    try {
//      System.err.println(midiout);
      if (midiout == null) {
        musicPlayer[i] =new SMFPlayer();
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
        musicPlayer[i] = new SMFPlayer(dev);
      }
      ((SMFPlayer)musicPlayer[i]).readSMF(seq);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data");
    }
  }

  public void smfread(Sequence seq) {
    smfread(0, seq);
  }
  
  /** 指定された標準MIDIファイルを読み込みます．読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(int i, InputStream input) {
    try {
      if (midiout == null) {
        musicPlayer[i] =new SMFPlayer();
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
        musicPlayer[i] = new SMFPlayer(dev);
      }
      ((SMFPlayer)musicPlayer[i]).readSMF(input);
      musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data");
    }
  }

  public void smfread(MIDIXMLWrapper midi) {
    smfread(0, midi);
  }

  /** MIDIXMLドキュメントを標準MIDIファイルに変換して読み込みます．
      読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(int i, MIDIXMLWrapper midi) {
    try {
      smfread(i, midi.getMIDIInputStream());
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid MIDIXML data");
    }
  }

  public void smfread(SCCXMLWrapper scc) {
    smfread(0, scc);
  }

  /** SCCXMLドキュメントを標準MIDIファイルに変換して読み込みます．
      読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(int i, SCCXMLWrapper scc) {
    try {
      smfread(i, scc.getMIDIInputStream());
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid SCCXML data");
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("parser error");
    } catch (TransformerException e) {
      throw new IllegalArgumentException("XML error");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }


/*
  public void midiread(MIDIXMLWrapper midi) {
    try {
      PipedOutputStream pout = new PipedOutputStream();
      PipedInputStream pin = new PipedInputStream(pout);
      midi.writeAsSMF(pout);
      musicPlayer = new SMFPlayer();
      ((SMFPlayer)musicPlayer).readSMF(pin);
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data");
    }
  }
*/

/*
  public void sccread(SCCXMLWrapper scc) {
    try {
      midiread(scc.toMIDIXML());
    } catch (ParserConfigurationException e) {
      throw IllegalStateExcpetion("parser configuration exception");
    } 
  }
*/

  /** すでに読み込まれた音楽データの再生を開始します．*/
  public void playMusic() {
    playMusic(0);
  }

  public void playMusic(int i) {
    musicSync[i].play();
  }

  /** 再生中の音楽を停止します．*/
  public void stopMusic() {
    stopMusic(0);
  }

  public void stopMusic(int i) {
    musicSync[i].stop();
  }

  public boolean isNowPlaying() {
    return isNowPlaying(0);
  }

  /** 現在，音楽を再生中かどうかを返します．*/
  public boolean isNowPlaying(int i) {
    return musicPlayer[i] != null && musicPlayer[i].isNowPlaying();
  }

  public void setMusicLoop(boolean b) {
    setMusicLoop(0, b);
  }

  public void setMusicLoop(int i, boolean b) {
    if (musicPlayer[i] != null)
      musicPlayer[i].setLoopEnabled(b);
    else
      throw new IllegalStateException("setMusicLoop should be called after a music file is read.");
  }

  public void setMicrosecondPosition(long t) {
    setMicrosecondPosition(0, t);
  }

  /** 次回再生時の音楽の再生開始箇所をマイクロ秒単位で指定します．
      ただし，このメソッドは音楽停止中しか使用できません．*/
  public void setMicrosecondPosition(int i, long t) {
    musicPlayer[i].setMicrosecondPosition(0);
  }

  public long getMicrosecondPosition() {
    return getMicrosecondPosition(0);
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をマイクロ秒単位で
      返します．*/
  public long getMicrosecondPosition(int i) {
    if (musicPlayer[i] == null)
      return 0;
    else
      return musicPlayer[i].getMicrosecondPosition();
  }

  public long getTickPosition() {
    return getTickPosition(0);
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をティック単位で
      返します．
      ただし，このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public long getTickPosition(int i) {
    if (musicPlayer[i] == null)
      return 0;
    else
      return musicPlayer[i].getTickPosition();
  }

  public int getTicksPerBeat() {
    return getTicksPerBeat(0);
  }

  /** 現在読み込まれているMIDIデータのTicks Per Beat（1拍あたりの
      ティック数）を返します．
      このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public int getTicksPerBeat(int i) {
    if (musicPlayer[i] == null)
      return 0;
    else
      return musicPlayer[i].getTicksPerBeat();
  }

  public void addMusicListener(MusicListener l) {
    addMusicListener(0, l);
  }
   
  public void addMusicListener(int i, MusicListener l) {
    musicSync[i].addMusicListener(l);
  }

  public void waitForMusicStopped() {
    waitForMusicStopped(0);
  }

  /** 音楽の再生が停止されるまで，スレッドを停止します．*/
  public void waitForMusicStopped(int i) {
    try {
      while (isNowPlaying(i)) {
        Thread.currentThread().sleep(100);
      }
    } catch (InterruptedException e) {
      return;
    }
  }

  /** 仮想鍵盤を表示し，キーボードのキーを押すと，対応するMIDIイベントが
      出力される「モジュール」を生成します，*/
  public MidiInputModule createVirtualKeyboard() {
    try {
      VirtualKeyboard vkb = new VirtualKeyboard();
      MidiInputModule midiin = new MidiInputModule(vkb);
      midiin.setTickTimer(this);
      addSPModule(midiin);
      return midiin;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }
  
  /** 仮想鍵盤を表示し，キーボードのキーを押すと，対応するMIDIイベントが
      出力される「モジュール」を生成します，*/
  public MidiInputModule createVirtualKeyboard(Component c) {
    try {
      VirtualKeyboard vkb = new VirtualKeyboard(c);
      MidiInputModule midiin = new MidiInputModule(vkb);
      midiin.setTickTimer(this);
      addSPModule(midiin);
      return midiin;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  /** 認識済みのMIDI入力デバイスからMIDIイベントを受け付けてそのまま出力する
      「モジュール」を生成して返します．
      このメソッドは，<tt>showMidiInChooser</tt>メソッドによって
      MIDI入力デバイスを選択した後でしか使用できません．*/
  public MidiInputModule createMidiIn() {
    try {
      if (midiin == null) {
        throw new IllegalStateException
          ("MIDI IN device has not been selected yet");
      } else {
	  MidiInputModule midiin2 = new MidiInputModule(SoundUtils.getMidiInDeviceByName(midiin.getName()));
	  addSPModule(midiin2);
	  return midiin2;
      }
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  /** 認識済みのMIDI出力デバイスに，入力されたMIDIイベントを出力する「モジュール」を
      生成して返します．*/
  public MidiOutputModule createMidiOut() {
    try {
      if (midiout == null) {
	  MidiOutputModule midiout2 = new MidiOutputModule();
	  addSPModule(midiout2);
	  return midiout2;
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
	MidiOutputModule midiout2 = new MidiOutputModule(dev);
	addSPModule(midiout2);
	return midiout2;
      }
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  public void showAudioMixerChooser(Component parent) {
    Object selected = JOptionPane.showInputDialog(
      parent, "Select Audio Mixer.", "Select Audio Mixer...", 
      JOptionPane.PLAIN_MESSAGE, null, 
      AudioSystem.getMixerInfo(), null);
    if (selected != null)
      mixer = (Mixer.Info)selected;
  }
    
  /** 認識済みのMIDI入力デバイスの選択ダイアログを表示します．
      表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には
      <tt>null</tt>を指定することもできます．*/
  public void showMidiInChooser(Component parent) {
    try {
      Object selected = 
        JOptionPane.showInputDialog(
          parent, "Select MIDI IN Device.", "Select MIDI IN Device...", 
          JOptionPane.PLAIN_MESSAGE, null, 
          SoundUtils.getMidiInDeviceInfo().toArray(), null);
      if (selected != null)
        midiin = (MidiDevice.Info)selected;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }

  /** 認識済みのMIDI出力デバイスの選択ダイアログを表示します．
      表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には
      <tt>null</tt>を指定することもできます．*/
  public void showMidiOutChooser(Component parent) {
    try {
      Object selected = 
        JOptionPane.showInputDialog(
          parent, "Select MIDI OUT Device.", "Select MIDI OUT Device...", 
          JOptionPane.PLAIN_MESSAGE, null, 
          SoundUtils.getMidiOutDeviceInfo().toArray(), null);
      if (selected != null)
        midiout = (MidiDevice.Info)selected;
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
  }
    
  /** マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と
      出力する「モジュール」を生成します．
      サンプリング周波数は16kHzとします．*/
  public WindowSlider createMic() {
    return createMic(16000);
  }

  /** マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と
      出力する「モジュール」を生成します．
      <tt>fs</tt>にはサンプリング周波数をHz単位で指定します．*/
  public WindowSlider createMic(int fs) {
    try {
      mic = 
        AudioInputStreamWrapper.createWrapper16(fs, mixer);
      WindowSlider winslider = new WindowSlider(false);
      winslider.setInputData(mic);
      winslider.setTickTimer(this);
//      mic.getLine().start();
      addSPModule(winslider);
      return winslider;
    } catch (LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  public void closeMic() {
    if (mic != null)
      mic.getLine().close();
  }


  /** 現在サウンドカードから再生中の音を受け取って，その波形データを短区間ごとに区切った
      波形断片を次々と出力する「モジュール」を生成します．
      （新しいwavreadへの対応は要チェック）*/
  public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
    SynchronizedWindowSlider winslider = 
      new SynchronizedWindowSlider(isStereo);
    winslider.setInputData(wav);
    addMusicListener(winslider);
    addSPModule(winslider);
    return winslider;
  }

  public MidiEventSender createMidiEventSender() {
    MidiEventSender evtsender = new MidiEventSender();
    addSPModule(evtsender);
    return evtsender;
  }

  /** 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます．
      <tt>createMic</tt>などを使用する際には必須です．*/
  public void readConfig(String filename) {
    try {
      AmusaParameterSet.getInstance().setAnotherParameterSet(
        (ConfigXMLWrapper)CMXFileWrapper.readfile(filename));
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read config file: " + filename);
    }
  }

  /** 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます．
      <tt>createMic</tt>などを使用する際には必須です．*/
  public void readConfig(InputStream input) {
    try {
      AmusaParameterSet.getInstance().setAnotherParameterSet(
        (ConfigXMLWrapper)CMXFileWrapper.read(input));
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read config file");
    }
  }

  /** 音楽推論用のオブジェクトを返します．*/
  public static MusicRepresentation 
  createMusicRepresentation(int measure, int division) {
    return MusicRepresentationFactory.create(measure, division);
  }

    public static MidiEventWithTicktime 
	createShortMessageEvent(byte[] message, long tick, long position) {
	return MidiEventWithTicktime.createShortMessageEvent(message, tick, 
							     position);
    }

    public static MidiEventWithTicktime createShortMessageEvent
	(java.util.List<? extends Number> message, long tick, long position) {
	return MidiEventWithTicktime.createShortMessageEvent(message, tick, 
							     position);
    }

  public static MidiEventWithTicktime createControlChangeEvent(long position, int ch, int type, int value) {
    return MidiEventWithTicktime.createControlChangeEvent(position, ch, type, value);
  }

  public static MidiEventWithTicktime createNoteOffEvent(long position, int ch, int nn, int vel) {
    return MidiEventWithTicktime.createNoteOffEvent(position, ch, nn, vel);
  }

  public static MidiEventWithTicktime createNoteOnEvent(long position, int ch, int nn, int vel) {
    return MidiEventWithTicktime.createNoteOnEvent(position, ch, nn, vel);
  }

  public static MidiEventWithTicktime createProgramChangeEvent(long position, int ch, int value) {
    return MidiEventWithTicktime.createProgramChangeEvent(position, ch, value);
  }


  public TappingModule createTappingModule(Component c) {
    TappingModule tap = new TappingModule();
    tap.setTickTimer(this);
    c.addKeyListener(tap);
    addSPModule(tap);
    return tap;
  }

  public void sleep(long ms) {
    try {
      Thread.currentThread().sleep(ms);
    } catch (InterruptedException e) {}
  }
    

}
