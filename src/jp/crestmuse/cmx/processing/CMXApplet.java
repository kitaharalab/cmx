package jp.crestmuse.cmx.processing;

import processing.core.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.sound.*;
import jp.crestmuse.cmx.inference.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;

/** このクラスは，CMXの主要な機能をあたかもProcessingの一機能のように使えるようにした
基底クラスです．このクラスのサブクラスを作成し，<tt>start</tt>メソッドを呼び出すことで
作成したプログラムの実行が始まります．このクラスはPAppletクラスを継承していますので，
<tt>ellipseなどのProcessing用のメソッドも利用できます．*/
public class CMXApplet extends PApplet implements MusicListener,TickTimer {

  private static final CMXController ctrl = CMXController.getInstance();

  /** CMXが対応しているXML形式の文書オブジェクトを生成します．
      たとえば，SCCXML形式の文書オブジェクトを生成する際には，
      <tt> createDocument(SCCXMLWrapper.TOP_TAG) </tt> とします．*/
  public static CMXFileWrapper createDocument(String toptag) {
    return ctrl.createDocument(toptag);
  }

  /** CMXが対応しているXML形式の文書を読み込みます．*/
  public CMXFileWrapper readfile(String filename) {
//    return ctrl.read(createInput(filename));
    return ctrl.readfile(filename);
  }

  /** CMXが対応しているXML形式の文書を読み込みます．*/
  public CMXFileWrapper read(InputStream input) {
    return ctrl.read(input);
  }

  public void writefile(CMXFileWrapper f, String filename) {
//    ctrl.write(f, createOutput(filename));
    ctrl.writefile(f, filename);
  }

  public void write(CMXFileWrapper f, OutputStream output) {
    ctrl.write(f, output);
  }

  public void writefileAsSMF(SCCXMLWrapper f, String filename) {
    ctrl.writefileAsSMF(f, filename);
  }

  public void writefileAsSMF(MIDIXMLWrapper f, String filename) {
    ctrl.writefileAsSMF(f, filename);
  }

  public void writeAsSMF(SCCXMLWrapper f, OutputStream output) {
    ctrl.writeAsSMF(f, output);
  }

  public void writeAsSMF(MIDIXMLWrapper f, OutputStream output) {
    ctrl.writeAsSMF(f, output);
  }

  public void println(CMXFileWrapper f) {
    ctrl.println(f);
  }

  /** リアルタイム処理用の「モジュール」を登録します．*/
  public void addSPModule(ProducerConsumerCompatible module) {
    ctrl.addSPModule(module);
  }

