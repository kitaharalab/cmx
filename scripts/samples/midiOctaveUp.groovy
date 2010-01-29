import jp.crestmuse.cmx.sound.*
import static jp.crestmuse.cmx.sound.Utils.*
import jp.crestmuse.cmx.amusaj.sp.*
import jp.crestmuse.cmx.amusaj.filewrappers.*
import javax.sound.midi.*

class OctaveUp extends SPModule {

  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    def midievent = src[0]
    def midimsg = midievent.getMessage().getMessage()
//    midimsg[1] += 12
    dest[0].add(midievent)
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
def mididev = getMidiDevice(false)
mididev.open()
def midiout = new MidiOutputModule(mididev.getReceiver())
def octaveUp = new OctaveUp()
def exec = new SPExecutor()
exec.addSPModule(midiin)
exec.addSPModule(octaveUp)
exec.addSPModule(midiout)
exec.connect(midiin, 0, octaveUp, 0)
exec.connect(octaveUp, 0, midiout, 0)
exec.start()
