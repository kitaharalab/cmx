import jp.crestmuse.cmx.sound.*
import jp.crestmuse.cmx.amusaj.sp.*
import jp.crestmuse.cmx.amusaj.filewrappers.*

class MidiPrint extends SPModule {

  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    def midimsg = src[0].getMessage()
    println("MIDI Message: ${midimsg.getStatus()}\t${midimsg.getData1()}\t${midimsg.getData2()}")
  }

  Class[] getInputClasses() {
    [ MidiEventWithTicktime.class ]
  }

  Class[] getOutputClasses() {
    []
  }
}

def vk = new VirtualKeyboard()
def midiin = new MidiInputModule(vk)
def midiprint = new MidiPrint()
def exec = new SPExecutor()
exec.addSPModule(midiin)
exec.addSPModule(midiprint)
exec.connect(midiin, 0, midiprint, 0)
exec.start()