  /** 登録済みの「モジュール」の接続方法を定義します．*/
  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    ctrl.connect(output, ch1, input, ch2);
  }

  /** 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void wavread(String filename) {
//    ctrl.wavread(createInput(filename));
    ctrl.wavread(filename);
    ctrl.addMusicListener(this);
  }

  /** 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void mp3read(String filename) {
//    ctrl.mp3read(createInput(filename));
    ctrl.mp3read(filename);
    ctrl.addMusicListener(this);
  }

  /** 指定された標準MIDIファイルを読み込みます．読み込まれた標準MIDIファイルは，
      このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに
      読み込まれます．*/
  public void smfread(String filename) {
//    ctrl.smfread(createInput(filename));
    ctrl.smfread(filename);
    ctrl.addMusicListener(this);
  }

  public void smfread(MIDIXMLWrapper midixml) {
    ctrl.smfread(midixml);
    ctrl.addMusicListener(this);
  }

  public void smfread(SCCXMLWrapper sccxml) {
    ctrl.smfread(sccxml);
    ctrl.addMusicListener(this);
  }

  /** すでに読み込まれた音楽データの再生を開始します．*/
  public void playMusic() {
    ctrl.playMusic();
  }

  /** 再生中の音楽を停止します．*/
  public void stopMusic() {
    ctrl.stopMusic();
  }

  /** 現在，音楽を再生中かどうかを返します．*/
  public boolean isNowPlaying() {
    return ctrl.isNowPlaying();
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をマイクロ秒単位で
      返します．*/
  public long getMicrosecondPosition() {
    return ctrl.getMicrosecondPosition();
  }

  /** 次回再生時の音楽の再生開始箇所をマイクロ秒単位で指定します．
      ただし，このメソッドは音楽停止中しか使用できません．*/
  public void setMicrosecondPosition(long t) {
    ctrl.setMicrosecondPosition(t);
  }

  /** 現在の再生中の音楽データにおける現在の再生箇所をティック単位で
      返します．
      ただし，このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public long getTickPosition() {
    return ctrl.getTickPosition();
  }

  /** 現在読み込まれているMIDIデータのTicks Per Beat（1拍あたりの
      ティック数）を返します．
      このメソッドは読み込み済みのデータがMIDIデータのときしか
      使用できません．*/
  public int getTicksPerBeat() {
    return ctrl.getTicksPerBeat();
  }

  /** このメソッドは呼び出さないでください．*/
  public void musicStarted(MusicPlaySynchronizer ms) {
    musicStarted();
  }

  /** このメソッドは呼び出さないでください．*/
  public void musicStopped(MusicPlaySynchronizer ms) {
    musicStopped();
  }

  /** このメソッドは呼び出さないでください．*/
  public void synchronize(double currentTime, long currentTick, 
                          MusicPlaySynchronizer ms) {
    synchronize();
  }

  /** 音楽の再生が開始されたタイミングで，このメソッドが自動的に呼び出されます．
      再生の開始に同期して何かの処理を行いたい場合は，このメソッドを
      オーバーライドしてください．*/
  protected void musicStarted() {
    // do nithing
  }

  /** 音楽の再生が停止されたタイミングで，このメソッドが自動的に呼び出されます．
      再生の停止に同期して何かの処理を行いたい場合は，このメソッドを
      オーバーライドしてください．*/
  protected void musicStopped() {
    // do nothing
  }

  protected void synchronize() {
    // do nothing
  }

  /** 仮想鍵盤を表示し，キーボードのキーを押すと，対応するMIDIイベントが
      出力される「モジュール」を生成します，*/
  public MidiInputModule createVirtualKeyboard() {
    if (this instanceof Component)
      return ctrl.createVirtualKeyboard(this);
    else
      return ctrl.createVirtualKeyboard();
  }

  /** 認識済みのMIDI入力デバイスからMIDIイベントを受け付けてそのまま出力する
      「モジュール」を生成して返します．
      このメソッドは，<tt>showMidiInChooser</tt>メソッドによって
      MIDI入力デバイスを選択した後でしか使用できません．*/
  public MidiInputModule createMidiIn() {
    return ctrl.createMidiIn();
  }

  /** 認識済みのMIDI出力デバイスに，入力されたMIDIイベントを出力する「モジュール」を
      生成して返します．*/
  public MidiOutputModule createMidiOut() {
    return ctrl.createMidiOut();
  }
  
  /** 認識済みのMIDI入力デバイスの選択ダイアログを表示します．*/
  public void showMidiInChooser() {
    ctrl.showMidiInChooser(this);
  }

  /** 認識済みのMIDI出力デバイスの選択ダイアログを表示します．*/
  public void showMidiOutChooser() {
    ctrl.showMidiOutChooser(this);
  }

  /** マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と
      出力する「モジュール」を生成します．
      サンプリング周波数は16kHzとします．*/
  public WindowSlider createMic() {
    return ctrl.createMic();
  }

  /** マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と
      出力する「モジュール」を生成します．
      <tt>fs</tt>にはサンプリング周波数をHz単位で指定します．*/
  public WindowSlider createMic(int fs) {
    return ctrl.createMic(fs);
  }

  /** 現在サウンドカードから再生中の音を受け取って，その波形データを短区間ごとに区切った
      波形断片を次々と出力する「モジュール」を生成します．*/
  public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
    return ctrl.createWaveCapture(isStereo);
  }

  /** 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます．
      <tt>createMic</tt>などを使用する際には必須です．*/
  public void readConfig(String filename) {
//    ctrl.readConfig(createInput(filename));
    ctrl.readConfig(filename);
  }

  /** 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます．
      <tt>createMic</tt>などを使用する際には必須です．*/
  public void readConfig(InputStream input) {
    ctrl.readConfig(input);
  }

  public MidiEventSender createMidiEventSender() {
    return ctrl.createMidiEventSender();
  }

  /** このメソッドは呼び出さないでください．*/
  public void handleDraw() {
    if (frameCount == 1) {
      autostart();
      super.handleDraw();
    } else {
      super.handleDraw();
    }
  }

  private void autostart() {
    println("SPExector automatically started.");
    ctrl.startSP();
  }

  public static void main(String className) {
    main(new String[]{className});
  }

  /** このクラス（スケッチブック）の実行を開始します．
      <tt>className</tt>には，当該サブクラスの名前を指定します．*/
  public static void start(String className) {
    main(new String[]{className});
  }

  /** 音楽推論用のオブジェクトを返します．*/
  public MusicRepresentation 
  createMusicRepresentation(int measure, int division) {
    return ctrl.createMusicRepresentation(measure, division);
  }

  public void sleep(long ms) {
    try {
      Thread.currentThread().sleep(ms);
    } catch (InterruptedException e) {}
  }
    
}
