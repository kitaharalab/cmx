package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.sound.*;
import javax.sound.midi.*;

import jp.crestmuse.cmx.commands.DeviationInstanceExtractor;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.gui.sound.*;
import jp.crestmuse.cmx.sound.*;

/**
 * とりあえずはMidi(DeviationInstance)を再生するプレイヤー
 * @author Ryosuke Tokuami
 * @since 2008.5.27
 */

public class CMXDeviationInstancePlayer extends JFrame implements ActionListener, ChangeListener{

	/**
	 * @param args
	 */
	
	//Default Parameter
	private final static int default_mainframe_width = 480;
	private final static int default_mainframe_height = 200
	;
	//private final int default_layout_align = 10;
	private final static String default_mainframe_title = "CMXDeviationInstancePlayer";
	
	//Deviation File field
	private static File musicfile;
	private DeviationInstanceWrapper dev;
	private int ticksPerBeat = 480;
	
	
	//Status
	private boolean playing = false;	//true = 再生中
	//private boolean deviation = false; //true = Deviation On
	private boolean filechoosed = false; //true = ファイル取得済み
	private boolean isMIDI = false;
	
	//CompornentInstances
	private MenuBar menubar = new MenuBar();
	private Container cont = getContentPane();
	
	private MusicSlider musicslider = new MusicSlider();
	private CurrentTimeLabel currenttimelabel = new CurrentTimeLabel();
	
	private PlayerButton back_button = new PlayerButton("Back");
	private PlayerButton play_button = new PlayerButton("Play");
	private PlayerButton stop_button = new PlayerButton("Stop");
	
	private FileNameLabel filenamelabel = new FileNameLabel();	
	private DeviationCheckBox deviationcheckbox = new DeviationCheckBox();
	
	private MyMidiOperator midioperator = new MyMidiOperator();
	private PlayingThread thread = new PlayingThread();
	
	
	//Constructor
	public CMXDeviationInstancePlayer(){
		
		this.addWindowListener(new MyWindowListener());
		this.setSize(default_mainframe_width, default_mainframe_height);
		this.setTitle(default_mainframe_title);
		
		//メニュー表示
		this.setJMenuBar(menubar);
		
		//パネルをPaneに追加
		this.setContentPane(cont);
		cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
		JPanel panel0 = new JPanel();
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		cont.add(panel0);
		cont.add(panel1);
		cont.add(panel2);
		
		
		//プレイヤの各コンポーネントの配置、リスナに登録
		//SliderPanel part
		panel0.setLayout(new FlowLayout());
		panel0.add(musicslider);
		panel0.add(currenttimelabel);
		musicslider.addChangeListener(this);
		
		//PlayerPanel part
		panel1.setLayout(new FlowLayout());
		panel1.add(back_button);
		panel1.add(play_button);
		panel1.add(stop_button);
		back_button.addActionListener(this);
		play_button.addActionListener(this);
		stop_button.addActionListener(this);
		
		//InformationPanel part
		panel2.setLayout(new FlowLayout());
		//TODO 暫定的にfilenamelabelを非表示
		//panel2.add(filenamelabel);
		panel2.add(deviationcheckbox);
		deviationcheckbox.addActionListener(this);

		thread.start();

		this.setVisible(true);
	}
	
	//メインフレームのアクションリスナ
	public void actionPerformed(ActionEvent ev) {
		
		if(ev.getSource().equals(back_button)) pushBackButton();
		if(ev.getSource().equals(play_button)) pushPlayButton();
		if(ev.getSource().equals(stop_button)) pushStopButton();
		if(ev.getSource().equals(deviationcheckbox))
		  midioperator.exchangeCurrentSequencer();
	}
	
	//メインフレームのチェンジリスナ
	public void stateChanged(ChangeEvent e) {
		if(!playing){
			midioperator.currentPlayer.setMicrosecondPosition((long)(musicslider.getValue()*1000000));
		}
		
		updateCompornents();
	}
	
