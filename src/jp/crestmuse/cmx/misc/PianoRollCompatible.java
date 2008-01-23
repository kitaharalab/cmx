package jp.crestmuse.cmx.misc;
import jp.crestmuse.cmx.handlers.*;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public interface PianoRollCompatible { 
  void processNotes(CommonNoteHandler h) 
    throws TransformerException, IOException, ParserConfigurationException, 
    SAXException;
  ArrayList<SimpleNoteList> getPartwiseNoteList(int ticksPerBeat)
    throws TransformerException, IOException, ParserConfigurationException,
    SAXException;
  InputStream getMIDIInputStream() throws IOException, TransformerException,
    ParserConfigurationException, SAXException;
}