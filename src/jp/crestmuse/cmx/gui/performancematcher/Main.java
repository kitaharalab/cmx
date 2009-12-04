package jp.crestmuse.cmx.gui.performancematcher;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;

public class Main {

  public static void main(String[] args) throws IOException,
      ParserConfigurationException, SAXException, TransformerException,
      InvalidMidiDataException {
    new FrameController(args[0], args[1]);
  }

}