	public void pushBackButton(){
		try{
			midioperator.backPlayingFile();
			musicslider.updateMusicSliderPosition();
			updateCompornents();
		}
		catch(Exception e){
			System.out.println(e + " in pushBackButton()");
		}
	}
	
	public void pushPlayButton(){
		try{
			midioperator.startPlayingFile();
			playing = true;
			updateCompornents();
		}
		catch(Exception e){
			System.out.println(e + " in pushPlayButton");
		}
	}
	
	void pushStopButton(){
		try{
			midioperator.stopPlayingFile();
			playing = false;
			updateCompornents();
		}
		catch(Exception e){
			System.out.println(e + " in pushStopButton");
		}
	}
	
	public void updateButtons(){
		
		if(!filechoosed){
			back_button.setEnabled(filechoosed);
			play_button.setEnabled(filechoosed);
			stop_button.setEnabled(filechoosed);
		}
		else{
			back_button.setEnabled(filechoosed);
			play_button.setEnabled(filechoosed);
		}
		
		if(playing){
			back_button.setEnabled(playing);
			play_button.setEnabled(!playing);
			stop_button.setEnabled(playing);
		}
		
	}
	
	public void updateTitleBar(){
		if(filechoosed){
			//TODO XMLを読んでタイトルを取得するように変更する
			this.setTitle(default_mainframe_title + " : " + musicfile.getName());
		}
	}
	
	public void updateCompornents(){
		updateButtons();
		musicslider.updateMusicSlider();
		currenttimelabel.updateTimeLabel();
		filenamelabel.updateFileNameField();
		deviationcheckbox.updateCheckBox();
		menubar.updateMenuBar();
		this.updateTitleBar();
	}
	
	
	//Inner Class: MidiOperator
	//こいつでMidiをファイルを操作、再生&途中再生等
	class MyMidiOperator{
		
		/*MidiDevice device;
		Sequencer dev_sequencer;
		Sequencer mid_sequencer;
		Sequencer current_sequencer = mid_sequencer;*/
		
		SCCXMLWrapper musicScc;
		SCCXMLWrapper devScc;
		
		SMFPlayer musicPlayer;
		SMFPlayer devPlayer;
		SMFPlayer currentPlayer;
		
		public MyMidiOperator(){
		  try {
			  musicPlayer = new SMFPlayer();
			  devPlayer = new SMFPlayer();
			  currentPlayer = devPlayer;
		  } catch (MidiUnavailableException e) {
			  e.printStackTrace();
		  }
		}
		
