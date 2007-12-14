/**
 * mitsuyo 2007/11/14
 */
package jp.crestmuse.cmx.gui.sound;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class SoundUtility {
	/**
	 * ミリ秒で表現される値をBPM(Beat per Minute)に変換します。
	 * @param ms
	 * @param bpm
	 * @return
	 */
	static double millisecondToBeatLength(double ms, double bpm) {
		double bt = bpmToBeatTime(bpm);
		return castDouble(1 / (bt / ms));
	}

	static double beatTimeToBPM(double beatTime) {
		return SoundUtility.castDouble((1000. / beatTime) * 60);
	}

	static double bpmToBeatTime(double bpm) {
		return SoundUtility.castDouble(1000. / (bpm / 60.));
	}

	public static double castDouble(double val) {
		int x = (int) (val * 1000. + 0.5);
		return x / 1000.;
	}
}
