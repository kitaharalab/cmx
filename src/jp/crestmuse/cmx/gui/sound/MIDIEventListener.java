package jp.crestmuse.cmx.gui.sound;

/**
 * MIDIイベントを受け取るためのリスナーインタフェースです．
 * <p>
 * MIDIイベントに関連するクラスは、このインタフェースを実装します。さらに、それらのクラスによって作成されたオブジェクトは、コンポーネントの
 * addMIDIEventListener メソッドを使用することによってコンポーネントに登録されます．
 * イベントが発生すると、イベント内容に合わせて，オブジェクトの editConfig メソッドが呼び出されます。
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @since 2007.9.20 (beta version)
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public interface MIDIEventListener {
	public void stopPlaying();

	public void startPlaying(String smfFilename, MIDIExpressionDataSet mdx,
			TimeEventMap timeEventMap);
}
