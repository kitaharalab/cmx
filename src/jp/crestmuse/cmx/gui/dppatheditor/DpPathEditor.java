package jp.crestmuse.cmx.gui.dppatheditor;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class DpPathEditor {

  public static void main(String[] args) throws ParserConfigurationException,
      SAXException, IOException, TransformerException, InvalidMidiDataException {
    new FrameController(args[0], args[1]);
  }

}
