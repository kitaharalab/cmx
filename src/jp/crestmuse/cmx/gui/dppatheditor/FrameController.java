package jp.crestmuse.cmx.gui.dppatheditor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.view.DeviatedPerformancePlayer;
import jp.crestmuse.cmx.misc.PerformanceMatcher3;

public class FrameController implements Runnable {

  private String scoreFilename;
  private String pfmFilename;
  private PerformanceMatcher3 pm3;
  private int[] score2pfm;
  private DeviatedPerformance deviatedPerformance;
  private DeviatedPerformancePlayer player;
  private MainFrame frame;

  public FrameController(String scoreFilename, String pfmFilename)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException, InvalidMidiDataException, MidiUnavailableException {
    this.scoreFilename = scoreFilename;
    this.pfmFilename = pfmFilename;
    pm3 = new PerformanceMatcher3(
        (MusicXMLWrapper) CMXFileWrapper.readfile(scoreFilename),
        MIDIXMLWrapper.readSMF(pfmFilename));
    DeviationInstanceWrapper deviation = pm3.extractDeviation();
    score2pfm = pm3.getScore2Pfm().clone();
    deviation.finalizeDocument();
    deviatedPerformance = new DeviatedPerformance(deviation);
    player = new DeviatedPerformancePlayer();
    player.changeDeviation(deviatedPerformance);
    SwingUtilities.invokeLater(this);
  }

  public void run() {
    if (frame != null)
      frame.dispose();
    try {
      frame = new MainFrame(deviatedPerformance, pm3, score2pfm, player, this);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void reGenerateDeviation() throws ParserConfigurationException,
      SAXException, IOException, TransformerException, InvalidMidiDataException {
    frame.setVisible(false);
    pm3 = new PerformanceMatcher3(
        (MusicXMLWrapper) CMXFileWrapper.readfile(scoreFilename),
        MIDIXMLWrapper.readSMF(pfmFilename));
    DeviationInstanceWrapper dev = pm3.extractDeviation(frame.getScore2pfm());
    score2pfm = pm3.getScore2Pfm().clone();
    dev.finalizeDocument();
    deviatedPerformance = new DeviatedPerformance(dev);
    player.changeDeviation(deviatedPerformance);
    SwingUtilities.invokeLater(this);
  }

  public void save() throws InvalidFileTypeException, IOException, SAXException {
    deviatedPerformance.calcDeviation().writefile("out.xml");
  }

}