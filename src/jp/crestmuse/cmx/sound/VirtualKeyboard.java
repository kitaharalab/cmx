package jp.crestmuse.cmx.sound;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class VirtualKeyboard extends JFrame implements MidiDevice {

  public static int VELOCITY = 127;
  public static int BASE_NOTE_NUM = 60;
  private boolean isOpen = false;
  private Transmitter transmitter = new VirtualKeyboardTransmitter();
  
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

  private class VirtualKeyboardTransmitter implements Transmitter{

    private Receiver receiver = null;
    private HashMap<Character, Integer> key2num;

    public VirtualKeyboardTransmitter(){
      receiver = null;
      key2num = new HashMap<Character, Integer>();
      key2num.put('a', BASE_NOTE_NUM);
      key2num.put('w', BASE_NOTE_NUM + 1);
      key2num.put('s', BASE_NOTE_NUM + 2);
      key2num.put('e', BASE_NOTE_NUM + 3);
      key2num.put('d', BASE_NOTE_NUM + 4);
      key2num.put('f', BASE_NOTE_NUM + 5);
      key2num.put('t', BASE_NOTE_NUM + 6);
      key2num.put('g', BASE_NOTE_NUM + 7);
      key2num.put('y', BASE_NOTE_NUM + 8);
      key2num.put('h', BASE_NOTE_NUM + 9);
      key2num.put('u', BASE_NOTE_NUM + 10);
      key2num.put('j', BASE_NOTE_NUM + 11);
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
          addKeyListener(new KeyListener(){
            private boolean pressed = false;
            public void keyPressed(KeyEvent e) {
              if(receiver == null || !key2num.containsKey(e.getKeyChar())) return;
              if(pressed) return;
              try {
                ShortMessage sm = new ShortMessage();
                sm.setMessage(ShortMessage.NOTE_ON, key2num.get(e.getKeyChar()), VELOCITY);
                receiver.send(sm, -1);
              } catch (InvalidMidiDataException e1) {
                e1.printStackTrace();
              }
              pressed = true;
            }
            public void keyReleased(KeyEvent e) {
              if(receiver == null || !key2num.containsKey(e.getKeyChar())) return;
              try {
                ShortMessage sm = new ShortMessage();
                sm.setMessage(ShortMessage.NOTE_OFF, key2num.get(e.getKeyChar()), VELOCITY);
                receiver.send(sm, -1);
              } catch (InvalidMidiDataException e1) {
                e1.printStackTrace();
              }
              pressed = false;
            }
            public void keyTyped(KeyEvent e) {
            }
          });
          setVisible(true);
        }
      });
    }

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

}