package jp.crestmuse.cmx.gui.sound;

import java.util.*;

import javax.sound.midi.*;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class MIDIExpressionDataSet {
	/** 分解能 */
	public static int TICKSPERBEAT = 480;

	/** NOTE ONの直後にOFFが来るのを避ける時間範囲 (ミリ秒) */
	private static int ON_OFF_RESOLUTION_TIME = 3; // milliseconds

	/** MIDIイベントが密集するのを避ける時間範囲（ミリ秒) */
	private static int EVENT_RESOLUTION_TIME = 3; // milliseconds

	/** オフヴェロシティ値 */
	private static int OFF_VELOCITY = 127;

	/** デバッグモード */
	private static boolean DEBUG = false;

	/** ある時刻上に存在するMIDIイベントリスト */
	private static LinkedList<NoteEvent> midiEventList;

	/** ある時刻上に存在する音符リスト */
	private TimeEventMap timeEventMap;

	/** 基準拍数（1小節内の拍子） */
	private int baseBeat = 4;

	/** 基準テンポ (beat per minute) */
	private double baseBPM = 120;

	/** 基準テンポ (beat time) */
	private long baseBeatTime = 500;

	/** 楽曲データの全長(時間) */
	private double maxLength = 0;

	/** ある時刻上に存在するテンポリスト */
	private TreeMap<Double, List<Long>> tempoEventMap;

	/**   */
	private TreeMap<Integer, Long> soundNotesStatus;

	/** 演奏開始時刻 */
	private double selectTimeFrom = 0.;
	/** 演奏終了時刻 */
	private double selectTimeEnd = 0.;

	/** 演奏データの総演奏時間長 */
	private double performanceLength = 0.;

	public static MIDIExpressionDataSet createMIDIExpressionDataSet() {
		MIDIExpressionDataSet mdx = new MIDIExpressionDataSet();
		return mdx;
	}

	private MIDIExpressionDataSet() {
		super();
		timeEventMap = new TimeEventMap();
		tempoEventMap = new TreeMap<Double, List<Long>>();
		midiEventList = new LinkedList<NoteEvent>();
		soundNotesStatus = new TreeMap<Integer, Long>();
	}

	void addTempoEventMap(int measure, double beat, long tempo) {
		int m = measure - 1;
		double b = (long) ((beat - 1.) * 1000);
		double onset = (m * baseBeat + b);
		List<Long> list = (tempoEventMap.containsKey(onset)) ? tempoEventMap
				.get(onset) : new ArrayList<Long>();
		list.add(tempo);
		tempoEventMap.put(onset, list);
	}

	/**
	 * サンプルの音列を生成します．
	 * @param console
	 */
	void createSampleNotes() {
		/*
		 * =======================================================================
		 * テスト音符作成
		 * =======================================================================
		 */
		timeEventMap.clear();
		tempoEventMap.clear();
		addNoteToTimeEventMap("ON", 1, 1.0, 60, 50, 0.5); // 小節，拍，MIDIノートナンバー，音長（1拍1.0）
		addNoteToTimeEventMap("ON", 1, 1.6, 62, 60, 0.5);
		addNoteToTimeEventMap("ON", 1, 2.0, 64, 70, 0.5);
		addNoteToTimeEventMap("ON", 1, 2.5, 65, 80, 0.5);
		addNoteToTimeEventMap("ON", 1, 3.0, 67, 90, 1.0);
		addNoteToTimeEventMap("ON", 1, 4.0, 74, 64, 1.0);
		addNoteToTimeEventMap("ON", 2, 1.0, 72, 64, 1.0);
		addTempoEventMap(1, 1.0, 400);
		addTempoEventMap(1, 2.0, 600);
		addTempoEventMap(1, 3.0, 400);
		addTempoEventMap(1, 4.0, 600);
		addTempoEventMap(1, 5.0, 400);
		addNoteOffEvents();
		System.out.println(timeEventMap.toString());
		System.out.println("Time event map: (maxlength: " + maxLength + ")");
		setDeltaTimes();
	}

	final int getBaseBeat() {
		return baseBeat;
	}

	final double getBaseBeatTime() {
		return baseBeatTime;
	}

	final double getBPM() {
		return baseBPM;
	}

	final double getMaxLength() {
		return maxLength;
	}

	final List<NoteEvent> getMidiEventList() {
		return midiEventList;
	}

	/**
	 * @param preTime
	 * @param currentTime
	 * @return
	 */
	final List<MidiMessage> getMidiMessageList(double preTime,
			double currentTime) {
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		for (NoteEvent ev : midiEventList) {
			if (ev.getOnset() >= currentTime)
				break;
			if (ev.getOnset() < preTime)
				continue;
			list.add(ev.getMidiMessage());
		}

		return list;
	}

	// final double getSelectTimeEnd() {
	// return selectTimeEnd;
	// }

	/**
	 * 楽曲の演奏時間長を取得します。
	 * @return performanceLength
	 */
	double getPerformanceLength() {
		return performanceLength;
	}

	TimeEventMap getTimeEventMap() {
		return timeEventMap;
	}

	/**
	 * @param onsetFrom
	 * @param onsetTo
	 * @return
	 */
	public TimeEventMap getTimeEventMapIn(double onsetFrom, double onsetTo) {
		TimeEventMap map = (TimeEventMap) timeEventMap.subMap(onsetFrom,
																onsetTo);
		TimeEventMap tmap = new TimeEventMap();
		for (double onset : map.keySet()) {
			tmap.put(onset - onsetFrom, map.get(onset));
		}
		return tmap;
	}

	void resetSelectingGroup() {
		selectTimeFrom = 0.;
		selectTimeEnd = getPerformanceLength();
	}

	void setBaseBeat(int baseBeat, int beatType) {
		this.baseBeat = (int) (baseBeat / (beatType / 4.));
	}

	/**
	 * 基準テンポ(beat time)を設定します．このメソッドを実行すると，基準テンポ(BPM)も同時に計算されます．
	 * @param beatTime
	 *        設定する beat time
	 */
	void setBaseBeatTime(double beatTime) {
		this.baseBeatTime = (int) beatTime;
		baseBPM = SoundUtility.beatTimeToBPM(beatTime);
	}

	/**
	 * 基準テンポ(beat per minute)を設定します．このメソッドを実行すると，基準テンポ(beat time)も同時に計算されます．
	 * @param bpm
	 *        設定する BPM
	 */
	void setBPM(double bpm) {
		this.baseBPM = bpm;
		baseBeatTime = (int) SoundUtility.bpmToBeatTime(bpm);
	}

	/**
	 * 楽曲のテンポマップを設定します．
	 */
	void setDefaultTempo() {
		tempoEventMap.clear();
		int len = (int) maxLength;
		for (int i = 0; i < len; i++) {
			List<Long> list = new ArrayList<Long>();
			list.add(baseBeatTime);
			tempoEventMap.put((double) i, list);
		}
	}

	/**
	 * 時刻順にソートされたノートイベントをMIDIイベントリストとしてセットします． <br>
	 * @param timeEventMap
	 */
	synchronized void setMIDIEventList(TimeEventMap timeEventMap) {
		midiEventList.clear();
		if (DEBUG)
			System.err.println("==== Set MIDI Event list ====");
		for (double t : timeEventMap.keySet()) {
			if (t < selectTimeFrom || t > selectTimeEnd)
				continue;
			for (NoteEvent ev : timeEventMap.get(t)) {
				// MIDIイベントの作成
				if (DEBUG)
					System.out.println(ev);

				int status = 0;
				if (ev.getMessageType().equals("ON")) {
					status = ShortMessage.NOTE_ON;

					soundNotesStatus.put(ev.getNoteNum(), ev.getOnset());

				} else if (ev.getMessageType().equals("OFF")) {
					status = ShortMessage.NOTE_OFF;
					if (soundNotesStatus.containsKey(ev.getNoteNum())) {
						soundNotesStatus.remove(ev.getNoteNum());
					}
				} else {
					System.err.println("??? status=" + status);
				}

				ShortMessage msg = new ShortMessage();
				try {
					msg.setMessage(status, ev.getNoteNum(), ev.getVelocity());
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}

				// 時刻ソートされたイベント配列へ登録
				ev.setMidiMessge(msg);
				midiEventList.add(ev);

				if (DEBUG)
					System.err.println("   ontime " + ev.getOnset() + " note "
							+ msg.getData1() + " " + ev.getMessageType()
							+ " length " + ev.getLength());
			}
		}
		expandMIDIEvents();
		if (DEBUG)
			System.err.println("==== (end) MIDI Event list ====");
	}
/*
	void sortEventsByTime(CMXMusicData data)
			throws ParserConfigurationException, SAXException,
			TransformerException, IOException {
		// if (data.getDeviationXML() == null) {
		data.createDeviationDocuments(data.getInputFileName());

		data.setDefaultExpression();
		// }
		data.setNoteExpressions();
		data.finalizeDocument();
		SCCXMLWrapper scc = data.outputToSCCXML(TICKSPERBEAT);
		MIDIXMLWrapper smf = data.outputToMIDIXML(scc);
		data.outputToSMF(smf);
		data.outputToNoteFile();

		setTimeEventMap(data, data.getNoteExpressionMap());

	}
*/
	private synchronized void addNoteOffEvents() {
		TimeEventMap tmp = new TimeEventMap();
		for (double key : timeEventMap.keySet()) {
			for (NoteEvent ev : timeEventMap.get(key)) {
				if (ev.getMessageType().equals("OFF"))
					continue;
				double offsetTime = SoundUtility.castDouble(ev.getBeatOnset()
						+ ev.getLength());
				double b = offsetTime - ev.getMeasure();
				if (timeEventMap.containsKey(offsetTime)) {
					List<NoteEvent> list = timeEventMap.get(offsetTime);
					list.add(0, new NoteEvent("OFF", ev.getMeasure(), b, ev
							.getNoteNum(), OFF_VELOCITY, 0., getBaseBeat()));
				} else if (tmp.containsKey(offsetTime)) {
					List<NoteEvent> list = tmp.get(offsetTime);
					list.add(0, new NoteEvent("OFF", ev.getMeasure(), b, ev
							.getNoteNum(), OFF_VELOCITY, 0., getBaseBeat()));
				} else {
					LinkedList<NoteEvent> list = new LinkedList<NoteEvent>();
					list.add(0, new NoteEvent("OFF", ev.getMeasure(), b, ev
							.getNoteNum(), OFF_VELOCITY, 0., getBaseBeat()));
					tmp.put(offsetTime, list);
				}
				if (maxLength < offsetTime)
					maxLength = offsetTime;
			}
		}
		timeEventMap.putAll(tmp);
	}

	/**
	 * @param x
	 * @param m
	 * @param notenum
	 */
	/*private void addNoteToTimeEventMap(MusicExpression x, final int m,
			int notenum) {
		double onset = x.onset() - (m - 1) * baseBeat + 1;
		addNoteToTimeEventMap("ON", m, onset, notenum, (int) x.velocity(), x
				.length());
	}*/

	private synchronized void addNoteToTimeEventMap(String status, int measure,
			double beat, int notenum, int vel, double length) {
		int m = measure - 1; // ゼロ始まりにする
		double b = SoundUtility.castDouble(beat - 1); // ゼロ始まりにする
		double onset = m * baseBeat + b;
		LinkedList<NoteEvent> list = (timeEventMap.containsKey(onset))
				? timeEventMap.get(onset)
				: new LinkedList<NoteEvent>();
		list.add(new NoteEvent(status, m, b, notenum, vel, length,
				getBaseBeat()));
		timeEventMap.put(onset, list);
		if (maxLength < onset + length) {
			maxLength = onset + length;
		}
	}

	/**
	 * MIDIイベントの発行時刻が密集していた場合，TIME_REVOLUTION分の間隔をあける
	 */
	private synchronized void expandMIDIEvents() {
		if (DEBUG)
			System.out.println("======== expandMIDIEvents ============");
		NoteEvent pre = null;
		Iterator<NoteEvent> it = midiEventList.iterator();
		while (it.hasNext()) {
			NoteEvent ev = it.next();
			if (pre != null
					&& ev.getOnset() - pre.getOnset() < EVENT_RESOLUTION_TIME) {
				ev.setOnset(pre.getOnset() + EVENT_RESOLUTION_TIME);
			}
			if (DEBUG)
				System.out.println("time=" + ev.getOnset() + " note "
						+ ev.getNoteNum() + " " + ev.getMessageType()
						+ " onset=" + ev.getOnset() + " length="
						+ ev.getLength());
			pre = ev;
		}
		if (DEBUG)
			System.out.println("======== (end) expandMIDIEvents ============");

	}

	/**
	 * @param startNote
	 * @return
	 */
	private <N> double getNoteEventOnset(N note) {
		if (note instanceof MusicXMLWrapper.Note) {
			MusicXMLWrapper.Note n = (Note) note;
			for (Double t : timeEventMap.keySet()) {
				for (NoteEvent ev : timeEventMap.get(t)) {
					if (n.measure().number() == ev.getMeasure() + 1
							&& n.beat() == ev.getBeat() + 1
							&& n.notenum() == ev.getNoteNum()) {
						return ev.getBeatOnset();
					}
				}
			}
			return -1;
		}
		throw new UnsupportedClassVersionError("note is an instance of "
				+ note.getClass().getName());
	}

	private NoteEvent getNoteOffEvent(NoteEvent onEvent, double from, int res) {
		for (double t : timeEventMap.keySet()) {
			if (t < from)
				continue;
			if (t > from + SoundUtility.millisecondToBeatLength(res, getBPM()))
				break;
			for (NoteEvent ev : timeEventMap.get(t)) {
				if (ev.getMessageType().equals("ON"))
					continue;

				long diff = ev.getOnset() - onEvent.getOnset();
				if (ev.getNoteNum() == onEvent.getNoteNum() && diff > -res
						&& diff < res) {
					return ev;
				}
			}
		}
		return null;
	}

	private synchronized void setDeltaTimes() {
		int beatLength = (int) maxLength;
		int beatsum = 0;
		// selectTimeFrom = selectTimeEnd = 0;// 現在の累積時刻
		if (DEBUG)
			System.out.println("======== setDeltaTimes ============");
		for (int currentBeat = timeEventMap.firstKey().intValue(); currentBeat < beatLength; currentBeat++) {
			int tempo = (int) getBaseBeatTime(); // TODO tempoは常に曲冒頭の値
			for (double t : timeEventMap.keySet()) {
				if (t < currentBeat)
					continue;
				if (t >= currentBeat + 1)
					break;
				List<NoteEvent> list = timeEventMap.get(t);
				for (NoteEvent ev : list) {
					if (ev.getOnset() >= 0)
						continue;

					ev.setOnset((long) (beatsum + tempo * (t - currentBeat)));
					// onset が積分値
					// tempo をいずれ次の(予測)拍に変える

					if (DEBUG)
						System.out.println("mstime=" + ev.getOnset() + " note "
								+ ev.getNoteNum() + " " + ev.getMessageType()
								+ " onset=" + ev.getOnset() + " length="
								+ ev.getLength());
				}
			}
			setPerformanceLength(selectTimeEnd = beatsum += tempo);
		}
		/*
		 * 最後のOFFイベント TODO 上のループのこぴぺ
		 */
		if (timeEventMap.containsKey((double) beatLength)) {
			for (NoteEvent ev : timeEventMap.get((double) beatLength)) {
				if (ev.getOnset() >= 0)
					continue;
				ev.setOnset(beatsum);
				// onset が積分値
				// tempomap[i] が次の(予測)拍に変わる
				if (DEBUG)
					System.out.println("time=" + ev.getOnset() + " note "
							+ ev.getNoteNum() + " " + ev.getMessageType()
							+ " onset=" + ev.getOnset() + " length="
							+ ev.getLength());
			}
		}
		if (DEBUG)
			System.out.println("======== (end) setDeltaTimes ============");

	}

	/**
	 * @param performanceLength
	 *        設定する performanceLength
	 */
	private void setPerformanceLength(double performanceLength) {
		this.performanceLength = performanceLength;
	}

	/*
	private void setTimeEventMap(CMXMusicData data,
			NoteExpressionMap noteExpressionMap) {
		timeEventMap.clear();
		for (Object obj : noteExpressionMap.keySet()) {
			// Note n = (Note) obj;
			MusicExpression x = (MusicExpression) noteExpressionMap.get(obj);
			// if (!n.rest())
			// addNoteToTimeEventMap(x, n.measure().number(), n.notenum());

			MusicXMLWrapper.Note note = ((MusicXMLWrapper.Note) obj);
			if (!note.rest())
				addNoteToTimeEventMap(x, note.measure().number(), note
						.notenum());
		}
		addNoteOffEvents();
		System.out.println("Time event map: (maxlength: " + maxLength + ")");
		setDefaultTempo();
		setDeltaTimes();
		switchOFFEvents();
	}
*/
	private synchronized void switchOFFEvents() {
		if (DEBUG)
			System.out.println("======== switchOFFEvents ============");
		for (double t : timeEventMap.keySet()) {
			if (t < selectTimeFrom || t > selectTimeEnd)
				continue;
			for (NoteEvent ev : timeEventMap.get(t)) {
				if (!ev.getMessageType().equals("ON"))
					continue;
				while (true) {
					NoteEvent futureOffEvent = getNoteOffEvent(ev, t,
																ON_OFF_RESOLUTION_TIME);
					if (futureOffEvent == null)
						break;

					futureOffEvent.setOnset(ev.getOnset()
							- ON_OFF_RESOLUTION_TIME);
				}
			}
		}
		if (DEBUG) {
			for (double t : timeEventMap.keySet()) {
				for (NoteEvent ev : timeEventMap.get(t)) {
					System.out.println("time=" + ev.getOnset() + " note "
							+ ev.getNoteNum() + " " + ev.getMessageType()
							+ " onset=" + ev.getOnset() + " length="
							+ ev.getLength());
				}
			}
			System.out.println("======== (end) switchOFFEvents ============");
		}
	}

}
