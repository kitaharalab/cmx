import jp.crestmuse.cmx.sound.*
import static jp.crestmuse.cmx.sound.Utils.*
import jp.crestmuse.cmx.amusaj.sp.*
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.*
import jp.crestmuse.cmx.amusaj.filewrappers.*
import javax.sound.midi.*

/***********************************************************************
   midiOctaveUp.groovy 
     recieves MIDI messages input by the user through a virtual keyboard, 
     moves up an octave, and transmits them to Java's software synthesizer.
 ***********************************************************************/

class OctaveUp extends SPModule {

  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    def (status, data1, data2) = src[0].getMessageInByteArray()
    def newevent = createShortMessageEvent([status, data1+12, data2], 0, 
                                           src[0].music_position)
    dest[0].add(newevent)
  }

  Class[] getInputClasses() {
    [ MidiEventWithTicktime.class ]
  }

  Class[] getOutputClasses() {
    [ MidiEventWithTicktime.class ]
  }
}

def vk = new VirtualKeyboard()
def midiin = new MidiInputModule(vk)
def mididev = getMidiDeviceByName("Java Sound Synthesizer", 
                                  MidiDeviceType.OUTPUT);
mididev.open()
def midiout = new MidiOutputModule(mididev)
def octaveUp = new OctaveUp()
def exec = new SPExecutor()
exec.addSPModule(midiin)
exec.addSPModule(octaveUp)
exec.addSPModule(midiout)
exec.connect(midiin, 0, octaveUp, 0)
exec.connect(octaveUp, 0, midiout, 0)
exec.start()
