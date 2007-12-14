package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;

import org.w3c.dom.*;

public class MIDIXMLTest {
    public static void main(String[] args) {
	try {
	MIDIXMLWrapper w = 
	    (MIDIXMLWrapper)CMXFileWrapper.createDocument("MIDIFile");
	w.addElementsFirstForFormat1(2, 480);
	w.newTrack(1);
	w.addMIDIChannelMessage("NoteOn", 480, (byte)1, 60, 100);
	w.addMIDIChannelMessage("NoteOff", 480, (byte)1, 60, 100);
	w.addMIDIChannelMessage("NoteOn", 480, (byte)1, 60, 100);
	w.addMIDIChannelMessage("NoteOff", 480, (byte)1, 60, 100);
	w.endTrack();
	w.newTrack(2);
	//w.addMIDIChannelMessage("ProgramChange", 100, (byte)2, 48, 0);
	w.addMIDIChannelMessage("NoteOn", 100, (byte)2, 72, 80);
	w.addMIDIChannelMessage("NoteOff", 1200, (byte)2, 72, 80);
	w.endTrack();
	w.write(System.out);
	w.writefileAsSMF("tmp.mid");
	//	MIDIXMLWrapper.Track track1 = w.getTrackNodeInterface(1);
	//	byte[] b = track1.toMIDIMessageBinary();
	//	for (int k = 0; k < b.length; k++)
	//	    System.out.println(b[k]);
	SCCXMLWrapper sccxml = 
		(SCCXMLWrapper) CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
	w.toSCCXML(sccxml);
	System.out.println();
	sccxml.write(System.out);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
