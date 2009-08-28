package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.crestmuse.cmx.sound.MusicPlaySynchronized;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;

public class MainFrame extends JFrame implements MusicPlaySynchronized {

  /**
   * MainFrameクラス唯一のインスタンス．
   */
  public static MainFrame getInstance() {
    return instance;
  }

  private static MainFrame instance = new MainFrame();
  // TODO ディスプレイに合わせて大きさ変える
  private static Dimension PIANO_ROLL_DIM = new Dimension(640, 320);
  private static Dimension CURVES_VELOCITY_DIM = new Dimension(640, 100);
  private static Dimension LISTS_DIM = new Dimension(120, 1);
  private DeviatedPerformancePlayer deviatedPerformancePlayer;
  private MusicPlaySynchronizer synchronizer;
//  private boolean showAsTickTime;
  private JMenuItem openMenuItem;
  private JCheckBoxMenuItem showAsRealTime;
  private DeviatedPerformanceView currentPerformance;
  private JList performances;
  private JScrollPane pianoRollScrollPane;
  private JScrollPane curvesScrollPane;
  private JScrollPane velocityScrollPane;
  private JPanel pianoRollHolder;
  private JPanel curveHolder;
  private JPanel velocityHolder;
  private JPanel notelistHolder;
  private JPanel noteEditHolder;
//  private JFrame mainFrame;
  private JSlider currentPositionSlider;
  private JSlider scale;

