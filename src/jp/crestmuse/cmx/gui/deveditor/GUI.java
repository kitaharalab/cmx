package jp.crestmuse.cmx.gui.deveditor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.sound.MusicPlaySynchronized;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;

/**
 * このクラスは一つのCorePlayerと複数のPianoRollPanelを持つDeviationエディターのメインクラスです．
 * @author ntotani
 *
 */
public class GUI implements MusicPlaySynchronized {

  /**
   * GUIクラス唯一のインスタンス．
   */
  public static GUI Instance() { return instance; }
  private static GUI instance;

  private CorePlayer corePlayer;
  private MusicPlaySynchronizer synchronizer;
  private boolean showAsTickTime;
  private JMenuItem openMenuItem;
  private JCheckBoxMenuItem tempoMenuItem;
  private JComboBox comboBox;
  private ArrayList<PianoRollPanel> pianoRollPanels;
  private PianoRollPanel showingPanel;
  private JScrollPane scrollPane;
  private JFrame mainFrame;
  private JSlider currentPositionSlider;

  private GUI() throws MidiUnavailableException {
    corePlayer = new CorePlayer();
    synchronizer = new MusicPlaySynchronizer(corePlayer);
    synchronizer.addSynchronizedComponent(this);
    showAsTickTime = true;
    pianoRollPanels = new ArrayList<PianoRollPanel>();

    mainFrame = new JFrame();
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setSize(640, 480);
    mainFrame.getContentPane().setLayout(new BorderLayout());
    setMenuBar();
    setScrollPane();
    JPanel south = new JPanel(new FlowLayout());
    setList(south);
    setSlider(south);
    setButtons(south);
    mainFrame.getContentPane().add(south, BorderLayout.SOUTH);
    mainFrame.setVisible(true);
  }
  
