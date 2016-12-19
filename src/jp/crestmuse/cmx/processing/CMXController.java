package jp.crestmuse.cmx.processing;

import groovy.lang.Closure;

import java.awt.Component;
import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.inference.MusicRepresentation;
import jp.crestmuse.cmx.inference.MusicRepresentationFactory;
import jp.crestmuse.cmx.math.ComplexArray;
import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.Operations;
import jp.crestmuse.cmx.sound.AudioDataCompatible;
import jp.crestmuse.cmx.sound.AudioInputStreamWrapper;
import jp.crestmuse.cmx.sound.MIDIConsts;
import jp.crestmuse.cmx.sound.MusicListener;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;
import jp.crestmuse.cmx.sound.MusicPlayer;
import jp.crestmuse.cmx.sound.SMFPlayer;
import jp.crestmuse.cmx.sound.SoundUtils;
import jp.crestmuse.cmx.sound.TickTimer;
import jp.crestmuse.cmx.sound.VirtualKeyboard;
import jp.crestmuse.cmx.sound.WAVPlayer2;

import org.xml.sax.SAXException;

import processing.core.PApplet;

/**********************************************************************
 * このクラスは，CrestMuse Toolkit (CMX)の主要な機能を簡単に呼び出せるようにしたクラスです．
 * ただし，現時点ではCMXのすべての機能を呼び出せるようになっているわけではありません．
 * 
 * このクラスを利用する際には，getInstanceメソッドでインスタンスを取得してから，各種メソッドを利用します．
 **********************************************************************/

public class CMXController implements TickTimer, MIDIConsts {

	private static final CMXController me = new CMXController();

	private SPExecutor spexec = null;
	private MusicPlayer[] musicPlayer = new MusicPlayer[256];
	// private MusicPlayer musicPlayer = null;
	private MusicPlaySynchronizer[] musicSync = new MusicPlaySynchronizer[256];
	// private MusicPlaySynchronizer musicSync = null;
	private AudioInputStreamWrapper mic = null;
	private AudioDataCompatible wav = null;
	private SynchronizedWindowSlider winslider = null;

	private MidiDevice.Info[] midiins = new MidiDevice.Info[256];
	private MidiDevice.Info[] midiouts = new MidiDevice.Info[256];
	private Mixer.Info mixer = null;

  private long startTime;

	private CMXController() {

	}

	/**
	 * このクラスのインスタンスを返します．
	 * 
	 * @return CMXController のインスタンス
	 */
	public static CMXController getInstance() {
		return me;
	}

	/**
	 * CMXが対応しているXML形式の文書オブジェクトを生成します．<br>
	 * たとえば，SCCXML形式の文書オブジェクトを生成する際には，
	 * {@code createDocument(SCCXMLWrapper.TOP_TAG)} とします．
	 * 
	 * @param toptag 作成するドキュメントタイプのTOP_TAG
	 * @return 指定されたTOP_TAGに対応する文書オブジェクト
	 */
	public static CMXFileWrapper createDocument(String toptag) {
		try {
			return CMXFileWrapper.createDocument(toptag);
		} catch (InvalidFileTypeException e) {
			throw new IllegalArgumentException("Invalid file type: " + toptag);
		}
	}

	/**
	 * CMXが対応しているXML形式の文書を読み込みます．
	 * 
	 * @param filename XMLファイル名
	 * @return 指定されたファイルの文書オブジェクト
	 */
	public static CMXFileWrapper readfile(String filename) {
		try {
			return CMXFileWrapper.readfile(filename);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read file: " + filename);
		}
	}

	/**
	 * CMXが対応しているXML形式の文書を読み込みます．
	 * 
	 * @param input XML形式の入力ストリーム
	 * @return 指定されたストリームの文書オブジェクト
	 */
	public static CMXFileWrapper read(InputStream input) {
		try {
			return CMXFileWrapper.read(input);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read file");
		}
	}

	/**
	 * 標準MIDIファイルをMIDIXML形式で読み込みます．
	 * 
	 * @param filename 標準MIDIファイル名
	 * @return 指定されたファイルから生成されたMIDIXMLオブジェクト
	 */
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

	/**
	 * 標準MIDIファイルをMIDIXML形式で読み込みます．
	 * 
	 * @param input 標準MIDIファイルのストリーム
	 * @return 指定されたストリームから生成されたMIDIXMLオブジェクト
	 */
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

  /** 標準MIDIファイルをSCCIXML形式で読み込みます． */
  /*
  public static SCCXMLWrapper readSMFAsSCCXML(String filename) {
    try {
      return MIDIXMLWrapper.readSMF(filename).toSCCXML();
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
  */

