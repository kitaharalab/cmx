package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.gui.sound.MIDIController;
import jp.crestmuse.cmx.misc.*;

/**
 * MusicXMLならびにDeviationInstanceXMLの簡易ビューアです． CMXMusicDataFrame is a simple GUI
 * for MusicXML and DeviationInstanceXML. It provides the following function:
 * <ol>
 * <li>view a MusicXML file or DeviationInstanceXML.
 * <li>play the file.
 * </ol>
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
public class CMXMusicDataFrame extends JFrame implements ActionListener,
		ChangeListener {

	private static final String WINDOW_TITLE = "CMXMusicData Viewer";
	private static final String CMD_OPEN = "open";
	private static final String CMD_EXIT = "exit";
	private static final String EMPTY_FILENAME = "";
	private static final String CMD_PLAY = "play";
	private static final String CMD_STOP = "stop";
	/** パネルサイズ（縦幅 [pixel]） */
	private int frameHeight = 340;
	/** パネルサイズ（横幅 [pixel]） */
	int frameWidth = 780;
	private int ticksPerBeat;
	private String midiDeviceName;
	// private CMXCommandForGUI editor;
	private PianoRollCompatible filewrapper = null;
	// private MusicXMLWrapper xml;
	// private DeviationInstanceWrapper dev;
	private InputStream instream = null;
	private String lastSelectedPath = ".";
	private String selectedFileName;
	// private File selectedFile;
	private PianoRollPanel pianoroll;
	private VelocityPanel velocityPanel;
	private MIDIController synthe;
	private JButton startButton;
	private JButton stopButton;
	private JTextField widthText;
	private JSlider widthbar;
	private JSplitPane splitPane;
	private JTextField musicLengh;

	public static void notifyStopPlaying() {
		throw new UnsupportedOperationException(); // TODO 実装
	}

	public CMXMusicDataFrame(int ticksPerBeat) {
		super(WINDOW_TITLE);
		this.ticksPerBeat = ticksPerBeat;
		JFrame.setDefaultLookAndFeelDecorated(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setJMenuBar(createMenubar());
		add(createNaviButton(), BorderLayout.NORTH);
		pianoroll = new PianoRollPanel(320, 240);
		velocityPanel = new VelocityPanel(frameWidth);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pianoroll,
						velocityPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(frameHeight);
		splitPane.setPreferredSize(new Dimension(frameWidth, frameHeight
				+ VelocityPanel.PANEL_HEIGHT));

		// add(pianoroll, BorderLayout.CENTER);
		// add(velocityPanel, BorderLayout.PAGE_END);
		add(splitPane, BorderLayout.CENTER);
	}

	// public CMXMusicDataFrame(CMXCommandForGUI editor) {
	// super(WINDOW_TITLE);
	// this.editor = editor;
	// init();
	// }

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals(CMD_OPEN)) {
			setFile(getInputFileFromDialog());
			startButton.setEnabled(true);
			repaint();
		} else if (cmd.equals(CMD_PLAY)) {
			try {
				instream = filewrapper.getMIDIInputStream();
				synthe.startPlaying(instream);
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
			} catch (Exception ex) {
				showErrorMessage(ex);
			}
		} else if (cmd.equals(CMD_STOP)) {
			synthe.stopPlaying();
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		} else if (cmd.equals(CMD_EXIT)) {
			System.exit(1);
		}
	}

	public void setMIDIDeviceName(String midiDeviceName) {
		this.midiDeviceName = midiDeviceName;
		this.synthe = new MIDIController(midiDeviceName);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == widthbar) {
			widthText.setText(String.valueOf(widthbar.getValue()));
			Dimension size = new Dimension(frameWidth + widthbar.getValue()*5,
					frameHeight);
			pianoroll.getPianoroll().setPreferredSize(size);
			pianoroll.revalidate();
			System.out.println(size);
		}
		repaint();
	}

	void setFile(String filename) {
		if (filename != null) {
			selectedFileName = filename;
			readXMLFile(filename);
			if (filewrapper != null) {
				try {
					pianoroll.setMusicData(filewrapper, ticksPerBeat);
				} catch (Exception e) {
					showErrorMessage(e);
				}
			}
		}
	}

	private JButton createButton(JComponent obj, String label,
			String actionCommand, boolean editable, ActionListener target) {
		JButton btn = new JButton(label);
		btn.setVerticalTextPosition(AbstractButton.CENTER);
		btn.setHorizontalTextPosition(AbstractButton.LEADING); // aka LEFT, for
		btn.setEnabled(editable);
		btn.addActionListener(target);
		btn.setActionCommand(actionCommand);
		obj.add(btn);
		return btn;
	}

	/**
	 * メニューバーを作成します。Create a menubar.
	 * @return 作成されるJMenubar
	 */
	private JMenuBar createMenubar() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');

		createMenuItem(this, menu, "Open File...", CMD_OPEN, KeyEvent.VK_O);
		menu.addSeparator();
		createMenuItem(this, menu, "Exit", CMD_EXIT, KeyEvent.VK_Q);

		menubar.add(menu);
		return menubar;
	}

	/**
	 * メニューアイテムを作成します。
	 * @param l
	 *        アクションリスナーを持つオブジェクト
	 * @param menu
	 *        アイテムを格納するメニュー
	 * @param name
	 *        アイテムに表示する文字列
	 * @param action
	 *        アクションイベントを識別するためのコマンド名
	 * @param key
	 *        ショートカットキーとなるキーイベント
	 */
	private void createMenuItem(ActionListener l, final JMenu menu,
			final String name, final String action, final int key) {
		createMenuItem(l, menu, name, action, key, ActionEvent.CTRL_MASK);
	}

	/**
	 * メニューアイテムを作成します。
	 * @param l
	 *        アクションリスナーを持つオブジェクト
	 * @param menu
	 *        アイテムを格納するメニュー
	 * @param name
	 *        アイテムに表示する文字列
	 * @param action
	 *        アクションイベントを識別するためのコマンド名
	 * @param key
	 *        ショートカットキーとなるキーイベント
	 * @param opt
	 *        ショートカットキーとなるキーイベントのオプションキー
	 */
	private void createMenuItem(ActionListener l, final JMenu menu,
			final String name, final String action, final int key, final int opt) {
		JMenuItem menuItem;
		menuItem = new JMenuItem(name);
		menuItem.setMnemonic(key);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(key, opt));
		menuItem.setActionCommand(action);
		menuItem.addActionListener(l);
		menu.add(menuItem);
	}

	/**
	 * @return
	 */
	private JPanel createNaviButton() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		startButton = createButton(panel, "Play", CMD_PLAY, false, this);
		stopButton = createButton(panel, "Stop", CMD_STOP, false, this);
		widthbar = new JSlider(JSlider.HORIZONTAL, 10, 100, 30);
		widthbar.addChangeListener(this);
		widthText = new JTextField(3);
		widthText.setText(String.valueOf(widthbar.getValue()));
		musicLengh = new JTextField(6);
		panel.add(startButton);
		panel.add(stopButton);
		panel.add(widthbar);
		panel.add(widthText);
		panel.add(new JLabel("music length:"));
		panel.add(musicLengh);
		return panel;
	}

	/**
	 * ダイアログからファイルを読み込みます。
	 */
	private String getInputFileFromDialog() {
		JFileChooser fc = new JFileChooser(lastSelectedPath);

		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return EMPTY_FILENAME;
		}
		File selectedFile = fc.getSelectedFile();
		String selectedFileName = selectedFile.getAbsolutePath();
		lastSelectedPath = selectedFile.getParent();
		// DeviationInstanceWrapper.changeDefaultMusicXMLDirName(lastSelectedPath);
		return selectedFileName;
	}

	/**
	 * MusicXMLまたはDeviationInstanceXML形式のファイルを読み込みます。
	 * MusicXML形式が読み込まれた場合，新しい空のDeviationInstanceXMLドキュメントを生成します。
	 */

	private void readXMLFile(String filename) {
		try {
			if (filename.endsWith(".mid")) {
				filewrapper = MIDIXMLWrapper.readSMF(filename);
			} else {
				CMXFileWrapper f = CMXFileWrapper.readfile(filename);
				if (f instanceof PianoRollCompatible) {
					filewrapper = (PianoRollCompatible) f;
					// } else if (f instanceof MIDIXMLWrapper) {
					// filewrapper = ((MIDIXMLWrapper)f).toSCCXML();
				} else if (f instanceof DeviationInstanceWrapper) {
					filewrapper = ((DeviationInstanceWrapper) f).toSCCXML(ticksPerBeat);
				} else {
					throw new InvalidFileTypeException();
				}
			}
			
		} catch (Exception e) {
			showErrorMessage(e);
		}
	}

	/*
	 * private void readXMLFile(String filename) { System.out.println(filename + "
	 * is reading..."); try { CMXFileWrapper cmx =
	 * CMXFileWrapper.readfile(filename); if (cmx instanceof MusicXMLWrapper) {
	 * xml = (MusicXMLWrapper) cmx; dev =
	 * DeviationInstanceWrapper.createDeviationInstanceFor(xml);
	 * dev.createDeviationDataSet().addElementsToWrapper(); } else if (cmx
	 * instanceof DeviationInstanceWrapper) { dev = (DeviationInstanceWrapper)
	 * cmx; xml = dev.getTargetMusicXML(); } System.out.println(" done."); } catch
	 * (Exception e) { e.printStackTrace(); } }
	 */

	private void showErrorMessage(Exception e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(this, e.toString());
	}
}
