import jp.crestmuse.cmx.sound.*
import jp.crestmuse.cmx.amusaj.sp.*
import jp.crestmuse.cmx.amusaj.filewrappers.*

/************************************************************
   midiinput.groovy 
     prints MIDI messages input by the user through 
     a virtual keyboard displayed on the screen. 
 ************************************************************/
     
class MidiPrint extends SPModule {

  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    println src[0].getMessageInByteArray()
    def (status, data1, data2) = src[0].getMessageInByteArray()
    println("MIDI Message: ${status}\t${data1}\t${data2}")
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
