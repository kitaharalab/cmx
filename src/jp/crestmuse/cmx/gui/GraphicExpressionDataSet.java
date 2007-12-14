package jp.crestmuse.cmx.gui;

import java.awt.Rectangle;
import java.util.*;

import javax.xml.transform.TransformerException;

import jp.crestmuse.cmx.gui.PianoRollPanel.KeyBoard;
import jp.crestmuse.cmx.misc.*;

class GraphicExpressionDataSet {
  /** ピアノロール上で座標から音符を取得するマップ． */
  private HashMap<NoteExpressionRectangle, NoteCompatible> axisToNoteMap;

  /** ピアノロール上で音符から座標を取得するマップ． */
  private HashMap<NoteCompatible, NoteExpressionRectangle> noteToAxisMap;

  /** 楽譜の描画上における長さ */
  private int musicLength;

  GraphicExpressionDataSet() {
    super();
    axisToNoteMap = new HashMap<NoteExpressionRectangle, NoteCompatible>();
    noteToAxisMap = new HashMap<NoteCompatible, NoteExpressionRectangle>();
  }

  final HashMap<NoteExpressionRectangle, NoteCompatible> getAxisToNoteMap() {
    return axisToNoteMap;
  }

  /**
   * @return musicLength
   */
  int getMusicLength() {
    return musicLength;
  }

  final HashMap<NoteCompatible, NoteExpressionRectangle> getNoteToAxisMap() {
    return noteToAxisMap;
  }

  /**
   * @param note
   * @return
   */
  NoteExpressionRectangle getNoteToAxisMap(NoteCompatible note) {
    return noteToAxisMap.get(note);
  }

  /**
   * 各音符の描画座標を取得する．
   * @param container
   * @param xml
   */
  void setNotePositions(List<SimpleNoteList> partlist, int ticksPerBeat) {
    noteToAxisMap.clear();
    axisToNoteMap.clear();
    musicLength = 0;
    for (SimpleNoteList notelist : partlist) {
      for (NoteCompatible note : notelist) {
        int ypos = PianoRollPanel.KeyBoard.keyHeight * 
          PianoRollPanel.getPitch(note.notenum());
        int onset = PianoRollPanel.getWidthOfNote
          ((double)note.onset(ticksPerBeat) / (double)ticksPerBeat);
        NoteExpressionRectangle rect = 
          new NoteExpressionRectangle(onset, ypos);
        rect.setScoreRectangle(
          new Rectangle(onset, ypos, 
                        PianoRollPanel.getWidthOfNote(
                          (double)note.duration(ticksPerBeat) 
                          / (double)ticksPerBeat), KeyBoard.keyHeight)
        );
        int partLength = 
          PianoRollPanel.getWidthOfNote
          ((double)notelist.lastOffset() / (double)ticksPerBeat);
        if (partLength > musicLength)
          musicLength = partLength;
        noteToAxisMap.put(note, rect);
        axisToNoteMap.put(rect, note);
      }
    }
  }
        
    

/*
	void setNotePositions(MusicXMLWrapper xml,
			final DeviationInstanceWrapper dev) {
		noteToAxisMap.clear();
		axisToNoteMap.clear();
		musicLength = 0;
		try {
			xml.processNotePartwise(new CMXNoteHandler() {

				public void processMusicData(MusicData md,
						MusicXMLWrapper wrapper) {

					super.processMusicData(md, wrapper);

					if (!(md instanceof Note))
						return;

					Note n = (Note) md;

					// Y軸
					int ypos = PianoRollPanel.KeyBoard.keyHeight;
					if (!n.rest())
						ypos *= PianoRollPanel.getPitch(n.notenum());
					else if (pre != null && !pre.rest() && !pre.chord())
						ypos *= PianoRollPanel.getPitch(pre.notenum());
					else
						ypos *= PianoRollPanel.getPitch(60);

					// X軸
					int onset = PianoRollPanel.getWidthOfNote(currentOnset);
					NoteExpressionRectangle rect = new NoteExpressionRectangle(
							onset, ypos);
					rect.setScoreRectangle(new Rectangle(onset, ypos,
							PianoRollPanel.getWidthOfNote(n.duration()
									/ currentDivision), KeyBoard.keyHeight));
					musicLength += onset + rect.getScoreRectangle().width;

					NoteDeviation x = dev.getNoteDeviation(n);
					if (x != null) {
						onset = (int) (currentOnset + x.attack());
						int length = PianoRollPanel.getWidthOfNote(onset
								+ x.release()); // 表情付け処理後の音長

						rect.setPerformanceRectangle(new Rectangle(onset, ypos,
								length, KeyBoard.keyHeight));
						musicLength += onset
								+ rect.getPerformanceRectangle().width;
						if (pre != null && !isFirstNote) {
							int ioi = PianoRollPanel.getWidthOfNote(n.onset()
									+ x.attack() - pre.onset());
							rect.setIOIRectangle(new Rectangle(onset, ypos,
									ioi, KeyBoard.keyHeight));
							musicLength += onset + rect.getIOIRectangle().width;
						}
					}
					noteToAxisMap.put(n, rect);
					axisToNoteMap.put(rect, n);

					super.endNote(n);
				}

			});
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
*/

	class NoteExpressionRectangle {
		private int onset;
		private int yposition;
		private Rectangle score;
		private Rectangle perform;
		private Rectangle ioi;

		/**
		 * @param onset
		 * @param ypos
		 */
		NoteExpressionRectangle(int onset, int ypos) {
			this.setOnset(onset);
			setYposition(ypos);
		}

		Rectangle getIOIRectangle() {
			return ioi;
		}

		/**
		 * @return onset
		 */
		int getOnset() {
			return onset;
		}

		Rectangle getPerformanceRectangle() {
			return perform;
		}

		Rectangle getScoreRectangle() {
			return score;
		}

		/**
		 * @return yposition
		 */
		int getYposition() {
			return yposition;
		}

		void setIOIRectangle(Rectangle r) {
			ioi = r;
		}

		/**
		 * @param onset
		 *        設定する onset
		 */
		void setOnset(int onset) {
			this.onset = onset;
		}

		void setPerformanceRectangle(Rectangle r) {
			perform = r;
		}

		void setScoreRectangle(Rectangle r) {
			score = r;
		}

		/**
		 * @param yposition
		 *        設定する yposition
		 */
		void setYposition(int yposition) {
			this.yposition = yposition;
		}
	}
}
