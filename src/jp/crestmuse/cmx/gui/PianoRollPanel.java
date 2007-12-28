package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.gui.GraphicExpressionDataSet.NoteExpressionRectangle;
import jp.crestmuse.cmx.misc.*;

/**
 * ピアノロールを描画するパネルオブジェクトです．
 * @author Mitsuyo Hashida
 * @since 2007.2
 */
public class PianoRollPanel extends JScrollPane implements MouseListener,
		MouseMotionListener {
	private static final long serialVersionUID = 1L;

	/** 四分音符あたりの横幅[pixel] */
	private final static double beatPixel = 30.;

	/** 鍵盤を描画するパネル */
	private static KeyBoard keyboard;

	/** ピアノロール部を描画するパネル */
	private PianoRoll pianoroll;

	/** ベロシティを描画するパネル */
	// private VelocityPanel velocityPanel;
	private PianoRollCompatible filewrapper = null;
	private ArrayList<SimpleNoteList> partlist = null;

	/** MusicXMLオブジェクト */
	// private MusicXMLWrapper xml;
	/** DeviationInstanceXMLオブジェクト */
	// private DeviationInstanceWrapper dev;
	/** TODO（実装中）押されている鍵盤のノート番号リスト（ソート済） */
	private TreeSet<Integer> noteNumberOfPushedKey;

	/** TODO（実装中）ピアノロール描画専用の楽曲データ形式 */
	private GraphicExpressionDataSet gxpr;

	/** TODO（実装中）選択中の音符オブジェクト */
	private ArrayList<NoteCompatible> selectedNoteList;

	/** TODO（実装中）マウスの選択範囲 */
	private Rectangle selectedMouseBox;

	private Point currentMousePoint;

	private boolean isControlMask;

	public static int getPitch(int notenumber) {
		return keyboard.getPitch(notenumber);
	}

	// public static void main(String[] args) {
	// CMXMusicDataFrame.main(args);
	// }

	/**
	 * 発音時刻や音長に対する横軸の長さを求めます．
	 * @param val
	 * @return
	 */
	static int getWidthOfNote(final double val) {
		return (int) (val * beatPixel);
	}

	/**
	 * コンストラクタ
	 * @param height
	 * @param width
	 */
	public PianoRollPanel(int width, int height) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setMinimumSize(new Dimension(width, height));
		setOpaque(true);
		setAutoscrolls(true);
		
		currentMousePoint = new Point();
		selectedMouseBox = new Rectangle();

		gxpr = new GraphicExpressionDataSet();		// 実装中
		selectedNoteList = new ArrayList<NoteCompatible>();		// 実装中
		// playingScheduler = new PianoRollScheduler();		// 実装中
		noteNumberOfPushedKey = new TreeSet<Integer>();		// 実装中
		
		/* 各描画用オブジェクトを生成 */
		pianoroll = new PianoRoll();
		keyboard = new KeyBoard();
		setViewportView(pianoroll);
		setRowHeaderView(keyboard);
		pianoroll.addMouseListener(this);
		pianoroll.addMouseMotionListener(this);
	}

	/**
	 * @return selectedNoteList
	 */
	public final ArrayList<NoteCompatible> getSelectedNoteList() {
		return selectedNoteList;
	}

	/**
	 * @return
	 */
	public boolean hasSelectedNotelist() {
		return selectedNoteList.size() > 0;
	}

	/**
	 * @return isMouseSelectBoxDraw
	 */
	public final boolean isMouseSelectBoxDraw() {
		return pianoroll.isMouseSelectBoxDraw;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		currentMousePoint = e.getPoint();
		setMouseBoxEnd(e.getPoint());
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		if (e.getSource().equals(pianoroll)) {
			pianoroll.setVisibleMouseAxis(true);
			currentMousePoint = e.getPoint();
			System.out.println("currentMousePoint ON");
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		if (e.getSource().equals(pianoroll)) {
			pianoroll.setVisibleMouseAxis(false);
			System.out.println("currentMousePoint OFF");
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		currentMousePoint = e.getPoint();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		isControlMask = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
				? true
				: false;
		System.out.println("control mask:" + isControlMask);
		memoryMouseAxis(e.getPoint());
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != MouseEvent.CTRL_DOWN_MASK)
			isControlMask = false;
		System.out.println("control mask:" + isControlMask);
		pianoroll.isMouseSelectBoxDraw = false;
		repaint();
	}

	void closeMusicData() {
		filewrapper = null;
		partlist = null;
		// if (xml != null) {
		// xml = null;
		// }
		noteNumberOfPushedKey.clear();
	}

	/**
	 * @param point
	 */
	void endSelectMouseBox(Point point) {
		setMouseBoxEnd(point);
		pianoroll.isMouseSelectBoxDraw = false;
	}

	/**
	 * マウスの選択範囲に入った音符を取得する
	 */
	void extractNotes() {
		if (filewrapper == null)
			return;
		selectedNoteList.clear();

		HashMap<NoteExpressionRectangle, NoteCompatible> map = gxpr
				.getAxisToNoteMap();
		int selX = selectedMouseBox.x - pianoroll.axisX;
		int selW = selX + selectedMouseBox.width;
		int selY = selectedMouseBox.y;
		int selH = selY + selectedMouseBox.height;
		for (NoteExpressionRectangle obj : map.keySet()) {
			Rectangle r = obj.getPerformanceRectangle();
			boolean exist = false;
			if (selX - obj.getOnset() < r.getWidth() && selW > obj.getOnset()
					&& selY - obj.getYposition() < r.getHeight()
					&& selH > obj.getYposition())
				exist = true;

			if (exist) {
				NoteCompatible n = map.get(obj);
				selectedNoteList.add(n);
			}
		}

	}

	/**
	 * @return
	 */
	GraphicExpressionDataSet getGraphicExpressionDataSet() {
		return gxpr;
	}

	/**
	 * @return keyboard
	 */
	KeyBoard getKeyboard() {
		return keyboard;
	}

	/**
	 * @return pianoroll
	 */
	PianoRoll getPianoroll() {
		return pianoroll;
	}

	/**
	 * @param point
	 */
	void memoryMouseAxis(Point point) {
		pianoroll.mouseEndPoint = pianoroll.mouseStartPoint = point;
		pianoroll.isMouseSelectBoxDraw = true;
	}

	/**
	 * @param point
	 */
	void setMouseBoxEnd(Point point) {
		pianoroll.mouseEndPoint = point;
	}

	/**
	 * @param xml
	 * @param dev
	 */

	void setMusicData(PianoRollCompatible filewrapper, int ticksPerBeat)
			throws TransformerException, IOException, SAXException,
			ParserConfigurationException {
		this.filewrapper = filewrapper;
		partlist = filewrapper.getPartwiseNoteList(ticksPerBeat);
		gxpr.setNotePositions(partlist, ticksPerBeat);
		setPreferredSize(new Dimension(gxpr.getMusicLength(),240));
		revalidate();
		// this.filewrapper = filewrapper;
		// gxpr.setNotePositions(filewrapper, ticksPerBeat);
		keyboard.setKeyRegister();
		System.out.println("pianoroll preferred size: "
				+ pianoroll.getPreferredSize());
		repaint();
	}

	/*
	 * void setMusicData(MusicXMLWrapper xml, DeviationInstanceWrapper dev) {
	 * this.xml = xml; this.dev = dev; gxpr.setNotePositions(xml, dev);
	 * keyboard.setKeyRegister(); frameWidth = gxpr.getMusicLength(); //
	 * pianoroll.setPreferredSize(new Dimension(frameWidth, frameHeight));
	 * scrollPane
	 * .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	 * System.out.println("pianoroll preferred size: " +
	 * pianoroll.getPreferredSize()); repaint(); }
	 */

	/**
	 * @param voice
	 * @return
	 */
	protected final Color setPartColor(int voice) {
		int partSize = partlist.size();
		// int partSize = xml.getPartList().length;
		return new Color((int) (255. * (voice / partSize)),
				(int) (255. * (voice / 2 / partSize)),
				(int) (255. * ((partSize - voice) / partSize)));
	}

	public class KeyBoard extends JPanel {
		private static final long serialVersionUID = 1L;

		/** キーボード一音の描画の縦幅[pixel] */
		final static int keyHeight = 7;

		/** キーボード一音の描画の横幅[pixel] */
		final int keyWidth = 65;

		/** 音域（描画する鍵盤の数） */
		int keyRegister;

		/** 最大音域数 */
		private int maximumKeyRegister = 88;

		/** 最低音 */
		private int bottomNoteNumber = 2000;

		/** 最高音 */
		private int topNoteNumber = 0;

		/** Y座標に対応するノートナンバーのマップ */
		private TreeMap<Integer, Integer> keyboardMap;

		/** 白鍵リスト */
		private ArrayList<Integer> whiteMidiKey;

		KeyBoard() throws NullPointerException {
			super(true);
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(keyWidth, keyRegister));
			createWhiteMidiKey();
			setKeyRegister();
			keyboardMap = new TreeMap<Integer, Integer>();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(final Graphics g) {
			/* おまじない */
			final Graphics2D g2 = (Graphics2D) g;
			super.paintComponent(g2);
			final RenderingHints qualityHints = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING,
												RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHints(qualityHints);

			/* キーボード表示 */
			drawKeyboard(g2);
		}

		int addSelectedKeyList(int y) {
			int key = getKeyPosition(y);
			noteNumberOfPushedKey.add(key);
			repaint();
			return key;
		}

		/**
		 * キーボードを薄く描画します．
		 * @param g
		 */
		void drawBackgroundKeyboard(final Graphics2D g) {
			int curHeight = 0;
			for (int i = 0; i < maximumKeyRegister; i++) {
				if (whiteMidiKey.contains(getPitch(i) % 12)) {
					// 白鍵
					g.setColor(Color.lightGray);
					g.drawLine(0, curHeight, 5000, curHeight);
				} else {
					// 黒鍵
					g.setColor(Color.getHSBColor((float) 0.5, (float) 0., (float) 0.9));
					g.fillRect(0, curHeight, 5000, keyHeight);
				}
				curHeight += keyHeight;
			}
		}

		/**
		 * @param notenumber
		 * @return
		 */
		int getPitch(int notenumber) {
			return topNoteNumber - notenumber + 5;
		}

		void removeSelectedKeyList(int key) {
			if (noteNumberOfPushedKey.contains(key)) {
				noteNumberOfPushedKey.remove(key);
			}
			repaint();
		}

		/**
		 * 白鍵のピッチクラスをインスタンス化します．
		 */
		private void createWhiteMidiKey() {
			whiteMidiKey = new ArrayList<Integer>();
			whiteMidiKey.add(0);
			whiteMidiKey.add(2);
			whiteMidiKey.add(4);
			whiteMidiKey.add(5);
			whiteMidiKey.add(7);
			whiteMidiKey.add(9);
			whiteMidiKey.add(11);
		}

		/**
		 * キーボードを描画します．
		 */
		private void drawKeyboard(final Graphics2D g) {
			int curHeight = 0;
			keyboardMap.clear();
			for (int i = 0; i < maximumKeyRegister; i++) {
				final int pitch = getPitch(i);
				// 鍵盤座標を取得
				keyboardMap.put(curHeight, pitch);
				if (whiteMidiKey.contains(pitch % 12)) {
					// 白鍵
					g.setColor(Color.black);
					g.draw3DRect(getX(), curHeight, keyWidth, keyHeight, true);
				} else {
					// 黒鍵
					g.setColor(Color.black);
					g.fill3DRect(getX(), curHeight, keyWidth, keyHeight, true);
				}
				g.setColor(Color.green);
				g.drawString(String.valueOf(pitch), keyWidth - 20, curHeight
						+ keyHeight);
				curHeight += keyHeight;
			}
			// 今なっている音を色づけ
			g.setColor(Color.yellow);
			for (int key : noteNumberOfPushedKey) {
				g.fillRect(getX(), getPitch(key) * keyHeight, keyWidth, keyHeight);
			}
		}

		/** 入力Y座標から鍵盤の位置（音高）を取得します） */
		private int getKeyPosition(int y) {
			for (int key : keyboardMap.keySet()) {
				if (key > y)
					return keyboardMap.get(key) + 1;
			}
			return -1;
		}

		private void setKeyRegister() {
			if (filewrapper == null) {
				topNoteNumber = 84;
				bottomNoteNumber = 22;
			} else {
				topNoteNumber = 0;
				bottomNoteNumber = 1000;
				for (SimpleNoteList notelist : partlist) {
					if (notelist.topNoteNum() > topNoteNumber)
						topNoteNumber = notelist.topNoteNum();
					if (notelist.bottomNoteNum() < bottomNoteNumber)
						bottomNoteNumber = notelist.bottomNoteNum();
				}
			}
			keyRegister = topNoteNumber - bottomNoteNumber + 12;
		}
	}

	/**
	 * ピアノロール（音符部分）を描画するクラスオブジェクトです．
	 * @author Mitsuyo Hashida
	 */
	class PianoRoll extends JPanel {
		private static final long serialVersionUID = 1L;

		private final int axisX = 10;
		private boolean isMouseSelectBoxDraw;
		private Point mouseStartPoint;
		private Point mouseEndPoint;
		private boolean mouseVisible;

		PianoRoll() throws NullPointerException {
			super(true);
			setBackground(Color.WHITE);
		}

		void setVisibleMouseAxis(boolean b) {
			mouseVisible = b;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(final Graphics g) {
			/* おまじない */
			final Graphics2D g2 = (Graphics2D) g;
			super.paintComponent(g2);
			final RenderingHints qualityHints = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING,
												RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHints(qualityHints);

			/* 背景の塗り絵 */
			getKeyboard().drawBackgroundKeyboard(g2);

			/* （オプション）座標軸を左の鍵盤パネルから少し右にずらす */
			g2.translate(axisX, 0);

			/* 楽譜が読み込まれてないなら以下の描画は不要 */
			if (filewrapper != null && partlist != null) {
				for (SimpleNoteList notelist : partlist)
					for (NoteCompatible note : notelist)
						drawNotes(g2, note, notelist.serial());
			}
			/* 選択範囲の矩形を描く． */
			if (isMouseSelectBoxDraw)
				drawSelectedMouseBox(g2);
			/* マウス座標を表示 */
			if (mouseVisible) {
				g2.setColor(Color.gray);
				g2.drawString("[" + currentMousePoint.x + ", " + currentMousePoint.y
						+ "]", currentMousePoint.x - axisX, currentMousePoint.y);
			}
		}

		/**
		 * 一つの音符をピアノロールに描画します．Draw a note.
		 * @param g
		 * @param note
		 * @param onset
		 * @param partId
		 * @return
		 */
		private void drawNotes(final Graphics2D g, NoteCompatible note, int partId) {
			int y = getPitch(note.notenum()) * KeyBoard.keyHeight;
			/* 色づけ */
			Color c = setPartColor(partId);
			if (selectedNoteList.contains(note))
				c = Color.red;
			g.setColor(c);

			NoteExpressionRectangle rect = gxpr.getNoteToAxisMap(note);
			Rectangle r;
			/* 演奏上の音符 */
			if (rect.getIOIRectangle() != null) {
				r = rect.getIOIRectangle();
				r.y = y;
				g.draw3DRect(r.x, r.y, r.width, r.height, true);
			}
			if (rect.getPerformanceRectangle() != null) {
				r = rect.getPerformanceRectangle();
				r.y = y;
				g.fill3DRect(r.x, r.y, r.width, r.height, true);
			}
			/* 楽譜上の音符 */
			g.setColor(c.brighter());
			r = rect.getScoreRectangle();
			r.y = y;
			g.fill3DRect(r.x, r.y, r.width, r.height, true);
			// return onset + note.duration();
		}

		/** マウスの選択矩形を表示します。 */
		private void drawSelectedMouseBox(Graphics2D g2) {
			int sx = (mouseStartPoint.x < mouseEndPoint.x)
					? mouseStartPoint.x
					: mouseEndPoint.x;
			int sy = (mouseStartPoint.y < mouseEndPoint.y)
					? mouseStartPoint.y
					: mouseEndPoint.y;
			int w = (mouseStartPoint.x < mouseEndPoint.x)
					? mouseEndPoint.x - sx
					: mouseStartPoint.x - sx;
			int h = (mouseStartPoint.y < mouseEndPoint.y)
					? mouseEndPoint.y - sy
					: mouseStartPoint.y - sy;
			selectedMouseBox = new Rectangle(sx, sy, w, h);
			g2.setColor(Color.black);
			if (isControlMask)
				g2.drawLine(mouseStartPoint.x - axisX, mouseStartPoint.y,
										mouseEndPoint.x - axisX, mouseEndPoint.y);
			else
				g2.drawRect(sx - axisX, sy, w, h);
		}

		/**
		 * 小節線を描画します。Draw a measure bar.
		 */
		// private void drawMeasureLine(final Graphics2D g, int onset) {
		// g.setColor(Color.black);
		// }
	}
}
