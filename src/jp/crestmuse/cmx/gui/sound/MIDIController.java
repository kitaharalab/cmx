package jp.crestmuse.cmx.gui.sound;

import java.io.*;
import java.util.*;

import javax.sound.midi.*;

import jp.crestmuse.cmx.gui.CMXMusicDataFrame;
import jp.crestmuse.cmx.sound.*;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class MIDIController implements MIDIEventListener {

	public static final String THREAD_PLAYER = "Thread Player";

	public static final String SMF_PLAYER = "SMF Player";

	private static final int MIDI_TIMESTAMP = -1;

	/** デフォルトのMIDIチャンネル(0)に指定するインストゥルメント番号 */
	private static final int DEFAULT_INSTRUMENT = 0;

	/** デフォルトのMIDIデバイス名（Windows） */
	private static final String DEFAULT_DEVICENAME = "Microsoft GS Wavetable SW Synth";

	private static final boolean DEBUG = false;

	/** MIDI再生時に使用する関数の切り替え */
	private static String FUNCTION_MODE = SMF_PLAYER;

	private SMFPlayer smfplayer;

	private ThreadPlayer threadPlayer;

	/** 内部音源 */
	private MidiDevice device;

	/**
	 * 音源や外部のMIDIデバイスがMIDIメッセージを受け取る窓口． ここでは内部音源のMIDIメッセージ受け取りに使用します．
	 */
	private Receiver receiver;

	/**
	 * 発音中であるかどうかを記憶するイベントバッファ
	 */
	private Map<Integer, Boolean> soundNotesStatus;

	/**
	 * MIDIコントローラオブジェクトを生成します．
	 * @param deviceName
	 *        MIDIデバイス名
	 * @return MIDIControllerオブジェクト
	 */
	public static MIDIController createMIDIController(String deviceName) {
		MIDIController synthe = new MIDIController(deviceName);
		return synthe;
	}

	/**
	 * MIDIイベントリストを再生する方法を切り替えます．
	 * @param property
	 */
	public static void setupPlayerFunc(String property) {
		FUNCTION_MODE = property;
	}

	/**
	 * MIDIコントローラオブジェクトを生成し，指定されたMIDIデバイスをオープンします．
	 * <p>
	 * <em>このオブジェクトを生成したならば，プログラム終了時には必ずcloseメソッドを呼び出すことを忘れないでください．</em>
	 * でないとプログラムが終了してもMIDIデバイスが開放されず，他プログラムで使用することができなくなります．
	 * @param deviceName
	 *        MIDIデバイス名
	 * @return MIDIControllerオブジェクト
	 */
	public MIDIController(String deviceName) {
		threadPlayer = null;
		soundNotesStatus = new TreeMap<Integer, Boolean>();
		for (int i = 0; i < 127; i++)
			soundNotesStatus.put(i, false);
		openMidiDevice(deviceName);
	}

	/**
	 * MIDIデバイスをクローズします．
	 * <p>
	 * プログラム終了時，MIDIデバイスは明示的にクローズされる必要があります．し忘れると，プログラムが終了してもデバイスがメモリ空間から開放されないため，他プログラムで使えなくなります．
	 * もしマシンのアドミニストレータ権限を持っていないユーザでMIDIデバイスをクローズし損ねた場合，最も手っ取り早い回復方法はオペレーティングシステムを再起動することです．
	 */
	public void close() {
		if (receiver != null)
			receiver.close();
		if (device != null)
			device.close();
	}

	/**
	 * 現在時刻より前にあるMIDIイベントリストの最初のイベントを取得します．<br>
	 * 取得したイベントはMIDIイベントリストから削除されます．
	 * @param currentTime
	 * @return
	 */
	public final MidiMessage getMidiMessage(
			LinkedList<NoteEvent> midiEventList, double currentTime) {
		if (midiEventList.size() > 0
				&& midiEventList.peek().getOnset() < currentTime)
			return midiEventList.poll().getMidiMessage();
		return null;
	}

	/**
	 * 指定されたノートナンバーを消音します．
	 * @param noteNumber
	 */
	public void noteOff(int noteNumber) {
		try {
			ShortMessage message = new ShortMessage();
			message.setMessage(ShortMessage.NOTE_OFF, noteNumber, 127);
			sendMessage(message);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 指定されたノートナンバーを指定された強さ(velocity)で発音します．
	 * @param noteNumber
	 * @param velocity
	 */
	public void noteOn(int noteNumber, int velocity) {
		try {
			ShortMessage message = new ShortMessage();
			message.setMessage(ShortMessage.NOTE_ON, noteNumber, velocity);
			sendMessage(message);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void startPlaying(String smfFilename) {

		try {
			if (smfplayer != null)
				smfplayer.close();
			smfplayer = new SMFPlayer();
			smfplayer.readSMF(new File(smfFilename));
			smfplayer.play();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void startPlaying(InputStream instream) {

		try {
			if (smfplayer != null)
				smfplayer.close();
			smfplayer = new SMFPlayer();
			smfplayer.readSMF(instream);
			smfplayer.play();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void startPlaying(MIDIExpressionDataSet mdx,
			TimeEventMap timeEventMap) {

		mdx.setMIDIEventList(timeEventMap);

		try {
			if (threadPlayer != null && threadPlayer.isAlive())
				resetReceiver();
			threadPlayer = new ThreadPlayer((LinkedList<NoteEvent>) mdx
					.getMidiEventList());
			threadPlayer.start();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stopPlaying() {
		if (FUNCTION_MODE.equals(SMF_PLAYER)) {
			smfplayer.stop();
		} else if (FUNCTION_MODE.equals(THREAD_PLAYER)) {
			threadPlayer.isRunning = false;
			sendAllSoundOff();
		}
	}

	/**
	 * Javaから利用できるMIDIデバイスを一覧する．
	 */
	private void checkAllMidiDevices() {
		MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
		System.out.println("There are " + info.length + " devices.");
		for (int i = 0; i < info.length; i++) {
			System.out.println("Midi device " + i);
			System.out.println("  Description:" + info[i].getDescription());
			System.out.println("  Name:" + info[i].getName());
			System.out.println("  Vendor:" + info[i].getVendor());
			try {
				MidiDevice device = MidiSystem.getMidiDevice(info[i]);
				if (device instanceof Sequencer) {
					System.out.println("  *** This is Sequencer.");
				}
				if (device instanceof Synthesizer) {
					System.out.println("  *** This is Synthesizer.");
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	private MidiDevice getInternalSoftwareSynthesizer(String deviceName)
			throws MidiUnavailableException {
		MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
		// ここでは、全MIDIデヴァイスを見て、名前がdeviceName
		// と一致するものを探して、そのデヴァイスを返します。
		int defaultIndex = 0;
		for (int i = 0; i < info.length; i++) {
			if (info[i].getName().equals(deviceName)) {
				return MidiSystem.getMidiDevice(info[i]);
			} else if (info[i].getName().equals(DEFAULT_DEVICENAME)) {
				defaultIndex = i;
			}
		}
		return MidiSystem.getMidiDevice(info[defaultIndex]);
	}

	private void openMidiDevice(String deviceName) {
		checkAllMidiDevices();
		if (device != null)
			close();
		try {
			// ↓ MIDIを鳴らすためのMIDI device
			// を取得。詳しくは関数の実装を見てね。
			device = getInternalSoftwareSynthesizer(deviceName);
			if (!device.isOpen()) {
				device.open();
				System.out.println(device.getDeviceInfo().getName()
						+ " is opened.");
			}
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			device.close();
			return;
		}

		// MIDIメッセージを受け取る窓口をMIDI音源側に作ります。
		try {
			receiver = device.getReceiver();
		} catch (MidiUnavailableException e) {
			try {
				receiver = MidiSystem.getReceiver();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
				return;
			}
		}
		try {
			// MIDI音源の楽器の番号を指定します。（プログラムチェンジ）
			ShortMessage message = new ShortMessage();
			message.setMessage(ShortMessage.PROGRAM_CHANGE, 0,
								DEFAULT_INSTRUMENT, 0);
			sendMessage(message);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return;
		}
	}

	private void resetReceiver() throws InterruptedException {
		if (threadPlayer != null) {
			System.out.println("synthesizer reset...");
			threadPlayer.join(1000);
		}
	}

	/**
	 * 鳴っているすべての音を消音します．
	 */
	private void sendAllSoundOff() {
		System.out.println("all sound off");
		ShortMessage msg = new ShortMessage();
		try {
			// コントロールチェンジの All Sound Off (120) を使用
			msg.setMessage(ShortMessage.CONTROL_CHANGE, 120, 0);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		// 全ての音を消します。
		for (int i = 0; i < 127; i++) {
			try {
				msg.setMessage(ShortMessage.NOTE_OFF, i, 127);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			sendMessage(msg);
		}
	}

	/**
	 * MIDIデバイスにMIDIメッセージを送信します．
	 * @param message
	 */
	private synchronized void sendMessage(MidiMessage message) {
		receiver.send(message, MIDI_TIMESTAMP);
		if (message instanceof ShortMessage) {
			ShortMessage msg = (ShortMessage) message;
			if (DEBUG)
				System.out.println("cmd " + msg.getCommand() + "data1 = "
						+ msg.getData1() + " data2 = " + msg.getData2());
		} else if (message instanceof MetaMessage) {
			MetaMessage msg = (MetaMessage) message;
			if (DEBUG)
				System.out.println("cmd type = " + msg.getType() + "data1 = "
						+ msg.getData()[0] + " data2 = " + msg.getData()[1]
						+ " data3 = " + msg.getData()[2]);
		}
	}

	/**
	 * currentTime 以前のMIDIメッセージをひとつ取得し， MIDIデバイスに送信します．
	 * MIDIイベントリストは時刻順にソートされていることが 前提条件です．
	 * @author Mitsuyo Hashida
	 */
	public class ThreadPlayer extends Thread {
		/** ループを回す時間間隔（ミリ秒） */
		private static final int RUNNING_TIME_INTERVAL = 3;

		/** runメソッドのループを継続する判定値 */
		private boolean isRunning = false;

		private LinkedList<NoteEvent> midiEventList;

		/**
		 * @param midiEventLIst
		 */
		public ThreadPlayer(LinkedList<NoteEvent> midiEventLIst) {
			midiEventList = midiEventLIst;
		}

		@Override
		public void run() {
			isRunning = true;

			// ループ直前の現在時刻を取得しておく
			double offsetTime = System.currentTimeMillis();
			if (midiEventList.size() > 0)
				offsetTime -= midiEventList.getFirst().getOnset();

			System.out.println("===== start playing =====");
			if (DEBUG) {
				for (NoteEvent ev : midiEventList) {
					System.out.println(ev);
				}
			}
			while (true) {
				try {
					Thread.sleep(RUNNING_TIME_INTERVAL); // 実行間隔を空ける
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}

				/* 現在時刻を取得 */
				double currentTime = System.currentTimeMillis() - offsetTime;

				/* ループ終了条件 */
				if (!isRunning) {
					// 停止命令が出た
					break;
				}
				if (midiEventList.size() <= 0) {
					// 最後まで再生したら1.5秒後に終了
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("midimessage finished.");
					CMXMusicDataFrame.notifyStopPlaying();
					break;
				}
				if (DEBUG)
					System.out.println("#midi current time = " + currentTime);

				/*
				 * 現在時刻より前にあるMIDIイベントリストの最初のイベントを取得します．<br>
				 * 取得したイベントはMIDIイベントリストから削除されます．
				 */
				ShortMessage msg = (ShortMessage) getMidiMessage(midiEventList,
																	currentTime);
				if (msg == null) {
					if (DEBUG)
						System.err.println("msg data is null at time "
								+ currentTime);
				} else {
					if (msg.getStatus() == ShortMessage.NOTE_ON) {
						soundNotesStatus.put(msg.getData1(), true);
					} else if (msg.getStatus() == ShortMessage.NOTE_OFF) {
						if (!soundNotesStatus.get(msg.getData1()))
							continue;
						soundNotesStatus.put(msg.getData1(), false);
					}
					sendMessage(msg);
					msg = null;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.gui.sound.MIDIEventListener#startPlaying(java.lang.String,
	 *      jp.crestmuse.cmx.gui.sound.MIDIExpressionDataSet,
	 *      jp.crestmuse.cmx.gui.sound.TimeEventMap)
	 */
	public void startPlaying(String smfFilename, MIDIExpressionDataSet mdx,
			TimeEventMap timeEventMap) {
		// 使わない予定．
	}

}
