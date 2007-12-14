package jp.crestmuse.cmx.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public class NoteViewPanel extends JPanel implements MouseMotionListener {
  String filename;
  MusicXMLWrapper musicxml;
  TreeView<MusicXMLWrapper.Note> noteview;
  TreeLabel label;

  public NoteViewPanel(String filename) throws Exception {
    super();
    this.filename = filename;
    musicxml = (MusicXMLWrapper)CMXFileWrapper.readfile(filename);
    noteview = musicxml.getPartwiseNoteView().get(0);
    setLayout(null);
    label = addLabels(noteview.getRoot(), 10, 10);
  }

  private TreeLabel addLabels(MusicXMLWrapper.Note note, 
                              int x, int y) {
    TreeLabel l = new TreeLabel(note != null ? note.toString() : "null");
    l.setOpaque(true);
    l.setBackground(Color.CYAN);
    l.addMouseMotionListener(this);
    Dimension d = l.getPreferredSize();
    l.setBounds(x, y, d.width, d.height);
    add(l);
    if (noteview.hasNextL()) {
      l.childL = addLabels(noteview.nextL(), x, y+20);
      noteview.parent();
    }
    if (noteview.hasNextR()) {
      l.childR = addLabels(noteview.nextR(), x+150, y);
      noteview.parent();
    }
    return l;
  }

  public void mouseDragged(MouseEvent e) {
    TreeLabel l = (TreeLabel)e.getSource();
    int dx = e.getX();
    int dy = e.getY();
    updateLocations(l, dx, dy);
    repaint();
  }

  public void mouseMoved(MouseEvent e) {
    //
  }

  private void updateLocations(TreeLabel l, int dx, int dy) {
    Point p = l.getLocation();
    l.setLocation(p.x + dx, p.y + dy);
    if (l.childL != null)
      updateLocations(l.childL, dx, dy);
    if (l.childR != null)
      updateLocations(l.childR, dx, dy);
  }

  public void paint(Graphics g) {
    super.paint(g);
    drawLines(label, g);
  }

  private void drawLines(TreeLabel l, Graphics g) {
    if (l.childL != null) {
      Point p1 = l.getLocation();
      Point p2 = l.childL.getLocation();
      g.drawLine(p1.x, p1.y, p2.x, p2.y);
      drawLines(l.childL, g);
    }
    if (l.childR != null) {
      Point p1 = l.getLocation();
      Point p2 = l.childR.getLocation();
      g.drawLine(p1.x, p1.y, p2.x, p2.y);
      drawLines(l.childR, g);
    }
  }

  private class TreeLabel extends JLabel {
    private TreeLabel childL;
    private TreeLabel childR;
    TreeLabel(String s) {
      super(s);
    }
  }

  public static void main(String[] args) {
    try {
      JFrame f = new JFrame();
      f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      f.setSize(640, 480);
      f.getContentPane().add(new NoteViewPanel(args[0]));
      f.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}