package jp.crestmuse.cmx.misc;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public interface PianoRollCompatible { 
  ArrayList<SimpleNoteList> getPartwiseNoteList(int ticksPerBeat)
    throws TransformerException, IOException, ParserConfigurationException,
    SAXException;
  InputStream getMIDIInputStream() throws IOException, TransformerException,
    ParserConfigurationException, SAXException;
}