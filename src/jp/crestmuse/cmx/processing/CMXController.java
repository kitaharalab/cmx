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

/**********************************************************************
このクラスは，CrestMuse Toolkit (CMX)の主要な機能を簡単に呼び出せるようにしたクラスです．
ただし，現時点ではCMXのすべての機能を呼び出せるようになっているわけではありません．

このクラスを利用する際には，getInstanceメソッドでインスタンスを取得してから，各種メソッドを
利用します．
 **********************************************************************/

public class CMXController implements TickTimer {

  private static final CMXController me = new CMXController();

  private SPExecutor spexec = null;
  private MusicPlayer musicPlayer = null;
  private MusicPlaySynchronizer musicSync = null;
  private AudioInputStreamWrapper mic = null;
  private AudioDataCompatible wav = null;

  private MidiDevice.Info midiin = null;
  private MidiDevice.Info midiout = null;


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

  public static void writefile(CMXFileWrapper f, String filename) {
    try {
      f.writefile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file: " + filename);
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error: " + filename);
    }
  }

  public static void write(CMXFileWrapper f, OutputStream output) {
    try {
      f.write(output);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }

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

  public static void writefileAsSMF(MIDIXMLWrapper f, String filename) {
    try {
      f.writefile(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file: " + filename);
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error: " + filename);
    }
  }

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

  public static void writeAsSMF(MIDIXMLWrapper f, OutputStream output) {
    try {
      f.write(output);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot write file");
    } catch (SAXException e) {
      throw new IllegalArgumentException("XML error");
    }
  }

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

  /** 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void wavread(String filename) {
    try {
      musicPlayer = new WAVPlayer(wav = WAVWrapper.readfile(filename));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  /** 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void wavread(InputStream input) {
    try {
      musicPlayer = new WAVPlayer(wav = WAVWrapper.read(input));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.sampled.LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  /** 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void mp3read(String filename) {
    try {
      musicPlayer = new WAVPlayer(wav = MP3Wrapper.readfile(filename));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
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
  public void mp3read(InputStream input) {
    try {
      musicPlayer = new WAVPlayer(wav = MP3Wrapper.read(input));
      musicSync = new MusicPlaySynchronizer(musicPlayer);
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

  /** 指定された標準MIDIファイルを読み込みます．読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(String filename) {
    try {
      musicPlayer =new SMFPlayer();
      ((SMFPlayer)musicPlayer).readSMF(filename);
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file: " + filename);
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data: " + filename);
    }
  }

  /** 指定された標準MIDIファイルを読み込みます．読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(InputStream input) {
    try {
      musicPlayer =new SMFPlayer();
      ((SMFPlayer)musicPlayer).readSMF(input);
      musicSync = new MusicPlaySynchronizer(musicPlayer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read file");
    } catch (javax.sound.midi.MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    } catch (javax.sound.midi.InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid MIDI data");
    }
  }

  public void smfread(MIDIXMLWrapper midi) {
    try {
      smfread(midi.getMIDIInputStream());
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid MIDIXML data");
    }
  }

  public void smfread(SCCXMLWrapper scc) {
    try {
      smfread(scc.getMIDIInputStream());
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
    musicSync.play();
  }

  /** 再生中の音楽を停止します．*/
  public void stopMusic() {
    musicSync.stop();
  }

  /** 現在，音楽を再生中かどうかを返します．*/
  public boolean isNowPlaying() {
    return musicPlayer != null && musicPlayer.isNowPlaying();
  }

  /** 次回再生時の音楽の再生開始箇所をマイクロ秒単位で指定します．
      ただし，このメソッドは音楽停止中しか使用できません．*/
  public void setMicrosecondPosition(long t) {
    musicPlayer.setMicrosecondPosition(0);
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をマイクロ秒単位で
      返します．*/
  public long getMicrosecondPosition() {
    if (musicPlayer == null)
      return 0;
    else
      return musicPlayer.getMicrosecondPosition();
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をティック単位で
      返します．
      ただし，このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public long getTickPosition() {
    if (musicPlayer == null)
      return 0;
    else
      return musicPlayer.getTickPosition();
  }

  /** 現在読み込まれているMIDIデータのTicks Per Beat（1拍あたりの
      ティック数）を返します．
      このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public int getTicksPerBeat() {
    if (musicPlayer == null)
      return 0;
    else
      return musicPlayer.getTicksPerBeat();
  }

  public void addMusicListener(MusicListener l) {
    musicSync.addMusicListener(l);
  }

  /** 音楽の再生が停止されるまで，スレッドを停止します．*/
  public void waitForMusicStopped() {
    try {
      while (isNowPlaying()) {
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
        return new MidiInputModule(
          SoundUtils.getMidiInDeviceByName(midiin.getName()));
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
        return new MidiOutputModule();
      } else {
        MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiout.getName());
        dev.open();
        return new MidiOutputModule(dev);
      }
    } catch (MidiUnavailableException e) {
      throw new DeviceNotAvailableException("MIDI device not available");
    }
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
      AudioInputStreamWrapper mic = 
        AudioInputStreamWrapper.createWrapper8(fs);
      WindowSlider winslider = new WindowSlider(false);
      winslider.setInputData(mic);
      winslider.setTickTimer(this);
      mic.getLine().start();
      return winslider;
    } catch (LineUnavailableException e) {
      throw new DeviceNotAvailableException("Audio device not available");
    }
  }

  /** 現在サウンドカードから再生中の音を受け取って，その波形データを短区間ごとに区切った
      波形断片を次々と出力する「モジュール」を生成します．*/
  public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
    SynchronizedWindowSlider winslider = 
      new SynchronizedWindowSlider(isStereo);
    winslider.setInputData(wav);
    addMusicListener(winslider);
    return winslider;
  }

  public MidiEventSender createMidiEventSender() {
    return new MidiEventSender();
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
  public MusicRepresentation 
  createMusicRepresentation(int measure, int division) {
    return MusicRepresentationFactory.create(measure, division);
  }


  public void sleep(long ms) {
    try {
      Thread.currentThread().sleep(ms);
    } catch (InterruptedException e) {}
  }
    

}
