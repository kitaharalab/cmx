package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.BorderLayout;
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
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.crestmuse.cmx.gui.deveditor.view.DeviatedPerformanceList.ListElement;
import jp.crestmuse.cmx.sound.MusicPlaySynchronized;
import jp.crestmuse.cmx.sound.MusicPlaySynchronizer;

public class MainFrame extends JFrame implements MusicPlaySynchronized {

  /**
   * MainFrameクラス唯一のインスタンス．
   */
  public static MainFrame getInstance() {
    return instance;
  }

  private static MainFrame instance;
  static {
    try {
      instance = new MainFrame();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
    }
  }
  // TODO ディスプレイに合わせて大きさ変える
  private static Dimension PIANO_ROLL_DIM = new Dimension(640, 320);
  private static Dimension CURVES_VELOCITY_DIM = new Dimension(640, 100);
  private static Dimension LISTS_DIM = new Dimension(240, 1);
  private DeviatedPerformancePlayer deviatedPerformancePlayer;
  private MusicPlaySynchronizer synchronizer;
  private JMenuItem openMenuItem;
  private JCheckBoxMenuItem showAsRealTime;
  private DeviatedPerformanceList performances;
  private JScrollPane pianoRollScrollPane;
  private JScrollPane curvesScrollPane;
  private JScrollPane velocityScrollPane;
  private JScrollPane noteListScrollPane;
  private JScrollPane noteEditScrollPane;
  private JSlider currentPositionSlider;

  // private JSlider scale;

