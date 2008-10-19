package jp.crestmuse.cmx.gui.deveditor;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.sound.MusicPlaySynchronized;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;

public class GUI implements MusicPlaySynchronized {
  // TODO 直前に開いたディレクトリを表示

  private CorePlayer corePlayer;
  private MusicPlaySynchronizer synchronizer;
  private DefaultListModel models;
  private JMenuItem openMenuItem;
  private ArrayList<PianoRollPanel> pianoRollPanels;
  private PianoRollPanel showingPanel;
  private JScrollPane pianoRollPanel;

  public GUI() throws MidiUnavailableException {
    corePlayer = new CorePlayer();
    synchronizer = new MusicPlaySynchronizer(corePlayer);
    synchronizer.addSynchronizedComponent(this);
    pianoRollPanels = new ArrayList<PianoRollPanel>();
    pianoRollPanel = new JScrollPane();
    JFrame f = new JFrame();
    f.setSize(640, 480);
    f.getContentPane().add(pianoRollPanel);
    f.setVisible(true);

    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 500);
    frame.getContentPane().setLayout(new FlowLayout());
    setMenuBar(frame);
    setList(frame);
    setButtons(frame);
    frame.setVisible(true);
  }

  private void setList(final JFrame frame) {
    models = new DefaultListModel();
    JList list = new JList(models);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        try {
          int index = ((JList)e.getSource()).getSelectedIndex();
          corePlayer.changeDeviation(index);
          showingPanel = pianoRollPanels.get(index);
          pianoRollPanel.setViewportView(showingPanel);
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
        synchronizer.play();
      }
    });
    JButton stop = new JButton("stop");
    stop.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        synchronizer.stop();
      }
    });
    JButton reset = new JButton("reset");
    reset.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        corePlayer.reset();
      }
    });

    frame.getContentPane().add(start);
    frame.getContentPane().add(stop);
    frame.getContentPane().add(reset);
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
  }

  public void synchronize(double currentTime, long currentTick, MusicPlaySynchronizer wavsync) {
    Point p = pianoRollPanel.getViewport().getViewPosition();
    p.x = showingPanel.getX(currentTime, currentTick);
    pianoRollPanel.getViewport().setViewPosition(p);
  }

  public void open(final String fileName) {
    Thread th = new Thread(){
      public void run() {
        try {
          CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
          CompiledDeviation cd = corePlayer.open(wrapper);
          pianoRollPanels.add(new PianoRollPanel(cd));
          SwingUtilities.invokeLater(new Runnable(){
            public void run() {
              String name = "dev" + models.getSize();
              models.addElement(name);
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
