package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.misc.*;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @since 2007/12/21
 */
class VelocityPanel extends JPanel {

	static final int PANEL_HEIGHT = 130;

	private static final long serialVersionUID = 1L;

	private java.util.List<SimpleNoteList> partlist;

	/**
	 * @param frameWidth
	 */
	public VelocityPanel(int width) {
		setBackground(Color.white);
		setMinimumSize(new Dimension(width, PANEL_HEIGHT));
	}

	/**
	 * @param filewrapper
	 * @param ticksPerBeat
	 * @param pianoroll
	 */
	public void setMusicData(PianoRollCompatible filewrapper, int ticksPerBeat,
			PianoRollPanel pianoroll) {
		try {
			partlist = filewrapper.getPartwiseNoteList(ticksPerBeat);
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Container#paintComponents(java.awt.Graphics)
	 */
	@Override
	public void paintComponents(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		if (partlist != null) {
			for (SimpleNoteList notelist : partlist)
				for (NoteCompatible note : notelist)
					drawVelocity(g2, note, notelist.serial());
		}
	}

	/**
	 * @param g2
	 * @param note
	 * @param i
	 */
	private void drawVelocity(Graphics2D g2, NoteCompatible note, int i) {
		throw new UnsupportedOperationException(); // TODO 実装
	}
}
