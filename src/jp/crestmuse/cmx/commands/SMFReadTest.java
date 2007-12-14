package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;

public class SMFReadTest {
  public static void main(String[] args) {
      try {
	  MIDIXMLWrapper w = MIDIXMLWrapper.readSMF(args[0]);
//          w.toSCCXML().write(System.out);
//          w.toSCCXML().toMIDIXML().writefileAsSMF(args[1]);
	  w.write(System.out);
//	  w.writefileAsSMF(args[0] + "2");
      } catch (Exception e) {
	  e.printStackTrace();
      }
  }
}