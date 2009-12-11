package jp.crestmuse.cmx.gui.performancematcher;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.misc.PerformanceMatcher3;

public class FrameController implements Runnable {

  private String scoreFilename;
  private String pfmFilename;
  private PerformanceMatcher3 pm3;
  private int[] score2pfm;
  private DeviatedPerformance deviatedPerformance;
  private EditFrame frame;

  public FrameController(String scoreFilename, String pfmFilename)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException, InvalidMidiDataException {
    this.scoreFilename = scoreFilename;
    this.pfmFilename = pfmFilename;
    pm3 = new PerformanceMatcher3(
        (MusicXMLWrapper) CMXFileWrapper.readfile(scoreFilename),
        MIDIXMLWrapper.readSMF(pfmFilename));
    DeviationInstanceWrapper dev = pm3.extractDeviation();
    score2pfm = pm3.getScore2Pfm();
    dev.finalizeDocument();
    deviatedPerformance = new DeviatedPerformance(dev);
    SwingUtilities.invokeLater(this);
  }

  public void run() {
    if (frame != null)
      frame.dispose();
    try {
      frame = new EditFrame(deviatedPerformance, this);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void changePair(Note src, Note dst) {
    int srcIndex = pm3.getMusicxmlwrappernote2Index().get(src);
    int dstIndex = pm3.getMusicxmlwrappernote2Index().get(dst);
    score2pfm[dstIndex] = score2pfm[srcIndex];
    score2pfm[srcIndex] = -1;
  }

  public void reGenerateDeviation() throws ParserConfigurationException,
      SAXException, IOException, TransformerException, InvalidMidiDataException {
    pm3 = new PerformanceMatcher3(
        (MusicXMLWrapper) CMXFileWrapper.readfile(scoreFilename),
        MIDIXMLWrapper.readSMF(pfmFilename));
    DeviationInstanceWrapper dev = pm3.extractDeviation(score2pfm);
    score2pfm = pm3.getScore2Pfm();
    dev.finalizeDocument();
    deviatedPerformance = new DeviatedPerformance(dev);
    SwingUtilities.invokeLater(this);
  }

}