package jp.crestmuse.cmx.gui;

import java.util.Properties;

import javax.swing.WindowConstants;

import jp.crestmuse.cmx.commands.CMXCommandForGUI;

public class DeviationInstanceEditor extends CMXCommandForGUI {

	private int ticksPerBeat;
	protected static Properties config;
	private String midiDeviceName;

	@Override
	protected boolean setOptionsLocal(String option, String value) {
		if (option.equals("-division")) {
			ticksPerBeat = Integer.parseInt(value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void preproc() {
		midiDeviceName = System.getProperty("MIDIDEVICE");
	}

	@Override
	protected void run() {
          CMXMusicDataFrame frame = new CMXMusicDataFrame(ticksPerBeat);
          frame.setMIDIDeviceName(midiDeviceName);
//		CMXMusicDataFrame frame = new CMXMusicDataFrame(this);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			DeviationInstanceEditor main = new DeviationInstanceEditor();
			main.start(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

//	public final int getTicksPerBeat() {
//		return ticksPerBeat;
//	}

//	public final String getMidiDeviceName() {
//		return midiDeviceName;
//	}

}
