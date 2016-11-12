import jp.crestmuse.cmx.commands.*
import jp.crestmuse.cmx.filewrappers.*

class RenConSample1 extends CMXCommand {

  def run(musicxml) {
    def dds = new DeviationDataSet(musicxml)
    dds.addNonPartwiseControl(1, 1, "tempo", 120)
    for (part in musicxml.getPartList()) {
      for (measure in part.getMeasureList()) {
        for (note in measure.getMusicDataList()) {
          if (note instanceof MusicXMLWrapper.Note) {
             def notations = note.getFirstNotations()
	     if (notations != null && notations.hasArticulation("staccato")) {
	       if (note.chordNotes() == null)
  	         dds.addNoteDeviation(note, 0.0, -0.5 * note.duration() / 4, 
                                      1.0, 1.0);
	       else
		 dds.addChordDeviation(note, 0.0, -0.5 * note.duration() / 4, 
                                       1.0, 1.0);
             }
             if (notations != null && notations.fermataType() != null) 
               dds.addNonPartwiseControl(note.measure().number(), note.beat(), 
	                                 "tempo-deviation", 0.5);
  	     if (note.grace())
               dds.addNoteDeviation(note, -0.125, 0.0, 1.0, 1.0);
          }
        }
      }
    }
    return dds.toWrapper()
  }
}

(new RenConSample1()).start(args)
