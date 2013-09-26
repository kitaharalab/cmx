import jp.crestmuse.cmx.processing.*
import jp.crestmuse.cmx.amusaj.sp.*

cmx = CMXController.getInstance()
midiin = cmx.newSPModule(
  execute: { src, dest ->
    println "Input byte data of MIDI message (e.g., 144 60 100)"
    print ">> "
    System.in.eachLine { line ->
      def input = line.split(" ")
      def newevent = cmx.createShortMessageEvent(
        [input[0].toInteger(), input[1].toInteger(), input[2].toInteger()], 
        0, 0)
      dest[0].add(newevent)
      print ">> "
    }
  },
  inputs: [], 
  outputs: [MidiEventWithTicktime.class]
)
midiout = cmx.createMidiOut()
cmx.connect(midiin, 0, midiout, 0)
cmx.startSP()