		public void setMusicFile(File file){
			//TODO　ここでDevとmidをそれぞれシーケンサにセット
			try{
			  try{
				//　とりあえずMidi再生版
				musicPlayer.readSMF(file);
				currentPlayer = musicPlayer;
				isMIDI = true;
				/*current_sequencer = MidiSystem.getSequencer();
				current_sequencer.open();
				current_sequencer.setSequence(s);
				*/
			  }catch(Exception ex){
				dev = (DeviationInstanceWrapper)CMXFileWrapper.readfile(file.getPath());
				musicScc = dev.getTargetMusicXML().makeDeadpanSCCXML(ticksPerBeat);
				devScc = dev.toSCCXML(ticksPerBeat);
				
				musicPlayer.readSMF(musicScc.getMIDIInputStream()); //ルズリではgetMIDInputだったがメソッドなし
				devPlayer.readSMF(devScc.getMIDIInputStream());
				deviationcheckbox.setSelected(true);
				isMIDI = false;
			  }
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void startPlayingFile(){
			try{
				currentPlayer.play();
			}
			catch(Exception e){
				System.out.println(e + " in startPlayingFile()");
			}
		}
		
		public void stopPlayingFile(){
			try{
				currentPlayer.stop();
			}
			catch(Exception e){
				System.out.println(e + " in stopPlayingFile()");
			}
		}
		
		public void backPlayingFile(){
			try{
				currentPlayer.back();
			}
			catch(Exception e){
				System.out.println(e+ " in backPlayingFile()");
			}
		}
		
		public void exchangeCurrentSequencer(){
		  stopPlayingFile();
		  playing = false;
		
		  if(currentPlayer.equals(musicPlayer)){
			  currentPlayer = devPlayer;
		  }else currentPlayer = musicPlayer;
		  	  backPlayingFile();
     
		  musicslider.updateMusicSliderPosition();
		  updateCompornents();
		}
		
		public void sequencerClose(){
			
			if(devPlayer != null){
				devPlayer.close();
			}
			if(musicPlayer != null){
				musicPlayer.close();
			}	
			if(currentPlayer != null){
				currentPlayer.close();
			}
		}
		
	}
	
	//Inner Class: PlayingThread
	class PlayingThread extends Thread{
		
		public void run(){
			while(true){
				try{
					if(playing){
						PlayingThread.sleep(400);
						musicslider.updateMusicSliderPosition();
						
						if(!midioperator.currentPlayer.isNowPlaying()){
							playing = false;
							updateCompornents();
						}
					}
					}
				catch(Exception e){
					System.out.println(e+ " in running thread");
				}
			}
		}
	}
	
	//Inner Class: MusicSlider
	class MusicSlider extends JSlider{
		
		private static final int default_slider_min = 0;
		private static final int default_slider_max = 0;
		private static final int default_slider_value = 0;
		
		
		//Constructor
		public MusicSlider(){
			super(SwingConstants.HORIZONTAL, default_slider_min, default_slider_max, default_slider_value);
			this.setEnabled(filechoosed);
		}
		
		public void updateMusicSlider(){
			
			if(!filechoosed){
				this.setEnabled(filechoosed);
			}
			else{
				this.setEnabled(filechoosed);
				this.setMaximum((int)(midioperator.currentPlayer.getMicrosecondLength()/1000000));
			}
			
			this.setEnabled(!playing);
		}
		
		public void updateMusicSliderPosition(){
			this.setValue((int)(midioperator.currentPlayer.getMicrosecondPosition()/1000000));
		}
		
		
	}
	
	//Inner Class: PlayerButton
	class PlayerButton extends JButton{
		
		public PlayerButton(String buttonname){
			super(buttonname);
			this.setEnabled(filechoosed);
		}
	}
	
	//Inner Class: CurrentTimeLabel
	class CurrentTimeLabel extends JLabel{
		
		DecimalFormat df = new DecimalFormat("00");
		
		public CurrentTimeLabel(){
			super();
			this.updateTimeLabel();
		}
		
		public void updateTimeLabel(){
			//TimeLabelはただSliderに追従するだけ
			this.setText(df.format(musicslider.getValue()/60)+":"+df.format(musicslider.getValue()%60) +" / "+ df.format(musicslider.getMaximum()/60)+":"+df.format(musicslider.getMaximum()%60));			
		}
	}
	
	//Inner Class: FileNameField
	class FileNameLabel extends JLabel{
		
		public FileNameLabel(){
			super();
			super.setSize(new Dimension(default_mainframe_width, default_mainframe_height/10 ));
			this.setText("- Choose DeviationXML -");
			super.setBorder(LineBorder.createBlackLineBorder());
			

		}
		
		public void updateFileNameField(){
			this.setText(musicfile.getName());
		}
		
	}
	
	//Inner Class: DeviationCheckBox
	class DeviationCheckBox extends JCheckBox{
		
		public DeviationCheckBox(){
			super();
			this.setText("Deviation On");
			this.setEnabled(filechoosed);
		}
		
		public void updateCheckBox(){
			this.setEnabled(filechoosed && !isMIDI);
		}
	}
	
	
	//Inner Class: MenuBar
	class MenuBar extends JMenuBar implements ActionListener{
		
		
		private String[] strMenu = {"File", "Help"};
		private JMenu[] menu = new JMenu[strMenu.length];
		private String[] strMenuItem0 = {"Open", "Exit"};
		private String[] strMenuItem1 = {"Help", "about this"};
		private JMenuItem[] item0 = new JMenuItem[strMenuItem0.length];
		private JMenuItem[] item1 = new JMenuItem[strMenuItem1.length];
		
		
		public MenuBar(){
		
			//メニューの生成
			for(int i=0; i<strMenu.length; i++){
				menu[i] = new JMenu(strMenu[i]);
			}			
		
			//メニューにアイテムの追加
			for(int i=0; i<strMenuItem0.length; i++){
				item0[i] = new JMenuItem(strMenuItem0[i]);
				menu[0].add(item0[i]);
			}
			for(int i=0; i<strMenuItem1.length; i++){
				item1[i] = new JMenuItem(strMenuItem1[i]);
				menu[1].add(item1[i]);
			}
			
			//メニューをメニューバーに登録
			this.add(menu[0]);
			this.add(menu[1]);
			
			//アイテムをアクションリスナに登録
			for(int i=0; i<strMenuItem0.length; i++){
				item0[i].addActionListener(this);
			}
			for(int i=0; i<strMenuItem1.length; i++){
				item1[i].addActionListener(this);
			}
			
		}
			

		private void menuFileOpen() {
			
			if(!playing){
				JFileChooser fchooser = new JFileChooser();
				
				int ret = fchooser.showOpenDialog(null);
				File obj = fchooser.getSelectedFile();
				
				if(ret == JFileChooser.APPROVE_OPTION){
					//staticなFileオブジェクトmusicfileに取得したobjを代入
					DeviationInstanceWrapper.changeDefaultMusicXMLDirName(obj.getParent());
				  musicfile = obj;
					filechoosed = true;
					midioperator.setMusicFile(musicfile);
					pushBackButton();
					updateCompornents();
				}
			}

		}
		
		private void menuFileExit() {
			midioperator.sequencerClose();
			System.exit(0);
		}
		
		public void updateMenuBar(){
			item0[0].setEnabled(!playing);
		}

		public void menuHelpHelp(){
			new HelpFrame();
		}
		
		public void menuHelpAbout(){
			new AboutFrame();
		}
		//メニューアイテムのアクションリスナ
		/*@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == item0[0]) menuFileOpen();
			if(e.getSource() == item0[1]) menuFileExit();
		}*/
		
		public void actionPerformed(ActionEvent e) {
		  if(e.getSource() == item0[0]) menuFileOpen();
		  if(e.getSource() == item0[1]) menuFileExit();
		  
		  if(e.getSource() == item1[0]) menuHelpHelp();
		  if(e.getSource() == item1[1]) menuHelpAbout();
		}
	}
	
	
	//Inner Class: MyWindowListener
	class MyWindowListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			midioperator.sequencerClose();
			System.exit(0);
		}
	}
	
