package jp.crestmuse.cmx.sound;

/*
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.Transmitter;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jp.crestmuse.cmx.amusaj.sp.MidiInputModule;
import jp.crestmuse.cmx.amusaj.sp.MidiOutputModule;
import jp.crestmuse.cmx.amusaj.sp.SPExecutor;
*/

public class VirtualKeyboard /*extends JFrame implements MidiDevice*/ {
  /*
  public static int VELOCITY = 127;
  public static int BASE_NOTE_NUM = 60;
  private char[] keys = { 'a', 'w', 's', 'e', 'd', 'f', 't', 'g', 'y', 'h',
      'u', 'j', 'k', 'o', 'l', 'p', ';', ':' };
  private int[] elevent2keys = { 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17 };
  private int[] seven2keys = { 1, 3, 6, 8, 10, 13, 15 };
  private boolean isOpen = false;
  private VirtualKeyboardTransmitter transmitter = new VirtualKeyboardTransmitter();
  private boolean[] pressed = new boolean[256];
  */

  public VirtualKeyboard() {
    /*
    JMenuItem up = new JMenuItem("up");
    up.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BASE_NOTE_NUM += 12;
        updateTitle();
      }
    });
    JMenuItem down = new JMenuItem("down");
    down.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BASE_NOTE_NUM -= 12;
        updateTitle();
      }
    });
    JMenu octave = new JMenu("Octave");
    octave.add(up);
    octave.add(down);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(octave);
    setJMenuBar(menuBar);
    updateTitle();
    add(new KeyboardPanel());
    pack();
    */
  }

  /*
  public VirtualKeyboard(Component c) {
    this();
    c.addKeyListener(getKeyListener());
  }

  private void updateTitle() {
    setTitle("C" + (BASE_NOTE_NUM / 12 - 2));
  }

  public void close() {
    isOpen = false;
    transmitter.close();
  }

  public Info getDeviceInfo() {
    return null;
  }

  public int getMaxReceivers() {
    return 0;
  }

  public int getMaxTransmitters() {
    return 1;
  }

  public long getMicrosecondPosition() {
    return -1;
  }

  public Receiver getReceiver() throws MidiUnavailableException {
    return null;
  }

  public List<Receiver> getReceivers() {
    return null;
  }

  public Transmitter getTransmitter() throws MidiUnavailableException {
    return transmitter;
  }

  public List<Transmitter> getTransmitters() {
    LinkedList<Transmitter> list = new LinkedList<Transmitter>();
    list.add(transmitter);
    return list;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void open() throws MidiUnavailableException {
    isOpen = true;
  }

  public KeyListener getKeyListener() {
    return transmitter.keylistener;
  }

  private class VirtualKeyboardTransmitter implements Transmitter,KeyListener {

    private Receiver receiver = null;
    private HashMap<Character, Integer> key2num;
    private KeyListener keylistener;

    public VirtualKeyboardTransmitter() {
      receiver = null;
      key2num = new HashMap<Character, Integer>();
      for (int i = 0; i < keys.length; i++)
        key2num.put(keys[i], i);
      addKeyListener(this);
      setVisible(true);
    }

//      keylistener = new KeyListener() {
          public synchronized void keyPressed(KeyEvent e) {
            System.err.println(pressed['a']);
            System.err.println(receiver);
            if (receiver == null || !key2num.containsKey(e.getKeyChar()))
              return;
            if (pressed[e.getKeyChar()])
              return;
            try {
              ShortMessage sm = new ShortMessage();
              sm.setMessage(ShortMessage.NOTE_ON, key2num.get(e.getKeyChar())
                            + BASE_NOTE_NUM, VELOCITY);
              receiver.send(sm, -1);
            } catch (InvalidMidiDataException e1) {
              e1.printStackTrace();
            }
            pressed[e.getKeyChar()] = true;
            repaint();
          }
          public synchronized void keyReleased(KeyEvent e) {
            System.err.println(pressed['a']);
            if (receiver == null || !key2num.containsKey(e.getKeyChar()))
              return;
            try {
              ShortMessage sm = new ShortMessage();
              sm.setMessage(ShortMessage.NOTE_OFF,
                            key2num.get(e.getKeyChar()) + BASE_NOTE_NUM, VELOCITY);
              receiver.send(sm, -1);
            } catch (InvalidMidiDataException e1) {
              e1.printStackTrace();
            }
            pressed[e.getKeyChar()] = false;
            repaint();
          }
          public void keyTyped(KeyEvent e) {
          }
//        };
//      SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
//          addKeyListener(keylistener);
//          setVisible(true);
//        }
//      });
//      addKeyListener(keylistener);
//      setVisible(true);
//    }

    public void close() {
      setVisible(false);
    }

    public Receiver getReceiver() {
      return receiver;
    }

    public void setReceiver(Receiver receiver) {
      this.receiver = receiver;
    }
  }

  private class KeyboardPanel extends JPanel {

    KeyboardPanel() {
      setPreferredSize(new Dimension(640, 240));
    }

    public void paint(Graphics g) {
      super.paint(g);
      Color green = new Color(0, 1, 0, 0.5f);
      for (int i = 0; i < 11; i++) {
        g.setColor(Color.BLACK);
        int x = getWidth() * (i + 1) / 11;
        g.drawLine(x, 0, x, getHeight());
        if (pressed[keys[elevent2keys[i]]]) {
          g.setColor(green);
          g.fillRect(x - getWidth() / 11, 0, getWidth() / 11, getHeight());
        }
        g.setColor(Color.BLACK);
        g.drawString(keys[elevent2keys[i]] + "", x - getWidth() / 11 + 2,
            getHeight() - 5);
      }
      float[] points = { 0.5f, 1.5f, 3.5f, 4.5f, 5.5f, 7.5f, 8.5f };
      for (int i = 0; i < points.length; i++) {
        g.setColor(Color.BLACK);
        int x = (int) (getWidth() * points[i] / 11);
        g.fillRect(x, 0, getWidth() / 11 - 2, getHeight() * 2 / 3);
        if (pressed[keys[seven2keys[i]]]) {
          g.setColor(green);
          g.fillRect(x, 0, getWidth() / 11 - 2, getHeight() * 2 / 3);
        }
        g.setColor(Color.WHITE);
        g.drawString(keys[seven2keys[i]] + "", x + 2, getHeight() * 2 / 3 - 5);
      }
    }

  }
  */

/*
  public static void main(String[] args) {
    try {
      VirtualKeyboard vk = new VirtualKeyboard();
      MidiInputModule mi = new MidiInputModule(vk);
      MidiOutputModule mo = new MidiOutputModule(MidiSystem.getReceiver());
      SPExecutor sp = new SPExecutor();
      sp.addSPModule(mi);
      sp.addSPModule(mo);
      sp.connect(mi, 0, mo, 0);
      sp.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
*/

}