  public static SCC readSMFAsSCC(String filename) {
    try {
      return MIDIXMLWrapper.readSMF(filename).toSCC();
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

  public static SCC readSMFAsSCC(InputStream input) {
    try {
      return MIDIXMLWrapper.readSMF(input).toSCC();
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot read data from InputStream");
    } catch (TransformerException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    }
  }
	/**
	 * CMXFileWrapperオブジェクトを，対応するXML形式でファイルに保存します．
	 * 
	 * @param f 保存する文書オブジェクト
	 * @param filename 保存ファイル名
	 * 
	 */
	public static void writefile(CMXFileWrapper f, String filename) {
		try {
			f.writefile(filename);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file: " + filename);
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error: " + filename);
		}
	}


  /** 標準MIDIファイルをMIDIXML形式で読み込みます． */
  public static SCCXMLWrapper readSMFAsSCCXML(InputStream input) {
    try {
      return MIDIXMLWrapper.readSMF(input).toSCCXML();
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
//  public static void writefile(CMXFileWrapper f, String filename) {
//    try {
//      f.writefile(filename);
//    } catch (IOException e) {
//      throw new IllegalArgumentException("Cannot write file: " + filename);
//    } catch (SAXException e) {
//      throw new IllegalArgumentException("XML error: " + filename);
//    }
//  }

	/** CMXFileWrapperオブジェクトを，対応するXML形式で出力ストリームに書き出します． */
	public static void write(CMXFileWrapper f, OutputStream output) {
		try {
			f.write(output);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file");
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error");
		}
	}

	/** SCCXMLドキュメントを標準MIDIファイルとしてファイルに保存します． */
	public static void writefileAsSMF(SCCXMLWrapper f, String filename) {
		try {
			f.toMIDIXML().writefile(filename);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file: " + filename);
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error: " + filename);
			// } catch (ParserConfigurationException e) {
			// throw new IllegalStateException("Parser error: " + filename);
			// } catch (TransformerException e) {
			// throw new IllegalArgumentException("XML error: " + filename);
		}
	}

	/** MIDIXMLドキュメントを標準MIDIファイルとしてファイルに保存します． */
	public static void writefileAsSMF(MIDIXMLWrapper f, String filename) {
		try {
			f.writefile(filename);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file: " + filename);
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error: " + filename);
		}
	}

	/** SCCXMLドキュメントを標準MIDIファイルとして出力ストリームに書き出します． */
	public static void writeAsSMF(SCCXMLWrapper f, OutputStream output) {
		try {
			f.toMIDIXML().write(output);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file");
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error");
			// } catch (ParserConfigurationException e) {
			// throw new IllegalStateException("Parser error");
			// } catch (TransformerException e) {
			// throw new IllegalArgumentException("XML error");
		}
	}

	/** MIDIXMLドキュメントを標準MIDIファイルとして出力ストリームに書き出します． */
	public static void writeAsSMF(MIDIXMLWrapper f, OutputStream output) {
		try {
			f.write(output);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot write file");
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error");
		}
	}

	/** CMXFileWrapperオブジェクトをXML形式で標準出力に書き出します． */
	public static void println(CMXFileWrapper f) {
		try {
			f.write(System.out);
		} catch (IOException e) {
			throw new IllegalArgumentException("I/O Error");
		} catch (SAXException e) {
			throw new IllegalArgumentException("XML error");
		}
	}

	/** リアルタイム処理用の「モジュール」を登録します． */
	public void addSPModule(ProducerConsumerCompatible module) {
		if (spexec == null)
			spexec = new SPExecutor();
		spexec.addSPModule(module);
	}

	public ProducerConsumerCompatible newSPModule(Map args)
	    throws ClassNotFoundException {
		final Closure execute = (Closure) args.get("execute");
		final Class[] inputClasses = getClassArray(args.get("inputs"));
		final Class[] outputClasses = getClassArray(args.get("outputs"));
		SPModule module = new SPModule() {
			public void execute(Object[] src, TimeSeriesCompatible[] dest) {
				execute.call(new Object[] { src, dest });
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
			classes = new Class[] { (Class) inputs };
		} else if (inputs instanceof String) {
			classes = new Class[] { Class.forName((String) inputs) };
		} else if (inputs instanceof Object[]) {
			Object[] a = (Object[]) inputs;
			classes = new Class[a.length];
			for (int i = 0; i < a.length; i++) {
				if (a[i] instanceof Class)
					classes[i] = (Class) a[i];
				else if (a[i] instanceof String)
					classes[i] = Class.forName((String) a[i]);
				else
					throw new IllegalArgumentException("Illegal argument: " + a[i]);
			}
		} else if (inputs instanceof java.util.List) {
			java.util.List l = (java.util.List) inputs;
			classes = new Class[l.size()];
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i) instanceof Class)
					classes[i] = (Class) l.get(i);
				else if (l.get(i) instanceof String)
					classes[i] = Class.forName((String) l.get(i));
				else
					throw new IllegalArgumentException("Illegal argument: " + l.get(i));
			}
		} else {
			throw new IllegalArgumentException("Illegal argument: " + inputs);
		}
		return classes;
	}

	/** 登録済みの「モジュール」の接続方法を定義します． */
	public void connect(ProducerConsumerCompatible output, int ch1,
	    ProducerConsumerCompatible input, int ch2) {
		spexec.connect(output, ch1, input, ch2);
	}

	/** 登録済みの「モジュール」を実行開始します． */
	public void startSP() {
		if (mic != null)
			mic.getLine().start();
		if (spexec != null)
			spexec.start();
                startTime = System.nanoTime() / 1000;
	}

	public void stopSP() {
		if (mic != null)
			mic.getLine().stop();
		if (spexec != null)
			spexec.stop();
	}

	public void wavread(AudioDataCompatible w) {
		wavread(0, w);
	}

	public void wavread(int i, AudioDataCompatible w) {
		try {
			if (musicPlayer[i] instanceof WAVPlayer2)
				((WAVPlayer2) musicPlayer[i]).changeWaveform(w);
			else
				musicPlayer[i] = new WAVPlayer2(w);
			musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);

			// kari
			if (i == 0) {
				wav = w;
				if (winslider != null) {
					winslider.setInputData(wav);
					addMusicListener(winslider);
				}
			}

		} catch (javax.sound.sampled.LineUnavailableException e) {
			throw new DeviceNotAvailableException("Audio device not available");
		} catch (IOException e) {
			throw new CMXIOException(e.toString());
		} catch (UnsupportedAudioFileException e) {
			throw new CMXIOException(e.toString());
		}
	}

	public void wavread(String filename) {
		wavread(0, filename);
	}

	/**
	 * 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
	 * このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 */
	public void wavread(int i, String filename) {
		try {
			wavread(i, WAVWrapper.readfile(filename));
			// musicPlayer[i] = new WAVPlayer(wav);
			// musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
		} catch (IOException e) {
			throw new CMXIOException("Cannot read file: " + filename);
			// } catch (javax.sound.sampled.LineUnavailableException e) {
			// throw new DeviceNotAvailableException("Audio device not available");
		}
	}

	public void wavread(InputStream input) {
		wavread(0, input);
	}

	/**
	 * 指定されたWAVファイルを読み込みます．読み込まれたWAVファイルは，
	 * このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 */
	public void wavread(int i, InputStream input) {
		try {
			// wav = WAVWrapper.read(input);
			// musicPlayer[i] = new WAVPlayer(wav);
			// musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
			wavread(i, WAVWrapper.read(input));
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read file");
			// } catch (javax.sound.sampled.LineUnavailableException e) {
			// throw new DeviceNotAvailableException("Audio device not available");
		}
	}

	public void mp3read(String filename) {
		mp3read(0, filename);
	}

	/**
	 * 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
	 * このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 */
	public void mp3read(int i, String filename) {
		try {
			wavread(MP3Wrapper.readfile(filename));
			// wav = MP3Wrapper.readfile(filename);
			// musicPlayer[i] = new WAVPlayer(wav);
			// musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
		} catch (IOException e) {
			throw new CMXIOException("Cannot read file: " + filename);
		} catch (DecoderException e) {
			throw new CMXIOException("Cannot decode MP3 file: " + filename);
		} catch (BitstreamException e) {
			throw new CMXIOException("Cannot decode MP3 file: " + filename);
			// } catch (javax.sound.sampled.LineUnavailableException e) {
			// throw new DeviceNotAvailableException("Audio device not available");
		}
	}

	/**
	 * 指定されたMP3ファイルを読み込みます．読み込まれたMP3ファイルは，
	 * このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 */
	public void mp3read(int i, InputStream input) {
		try {
			wavread(MP3Wrapper.read(input));
			// wav = MP3Wrapper.read(input);
			// musicPlayer[i] = new WAVPlayer(wav);
			// musicSync[i] = new MusicPlaySynchronizer(musicPlayer[i]);
		} catch (IOException e) {
			throw new CMXIOException("Cannot read file");
		} catch (DecoderException e) {
			throw new CMXIOException("Cannot decode MP3 file");
		} catch (BitstreamException e) {
			throw new CMXIOException("Cannot decode MP3 file");
			// } catch (javax.sound.sampled.LineUnavailableException e) {
			// throw new DeviceNotAvailableException("Audio device not available");
		}
	}

	/**
	 * 指定された標準MIDIファイルを読み込みます．
	 * 
	 * @param filename 読み込むファイル名
	 * @see #smfread(int, int, String)
	 */
	public void smfread(String filename) {
		smfread(0, 0, filename);
	}

	/**
	 * 指定されたMIDI出力デバイスの要素番号で標準MIDIファイルを読み込みます．
	 * 
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param filename 読み込むファイル名
	 * @see #smfread(int, int, String)
	 */
	public void smfread(int iMidi, String filename) {
		smfread(0, iMidi, filename);
	}

	/**
	 * 指定された標準MIDIファイルを読み込みます．<br>
	 * 読み込まれた標準MIDIファイルは，このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 * 
	 * @param iMusic MusicPlayerの要素番号(0-255)
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param filename 読み込むファイル名
	 */
	public void smfread(int iMusic, int iMidi, String filename) {
		try {
			if (midiouts[iMidi] == null) {
				musicPlayer[iMusic] = new SMFPlayer();
			} else {
				MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiouts[iMidi]
				    .getName());
				dev.open();
				musicPlayer[iMusic] = new SMFPlayer(dev);
			}
			((SMFPlayer) musicPlayer[iMusic]).readSMF(filename);
			musicSync[iMusic] = new MusicPlaySynchronizer(musicPlayer[iMusic]);
		} catch (IOException e) {
			throw new CMXIOException("Cannot read file: " + filename);
		} catch (javax.sound.midi.MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		} catch (javax.sound.midi.InvalidMidiDataException e) {
			throw new CMXIOException("Invalid MIDI data: " + filename);
		}
	}