  private void setMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("file");
    openMenuItem = new JMenuItem("open");
    openMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if(fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
          openMenuItem.setEnabled(false);
          open(fc.getSelectedFile().getAbsolutePath());
        }
      }
    });
    JMenuItem save = new JMenuItem("save");
    save.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if(fc.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
          save(fc.getSelectedFile());
        }
      }
    });
    JMenuItem quit = new JMenuItem("quit");
    quit.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    file.add(openMenuItem);
    file.add(save);
    file.add(quit);
    
    JMenu show = new JMenu("show");
    tempoMenuItem = new JCheckBoxMenuItem("tempo");
    tempoMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if(showingPanel != null)
          showingPanel.getTempoFrame().setVisible(tempoMenuItem.isSelected());
      }
    });
    show.add(tempoMenuItem);
    
    menuBar.add(file);
    menuBar.add(show);
    mainFrame.setJMenuBar(menuBar);
  }

  private void setScrollPane(){
    scrollPane = new JScrollPane();
    JSlider scale = new JSlider();
    scale.setMinimum(PianoRollPanel.WIDTH_PER_BEAT/2);
    scale.setValue(PianoRollPanel.WIDTH_PER_BEAT);
    scale.setMaximum(PianoRollPanel.WIDTH_PER_BEAT*2);
    scale.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        PianoRollPanel.WIDTH_PER_BEAT = ((JSlider)e.getSource()).getValue();
        if(showingPanel != null)
          showingPanel.updateScale();
        scrollPane.repaint();
      }
    });
    scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, scale);
    scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
    scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
    mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
  }

  private void setList(JPanel parent) {
    comboBox = new JComboBox();
    comboBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        try {
          // 表示中のテンポウィンドウがあれば隠す
          if(showingPanel != null)
            showingPanel.getTempoFrame().setVisible(false);
  
          // 新しいパネルに切り替える
          int index = ((JComboBox)e.getSource()).getSelectedIndex();
          corePlayer.changeDeviation(index);
          showingPanel = pianoRollPanels.get(index);
          showingPanel.updateScale();
          showingPanel.setScrollPane(scrollPane);
  
          // y座標を中央までずらす
          Point p = scrollPane.getViewport().getViewPosition();
          p.y = (showingPanel.getPreferredSize().height - scrollPane.getHeight())/2;
          scrollPane.getViewport().setViewPosition(p);
  
          // 再生位置スライダーを設定
          if(showAsTickTime)
            currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getTickLength());
          else
            currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getMicrosecondLength());
  
          // テンポウィンドウを表示（非表示）
          showingPanel.getTempoFrame().setVisible(tempoMenuItem.isSelected());
        } catch (InvalidMidiDataException e1) {
          e1.printStackTrace();
        }
      }
    });
    parent.add(comboBox);
  }

  private void setSlider(JPanel parent){
    currentPositionSlider = new JSlider();
    currentPositionSlider.setValue(0);
    currentPositionSlider.addMouseListener(new MouseListener(){
      public void mouseClicked(MouseEvent e) {
      }
      public void mouseEntered(MouseEvent e) {
      }
      public void mouseExited(MouseEvent e) {
      }
      public void mousePressed(MouseEvent e) {
      }
      public void mouseReleased(MouseEvent e) {
        if(showingPanel == null) return;
        setPlayPosition(currentPositionSlider.getValue());
      }
    });
    parent.add(currentPositionSlider);
  }

  private void setButtons(JPanel parent) {
    JButton start = new JButton("start");
    start.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JButton button = (JButton)e.getSource();
        if(synchronizer.isNowPlaying()){
          currentPositionSlider.setEnabled(true);
          synchronizer.stop();
          button.setText("start");
        }else{
          currentPositionSlider.setEnabled(false);
          synchronizer.play();
          button.setText("pause");
        }
      }
    });
    JButton reset = new JButton("reset");
    reset.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        currentPositionSlider.setEnabled(true);
        synchronizer.stop();
        setPlayPosition(0);
      }
    });
    JButton change = new JButton("change");
    change.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showAsTickTime = !showAsTickTime;
        if(showingPanel != null){
          showingPanel.updateNotes();
          mainFrame.repaint();
        }
        if(showAsTickTime){
          currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getTickLength());
          currentPositionSlider.setValue((int)corePlayer.getTickPosition());
        }else{
          currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getMicrosecondLength());
          currentPositionSlider.setValue((int)corePlayer.getMicrosecondPosition());
        }
      }
    });
    
    parent.add(start);
    parent.add(reset);
    parent.add(change);
  }

  public void start(MusicPlaySynchronizer wavsnyc) {
  }

  public void stop(MusicPlaySynchronizer wavsync) {
    corePlayer.reset();
    currentPositionSlider.setEnabled(true);
  }

  public void synchronize(final double currentTime, final long currentTick, MusicPlaySynchronizer wavsync) {
    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        Point p = scrollPane.getViewport().getViewPosition();
        p.x = showingPanel.getPlayPointX(currentTime, currentTick);
        int width = scrollPane.getViewport().getWidth();
        p.x = Math.max(0, Math.min(showingPanel.getPreferredSize().width - width, p.x - width/2));
        scrollPane.getViewport().setViewPosition(p);
        if(showAsTickTime)
          currentPositionSlider.setValue((int)currentTick);
        else
          currentPositionSlider.setValue((int)(currentTime*1000000));
      }
    });
    showingPanel.repaint();
  }
  
  /**
   * タイムラインの表示形式を返す．楽譜時刻の場合true,実時刻の場合falseを返す．
   * @return タイムラインの表示形式
   */
  public boolean getShowAsTickTime() { return showAsTickTime; }

  /**
   * ファイルを開く．MusicXMLかDeviationInstanceXML以外が指定されると無視される．
   * @param fileName
   */
  public void open(final String fileName) {
    Thread th = new Thread(){
      public void run() {
        try {
          CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
          CompiledDeviation cd = corePlayer.open(wrapper);
          pianoRollPanels.add(new PianoRollPanel(cd));
          SwingUtilities.invokeLater(new Runnable(){
            public void run() {
              comboBox.addItem("dev" + comboBox.getItemCount());
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            openMenuItem.setEnabled(true);
          }
        });
      }
    };
    th.start();
  }
  
  /**
   * ファイルを保存する．
   * @param file
   */
  public void save(final File file){
    Thread t = new Thread(){
      public void run(){
        try {
          corePlayer.writeFile(new FileOutputStream(file));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }

  /**
   * 再生位置を変更し、画面をその位置までスクロールする．引数は、楽譜時刻表示の場合tickを、実時刻表時の場合マイクロ秒で指定する．
   * @param position 再生位置
   */
  public void setPlayPosition(long position) {
    if(showAsTickTime)
      corePlayer.setTickPosition(position);
    else
      corePlayer.setMicrosecondPosition(position);
    synchronize(corePlayer.getMicrosecondPosition()/1000000.0, corePlayer.getTickPosition(), null);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        try {
          GUI.instance = new GUI();
        } catch (MidiUnavailableException e) {
          e.printStackTrace();
        }
      }
    });
  }

}
