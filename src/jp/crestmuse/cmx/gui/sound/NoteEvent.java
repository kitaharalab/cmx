/**
 * mitsuyo 2007/09/28
 */
package jp.crestmuse.cmx.gui.sound;

import javax.sound.midi.MidiMessage;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class NoteEvent {

	private String messageType; // ON or OFF

	private int noteNum;

	private double beatOnset;

	private int velocity;

	private MidiMessage msg;

	private double beat;

	private double length;

	private long onset;

	private int measure;

	/**
	 * @return beat
	 */
	double getBeat() {
		return beat;
	}

	/**
	 * @return measure
	 */
	int getMeasure() {
		return measure;
	}

	NoteEvent(String type, int measure, double b, int num, int vel,
			double length, int baseBeat) {
		this.measure = measure;
		this.beat = SoundUtility.castDouble(b);
		setNoteNum(num);
		this.beatOnset = SoundUtility.castDouble(measure * baseBeat + b);
		this.setLength(length);
		setVelocity(vel);
		setOnset(-1000);
		setMessageType(type);
		System.out.println(this);
	}

	/**
	 * @return beatOnset
	 */
	public final double getBeatOnset() {
		return beatOnset;
	}

	/**
	 * @return messageType
	 */
	public final String getMessageType() {
		return messageType;
	}

	public final MidiMessage getMidiMessage() {
		return msg;
	}

	/**
	 * @return noteNum
	 */
	final int getNoteNum() {
		return noteNum;
	}

	/**
	 * @return velocity
	 */
	public final int getVelocity() {
		return velocity;
	}

	/**
	 * @param messageType
	 *        設定する messageType
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setMidiMessge(MidiMessage msg) {
		this.msg = msg;
	}

	/**
	 * @param noteNum
	 *        設定する noteNum
	 */
	public void setNoteNum(int noteNum) {
		this.noteNum = noteNum;
	}

	/**
	 * @param velocity
	 *        設定する velocity
	 */
	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	@Override
	public String toString() {
		String str = getOnset() + " note " + getNoteNum() + ": "
				+ getMessageType() + " v" + velocity + ", beatOnset "
				+ getBeatOnset() + ", measure " + measure + ", beat " + beat
				+ ", length " + getLength();
		return str;
	}

	/**
	 * @return length
	 */
	double getLength() {
		return length;
	}

	/**
	 * @param length
	 *        設定する length
	 */
	private void setLength(double duration) {
		this.length = duration;
	}

	/**
	 * @param onset
	 *        設定する onset
	 */
	void setOnset(long onset) {
		this.onset = onset;
	}

	/**
	 * @return onset
	 */
	long getOnset() {
		return onset;
	}
}