  private MainFrame() {
    deviatedPerformancePlayer = new DeviatedPerformancePlayer();
    synchronizer = new MusicPlaySynchronizer(deviatedPerformancePlayer);
    synchronizer.addSynchronizedComponent(this);
    synchronizer.setSleepTime(16);
//    showAsTickTime = true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
//        mainFrame = new JFrame("DeviationEditor");
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DeviationEditor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMenuBar();
        JSplitPane left = setScrollPane();
        JPanel right = setEastPanel();
        JPanel south = new JPanel(new FlowLayout());
        setSlider(south);
        setButtons(south);
        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
            right);
        center.setOneTouchExpandable(true);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
        setVisible(true);
      }
    });
  }

  private void setMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("file");
    openMenuItem = new JMenuItem("open");
    openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    openMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
          openMenuItem.setEnabled(false);
          open(fc.getSelectedFile().getAbsolutePath());
        }
      }
    });
    JMenuItem save = new JMenuItem("save");
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if (fc.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
          save(fc.getSelectedFile());
        }
      }
    });
    JMenuItem quit = new JMenuItem("quit");
    quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    quit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    file.add(openMenuItem);
    file.add(save);
    file.add(quit);
    JMenu show = new JMenu("show");
    showAsRealTime = new JCheckBoxMenuItem("show as real time");
    show.add(showAsRealTime);

    menuBar.add(file);
    menuBar.add(show);
    setJMenuBar(menuBar);
  }

  private JSplitPane setScrollPane() {
    // init
    pianoRollScrollPane = new JScrollPane();
    pianoRollHolder = new JPanel(new CardLayout());
    curvesScrollPane = new JScrollPane();
    curveHolder = new JPanel(new CardLayout());
    velocityScrollPane = new JScrollPane();
    velocityHolder = new JPanel(new CardLayout());
    // TODO scaleの挙動おかしい
    scale = new JSlider();
    scale.setMinimum(PianoRollPanel.WIDTH_PER_BEAT / 2);
    scale.setValue(PianoRollPanel.WIDTH_PER_BEAT);
    scale.setMaximum(PianoRollPanel.WIDTH_PER_BEAT * 2);
    scale.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        PianoRollPanel.WIDTH_PER_BEAT = ((JSlider) e.getSource()).getValue();
//        if (currentPerformance != null)
//          currentPerformance.updateScale();
//        pianoRollScrollPane.updateUI();
      }
    });

    // piano roll
    pianoRollScrollPane.setPreferredSize(PIANO_ROLL_DIM);
    pianoRollScrollPane.getViewport().setScrollMode(
        JViewport.SIMPLE_SCROLL_MODE);
    pianoRollScrollPane.setViewportView(pianoRollHolder);
    pianoRollScrollPane.setRowHeaderView(new KeyBoardPanel());
    pianoRollScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
    pianoRollScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
    pianoRollScrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
    pianoRollScrollPane
        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    // curves
    curvesScrollPane.setPreferredSize(CURVES_VELOCITY_DIM);
    curvesScrollPane.setViewportView(curveHolder);
    curvesScrollPane.setRowHeaderView(new CurvesPanel.RowHeader());
    curvesScrollPane
        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    JScrollBar scrollBar = pianoRollScrollPane.getHorizontalScrollBar();
    curvesScrollPane.setHorizontalScrollBar(scrollBar);

    // velocity
    velocityScrollPane.setPreferredSize(CURVES_VELOCITY_DIM);
    velocityScrollPane.setViewportView(velocityHolder);
    velocityScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, scale);
    velocityScrollPane.setRowHeaderView(new VelocityPanel.RowHeader());
    velocityScrollPane
        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    velocityScrollPane.setHorizontalScrollBar(scrollBar);

    // split panes
    JSplitPane bottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        curvesScrollPane, velocityScrollPane);
    bottom.setOneTouchExpandable(true);
    JSplitPane top = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        pianoRollScrollPane, bottom);
    top.setOneTouchExpandable(true);
    return top;
  }

  private JPanel setEastPanel() {
    JPanel eastPanel = new JPanel(new GridLayout(3, 1));
    performances = new JList(new DefaultListModel());
    performances.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        setPerformanceAsSelectedValue();
      }
    });
    JScrollPane scroll = new JScrollPane();
    scroll.setPreferredSize(LISTS_DIM);
    scroll.setViewportView(performances);
    eastPanel.add(scroll);

    notelistHolder = new JPanel(new CardLayout());
    scroll = new JScrollPane();
    scroll.setViewportView(notelistHolder);
    eastPanel.add(scroll);

    noteEditHolder = new JPanel(new CardLayout());
    scroll = new JScrollPane();
    scroll.setViewportView(noteEditHolder);
    eastPanel.add(scroll);
    return eastPanel;
  }

  private void setPerformanceAsSelectedValue() {
    currentPerformance = (DeviatedPerformanceView) performances
        .getSelectedValue();
    try {
      deviatedPerformancePlayer.changeDeviation(currentPerformance
          .getDeviatedPerformance());
    } catch (InvalidMidiDataException e1) {
      e1.printStackTrace();
    }
    String id = currentPerformance.getID();
    ((CardLayout) pianoRollHolder.getLayout()).show(pianoRollHolder, id);
    ((CardLayout) curveHolder.getLayout()).show(curveHolder, id);
    ((CardLayout) velocityHolder.getLayout()).show(velocityHolder, id);
    ((CardLayout) notelistHolder.getLayout()).show(notelistHolder, id);
    ((CardLayout) noteEditHolder.getLayout()).show(noteEditHolder, id);

    // y座標を中央までずらす
    Point p = pianoRollScrollPane.getViewport().getViewPosition();
    p.y = (PianoRollPanel.HEIGHT_PER_NOTE * 128 - pianoRollScrollPane
        .getHeight()) / 2;
    pianoRollScrollPane.getViewport().setViewPosition(p);
    pianoRollScrollPane.updateUI();
    curvesScrollPane.updateUI();
    velocityScrollPane.updateUI();
    notelistHolder.updateUI();
    noteEditHolder.updateUI();

    // 再生位置スライダーを設定
    if (showAsRealTime.isSelected())
      currentPositionSlider.setMaximum((int) deviatedPerformancePlayer
          .getCurrentSequence().getMicrosecondLength());
    else
      currentPositionSlider.setMaximum((int) deviatedPerformancePlayer
          .getCurrentSequence().getTickLength());

    // タイトルを変更
    setTitle(currentPerformance.toString() + " - DeviationEditor");
  }

  private void setSlider(JPanel parent) {
    currentPositionSlider = new JSlider();
    currentPositionSlider.setValue(0);
    currentPositionSlider.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
        if (currentPerformance == null)
          return;
        setPlayPosition(currentPositionSlider.getValue());
      }
    });
    parent.add(currentPositionSlider);
  }

  private void setButtons(JPanel parent) {
    JButton start = new JButton("start");
    start.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (synchronizer.isNowPlaying()) {
          currentPositionSlider.setEnabled(true);
          synchronizer.stop();
          button.setText("start");
        } else {
          currentPositionSlider.setEnabled(false);
          synchronizer.play();
          button.setText("pause");
        }
      }
    });
    JButton reset = new JButton("reset");
    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        currentPositionSlider.setEnabled(true);
        synchronizer.stop();
        setPlayPosition(0);
      }
    });
    // TODO remove this button
    JButton change = new JButton("change");
    change.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        showAsTickTime = !showAsTickTime;
        if (currentPerformance != null) {
//          currentPerformance.updateNotes();
//          currentPerformance.updateScale();
          MainFrame.this.repaint();
        }
        if (!showAsRealTime.isSelected()) {
          currentPositionSlider.setMaximum((int) deviatedPerformancePlayer
              .getCurrentSequence().getTickLength());
          currentPositionSlider.setValue((int) deviatedPerformancePlayer
              .getTickPosition());
        } else {
          currentPositionSlider.setMaximum((int) deviatedPerformancePlayer
              .getCurrentSequence().getMicrosecondLength());
          currentPositionSlider.setValue((int) deviatedPerformancePlayer
              .getMicrosecondPosition());
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
    deviatedPerformancePlayer.reset();
    currentPositionSlider.setEnabled(true);
  }

  public void synchronize(final double currentTime, final long currentTick,
      MusicPlaySynchronizer wavsync) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Point p = pianoRollScrollPane.getViewport().getViewPosition();
        p.x = currentPerformance.getPianoRollPanel().getPlayPointX(currentTime,
            currentTick);
        int width = pianoRollScrollPane.getViewport().getWidth();
        p.x = Math.max(0, Math.min(currentPerformance.getPianoRollPanel()
            .getPreferredSize().width
            - width, p.x - width / 2));
        pianoRollScrollPane.getViewport().setViewPosition(p);
        if (p.x <= 0
            || p.x >= width - pianoRollScrollPane.getViewport().getWidth())
          currentPerformance.getPianoRollPanel().repaint();
        if (showAsRealTime.isSelected())
          currentPositionSlider.setValue((int) currentTick);
        else
          currentPositionSlider.setValue((int) (currentTime * 1000000));
      }
    });
  }

  /**
   * タイムラインの表示形式を返す．楽譜時刻の場合true,実時刻の場合falseを返す．
   * 
   * @return タイムラインの表示形式
   */
  public boolean getShowAsTickTime() {
    return !showAsRealTime.isSelected();
  }

  /**
   * ファイルを開く．MusicXMLかDeviationInstanceXML以外が指定されると無視される．
   * 
   * @param fileName
   */
  public void open(final String fileName) {
    Thread th = new Thread() {
      public void run() {
        try {
          final DeviatedPerformanceView dpv = new DeviatedPerformanceView(
              fileName);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              ((DefaultListModel) performances.getModel()).addElement(dpv);
              pianoRollHolder.add(dpv.getPianoRollPanel(), dpv.getID());
              curveHolder.add(dpv.getTempoPanel(), dpv.getID());
              velocityHolder.add(dpv.getVelocityPanel(), dpv.getID());
              notelistHolder.add(dpv.getNoteList(), dpv.getID());
              noteEditHolder.add(dpv.getNoteEditPanel(), dpv.getID());
              scale.addChangeListener(dpv.getPianoRollPanel());
              scale.addChangeListener(dpv.getTempoPanel());
              scale.addChangeListener(dpv.getVelocityPanel());
              if (performances.isSelectionEmpty()) {
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    performances.setSelectedIndex(0);
                    setPerformanceAsSelectedValue();
                  }
                });
              }
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            openMenuItem.setEnabled(true);
          }
        });
      }
    };
    th.start();
  }

  /**
   * ファイルを保存する．
   * 
   * @param file
   */
  public void save(final File file) {
    Thread t = new Thread() {
      public void run() {
        try {
          deviatedPerformancePlayer.writeFile(new FileOutputStream(file));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }

  /**
   * 再生位置を変更し、画面をその位置までスクロールする．引数は、楽譜時刻表示の場合tickを、実時刻表時の場合マイクロ秒で指定する．
   * 
   * @param position
   *          再生位置
   */
  public void setPlayPosition(long position) {
    if (!showAsRealTime.isSelected())
      deviatedPerformancePlayer.setTickPosition(position);
    else
      deviatedPerformancePlayer.setMicrosecondPosition(position);
    synchronize(deviatedPerformancePlayer.getMicrosecondPosition() / 1000000.0,
        deviatedPerformancePlayer.getTickPosition(), null);
  }

}