  private MainFrame() throws MidiUnavailableException {
    deviatedPerformancePlayer = new DeviatedPerformancePlayer();
    synchronizer = new MusicPlaySynchronizer(deviatedPerformancePlayer);
    synchronizer.addSynchronizedComponent(this);
    synchronizer.setSleepTime(16);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
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
    openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
        InputEvent.CTRL_DOWN_MASK));
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
    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
        InputEvent.CTRL_DOWN_MASK));
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if (fc.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
          save(fc.getSelectedFile());
        }
      }
    });
    JMenuItem quit = new JMenuItem("quit");
    quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        InputEvent.CTRL_DOWN_MASK));
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

    JCheckBoxMenuItem extra = new JCheckBoxMenuItem("extra");
    extra.setSelected(true);
    extra.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleExtra(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    JCheckBoxMenuItem voice1 = new JCheckBoxMenuItem("voice1");
    voice1.setSelected(true);
    voice1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleVoice1(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    JCheckBoxMenuItem voice2 = new JCheckBoxMenuItem("voice2");
    voice2.setSelected(true);
    voice2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleVoice2(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    JCheckBoxMenuItem voice3 = new JCheckBoxMenuItem("voice3");
    voice3.setSelected(true);
    voice3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleVoice3(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    JCheckBoxMenuItem voice4 = new JCheckBoxMenuItem("voice4");
    voice4.setSelected(true);
    voice4.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleVoice4(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    JCheckBoxMenuItem voiceOther = new JCheckBoxMenuItem("voice other");
    voiceOther.setSelected(true);
    voiceOther.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PianoRollPanel.toggleVoiceOther(((JCheckBoxMenuItem) e.getSource()).isSelected());
        repaint();
      }
    });

    show.add(voice1);
    show.add(voice2);
    show.add(voice3);
    show.add(voice4);
    show.add(voiceOther);
    show.add(extra);

    JMenu edit = new JMenu("edit");
    JMenuItem undo = new JMenuItem("undo");
    undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
        InputEvent.CTRL_DOWN_MASK));
    undo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        performances.getSelectedValue().getCommandInvoker().undo();
      }
    });
    JMenuItem redo = new JMenuItem("redo");
    redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        InputEvent.CTRL_DOWN_MASK));
    redo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        performances.getSelectedValue().getCommandInvoker().redo();
      }
    });
    JMenuItem setRec = new JMenuItem("set midi receiver");
    setRec.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DeviatedPerformancePlayer.LabeledMidiDevice[] devices = deviatedPerformancePlayer.getReceivers();
        Object ret = JOptionPane.showInputDialog(MainFrame.this,
            "choose midi receiver", "", JOptionPane.INFORMATION_MESSAGE, null,
            devices,
            devices[deviatedPerformancePlayer.getCurrentReceiverIndex()]);
        for (int i = 0; i < devices.length; i++)
          if (ret == devices[i]) {
            try {
              deviatedPerformancePlayer.setReciever(i);
            } catch (MidiUnavailableException e1) {
              e1.printStackTrace();
            }
            break;
          }
      }
    });
    edit.add(undo);
    edit.add(redo);
    edit.add(setRec);

    menuBar.add(file);
    menuBar.add(show);
    menuBar.add(edit);
    setJMenuBar(menuBar);
  }

  private JSplitPane setScrollPane() {
    // init
    pianoRollScrollPane = new JScrollPane();
    curvesScrollPane = new JScrollPane();
    velocityScrollPane = new JScrollPane();
    // TODO scaleの挙動おかしい
    // scale = new JSlider();
    // scale.setMinimum(PianoRollPanel.WIDTH_PER_BEAT / 2);
    // scale.setMaximum(PianoRollPanel.WIDTH_PER_BEAT * 2);
    // scale.setValue(PianoRollPanel.WIDTH_PER_BEAT);
    // scale.addChangeListener(new ChangeListener() {
    // public void stateChanged(ChangeEvent e) {
    // PianoRollPanel.WIDTH_PER_BEAT = ((JSlider) e.getSource()).getValue();
    // }
    // });

    // piano roll
    pianoRollScrollPane.setPreferredSize(PIANO_ROLL_DIM);
    pianoRollScrollPane.getViewport().setScrollMode(
        JViewport.SIMPLE_SCROLL_MODE);
    pianoRollScrollPane.setRowHeaderView(new KeyBoardPanel());
    pianoRollScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
    pianoRollScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
    pianoRollScrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
    pianoRollScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    // curves
    curvesScrollPane.setPreferredSize(CURVES_VELOCITY_DIM);
    curvesScrollPane.setRowHeaderView(new CurvesPanel.RowHeader());
    curvesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    JScrollBar scrollBar = pianoRollScrollPane.getHorizontalScrollBar();
    curvesScrollPane.setHorizontalScrollBar(scrollBar);

    // velocity
    velocityScrollPane.setPreferredSize(CURVES_VELOCITY_DIM);
    // velocityScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, scale);
    velocityScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());
    velocityScrollPane.setRowHeaderView(new VelocityPanel.RowHeader());
    velocityScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
    performances = new DeviatedPerformanceList();
    performances.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        setPerformanceAsSelectedValue();
      }
    });
    JScrollPane scroll = new JScrollPane();
    scroll.setPreferredSize(LISTS_DIM);
    scroll.setViewportView(performances);
    eastPanel.add(scroll);

    noteListScrollPane = new JScrollPane();
    eastPanel.add(noteListScrollPane);

    noteEditScrollPane = new JScrollPane();
    eastPanel.add(noteEditScrollPane);
    return eastPanel;
  }

  private void setPerformanceAsSelectedValue() {
    ListElement le = performances.getSelectedValue();
    if (le.isLoading())
      return;
    try {
      deviatedPerformancePlayer.changeDeviation(le.getDeviatedPerformance());
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
    pianoRollScrollPane.setViewportView(le.getPianoRollPanel());
    curvesScrollPane.setViewportView(le.getCurvesPanel());
    velocityScrollPane.setViewportView(le.getVelocityPanel());
    noteListScrollPane.setViewportView(le.getNoteList());
    noteEditScrollPane.setViewportView(le.getNoteEditPanel());

    // y座標を中央までずらす
    Point p = pianoRollScrollPane.getViewport().getViewPosition();
    p.y = (PianoRollPanel.HEIGHT_PER_NOTE * 128 - pianoRollScrollPane.getHeight()) / 2;
    pianoRollScrollPane.getViewport().setViewPosition(p);

    // 再生位置スライダーを設定
    if (showAsRealTime.isSelected())
      currentPositionSlider.setMaximum((int) deviatedPerformancePlayer.getCurrentSequence().getMicrosecondLength());
    else
      currentPositionSlider.setMaximum((int) deviatedPerformancePlayer.getCurrentSequence().getTickLength());

    // タイトルを変更
    setTitle(le.getName() + " - DeviationEditor");
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
        if (performances.isSelectionEmpty())
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
        // showAsTickTime = !showAsTickTime;
        // if (currentPerformance != null) {
        // currentPerformance.updateNotes();
        // currentPerformance.updateScale();
        // MainFrame.this.repaint();
        // }
        if (!showAsRealTime.isSelected()) {
          currentPositionSlider.setMaximum((int) deviatedPerformancePlayer.getCurrentSequence().getTickLength());
          currentPositionSlider.setValue((int) deviatedPerformancePlayer.getTickPosition());
        } else {
          currentPositionSlider.setMaximum((int) deviatedPerformancePlayer.getCurrentSequence().getMicrosecondLength());
          currentPositionSlider.setValue((int) deviatedPerformancePlayer.getMicrosecondPosition());
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
        PianoRollPanel prp = performances.getSelectedValue().getPianoRollPanel();
        Point p = pianoRollScrollPane.getViewport().getViewPosition();
        p.x = prp.getPlayPointX(currentTime, currentTick);
        int width = pianoRollScrollPane.getViewport().getWidth();
        p.x = Math.max(0, Math.min(prp.getPreferredSize().width - width, p.x
            - width / 2));
        pianoRollScrollPane.getViewport().setViewPosition(p);
        if (p.x <= 0
            || p.x >= width - pianoRollScrollPane.getViewport().getWidth()) {
          prp.repaint();
        }
        if (showAsRealTime.isSelected())
          currentPositionSlider.setValue((int) (currentTime * 1000000));
        else
          currentPositionSlider.setValue((int) currentTick);
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
    performances.addPerformance(fileName, noteListScrollPane,
        new DeviatedPerformanceList.ListElementLoadListener() {
          public void listElementLoaded() {
            if (performances.isSelectionEmpty()) {
              performances.setSelectedIndex(0);
              setPerformanceAsSelectedValue();
            }
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                openMenuItem.setEnabled(true);
              }
            });
            repaint();
          }

          public void listElementLoadFailed() {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                openMenuItem.setEnabled(true);
              }
            });
            repaint();
          }
        });
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