	//Inner Class: AboutFrame
	class AboutFrame extends JFrame implements ActionListener{
		
		private final String about_frame_title = "About";
		private final int default_aboutframe_width = 300;
		private final int default_aboutframe_height = 200;
		
		private Container cont = getContentPane();
		private JTabbedPane tabbedpane = new JTabbedPane();
		
		private JPanel panelCMXDI = new JPanel();
		private JButton buttonCMXDI = new JButton();
		private JPanel panelXalan = new JPanel();
		private JButton buttonXalan = new JButton();
		private JPanel panelXerces = new JPanel();
		private JButton buttonXerces = new JButton();
		
		
		private Desktop desktop = Desktop.getDesktop();
		
		public AboutFrame(){
			this.setTitle(about_frame_title);
			this.setSize(default_aboutframe_width, default_aboutframe_height);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			this.setContentPane(cont);
			cont.add(tabbedpane);
			
			panelCMXDIInit();
			panelXalanInit();
			panelXercesInit();
			buttonCMXDI.addActionListener(this);
			buttonXalan.addActionListener(this);
			buttonXerces.addActionListener(this);			
			
			tabbedpane.add("About this", panelCMXDI);
			tabbedpane.add("Xalan", panelXalan);
			tabbedpane.add("Xerces", panelXerces);
			
			
			this.setVisible(true);			
		}
		
