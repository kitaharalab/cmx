package jp.crestmuse.cmx.gui.deveditor;

import java.awt.Dimension;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.sound.MusicPlaySynchronized;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;

public class GUI implements MusicPlaySynchronized {

  private CorePlayer corePlayer;
  private MusicPlaySynchronizer synchronizer;
  private DefaultListModel playList;
  private JMenuItem openMenuItem;
  private ArrayList<PianoRollPanel> pianoRollPanels;
  private PianoRollPanel showingPanel;
  private JScrollPane scrollPane;
  private JFrame mainFrame;
  private JSlider currentPositionSlider;
  private boolean showAsTickTime;

  public GUI() throws MidiUnavailableException {
    corePlayer = new CorePlayer();
    synchronizer = new MusicPlaySynchronizer(corePlayer);
    synchronizer.addSynchronizedComponent(this);
    showAsTickTime = true;
    pianoRollPanels = new ArrayList<PianoRollPanel>();
    scrollPane = new JScrollPane();

    mainFrame = new JFrame();
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setSize(640, 720);
    mainFrame.getContentPane().setLayout(new FlowLayout());
    setMenuBar(mainFrame);
    scrollPane.setPreferredSize(new Dimension(512, 512));
    mainFrame.getContentPane().add(scrollPane);
    setList(mainFrame);
    setSlider();
    setButtons(mainFrame);
    mainFrame.setVisible(true);
  }

  private void setSlider(){
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
    mainFrame.getContentPane().add(currentPositionSlider);
  }

  private void setList(final JFrame frame) {
    playList = new DefaultListModel();
    JList list = new JList(playList);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        try {
          int index = ((JList)e.getSource()).getSelectedIndex();
          corePlayer.changeDeviation(index);
          showingPanel = pianoRollPanels.get(index);
          showingPanel.setShowAsTickTime(showAsTickTime);
          showingPanel.setScrollPane(scrollPane);
          Point p = scrollPane.getViewport().getViewPosition();
          p.y = (showingPanel.getHeight() - scrollPane.getHeight())/2;
          scrollPane.getViewport().setViewPosition(p);
          if(showAsTickTime)
            currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getTickLength());
          else
            currentPositionSlider.setMaximum((int)corePlayer.getCurrentSequence().getMicrosecondLength());
        } catch (InvalidMidiDataException e1) {
          e1.printStackTrace();
        }
      }
    });
    JScrollPane sp = new JScrollPane();
    sp.getViewport().setView(list);
    frame.getContentPane().add(sp);
  }

  private void setButtons(final JFrame frame) {
    JButton start = new JButton("start");
    start.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        currentPositionSlider.setEnabled(false);
        synchronizer.play();
      }
    });
    JButton stop = new JButton("stop");
    stop.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        currentPositionSlider.setEnabled(true);
        synchronizer.stop();
      }
    });
    JButton reset = new JButton("reset");
    reset.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setPlayPosition(0);
      }
    });
    JButton change = new JButton("change");
    change.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showAsTickTime = !showAsTickTime;
        if(showingPanel != null){
          showingPanel.setShowAsTickTime(showAsTickTime);
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

    frame.getContentPane().add(start);
    frame.getContentPane().add(stop);
    frame.getContentPane().add(reset);
    frame.getContentPane().add(change);
  }

  private void setMenuBar(final JFrame frame) {
    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("file");
    openMenuItem = new JMenuItem("open");
    openMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
          openMenuItem.setEnabled(false);
          open(fc.getSelectedFile().getAbsolutePath());
        }
      }
    });
    JMenuItem save = new JMenuItem("save");
    save.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if(fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION){
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
    menuBar.add(file);
    frame.setJMenuBar(menuBar);
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
        p.x = Math.max(0, Math.min(showingPanel.getWidth() - width, p.x - width/2));
        scrollPane.getViewport().setViewPosition(p);
        if(showAsTickTime)
          currentPositionSlider.setValue((int)currentTick);
        else
          currentPositionSlider.setValue((int)(currentTime*1000000));
      }
    });
    showingPanel.repaint();
  }

  public void open(final String fileName) {
    final GUI gui = this;
    Thread th = new Thread(){
      public void run() {
        try {
          CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
          CompiledDeviation cd = corePlayer.open(wrapper);
          pianoRollPanels.add(new PianoRollPanel(cd, gui));
          SwingUtilities.invokeLater(new Runnable(){
            public void run() {
              String name = "dev" + playList.getSize();
              playList.addElement(name);
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
          new GUI();
        } catch (MidiUnavailableException e) {
          e.printStackTrace();
        }
      }
    });
  }

}
