package jp.crestmuse.cmx.gui;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.*;
import jp.crestmuse.cmx.handlers.NoteHandlerPartwise;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @since Nov. 12, 2007
 */
public class CMXNoteHandler implements NoteHandlerPartwise {
	protected double currentOnset = 0;
	protected int partId = 0;
	protected double currentDivision = 1.;
	protected boolean isFirstNote = true;
	protected boolean isFirstNoteOfMeasure = true;
	protected Note pre = null;

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.handlers.NoteHandlerPartwise#beginMeasure(jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure,
	 *      jp.crestmuse.cmx.filewrappers.MusicXMLWrapper)
	 */
	public void beginMeasure(Measure measure, MusicXMLWrapper wrapper) {
		isFirstNoteOfMeasure = true;
	}

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.handlers.NoteHandlerPartwise#beginPart(jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part,
	 *      jp.crestmuse.cmx.filewrappers.MusicXMLWrapper)
	 */
	public void beginPart(Part part, MusicXMLWrapper wrapper) {
		partId++;
		currentOnset = 0;
		isFirstNote = true;
		pre = null;
	}

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.handlers.NoteHandlerPartwise#endMeasure(jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Measure,
	 *      jp.crestmuse.cmx.filewrappers.MusicXMLWrapper)
	 */
	public void endMeasure(Measure measure, MusicXMLWrapper wrapper) {}

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.handlers.NoteHandlerPartwise#endPart(jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Part,
	 *      jp.crestmuse.cmx.filewrappers.MusicXMLWrapper)
	 */
	public void endPart(Part part, MusicXMLWrapper wrapper) {}

	/*
	 * (non-Javadoc)
	 * @see jp.crestmuse.cmx.handlers.NoteHandlerPartwise#processMusicData(jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.MusicData,
	 *      jp.crestmuse.cmx.filewrappers.MusicXMLWrapper)
	 */
	public void processMusicData(MusicData md, MusicXMLWrapper wrapper) {
		if (md instanceof Attributes) {
			Attributes attr = (Attributes) md;
			currentDivision = attr.divisions();
		} else if (md instanceof Backup) {
			currentOnset -= md.duration() / currentDivision;
		} else if (md instanceof Forward) {
			currentOnset += md.duration() / currentDivision;
		} else if (md instanceof Note) {
			Note n = (Note) md;
			if (pre != null && !n.chord())
				currentOnset += pre.duration() / currentDivision;
		}
	}

	protected void endNote(Note cur) {
		if (isFirstNote)
			isFirstNote = false;
		if (isFirstNoteOfMeasure)
			isFirstNoteOfMeasure = false;
		if (cur.chord())
			return;
		this.pre = cur;
	}
}
