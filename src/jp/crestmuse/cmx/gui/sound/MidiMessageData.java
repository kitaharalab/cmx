package jp.crestmuse.cmx.gui.sound;

import javax.sound.midi.*;
import java.util.*;

// MIDI message と tick をペアにしたMIDI event を保持するクラスです。
// javax.sound.midi.* にある Sequencer を使うのとどちらが便利なのか分かりませんが、
// あっしは古い人間であるので、一応自作する路線を選びました。
// （だからタイマーも作ったし）
/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class MidiMessageData {
	ArrayList<MidiEvent> messages;

	public MidiMessageData() {
		messages = new ArrayList<MidiEvent>();
	}

	// ↓ CSVデータを読み込むときに、こいつでポンポンMIDI event を追加するつもりです
	public void add(ShortMessage message, long tick) {
		messages.add(new MidiEvent(message, tick));
	}

	public void add(MetaMessage message, long tick) {
		messages.add(new MidiEvent(message, tick));
	}

	// これはあるtick の範囲にあるMIDIイヴェントを返す関数で、
	// MIDI の再生のときに使用します。
	public ArrayList<MidiMessage> getMidiMessage(long tick1, long tick2) {
		int idx = 0;
		ArrayList<MidiMessage> array = new ArrayList<MidiMessage>();
		// こんなことしていて、リアルタイムに動いてくれるか少し不安です。
		// 他で（描画とか）で重たい処理が無ければ取り敢えずはOKかな？？
		while (true) {
			if (idx == messages.size())
				break;
			// どーでも良いことだけど、やっぱこういうときにオブジェクト指向ってどうかと思う
			// ポインタ使えば、一発でアクセスできるところをわざわざメソドを呼び出さなきゃ
			// いけないなんて。
			if (messages.get(idx).getTick() > tick1
					&& messages.get(idx).getTick() <= tick2) {
				array.add(messages.get(idx).getMessage());
			}
			idx++;
		}
		return array;
	}
}
