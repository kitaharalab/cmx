package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
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

public class CMXMusicViewer extends JFrame implements ActionListener, ChangeListener{

	/**
	 * @param args
	 */
	
	//Default Parameter
	private final static int default_mainframe_width = 480;
	private final static int default_mainframe_height = 200
	;
	//private final int default_layout_align = 10;
	private final static String default_mainframe_title = "CMXMusicViewer";
	
	//Deviation File field
	//TODO 要確認
	public static File musicfile;
	public DeviationInstanceWrapper dev; //DeviationInstanceって書いてたけどいいの？
	int ticksPerBeat = 480; // 曲から取得しなくて良いのか？
	
	
	//Status
	private boolean playing = false;	//true = 再生中
	private boolean deviation = false; //true = Deviation On
	private boolean filechoosed = false; //true = ファイル取得済み
	private boolean isMIDI = false;
	
	//CompornentInstances
	MenuBar menubar = new MenuBar();
	Container cont = getContentPane();
	
	MusicSlider musicslider = new MusicSlider();
	CurrentTimeLabel currenttimelabel = new CurrentTimeLabel();
	
	PlayerButton back_button = new PlayerButton("Back");
	PlayerButton play_button = new PlayerButton("Play");
	PlayerButton stop_button = new PlayerButton("Stop");
	
	FileNameLabel filenamelabel = new FileNameLabel();	
	DeviationCheckBox deviationcheckbox = new DeviationCheckBox();
	
	MyMidiOperator midioperator = new MyMidiOperator();
	PlayingThread thread = new PlayingThread();
	
	
	//Constructor
	public CMXMusicViewer(){
		
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
		panel2.add(filenamelabel);
		panel2.add(deviationcheckbox);
		deviationcheckbox.addActionListener(this);

		thread.start();

		this.setVisible(true);
	}
	
	//メインフレームのアクションリスナ
	public void actionPerformed(ActionEvent ev) {
		
		String cmd = ev.getActionCommand();
		
		if(cmd.equals("Back")) pushBackButton();
		if(cmd.equals("Play")) pushPlayButton();
		if(cmd.equals("Stop")) pushStopButton();
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
	
	void pushBackButton(){
		try{
			midioperator.backPlayingFile();
			musicslider.updateMusicSliderPosition();
			updateCompornents();
		}
		catch(Exception e){
			System.out.println(e + " in pushBackButton()");
		}
	}
	
	void pushPlayButton(){
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
	
	void updateButtons(){
		
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
	
	void updateCompornents(){
		updateButtons();
		musicslider.updateMusicSlider();
		currenttimelabel.updateTimeLabel();
		filenamelabel.updateFileNameField();
		deviationcheckbox.updateCheckBox();
		menubar.updateMenuBar();
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
		}
		
	}
	
	//Inner Class: PlayingThread
	class PlayingThread extends Thread{
		
		public void run(){
			while(true){
				try{
					if(playing){
						PlayingThread.sleep(200);
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
			//TODO あとでファイルを判別してDeviationが有効にできるかどうかを判定する
			//deviation = true;
			this.setEnabled(filechoosed && !isMIDI);
		}
	}
	
	//Inner Class: MenuBar
	class MenuBar extends JMenuBar implements ActionListener{
		
		
		private String[] strMenu = {"File"};
		private JMenu[] menu = new JMenu[strMenu.length];
		private String[] strMenuItem0 = {"Open", "Exit"};
		private JMenuItem[] item0 = new JMenuItem[strMenuItem0.length];
		
		
		
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
			
			//メニューをメニューバーに登録
			this.add(menu[0]);
			
			//アイテムをアクションリスナに登録
			for(int i=0; i<strMenuItem0.length; i++){
				item0[i].addActionListener(this);
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
			//if(playing){
				item0[0].setEnabled(!playing);
			//}
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
		}
	}
	
	
	//Inner Class: MyWindowListener
	class MyWindowListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			midioperator.sequencerClose();
			System.exit(0);
		}
	}
	
	
	
	public static void main(String[] args) {

		new CMXMusicViewer();
		
	}

}
