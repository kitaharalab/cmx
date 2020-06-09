package jp.crestmuse.cmx.misc;

import java.io.File;

import javax.swing.ImageIcon;

class Util {
	public final static String SEPARATOR = ".";

	public static final String TAB = "\t";

/*
	private final static String jpeg = "jpeg";

	private final static String jpg = "jpg";

	private final static String gif = "gif";

	private final static String tiff = "tiff";

	private final static String tif = "tif";

	private final static String png = "png";

	private final static String xml = "xml";

	private final static String deviationXML = "xml";

	private final static String rulemap = "rulemap";

	private final static String sound = "sound";

	private final static String note = "note";

	private final static String mid = "mid";

	private final static String csv = "csv";

	private final static String wav = "wav";

	private final static String mp3 = "mp3";

	private final static String dat = "dat";

	private final static String str = "str";

	private final static String scoreFile = "scr";
*/


	public final static int NULL_INTEGER = -1;

	public static double millisecondToBeatLength(double ms, double bpm) {
		double bt = bpmToBeatTime(bpm);
		return castDouble(1 / (bt / ms));
	}

	public static void main(String[] args) {
		int ms = 50;
		int bpm = 120;
		System.out.println(ms + " ms means " + millisecondToBeatLength(ms, bpm)
				+ " in BPM " + bpm + ".");
	}
	public static String combineFilePath(File path, String filename) {
		return path + "/" + filename;
	}
	public static double beatTimeToBPM(double beatTime) {
		return castDouble((1000. / beatTime) * 60);
	}

	public static double bpmToBeatTime(double bpm) {
		return castDouble(1000. / (bpm / 60.));
	}

	public static double castDouble(double val) {
		int x = (int) (val * 1000. + 0.5);
		return x / 1000.;
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = Util.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		}
		System.err.println("Couldn't find file: " + path);
		return null;
	}

//	public static void doNothing() {
//
//	}

	/**
	 * @return dat
	 */
//	public static String dat() {
//		return dat;
//	}

	/**
	 * @return deviationXML
	 */
//	public static String deviationXML() {
//		return deviationXML;
//	}

	public static void err(String strMessage) {
		System.err.println(strMessage);
	}

	public static void errorAndExit(String strMessage) {
		err(strMessage);
		System.exit(1);
	}

	public static String getNoteName(int noteNumber) {
		String n = "";
		if (noteNumber == 0)
			return "Rest";
		int pitch = noteNumber % 12;
		int octave = noteNumber / 12 - 1;
		switch (pitch) {
		case 0:
			n = "C";
			break;
		case 1:
			n = "C#";
			break;
		case 2:
			n = "D";
			break;
		case 3:
			n = "D#";
			break;
		case 4:
			n = "E";
			break;
		case 5:
			n = "F";
			break;
		case 6:
			n = "F#";
			break;
		case 7:
			n = "G";
			break;
		case 8:
			n = "G#";
			break;
		case 9:
			n = "A";
			break;
		case 10:
			n = "A#";
			break;
		case 11:
			n = "B";
			break;
		}
		return n + octave;
	}

	/**
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {
		String ext = null;
		ext = getExtension(f.getName());
		return ext;
	}

	public static String getExtension(String s) {
		String ext = null;
		int i = s.lastIndexOf(SEPARATOR);
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public static String getNameExceptExtension(String str) {
		return str.replace(SEPARATOR + getExtension(str), "");
	}

	/**
	 * @return gif
	 */
//	public static String gif() {
//		return gif;
//	}

	public static boolean isSmallerThan(double val, double ref) {
		if (val * val > ref * ref)
			return true;
		return false;
	}

	/**
	 * @return jpeg
	 */
//	public static String jpeg() {
//		return jpeg;
//	}

	/**
	 * @return jpg
	 */
//	public static String jpg() {
//		return jpg;
//	}

	/**
	 * @return mid
	 */
//	public static String mid() {
//		return mid;
//	}

	/**
	 * @return mp3
	 */
//	public static String mp3() {
//		return mp3;
//	}

	/**
	 * @return note
	 */
//	public static String note() {
//		return note;
//	}

//	public static void out(String strMessage) {
//		System.out.println(strMessage);
//	}

//	public static void outAndExit(String strMessage) {
//		out(strMessage);
//		System.exit(1);
//	}

//	public static int parseIndex(int idx) {
//		return idx - 1;
//	}

	/**
	 * @return png
	 */
//	public static String png() {
//		return png;
//	}

	/**
	 * @return rulemap
	 */
//	public static String rulemap() {
//		return rulemap;
//	}

	public static String setExtension(String fname, String ext) {
		return fname + SEPARATOR + ext;
	}

	/**
	 * @return sound
	 */
//	public static String sound() {
//		return sound;
//	}

//	public static void swap(int a, int b) {
//		int buf = a;
//		a = b;
//		b = buf;
//	}

	/**
	 * @return tif
	 */
//	public static String tif() {
//		return tif;
//	}

	/**
	 * @return csv
	 */
//	public static String csv() {
//		return csv;
//	}

	/**
	 * @return tiff
	 */
//	public static String tiff() {
//		return tiff;
//	}

	/**
	 * @return wav
	 */
//	public static String wav() {
//		return wav;
//	}

	/**
	 * @return xml
	 */
//	public static String xml() {
//		return xml;
//	}

	/**
	 * @return scoreFile
	 */
//	public static String scoreFile() {
//		return scoreFile;
//	}

	/**
	 * @return str
	 */
//	public static String structure() {
//		return str;
//	}

	public static final String fifthsToString(int fifths) {
		switch (fifths) {
		case 0:
			return "C";
		case 1:
			return "G";
		case 2:
			return "D";
		case 3:
			return "A";
		case 4:
			return "E";
		case 5:
			return "B";
		case 6:
			return "F#";
		case 7:
			return "C#";
		case 8:
			return "G#";
		case 9:
			return "D#";
		case 10:
			return "A#";
		case 11:
			return "E#";
		case -1:
			return "F";
		case -2:
			return "Bb";
		case -3:
			return "Eb";
		case -4:
			return "Ab";
		case -5:
			return "Db";
		case -6:
			return "Gb";
		case -7:
			return "Cb";
		case -8:
			return "Fb";
		case -9:
			return "Bbb";
		case -10:
			return "Ebb";
		case -11:
			return "Abb";
		}
		throw new NumberFormatException();
	}

	public static int getNoteNumber(String noteName) {
		int pitch, acci;
		String oct;
		acci = 0;
		if (noteName.length() == 0 || noteName.equalsIgnoreCase("rest"))
			return 0;
		pitch = getNoteScale(noteName.charAt(0));
		if (noteName.length() >= 3) {
			switch (noteName.charAt(1)) {
			case 'S':
				acci = 2;
				break;
			case '#':
				acci = 1;
				break;
			case 'b':
				acci = -1;
				break;
			case 'B':
				acci = -2;
				break;
			}
		}
		oct = noteName.substring(noteName.length() - 1);
		return (Integer.parseInt(oct) + 1) * 12 + (pitch + acci);
	}

	public static int getNoteScale(char note) {
		int n = 0;
		switch (note) {
		case 'C':
			n = 0;
			break;
		case 'D':
			n = 2;
			break;
		case 'E':
			n = 4;
			break;
		case 'F':
			n = 5;
			break;
		case 'G':
			n = 7;
			break;
		case 'A':
			n = 9;
			break;
		case 'B':
			n = 11;
			break;
		}
		return n;
	}
}