	/**
	 * 指定されたシーケンスを読み込み，シーケンサーにセットします．
	 * 
	 * @param seq 読み込むシーケンス
	 * @see #smfread(int, int, Sequence)
	 */
	public void smfread(Sequence seq) {
		smfread(0, 0, seq);
	}

	/**
	 * 指定されたMIDI出力デバイスの要素番号でシーケンスを読み込み，シーケンサーにセットします．
	 * 
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param seq 読み込むシーケンス
	 */
	public void smfread(int iMidi, Sequence seq) {
		smfread(0, iMidi, seq);
	}

	/**
	 * 指定されたシーケンスを読み込み，シーケンサーにセットします．<br>
	 * 読み込まれたシーケンスは，このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 * 
	 * @param iMusic MusicPlayerの要素番号(0-255)
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param seq 読み込むシーケンス
	 */
	public synchronized void smfread(int iMusic, int iMidi, Sequence seq) {
		try {
			if (midiouts[iMidi] == null) {
				musicPlayer[iMusic] = new SMFPlayer();
			} else {
				MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiouts[iMidi]
				    .getName());
				dev.open();
				musicPlayer[iMusic] = new SMFPlayer(dev);
			}
			((SMFPlayer) musicPlayer[iMusic]).readSMF(seq);
			musicSync[iMusic] = new MusicPlaySynchronizer(musicPlayer[iMusic]);
		} catch (javax.sound.midi.MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		} catch (javax.sound.midi.InvalidMidiDataException e) {
			throw new CMXIOException("Invalid MIDI data");
		}
	}

	/**
	 * 指定された標準MIDIファイルを読み込みます．
	 * 
	 * @param input 読み込む入力ストリーム
	 * @see #smfread(int, int, InputStream)
	 */
	public void smfread(InputStream input) {
		smfread(0, 0, input);
	}

	/**
	 * 指定されたMIDI出力デバイスの要素番号で標準MIDIファイルを読み込みます．
	 * 
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param input 読み込む入力ストリーム
	 * @see #smfread(int, int, InputStream)
	 */
	public void smfread(int iMidi, InputStream input) {
		smfread(0, iMidi, input);
	}

	/**
	 * 指定された標準MIDIファイルを読み込みます．<br>
	 * 読み込まれた標準MIDIファイルは，このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 * 
	 * @param iMusic MusicPlayerの要素番号(0-255)
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param input 読み込む入力ストリーム
	 */
	public void smfread(int iMusic, int iMidi, InputStream input) {
          try {
            if (midiouts[iMidi] == null) {
              musicPlayer[iMusic] = new SMFPlayer();
            } else {
              MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiouts[iMidi]
                                                                 .getName());
              dev.open();
              musicPlayer[iMusic] = new SMFPlayer(dev);
            }
            ((SMFPlayer) musicPlayer[iMusic]).readSMF(input);
            musicSync[iMusic] = new MusicPlaySynchronizer(musicPlayer[iMusic]);
          } catch (IOException e) {
            throw new CMXIOException("Cannot read file");
          } catch (javax.sound.midi.MidiUnavailableException e) {
            throw new DeviceNotAvailableException("MIDI device not available");
          } catch (javax.sound.midi.InvalidMidiDataException e) {
            throw new CMXIOException("Invalid MIDI data");
          }
	}

	/**
	 * MIDIXMLWrapperを指定してMIDIXMLドキュメントを標準MIDIファイルに変換して読み込みます．
	 * 
	 * @param midi 読み込むMIDIXMLドキュメント
	 * @see #smfread(int, int, MIDIXMLWrapper)
	 */
	public void smfread(MIDIXMLWrapper midi) {
		smfread(0, 0, midi);
	}

	/**
	 * MIDI出力デバイスの要素番号とMIDIXMLWrapperを指定してMIDIXMLドキュメントを標準MIDIファイルに変換して読み込みます．
	 * 
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param midi 読み込むMIDIXMLドキュメント
	 * @see #smfread(int, int, MIDIXMLWrapper)
	 */
	public void smfread(int iMidi, MIDIXMLWrapper midi) {
		smfread(0, iMidi, midi);
	}

	/**
	 * MIDIXMLドキュメントを標準MIDIファイルに変換して読み込みます．<br>
	 * 読み込まれた標準MIDIファイルは，このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 * 
	 * @param iMusic MusicPlayerの要素番号(0-255)
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param midi 読み込むMIDIXMLドキュメント
	 */
	public void smfread(int iMusic, int iMidi, MIDIXMLWrapper midi) {
		try {
			smfread(iMusic, iMidi, midi.getMIDIInputStream());
		} catch (IOException e) {
			throw new CMXIOException("Invalid MIDIXML data");
		}
	}

	/**
	 * SCCXMLドキュメントを標準MIDIファイルに変換して読み込みます．
	 * 
	 * @param scc 読み込むSCCXMLドキュメント
	 * @see #smfread(int, int, SCC)
	 */
	public void smfread(SCC scc) {
		smfread(0, 0, scc);
	}

	/**
	 * MIDI出力デバイスの要素番号を指定してSCCXMLドキュメントを標準MIDIファイルに変換して読み込みます．
	 * 
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param scc 読み込むSCCXMLドキュメント
	 * @see #smfread(int, int, SCC)
	 */
	public void smfread(int iMidi, SCC scc) {
		smfread(0, iMidi, scc);
	}

	/**
	 * SCCXMLドキュメントを標準MIDIファイルに変換して読み込みます．<br>
	 * 読み込まれた標準MIDIファイルは，このクラスのインスタンス内に保存され，playMusicメソッドが呼ばれたときに 読み込まれます．
	 * 
	 * @param iMusic MusicPlayerの要素番号(0-255)
	 * @param iMidi MIDI出力デバイスの要素番号(0-255)
	 * @param scc 読み込むSCCXMLドキュメント
	 */
	public void smfread(int iMusic, int iMidi, SCC scc) {
		try {
			smfread(iMusic, iMidi, scc.getMIDIInputStream());
		} catch (IOException e) {
			throw new CMXIOException("Invalid SCCXML data");
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("parser error");
		} catch (TransformerException e) {
			throw new CMXIOException("XML error");
		} catch (SAXException e) {
			throw new CMXIOException("XML error");
		}
	}

	/*
	 * public void midiread(MIDIXMLWrapper midi) { try { PipedOutputStream pout =
	 * new PipedOutputStream(); PipedInputStream pin = new PipedInputStream(pout);
	 * midi.writeAsSMF(pout); musicPlayer = new SMFPlayer();
	 * ((SMFPlayer)musicPlayer).readSMF(pin); musicSync = new
	 * MusicPlaySynchronizer(musicPlayer); } catch (IOException e) { throw new
	 * IllegalArgumentException("Cannot read file"); } catch
	 * (javax.sound.midi.MidiUnavailableException e) { throw new
	 * DeviceNotAvailableException("MIDI device not available"); } catch
	 * (javax.sound.midi.InvalidMidiDataException e) { throw new
	 * IllegalArgumentException("Invalid MIDI data"); } }
	 */

	/*
	 * public void sccread(SCCXMLWrapper scc) { try { midiread(scc.toMIDIXML()); }
	 * catch (ParserConfigurationException e) { throw
	 * IllegalStateExcpetion("parser configuration exception"); } }
	 */

	/** すでに読み込まれた音楽データの再生を開始します． */
	public void playMusic() {
		playMusic(0);
	}

	public void playMusic(int i) {
		stopMusic(i);
		if (getMicrosecondPosition(i) == getMicrosecondLength(i))
			setMicrosecondPosition(i, 0);
		musicSync[i].play();
	}

	/** 再生中の音楽を停止します． */
	public void stopMusic() {
		stopMusic(0);
	}

	public void stopMusic(int i) {
		if (musicSync != null && musicSync[i] != null)
			musicSync[i].stop();
	}

	public boolean isNowPlaying() {
		return isNowPlaying(0);
	}

	/** 現在，音楽を再生中かどうかを返します． */
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
			throw new IllegalStateException(
			    "setMusicLoop should be called after a music file is read.");
	}

	public void setMicrosecondPosition(long t) {
		setMicrosecondPosition(0, t);
	}

	/**
	 * 次回再生時の音楽の再生開始箇所をマイクロ秒単位で指定します． ただし，このメソッドは音楽停止中しか使用できません．
	 */
	public void setMicrosecondPosition(int i, long t) {
		musicPlayer[i].setMicrosecondPosition(t);
	}

	public long getMicrosecondPosition() {
		return getMicrosecondPosition(0);
	}

	/**
	 * 現在の再生中の音楽データにおける現在の再生箇所をマイクロ秒単位で 返します．
	 */
	public long getMicrosecondPosition(int i) {
          if (musicPlayer[i] == null)
            return System.nanoTime() / 1000 - startTime;
          else
            return musicPlayer[i].getMicrosecondPosition();
	}

	public long getMicrosecondLength() {
		return getMicrosecondLength(0);
	}

	public long getMicrosecondLength(int i) {
		return musicPlayer[i].getMicrosecondLength();
	}
	
	public void setTickPosition(long tick) {
		setTickPosition(0, tick);
	}
	
	/**
	 * 次回再生時の音楽の再生開始箇所をティック単位で指定します．<br>
	 * ただし，このメソッドはMIDIファイルが読み込まれている場合にしか使用出来ません．
	 * 
	 * @param i
	 * @param tick ティック位置
	 */
	public void setTickPosition(int i, long tick) {
		((SMFPlayer)musicPlayer[i]).setTickPosition(tick);
		//TO DO:例外処理の追加（unsupported operation exception
	}

	public long getTickPosition() {
		return getTickPosition(0);
	}

	/**
	 * 現在の再生中の音楽データにおける現在の再生箇所をティック単位で 返します． ただし，このメソッドは読み込み済みのデータがMIDIデータのときしか
	 * 使用できません．
	 */
	public long getTickPosition(int i) {
          if (musicPlayer[i] == null)
            return getMicrosecondPosition(i) * getTicksPerBeat(i) / 1000 / 500;
          else
            return musicPlayer[i].getTickPosition();
	}

	public int getTicksPerBeat() {
		return getTicksPerBeat(0);
	}

	/**
	 * 現在読み込まれているMIDIデータのTicks Per Beat（1拍あたりの ティック数）を返します．
	 * このメソッドは読み込み済みのデータがMIDIデータのときしか 使用できません．
	 */
	public int getTicksPerBeat(int i) {
		if (musicPlayer[i] == null)
			return 480;
		else
			return musicPlayer[i].getTicksPerBeat();
	}

	public float getTempoInBPM() {
		return getTempoInBPM(0);
	}

	public float getTempoInBPM(int i) {
		if (musicPlayer[i] == null) {
			throw new IllegalStateException("Music player #" + i + " is null.");
		} else if (musicPlayer[i] instanceof SMFPlayer) {
			return ((SMFPlayer) musicPlayer[i]).getTempoInBPM();
		} else {
			throw new IllegalStateException(
			    "getTempoInBPM() can be used only for MIDI files.");
		}
	}

	public void setTempoInBPM(float bpm) {
		setTempoInBPM(0, bpm);
	}

	public void setTempoInBPM(double bpm) {
		setTempoInBPM(0, (float) bpm);
	}

	public void setTempoInBPM(int i, float bpm) {
		if (musicPlayer[i] == null) {
			throw new IllegalStateException("Music player #" + i + " is null.");
		} else if (musicPlayer[i] instanceof SMFPlayer) {
			((SMFPlayer) musicPlayer[i]).setTempoInBPM(bpm);
		} else {
			throw new IllegalStateException(
			    "setTempoInBPM() can be used only for MIDI files.");
		}
	}

	public void setTempoInBPM(int i, double bpm) {
		setTempoInBPM(0, (float) bpm);
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

	/** 音楽の再生が停止されるまで，スレッドを停止します． */
	public void waitForMusicStopped(int i) {
		try {
			do {
				Thread.currentThread().sleep(100);
			} while (isNowPlaying(i));
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * 仮想鍵盤を表示し，キーボードのキーを押すと，対応するMIDIイベントが 出力される「モジュール」を生成します，
	 */
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

	/**
	 * 仮想鍵盤を表示し，キーボードのキーを押すと，対応するMIDIイベントが 出力される「モジュール」を生成します，
	 */
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

	/**
	 * 認識済みのMIDI入力デバイスの一覧から部分一致するデバイスを探し，セットします．
	 * 
	 * @param deviceName MIDI入力デバイスの名前 （大文字小文字問わず，デバイス名の一部も可）
	 */
	public void setMidiInDevice(String deviceName) {
		setMidiInDevice(0, deviceName);
	}

	/**
	 * 認識済みのMIDI入力デバイスの一覧から部分一致するデバイスを探し， 指定された要素番号に対応するデバイスとしてセットします．<br>
	 * <br>
	 * Note : 文字列が複数のデバイスに一致した場合でも，最初のデバイスをセットします．
	 * その為，複数の同一デバイスに対してはこのメソッドは正常に動作しません．
	 * 
	 * @param i MIDI入力デバイスの要素番号(0-255)
	 * @param deviceName MIDI入力デバイスの名前 （大文字小文字問わず，デバイス名の一部も可）
	 */
	public void setMidiInDevice(int i, String deviceName) {
		try {
			boolean isMatch = false;

			for (MidiDevice.Info info : SoundUtils.getMidiInDeviceInfo()) {
				if (info.getName().toLowerCase().indexOf(deviceName.toLowerCase()) != -1) {
					midiins[i] = info;
					isMatch = true;
				}
			}

			if (!isMatch)
				throw new DeviceNotAvailableException("MIDI device can't find");
		} catch (MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		}
	}

	/**
	 * 認識済みのMIDI出力デバイスの一覧から部分一致するデバイスを探し，セットします．
	 * 
	 * @param deviceName MIDI出力デバイスの名前 （大文字小文字問わず，デバイス名の一部も可）
	 */
	public void setMidiOutDevice(String deviceName) {
		setMidiOutDevice(0, deviceName);
	}

	/**
	 * 認識済みのMIDI出力デバイスの一覧から部分一致するデバイスを探し， 指定された要素番号に対応するデバイスとしてセットします．<br>
	 * <br>
	 * Note : 文字列が複数のデバイスに一致した場合でも，最初のデバイスをセットします．
	 * その為，複数の同一デバイスに対してはこのメソッドは正常に動作しません．
	 * 
	 * @param i MIDI出力デバイスの要素番号(0-255)
	 * @param deviceName MIDI出力デバイスの名前 （大文字小文字問わず，デバイス名の一部も可）
	 */
	public void setMidiOutDevice(int i, String deviceName) {
		try {
			boolean isMatch = false;

			for (MidiDevice.Info info : SoundUtils.getMidiInDeviceInfo()) {
				if (info.getName().toLowerCase().indexOf(deviceName.toLowerCase()) != -1) {
					midiouts[i] = info;
					isMatch = true;
				}
			}

			if (!isMatch)
				throw new DeviceNotAvailableException("MIDI device can't find");
		} catch (MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		}
	}

	/**
	 * 接続されているMICデバイスの一覧から部分一致するデバイスを探し，設定します．
	 * 
	 * @param deviceName MICデバイスの名前 （大文字小文字問わず，デバイス名の一部も可）
	 */
	public void setMicDevice(String deviceName) {
		boolean isMatch = false;

		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			if (info.getName().toLowerCase().indexOf(deviceName.toLowerCase()) != -1) {
				mixer = info;
				isMatch = true;
			}
		}

		if (!isMatch)
			throw new DeviceNotAvailableException("MIC device can't find");
	}

	/**
	 * 認識済みのMIDI入力デバイスからMIDIイベントを受け付けてそのまま出力する「モジュール」を生成して返します．<br>
	 * このメソッドは，<tt>showMidiInChooser</tt>メソッドによってMIDI入力デバイスを選択した後でしか使用できません．
	 */
	public MidiInputModule createMidiIn() {
		return createMidiIn(0);
	}

	/**
	 * 指定された要素番号に対応する認識済みのMIDI入力デバイスからMIDIイベントを受け付けて そのまま出力する「モジュール」を生成して返します．<br>
	 * このメソッドは，<tt>showMidiInChooser</tt>メソッドによってMIDI入力デバイスを選択した後でしか使用できません．
	 * 
	 * @param i MIDI入力デバイスの要素番号(0-255)
	 */
	public MidiInputModule createMidiIn(int i) {
          try {
            if (midiins[i] == null) {
              throw new IllegalStateException(
                "MIDI IN device has not been selected yet");
            } else {
              MidiInputModule midiin2 = new MidiInputModule(
                SoundUtils.getMidiInDeviceByName(midiins[i].getName()));
              midiin2.setTickTimer(this);
              addSPModule(midiin2);
              return midiin2;
            }
          } catch (MidiUnavailableException e) {
            throw new DeviceNotAvailableException("MIDI device not available");
          }
	}

	/**
	 * 認識済みのMIDI出力デバイスに，入力されたMIDIイベントを出力する「モジュール」を生成して返します．
	 */
	public MidiOutputModule createMidiOut() {
		return createMidiOut(0);
	}

	/**
	 * 指定された要素番号に対応する認識済みのMIDI出力デバイスに，入力されたMIDIイベントを出力する「モジュール」を生成して返します．
	 * 
	 * @param i MIDI出力デバイスの要素番号(0-255)
	 */
	public MidiOutputModule createMidiOut(int i) {
		try {
			if (midiouts[i] == null) {
				MidiOutputModule midiout2 = new MidiOutputModule();
				addSPModule(midiout2);
				return midiout2;
			} else {
				MidiDevice dev = SoundUtils.getMidiOutDeviceByName(midiouts[i]
				    .getName());
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
		Object selected = JOptionPane.showInputDialog(parent,
		    "Select Audio Mixer.", "Select Audio Mixer...",
		    JOptionPane.PLAIN_MESSAGE, null, AudioSystem.getMixerInfo(), null);
		if (selected != null)
			mixer = (Mixer.Info) selected;
	}

	/**
	 * 認識済みのMIDI入力デバイスの選択ダイアログを表示します．<br>
	 * 複数のデバイスを利用する場合は，デバイス毎に 0-255 の間で格納する要素番号を決め， 都度
	 * <tt>showMidiInChooser(int, Component)</tt>を呼び出してください．<br>
	 * 表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には<tt>null</tt>を指定することもできます．
	 */
	public void showMidiInChooser(Component parent) {
		showMidiInChooser(0, parent);
	}

	/**
	 * 指定された要素番号に対応する認識済みのMIDI入力デバイスの選択ダイアログを表示します．<br>
	 * 表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には<tt>null</tt>を指定することもできます．
	 */
	public void showMidiInChooser(int i, Component parent) {
		try {
			Object selected = JOptionPane.showInputDialog(parent,
			    "Select MIDI IN Device [" + i + "].", "Select MIDI IN Device...",
			    JOptionPane.PLAIN_MESSAGE, null, SoundUtils.getMidiInDeviceInfo()
			        .toArray(), null);
			if (selected != null)
				midiins[i] = (MidiDevice.Info) selected;
		} catch (MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		}
	}

	/**
	 * 認識済みのMIDI出力デバイスの選択ダイアログを表示します．<br>
	 * 複数のデバイスを利用する場合は，デバイス毎に 0-255 の間で格納する要素番号を決め， 都度
	 * <tt>showMidiOutChooser(int, Component)</tt>を呼び出してください．<br>
	 * 表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には<tt>null</tt>を指定することもできます．
	 */
	public void showMidiOutChooser(Component parent) {
		showMidiOutChooser(0, parent);
	}

	/**
	 * 指定された要素番号に対応する認識済みのMIDI出力デバイスの選択ダイアログを表示します．<br>
	 * 表示するダイアログボックスの親ウィンドウが不明な場合，<tt>parent</tt>には<tt>null</tt>を指定することもできます．
	 */
	public void showMidiOutChooser(int i, Component parent) {
		try {
			Object selected = JOptionPane.showInputDialog(parent,
			    "Select MIDI OUT Device [" + i + "].", "Select MIDI OUT Device...",
			    JOptionPane.PLAIN_MESSAGE, null, SoundUtils.getMidiOutDeviceInfo()
			        .toArray(), null);
			if (selected != null)
				midiouts[i] = (MidiDevice.Info) selected;
		} catch (MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
		}
	}

	/**
	 * マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と 出力する「モジュール」を生成します．<br>
	 * サンプリング周波数は16kHzとします．
	 */
	public WindowSlider createMic() {
		return createMic(16000);
	}

	/**
	 * マイクから波形データを受け取って，短区間ごとに区切った波形断片を次々と出力する「モジュール」を生成します．<br>
	 * <tt>fs</tt>にはサンプリング周波数をHz単位で指定します．
	 * 
	 * @param fs サンプリング周波数 (Hz単位)
	 */
	public WindowSlider createMic(int fs) {
		try {
			mic = AudioInputStreamWrapper.createWrapper16(fs, mixer);
			WindowSlider winslider = new WindowSlider(false);
			winslider.setInputData(mic);
			winslider.setTickTimer(this);
			// mic.getLine().start();
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

	/**
	 * 現在サウンドカードから再生中の音を受け取って，その波形データを短区間ごとに区切った 波形断片を次々と出力する「モジュール」を生成します．
	 * （新しいwavreadへの対応は要チェック）
	 */
	public SynchronizedWindowSlider createWaveCapture(boolean isStereo) {
		winslider = new SynchronizedWindowSlider(isStereo);
		if (wav != null) {
			winslider.setInputData(wav);
			addMusicListener(winslider);
		}
		addSPModule(winslider);
		return winslider;
	}

	/*
	 * public SamplingRateChanger createSamplingRateChanger(int oldRate, int
	 * newRate, boolean isStereo) { if (isStereo) return
	 * createSamplingRateChanger(oldRate, newRate, 2); else return
	 * createSamplingRateChanger(oldRate, newRate, 1); }
	 * 
	 * public SamplingRateChanger createSamplingRateChanger(int oldRate, int
	 * newRate, int nch) { SamplingRateChanger c = new
	 * SamplingRateChanger(oldRate, newRate, nch); addSPModule(c); return c; }
	 */

	public STFT createSTFT(boolean isStereo) {
		STFT stft = new STFT(isStereo);
		addSPModule(stft);
		return stft;
	}

	private class SpectrumViewerApplet extends PApplet {
		int w, h, m;
		double scale;
		SPModule module;
		DoubleArray x = null;

		private SpectrumViewerApplet(final int w, final int h, final int m,
		    final double scale) {
			this.w = w;
			this.h = h;
			this.m = m;
			this.scale = scale;
			module = new SPModule() {
				public Class[] getInputClasses() {
					return new Class[] { ComplexArray.class };
				}

				public Class[] getOutputClasses() {
					return new Class[] { ComplexArray.class };
				}

				public void execute(Object[] src, TimeSeriesCompatible[] dst) {
					x = Operations.abs((ComplexArray) src[0]);
					try {
						dst[0].add(src[0]);
					} catch (InterruptedException e) {
					}
				}
			};
		}

		public void setup() {
			size(w, h);
			frameRate(10);
		}

		public void draw() {
			background(255);
			if (x != null) {
				// int m = x.length();
				for (int i = 0; i < m; i++) {
					int amp = (int) (scale * x.get(i));
					rect(i * w / m, h - amp, w / m, amp);
				}
			}
		}
	}

	// kari
	public SPModule createSpectrumViewer(int w, int h, int m, double scale) {
		/*
		 * PApplet app = new PApplet() { SPModule module = new SPModule() { public
		 * Class[] getInputClasses() { return new Class[]{ComplexArray.class}; }
		 * public Class[] getOutputClasses() { return new
		 * Class[]{ComplexArray.class}; } public void execute(Object[] src,
		 * TimeSeriesCompatible[] dst) { DoubleArray x =
		 * Operations.abs((ComplexArray)src[0]); int m = x.length(); for (int i = 0;
		 * i < m; i++) rect(i * w / m, h, (i+1) * w / m, (int)(h - scale *
		 * x.get(i))); dst[0].add(src[0]); } }; public void setup() { size(w, h);
		 * noLoop(); } public void draw() { // do nothing } };
		 */
		SpectrumViewerApplet app = new SpectrumViewerApplet(w, h, m, scale);
		app.init();
		Frame f = new Frame();
		f.setSize(w, h);
		f.add(app);
		f.setVisible(true);
		addSPModule(app.module);
		return app.module;
	}

	public TrashOutModule createTrashOut() {
		TrashOutModule tom = new TrashOutModule();
		addSPModule(tom);
		return tom;
	}

	public MidiEventSender createMidiEventSender() {
		MidiEventSender evtsender = new MidiEventSender();
		addSPModule(evtsender);
		return evtsender;
	}

  public MidiRecorder2 createMidiRecorder() {
    MidiRecorder2 rec = new MidiRecorder2(this);
    rec.setTempo(120.0);   // kari
    addSPModule(rec);
    return rec;
  }
  
	/**
	 * 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます． <tt>createMic</tt>
	 * などを使用する際には必須です．
	 */
	public void readConfig(String filename) {
		try {
			AmusaParameterSet.getInstance().setAnotherParameterSet(
			    (ConfigXMLWrapper) CMXFileWrapper.readfile(filename));
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read config file: " + filename);
		}
	}

	/**
	 * 音響信号処理に関する各種パラメータや設定を記述してConfigXMLファイルを読み込みます． <tt>createMic</tt>
	 * などを使用する際には必須です．
	 */
	public void readConfig(InputStream input) {
		try {
			AmusaParameterSet.getInstance().setAnotherParameterSet(
			    (ConfigXMLWrapper) CMXFileWrapper.read(input));
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read config file");
		}
	}

	/** 音楽推論用のオブジェクトを返します． */
	public static MusicRepresentation createMusicRepresentation(int measure,
	    int division) {
		return MusicRepresentationFactory.create(measure, division);
	}

	public static MidiEventWithTicktime createShortMessageEvent(byte[] message,
	    long tick, long position) {
		return MidiEventWithTicktime.createShortMessageEvent(message, tick,
		    position);
	}

	public static MidiEventWithTicktime createShortMessageEvent(
	    java.util.List<? extends Number> message, long tick, long position) {
		return MidiEventWithTicktime.createShortMessageEvent(message, tick,
		    position);
	}

	public static MidiEventWithTicktime createControlChangeEvent(long position,
	    int ch, int type, int value) {
		return MidiEventWithTicktime.createControlChangeEvent(position, ch, type,
		    value);
	}

	public static MidiEventWithTicktime createNoteOffEvent(long position, int ch,
	    int nn, int vel) {
		return MidiEventWithTicktime.createNoteOffEvent(position, ch, nn, vel);
	}

	public static MidiEventWithTicktime createNoteOnEvent(long position, int ch,
	    int nn, int vel) {
		return MidiEventWithTicktime.createNoteOnEvent(position, ch, nn, vel);
	}

	public static MidiEventWithTicktime createProgramChangeEvent(long position,
	    int ch, int value) {
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
		} catch (InterruptedException e) {
		}
	}

	public boolean isControlSupported(int i, javax.sound.sampled.Control.Type type) {
		if (musicPlayer[i] instanceof WAVPlayer2)
			return ((WAVPlayer2) musicPlayer[i]).isControlSupported(type);
		else
			throw new UnsupportedOperationException(
			    "Only WAVPlayer2 supports isControlSupported()");
	}

	public boolean isMasterGainControlSupported(int i) {
		return isControlSupported(i, FloatControl.Type.MASTER_GAIN);
	}

	public boolean isMasterGainControlSupported() {
		return isMasterGainControlSupported(0);
	}

	public boolean isVolumeControlSupported(int i) {
		return isControlSupported(i, FloatControl.Type.VOLUME);
	}

	public boolean isVolumeControlSupported() {
		return isVolumeControlSupported(0);
	}

	public javax.sound.sampled.Control getControl(int i,
	    javax.sound.sampled.Control.Type type) {
		if (musicPlayer[i] instanceof WAVPlayer2)
			return ((WAVPlayer2) musicPlayer[i]).getControl(type);
		else
			throw new UnsupportedOperationException(
			    "Only WAVPlayer2 supports getControl()");
	}

	public FloatControl getMasterGainControl(int i) {
		return (FloatControl) getControl(i, FloatControl.Type.MASTER_GAIN);
	}

	public FloatControl getMasterGainControl() {
		return getMasterGainControl(0);
	}

	public FloatControl getVolumeControl(int i) {
		return (FloatControl) getControl(i, FloatControl.Type.VOLUME);
	}

	public FloatControl getVolumeControl() {
		return getVolumeControl(0);
	}

	/*
	 * public FloatControl getMasterGainControl(int i) { if (musicPlayer[i]
	 * instanceof WAVPlayer2) return
	 * ((WAVPlayer2)musicPlayer[i]).getMasterGainControl(); else throw new
	 * UnsupportedOperationException
	 * ("Only WAVPlayer2 supports getMasterGainControl()"); }
	 * 
	 * public FloatControl getMasterGainControl() { return
	 * getMasterGainControl(0); }
	 */

}