		public void actionPerformed(ActionEvent ev){
			
			if(ev.getSource().equals(buttonCMXDI)) jumptoURL("http://www.crestmuse.jp/cmx/");
			if(ev.getSource().equals(buttonXalan)) jumptoURL("http://xalan.apache.org/");
			if(ev.getSource().equals(buttonXerces)) jumptoURL("http://xerces.apache.org/");
		}
		
		
		public void panelCMXDIInit(){
			
			String text = "CMXDeviationInstancePlayer\n" +
					"Version: 0.1.0\n" +
					"\n" +
					"(c)Copyright CrestMuseXML Development Project\n" +
					"2006, 2008. All right reserved.";
			
			panelCMXDI.setLayout(new FlowLayout(FlowLayout.LEFT));
			JTextArea textarea = new JTextArea(text);
			textarea.setEditable(false);
			textarea.setBackground(panelCMXDI.getBackground());
			
			buttonCMXDI.setText("http://www.crestmuse.jp/cmx/");
			
			panelCMXDI.add(textarea);
			panelCMXDI.add(buttonCMXDI);
		}
		
		
		public void panelXalanInit(){
			
			String text = "Xalan Java\n" +
					"Version: *.*.*\n" +
					"\n" +
					"This is under\n" +
					"  the Apache Software License, Version 2.0.";
			panelXalan.setLayout(new FlowLayout(FlowLayout.LEFT));
			JTextArea textarea = new JTextArea(text);
			textarea.setEditable(false);
			textarea.setBackground(panelXalan.getBackground());
			
			buttonXalan.setText("http://xalan.apache.org/");
			
			panelXalan.add(textarea);
			panelXalan.add(buttonXalan);
		}
		
		public void panelXercesInit(){
			
			String text = "Xerces\n" +
					"Version: *.*.*\n" +
					"\n" +
					"This is under\n" +
					"  the Apache Software License, Version 2.0.";
			panelXerces.setLayout(new FlowLayout(FlowLayout.LEFT));
			JTextArea textarea = new JTextArea(text);
			textarea.setEditable(false);
			textarea.setBackground(panelXerces.getBackground());
			
			buttonXerces.setText("http://xerces.apache.org/");
			
			panelXerces.add(textarea);
			panelXerces.add(buttonXerces);
		}
		
		public void jumptoURL(String urlstring){
			
			try{
				desktop.browse(new URI(urlstring));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	//Inner Class: HelpFrame
	class HelpFrame extends JFrame{
		
		private final String help_frame_title = "Help";
		private final int default_helpframe_width = 600;
		private final int default_helpframe_height = 300;
		
		private Container cont = getContentPane();
		
		private String helptext = "Before playing, you must choose SMF or DeviationXML from \"File\" menu.\n" +
				"\n" +
				"Play: Start playing music.\n" +
				"Back: Return music to first position.\n" +
				"Stop: Stop playing music.\n" +
				"Deviation on checkbox: Enable/Disable deviation.\n" +
				"\n" +
				"File -> Open : Open filechoose dialog. You can choose SMF or DeviationXML.\n" +
				"File -> Exit : Exit this application.\n" +
				"\n" +
				"Help -> Help : Show this document.\n" +
				"Help -> about this : Show this player's information and using library license.";
		
		public HelpFrame(){
			this.setTitle(help_frame_title);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setSize(default_helpframe_width, default_helpframe_height);
			this.setContentPane(cont);
			
			JTextArea textarea = new JTextArea(helptext);
			textarea.setEditable(false);
			textarea.setBackground(this.getBackground());
			
			cont.add(textarea);
			
			this.setVisible(true);
		}
	}
	
	public static void main(String[] args) {

		new CMXDeviationInstancePlayer();
		
	}

}
