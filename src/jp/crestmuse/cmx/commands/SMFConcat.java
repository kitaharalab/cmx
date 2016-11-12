package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.handlers.*;
import java.util.*;
import java.io.*;


class SMFConcat {
  int serial;
  SMFConcat(String filelist, String outfilename) throws Exception {
    final SCCXMLWrapper outdata = 
      (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    outdata.setDivision(480);
    final SCCXMLWrapper outdata2 = 
      (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    outdata2.setDivision(480);
    BufferedReader r = new BufferedReader(new FileReader(filelist));
//    List<SCCXMLWrapper> indata = new ArrayList<SCCXMLWrapper>();
//    List<Integer> from = new ArrayList<Integer>();
//    List<Integer> thru = new ArrayList<Integer>();
    serial = 0;
    outdata2.beginHeader();
    String line;
    while ((line = r.readLine()) != null) {
      String[] ss = line.split(" ");
      SCCXMLWrapper scc = MIDIXMLWrapper.readSMF(ss[0]).toSCCXML();
      final int t0 = Integer.parseInt(ss[1]);
      final int t1 = Integer.parseInt(ss[2]);
      scc.processNotes(new SCCHandler() {
          public void	beginHeader(SCCXMLWrapper w) {}
          public void 	beginPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
            outdata.newPart(serial++, part.channel(), part.prognum(), 
                            part.volume());
          }
          public void	endHeader(SCCXMLWrapper w) {}
          public void 	endPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
            outdata.endPart();
          }
          public void 	processHeaderElement(int timestamp, java.lang.String name, 
                                    java.lang.String content, SCCXMLWrapper w){
            outdata2.addHeaderElement(timestamp - t0 + t1, 
                                      name, content);
          }
          public void 	processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w) {
            outdata.addNoteElement(note.onset() - t0 + t1, 
                                   note.offset() - t0 + t1, 
                                   note.notenum(), note.velocity(), 
                                   note.offVelocity());
          }
        });
    }
    outdata2.endHeader();
    outdata.finalizeDocument();
    outdata.processNotes(new SCCHandler() {
        public void beginHeader(SCCXMLWrapper w) {}
        public void endHeader(SCCXMLWrapper w) {}
        public void beginPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
          outdata2.newPart(part.serial(), part.channel(), part.prognum(), 
                           part.volume());
        }
        public void endPart(SCCXMLWrapper.Part part, SCCXMLWrapper w) {
          outdata2.endPart();
        }
        public void processHeaderElement(int timestamp, String name,
                                  String content, SCCXMLWrapper w){}
        public void processNote(SCCXMLWrapper.Note note, SCCXMLWrapper w) {
          outdata2.addNoteElement(note.onset(), note.offset(), 
                                  note.notenum(), note.velocity(), 
                                  note.offVelocity());
        }
      });
    outdata2.finalizeDocument();
    outdata2.toMIDIXML().writefileAsSMF(outfilename);
  }

  public static void main(String[] args) {
    try {
      new SMFConcat(args[0], args[1]);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}